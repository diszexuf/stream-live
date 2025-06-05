import shutil
import subprocess
from typing import Dict, List, Tuple
from pydub import AudioSegment
import logging
import os
from pathlib import Path

from audio_processor import extract_audio_from_ts, replace_audio_segment, is_audio_silent
from speech_recognizer import transcribe_audio
from ad_detector import detect_ad
from utils import ensure_dir_exists, safe_delete_file

logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[logging.StreamHandler()]
)


class SegmentProcessor:
    def __init__(self, speech_model, ad_model):
        self.speech_model = speech_model
        self.ad_model = ad_model
        self.segment_buffer = {}  # Буфер для анализа стыков
        self.last_transcription = ""  # Последний текст для контекста

        # Параметры для обработки коротких сегментов
        self.min_audio_duration = 0.5  # Минимум полсекунды аудио
        self.silence_threshold = -55  # dB для определения тишины
        self.min_speech_duration = 0.3  # Минимум речи для транскрипции

    def _is_audio_too_short_or_silent(self, audio_file: str) -> Tuple[bool, str]:
        """
        Проверяет, слишком ли короткое или тихое аудио для обработки
        Returns: (is_problematic, reason)
        """
        try:
            if not os.path.exists(audio_file):
                return True, "audio_missing"

            audio = AudioSegment.from_wav(audio_file)
            duration = len(audio) / 1000.0

            # Слишком короткое
            if duration < self.min_audio_duration:
                return True, f"too_short_{duration:.2f}s"

            # Проверяем на тишину более детально
            if is_audio_silent(audio_file, self.silence_threshold, self.min_speech_duration):
                return True, "silent"

            # Проверяем среднюю громкость
            if audio.dBFS < self.silence_threshold:
                return True, f"too_quiet_{audio.dBFS:.1f}dB"

            return False, "ok"

        except Exception as e:
            logging.error(f"Ошибка при проверке аудио {audio_file}: {e}")
            return True, f"error_{str(e)}"

    def _safe_transcribe_audio(self, audio_file: str) -> List[Dict]:
        """Безопасная транскрипция с обработкой ошибок"""
        try:
            is_problematic, reason = self._is_audio_too_short_or_silent(audio_file)

            if is_problematic:
                logging.debug(f"Пропускаем транскрипцию: {reason}")
                return []

            transcriptions = transcribe_audio(audio_file, self.speech_model)

            # Фильтруем слишком короткие результаты
            if not transcriptions:
                logging.debug("Транскрипция пуста")
                return []

            # Проверяем качество транскрипции
            total_speech_time = 0
            valid_words = 0

            for trans in transcriptions:
                if 'start' in trans and 'end' in trans and 'word' in trans:
                    duration = trans['end'] - trans['start']
                    if duration > 0.1 and len(trans['word'].strip()) > 1:
                        total_speech_time += duration
                        valid_words += 1

            if total_speech_time < self.min_speech_duration or valid_words == 0:
                logging.debug(f"Недостаточно речи: {total_speech_time:.2f}s, слов: {valid_words}")
                return []

            return transcriptions

        except Exception as e:
            logging.error(f"Ошибка транскрипции {audio_file}: {e}")
            return []

    def _combine_fragments_robust(self, transcriptions: List[Dict], min_pause: float = 0.15) -> List[Dict]:
        """Объединяет слова в фрагменты с улучшенной обработкой коротких фрагментов"""
        if not transcriptions:
            return []

        combined = []
        current_start = None
        current_end = None
        current_words = []

        for i, entry in enumerate(transcriptions):
            # Фильтруем очень короткие или подозрительные слова
            word = entry.get("word", "").strip()
            if len(word) < 1 or entry.get("end", 0) - entry.get("start", 0) < 0.05:
                continue

            if current_start is None:
                current_start = entry["start"]
                current_words = []

            current_words.append(word)
            current_end = entry["end"]

            # Проверяем паузу до следующего слова
            if i + 1 < len(transcriptions):
                next_entry = transcriptions[i + 1]
                pause = next_entry.get("start", 0) - current_end

                if pause >= min_pause or len(current_words) >= 10:  # Длинные фразы разбиваем
                    if len(" ".join(current_words).strip()) >= 3:  # Минимум 3 символа
                        combined.append({
                            "start": current_start,
                            "end": current_end,
                            "sentence": " ".join(current_words).strip(),
                            "words": current_words.copy()
                        })
                    current_words = []
                    current_start = None
            else:
                # Последний фрагмент
                if current_words and len(" ".join(current_words).strip()) >= 3:
                    combined.append({
                        "start": current_start,
                        "end": current_end,
                        "sentence": " ".join(current_words).strip(),
                        "words": current_words.copy()
                    })

        return combined

    def _detect_ads_with_context(self, fragments: List[Dict], previous_context: str = "") -> Tuple[List[tuple], str]:
        """
        Обнаруживает рекламу с учетом контекста предыдущих сегментов
        Returns: (ad_regions, current_text_for_next_context)
        """
        if not fragments:
            return [], ""

        ad_regions = []
        current_text = " ".join([frag['sentence'] for frag in fragments])

        # Анализируем каждый фрагмент
        for frag in fragments:
            sentence = frag['sentence']

            # Добавляем контекст для лучшего анализа
            context_sentence = f"{previous_context} {sentence}".strip()

            # Сначала проверяем с контекстом
            is_ad_with_context, conf_context = detect_ad(context_sentence, self.ad_model)
            is_ad_alone, conf_alone = detect_ad(sentence, self.ad_model)

            # Принимаем решение на основе обеих проверок
            is_ad = is_ad_with_context or (is_ad_alone and conf_alone > 0.9)
            confidence = max(conf_context, conf_alone)

            if is_ad:
                logging.debug(
                    f"Реклама обнаружена: '{sentence}' (контекст: {is_ad_with_context}, соло: {is_ad_alone}, conf: {confidence:.3f})")
                ad_regions.append((frag['start'], frag['end']))
            else:
                logging.debug(f"Обычная речь: '{sentence}' (conf: {confidence:.3f})")

        # Объединяем близкие регионы
        merged_regions = self._merge_overlapping_regions(ad_regions)

        # Сохраняем последние 50 символов для контекста
        context_for_next = current_text[-50:] if len(current_text) > 50 else current_text

        return merged_regions, context_for_next

    def _merge_overlapping_regions(self, regions: List[tuple]) -> List[tuple]:
        """Объединяет перекрывающиеся временные регионы с буфером"""
        if not regions:
            return []

        sorted_regions = sorted(regions, key=lambda x: x[0])
        merged = [sorted_regions[0]]

        for current in sorted_regions[1:]:
            last = merged[-1]
            # Добавляем небольшой буфер для объединения близких регионов
            if current[0] <= last[1] + 0.5:  # 0.5 секунды буфер
                merged[-1] = (last[0], max(last[1], current[1]))
            else:
                merged.append(current)

        return merged

    def _save_processed_segment(self, input_file: str, audio_file: str, audio: AudioSegment, output_file: str) -> bool:
        """Сохраняет обработанный сегмент с проверками"""
        try:
            # Экспортируем обработанное аудио
            audio.export(audio_file, format="wav")
            output_dir = Path(output_file).parent
            ensure_dir_exists(str(output_dir))

            # Проверяем, что у нас есть и видео и аудио дорожки
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
                # Есть видео - объединяем с обработанным аудио
                command = [
                    'ffmpeg', '-y', '-loglevel', 'error',
                    '-i', input_file,  # Исходное видео
                    '-i', audio_file,  # Обработанное аудио
                    '-c:v', 'copy',  # Копируем видео без перекодирования
                    '-c:a', 'aac', '-b:a', '128k',  # Кодируем аудио
                    '-map', '0:v:0',  # Видео из первого файла
                    '-map', '1:a:0',  # Аудио из второго файла
                    '-shortest',  # Обрезаем до короткой дорожки
                    '-avoid_negative_ts', 'make_zero',
                    output_file
                ]
            else:
                # Только аудио - просто копируем обработанный файл
                command = [
                    'ffmpeg', '-y', '-loglevel', 'error',
                    '-i', audio_file,
                    '-c:a', 'aac', '-b:a', '128k',
                    output_file
                ]

            subprocess.run(command, check=True, capture_output=True)
            logging.debug(f"Сегмент успешно сохранен: {output_file}")

            # Очищаем временные файлы
            safe_delete_file(audio_file)
            return True

        except subprocess.CalledProcessError as e:
            logging.error(f"Ошибка FFmpeg при сохранении {output_file}: {e}")
            return False
        except Exception as e:
            logging.error(f"Неожиданная ошибка при сохранении {output_file}: {e}")
            return False

    def process_ts_segment(self, input_file: str, output_file: str, segment_index: int) -> bool:
        """
        Обрабатывает TS сегмент с устойчивостью к коротким/тихим сегментам
        """
        logging.debug(f"Обработка сегмента {segment_index}: {input_file}")

        if not os.path.exists(input_file):
            logging.error(f"Входной файл не существует: {input_file}")
            return False

        try:
            # Извлекаем аудио
            audio_file = extract_audio_from_ts(input_file)
            if not audio_file or not os.path.exists(audio_file):
                logging.info(f"Нет аудио в сегменте {segment_index}, копируем как есть")
                shutil.copy2(input_file, output_file)
                return True

            # Проверяем качество аудио
            is_problematic, reason = self._is_audio_too_short_or_silent(audio_file)

            if is_problematic:
                logging.info(f"Сегмент {segment_index} пропущен ({reason}), копируем без изменений")
                shutil.copy2(input_file, output_file)
                safe_delete_file(audio_file)
                return True

            # Загружаем аудио
            audio = AudioSegment.from_wav(audio_file)

            # Безопасная транскрипция
            transcriptions = self._safe_transcribe_audio(audio_file)

            if not transcriptions:
                logging.info(f"Нет распознанной речи в сегменте {segment_index}, копируем как есть")
                shutil.copy2(input_file, output_file)
                safe_delete_file(audio_file)
                return True

            # Анализируем фрагменты
            fragments = self._combine_fragments_robust(transcriptions)

            if not fragments:
                logging.info(f"Нет значимых фрагментов в сегменте {segment_index}")
                shutil.copy2(input_file, output_file)
                safe_delete_file(audio_file)
                return True

            # Детекция рекламы с контекстом
            ad_regions, new_context = self._detect_ads_with_context(
                fragments,
                self.last_transcription
            )

            # Обновляем контекст для следующего сегмента
            self.last_transcription = new_context

            # Обрабатываем аудио если найдена реклама
            processed_audio = audio
            if ad_regions:
                logging.info(f"Обнаружена реклама в сегменте {segment_index}: {len(ad_regions)} регионов")
                for start, end in ad_regions:
                    logging.debug(f"Заменяем рекламу: {start:.2f}-{end:.2f}s")
                    processed_audio = replace_audio_segment(processed_audio, start, end)
            else:
                logging.debug(f"Реклама не обнаружена в сегменте {segment_index}")

            # Сохраняем результат
            temp_audio = f"temp_processed_{segment_index}.wav"
            success = self._save_processed_segment(input_file, temp_audio, processed_audio, output_file)

            safe_delete_file(audio_file)
            return success

        except Exception as e:
            logging.error(f"Критическая ошибка при обработке сегмента {segment_index}: {e}")
            # В случае ошибки копируем оригинал
            try:
                shutil.copy2(input_file, output_file)
                return True
            except Exception as copy_error:
                logging.error(f"Не удалось скопировать оригинал: {copy_error}")
                return False