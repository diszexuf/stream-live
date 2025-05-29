import subprocess
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
    level=logging.DEBUG,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[logging.StreamHandler()]
)


class SegmentProcessor:
    def __init__(self, speech_model, ad_model):
        self.speech_model = speech_model
        self.ad_model = ad_model
        self.prev_segment_data = None

    def _save_processed_segment(self, input_file: str, audio_file: str, audio: AudioSegment, output_file: str) -> bool:
        try:
            audio.export(audio_file, format="wav")
            output_dir = Path(output_file).parent
            ensure_dir_exists(str(output_dir))

            command = [
                'ffmpeg',
                '-y',
                '-i', input_file,
                '-i', audio_file,
                '-c:v', 'copy',
                '-c:a', 'aac',
                '-bsf:v', 'h264_mp4toannexb',
                '-map', '0:v:0',
                '-map', '1:a:0',
                output_file
            ]
            result = subprocess.run(command, check=True, capture_output=True, text=True)
            logging.debug(f"FFmpeg output: {result.stderr}")
            safe_delete_file(audio_file)
            return True
        except subprocess.CalledProcessError as e:
            logging.error(f"Ошибка FFmpeg при сохранении сегмента {output_file}: {e.stderr}")
            return False
        except Exception as e:
            logging.error(f"Неожиданная ошибка при сохранении сегмента {output_file}: {e}")
            return False

    def _combine_fragments(self, transcriptions: List[Dict], min_pause: float = 0.10) -> List[Dict]:
        if not transcriptions:
            return []
        combined = []
        current_start = None
        current_end = None
        current_words = []
        for i, entry in enumerate(transcriptions):
            if current_start is None:
                current_start = entry["start"]
                current_words = []
            current_words.append(entry["word"])
            current_end = entry["end"]
            if i + 1 < len(transcriptions):
                next_entry = transcriptions[i + 1]
                pause = next_entry["start"] - current_end
                if pause >= min_pause:
                    combined.append({
                        "start": current_start,
                        "end": current_end,
                        "sentence": " ".join(current_words),
                        "words": current_words.copy()
                    })
                    current_words = []
                    current_start = None
                    current_end = None
            else:
                combined.append({
                    "start": current_start,
                    "end": current_end,
                    "sentence": " ".join(current_words),
                    "words": current_words.copy()
                })
        return combined

    def _merge_overlapping_regions(self, regions: List[tuple]) -> List[tuple]:
        if not regions:
            return []
        sorted_regions = sorted(regions, key=lambda x: x[0])
        merged = [sorted_regions[0]]
        for current in sorted_regions[1:]:
            last = merged[-1]
            if current[0] <= last[1]:
                merged[-1] = (last[0], max(last[1], current[1]))
            else:
                merged.append(current)
        return merged

    def _detect_ads_in_fragments(self, fragments: List[Dict]) -> List[tuple]:
        ad_regions = []
        for frag in fragments:
            is_ad, _ = detect_ad(frag['sentence'], self.ad_model)
            if is_ad:
                ad_regions.append((frag['start'], frag['end']))
        return self._merge_overlapping_regions(ad_regions)

    def _detect_cross_segment_ads(self, combined_audio_file: str, prev_duration: float) -> Tuple[
        List[tuple], List[tuple]]:
        if not os.path.exists(combined_audio_file):
            logging.error(f"Файл {combined_audio_file} не существует для анализа стыка")
            return [], []
        transcriptions = transcribe_audio(combined_audio_file, self.speech_model)
        fragments = self._combine_fragments(transcriptions)
        prev_end_ad_regions = []
        next_start_ad_regions = []
        for frag in fragments:
            if frag['start'] < prev_duration and frag['end'] > prev_duration:
                is_ad, _ = detect_ad(frag['sentence'], self.ad_model)
                if is_ad:
                    prev_end_ad_regions.append((frag['start'], prev_duration))
                    next_start_ad_regions.append((0, frag['end'] - prev_duration))
        safe_delete_file(combined_audio_file)
        return prev_end_ad_regions, next_start_ad_regions

    def process_ts_segment(self, input_file: str, output_file: str, segment_index: int) -> bool:
        logging.debug(f"\n{'=' * 50}")
        logging.debug(f"Обработка сегмента {segment_index}")

        if not os.path.exists(input_file):
            logging.error(f"Входной файл {input_file} не существует")
            return False

        try:
            current_audio_file = extract_audio_from_ts(input_file)
            if current_audio_file is None or not os.path.exists(current_audio_file):
                logging.error(f"Не удалось извлечь аудио из {input_file}")
                return False
            current_audio = AudioSegment.from_wav(current_audio_file)
            current_duration = len(current_audio) / 1000

            if self.prev_segment_data is None:
                current_transcriptions = transcribe_audio(current_audio_file, self.speech_model)
                current_fragments = self._combine_fragments(current_transcriptions)
                ad_regions = self._detect_ads_in_fragments(current_fragments)
                processed_audio = current_audio
                for start, end in ad_regions:
                    processed_audio = replace_audio_segment(processed_audio, start, end)

                self.prev_segment_data = {
                    'audio': processed_audio,
                    'file': current_audio_file,
                    'index': segment_index,
                    'input': input_file,
                    'fragments': current_fragments
                }
                self._save_processed_segment(input_file, current_audio_file, processed_audio, output_file)
                logging.debug(f"Сегмент {segment_index} обработан и сохранен")
                return True

            prev_audio = self.prev_segment_data['audio']
            prev_audio_file = self.prev_segment_data['file']
            prev_index = self.prev_segment_data['index']
            prev_input = self.prev_segment_data['input']
            prev_duration = len(prev_audio) / 1000

            combined_audio = prev_audio + current_audio
            combined_audio_file = "temp_combined.wav"
            combined_audio.export(combined_audio_file, format="wav")
            if not os.path.exists(combined_audio_file):
                logging.error(f"Не удалось создать временный файл {combined_audio_file}")
                return False

            current_transcriptions = transcribe_audio(current_audio_file, self.speech_model)
            current_fragments = self._combine_fragments(current_transcriptions)
            current_ad_regions = self._detect_ads_in_fragments(current_fragments)

            prev_end_ad_regions, next_start_ad_regions = self._detect_cross_segment_ads(
                combined_audio_file, prev_duration
            )

            final_prev_audio = prev_audio
            for start, end in prev_end_ad_regions:
                logging.debug(f"Мьютим фрагмент рекламы на стыке в сегменте {prev_index}: {start}-{end}")
                final_prev_audio = replace_audio_segment(final_prev_audio, start, end)

            processed_current_audio = current_audio
            for start, end in next_start_ad_regions:
                logging.debug(f"Мьютим фрагмент рекламы на стыке в сегменте {segment_index}: {start}-{end}")
                processed_current_audio = replace_audio_segment(processed_current_audio, start, end)
            for start, end in current_ad_regions:
                logging.debug(f"Мьютим рекламу внутри сегмента {segment_index}: {start}-{end}")
                processed_current_audio = replace_audio_segment(processed_current_audio, start, end)

            prev_output = str(Path(output_file).parent / f"{prev_index}.ts")
            if self._save_processed_segment(prev_input, prev_audio_file, final_prev_audio, prev_output):
                self.prev_segment_data = {
                    'audio': processed_current_audio,
                    'file': current_audio_file,
                    'index': segment_index,
                    'input': input_file,
                    'fragments': current_fragments
                }
                logging.debug(f"Сегмент {prev_index} обработан и сохранен: {prev_output}")
                return True
            else:
                logging.error(f"Не удалось сохранить сегмент {prev_index}")
                return False

        except Exception as e:
            logging.error(f"Ошибка при обработке сегмента {segment_index}: {e}")
            return False

    def finalize_processing(self, output_file: str) -> None:
        if self.prev_segment_data:
            buf = self.prev_segment_data
            if self._save_processed_segment(buf['input'], buf['file'], buf['audio'], output_file):
                logging.debug(f"Финальный сегмент {buf['index']} обработан и сохранен")
            safe_delete_file(buf['file'])
