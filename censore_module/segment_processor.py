import shutil
import subprocess
import time
from typing import Dict, List, Tuple
from pydub import AudioSegment
import logging
import os
from pathlib import Path

from audio_processor import extract_audio_from_ts, replace_audio_segment
from speech_recognizer import transcribe_audio
from ad_detector import detect_ad
from utils import ensure_dir_exists, safe_delete_file

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(name)s - %(funcName)s - %(message)s',
    handlers=[logging.StreamHandler()]
)


class SegmentProcessor:
    def __init__(self, speech_model, ad_model):
        self.speech_model = speech_model
        self.ad_model = ad_model
        self.segment_buffer = {}
        self.last_transcription = ""
        self.min_audio_duration = 0.2
        self.silence_threshold = -40
        self.min_speech_duration = 0.1

    def _is_audio_too_short_or_silent(self, audio_file: str) -> Tuple[bool, str]:
        try:
            if not os.path.exists(audio_file):
                return True, "audio_missing"

            audio = AudioSegment.from_wav(audio_file)
            duration = len(audio) / 1000.0
            avg_dbfs = audio.dBFS

            logging.debug(
                f"Аудио {audio_file}: длительность {duration:.2f}s, громкость {avg_dbfs:.1f}dB (порог: {self.silence_threshold}dB)")

            if duration < self.min_audio_duration:
                return True, f"too_short_{duration:.2f}s"

            if avg_dbfs < self.silence_threshold:
                return True, f"too_quiet_{avg_dbfs:.1f}dB"

            return False, "ok"

        except Exception as e:
            logging.error(f"Ошибка при проверке аудио {audio_file}: {e}")
            return True, f"error_{str(e)}"

    def _safe_transcribe_audio(self, audio_file: str) -> List[Dict]:
        try:
            is_problematic, reason = self._is_audio_too_short_or_silent(audio_file)

            if is_problematic:
                logging.info(f"Пропускаем транскрипцию {audio_file}: {reason}")
                return []

            start_time = time.time()
            transcriptions = transcribe_audio(audio_file, self.speech_model)
            duration = time.time() - start_time

            logging.debug(f"Транскрипция {audio_file} заняла {duration:.2f} сек")

            if not transcriptions:
                logging.warning(f"Пустая транскрипция для {audio_file}. Причина: {reason}")
                return []

            total_speech_time = sum(
                trans['end'] - trans['start']
                for trans in transcriptions
                if 'start' in trans and 'end' in trans
            )

            if total_speech_time < self.min_speech_duration:
                logging.info(f"Недостаточно речи в {audio_file}: {total_speech_time:.2f}s")
                return []

            logging.debug(f"Общая длительность речи в {audio_file}: {total_speech_time:.2f}s")
            return transcriptions

        except Exception as e:
            logging.error(f"Ошибка транскрипции {audio_file}: {e}")
            return []

    def _combine_fragments_robust(self, transcriptions: List[Dict], min_pause: float = 0.3) -> List[Dict]:
        if not transcriptions:
            return []

        combined = []
        current_start = None
        current_end = None
        current_words = []

        for i, entry in enumerate(transcriptions):
            word = entry.get("word", "").strip()
            if not word:
                logging.debug(f"Пропущено пустое слово: {entry}")
                continue

            if current_start is None:
                current_start = entry["start"]
                current_words = []

            current_words.append(word)
            current_end = entry["end"]

            if i + 1 < len(transcriptions):
                next_entry = transcriptions[i + 1]
                pause = next_entry.get("start", 0) - current_end

                if pause >= min_pause:
                    if current_words:
                        sentence = " ".join(current_words).strip()
                        combined.append({
                            "start": current_start,
                            "end": current_end,
                            "sentence": sentence,
                            "words": current_words.copy()
                        })
                        logging.debug(f"Объединён фрагмент: {sentence} ({current_start:.2f}-{current_end:.2f}s)")
                    current_words = []
                    current_start = None
            else:
                if current_words:
                    sentence = " ".join(current_words).strip()
                    combined.append({
                        "start": current_start,
                        "end": current_end,
                        "sentence": sentence,
                        "words": current_words.copy()
                    })
                    logging.debug(f"Объединён фрагмент: {sentence} ({current_start:.2f}-{current_end:.2f}s)")

        if combined:
            sentences = [f"{frag['sentence']} ({frag['start']:.2f}-{frag['end']:.2f}s)" for frag in combined]
            logging.info(f"Объединённые фрагменты: {' | '.join(sentences)}")
        else:
            logging.info("Нет объединённых фрагментов")

        return combined

    def _detect_ads_with_context(self, fragments: List[Dict], previous_context: str = "") -> Tuple[List[tuple], str]:
        if not fragments:
            return [], ""

        ad_regions = []
        current_text = " ".join([frag['sentence'] for frag in fragments])

        for frag in fragments:
            sentence = frag['sentence']
            context_sentence = f"{previous_context} {sentence}".strip()

            is_ad_with_context, conf_context = detect_ad(context_sentence, self.ad_model)
            is_ad_alone, conf_alone = detect_ad(sentence, self.ad_model)

            is_ad = is_ad_with_context or (is_ad_alone and conf_alone > 0.9)
            confidence = max(conf_context, conf_alone)

            logging.info(
                f"Классификация текста: '{sentence}' (контекст: {is_ad_with_context}, соло: {is_ad_alone}, уверенность: {confidence:.3f})")

            if is_ad:
                ad_regions.append((frag['start'], frag['end']))
                logging.info(f"Реклама обнаружена: '{sentence}' ({frag['start']:.2f}-{frag['end']:.2f}s)")
            else:
                logging.info(f"Обычная речь: '{sentence}' ({frag['start']:.2f}-{frag['end']:.2f}s)")

        merged_regions = self._merge_overlapping_regions(ad_regions)
        context_for_next = current_text[-200:] if len(current_text) > 200 else current_text

        return merged_regions, context_for_next

    def _merge_overlapping_regions(self, regions: List[tuple]) -> List[tuple]:
        if not regions:
            return []

        sorted_regions = sorted(regions, key=lambda x: x[0])
        merged = [sorted_regions[0]]

        for current in sorted_regions[1:]:
            last = merged[-1]
            if current[0] <= last[1] + 0.5:
                merged[-1] = (last[0], max(last[1], current[1]))
            else:
                merged.append(current)

        return merged

    def _save_processed_segment(self, input_file: str, audio_file: str, audio: AudioSegment, output_file: str) -> bool:
        try:
            audio.export(audio_file, format="wav")
            output_dir = Path(output_file).parent
            ensure_dir_exists(str(output_dir))

            probe_cmd = [
                'ffprobe', '-v', 'error', '-select_streams', 'v:0',
                '-show_entries', 'stream=codec_type', '-of', 'csv=p=0', input_file
            ]

            try:
                result = subprocess.run(probe_cmd, capture_output=True, text=True, check=True)
                has_video = 'video' in result.stdout
            except:
                has_video = False

            if has_video:
                command = [
                    'ffmpeg', '-y', '-loglevel', 'error',
                    '-i', input_file,
                    '-i', audio_file,
                    '-c:v', 'copy',
                    '-c:a', 'aac', '-b:a', '128k',
                    '-map', '0:v:0',
                    '-map', '1:a:0',
                    '-shortest',
                    '-avoid_negative_ts', 'make_zero',
                    output_file
                ]
            else:
                command = [
                    'ffmpeg', '-y', '-loglevel', 'error',
                    '-i', audio_file,
                    '-c:a', 'aac', '-b:a', '128k',
                    output_file
                ]

            subprocess.run(command, check=True, capture_output=True)
            logging.debug(f"Сегмент успешно сохранён: {output_file}")
            safe_delete_file(audio_file)
            return True

        except subprocess.CalledProcessError as e:
            logging.error(f"Ошибка FFmpeg при сохранении {output_file}: {e}")
            return False
        except Exception as e:
            logging.error(f"Неожиданная ошибка при сохранении {output_file}: {e}")
            return False

    def process_ts_segment(self, input_file: str, output_file: str, segment_index: int) -> bool:
        logging.debug(f"Обработка сегмента {segment_index}: {input_file}")

        if not os.path.exists(input_file):
            logging.error(f"Входной файл не существует: {input_file}")
            return False

        try:
            audio_file = extract_audio_from_ts(input_file)
            if not audio_file or not os.path.exists(audio_file):
                logging.info(f"Нет аудио в сегменте {segment_index}, копируем как есть")
                shutil.copy2(input_file, output_file)
                return True

            is_problematic, reason = self._is_audio_too_short_or_silent(audio_file)

            if is_problematic:
                logging.info(f"Сегмент {segment_index} пропущен ({reason}), копируем без изменений")
                shutil.copy2(input_file, output_file)
                safe_delete_file(audio_file)
                return True

            audio = AudioSegment.from_wav(audio_file)
            transcriptions = self._safe_transcribe_audio(audio_file)

            if not transcriptions:
                logging.info(f"Нет распознанной речи в сегменте {segment_index}, копируем как есть")
                shutil.copy2(input_file, output_file)
                safe_delete_file(audio_file)
                return True

            fragments = self._combine_fragments_robust(transcriptions)

            if not fragments:
                logging.info(f"Нет значимых фрагментов в сегменте {segment_index}")
                shutil.copy2(input_file, output_file)
                safe_delete_file(audio_file)
                return True

            ad_regions, new_context = self._detect_ads_with_context(
                fragments,
                self.last_transcription
            )

            self.last_transcription = new_context

            processed_audio = audio
            if ad_regions:
                logging.info(f"Обнаружена реклама в сегменте {segment_index}: {len(ad_regions)} регионов")
                for start, end in ad_regions:
                    logging.debug(f"Заменяем рекламу: {start:.2f}-{end:.2f}s")
                    processed_audio = replace_audio_segment(processed_audio, start, end)
            else:
                logging.debug(f"Реклама не обнаружена в сегменте {segment_index}")

            temp_audio = f"temp_processed_{segment_index}.wav"
            success = self._save_processed_segment(input_file, temp_audio, processed_audio, output_file)

            safe_delete_file(audio_file)
            return success

        except Exception as e:
            logging.error(f"Критическая ошибка при обработке сегмента {segment_index}: {e}")
            try:
                shutil.copy2(input_file, output_file)
                return True
            except Exception as copy_error:
                logging.error(f"Не удалось скопировать оригинал: {copy_error}")
                return False
