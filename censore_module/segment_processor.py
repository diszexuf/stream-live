import subprocess
from typing import Dict, List, Tuple
from pydub import AudioSegment
import logging
import os

from audio_processor import extract_audio_from_ts, replace_audio_segment
from speech_recognizer import transcribe_audio
from ad_detector import detect_ad

# Настройка логирования
logging.basicConfig(
    level=logging.ERROR,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('processing.log', encoding='utf-8'),
        logging.StreamHandler()
    ]
)


class SegmentProcessor:
    def __init__(self, speech_model, ad_model):
        self.speech_model = speech_model
        self.ad_model = ad_model
        # Буферы для последовательной обработки
        self.prev_segment_data = None  # Данные предыдущего сегмента для обработки текущего

    def _save_processed_segment(self, input_file, audio_file, audio, output_file):
        """Сохраняет обработанный сегмент"""
        audio.export(audio_file, format="wav")
        command = [
            'ffmpeg',
            '-y',
            '-i', input_file,
            '-i', audio_file,
            '-c:v', 'copy',
            '-c:a', 'aac',
            '-map', '0:v:0',
            '-map', '1:a:0',
            output_file
        ]
        subprocess.run(command, check=True)

    def _combine_fragments(self, transcriptions: List[Dict], min_pause: float = 0.10) -> List[Dict]:
        """Объединяет слова в предложения на основе пауз"""
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
        """Объединяет перекрывающиеся временные интервалы"""
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
        """Обнаруживает рекламу в фрагментах"""
        ad_regions = []

        for frag in fragments:
            is_ad, _ = detect_ad(frag['sentence'], self.ad_model)
            if is_ad:
                ad_regions.append((frag['start'], frag['end']))

        return self._merge_overlapping_regions(ad_regions)

    def _detect_cross_segment_ads(self, combined_audio_file: str, prev_duration: float) -> Tuple[
        List[tuple], List[tuple]]:
        """
        Обнаруживает рекламу на стыке сегментов.
        Возвращает: (регионы в конце предыдущего сегмента, регионы в начале текущего сегмента)
        """
        # Распознаем речь в объединенном аудио
        transcriptions = transcribe_audio(combined_audio_file, self.speech_model)
        fragments = self._combine_fragments(transcriptions)

        prev_end_ad_regions = []
        next_start_ad_regions = []

        for frag in fragments:
            # Интересуют только фрагменты, пересекающие границу сегментов
            if frag['start'] < prev_duration and frag['end'] > prev_duration:
                is_ad, _ = detect_ad(frag['sentence'], self.ad_model)
                if is_ad:
                    # Если это реклама, добавляем регионы в соответствующие списки
                    # Регион для конца предыдущего сегмента
                    prev_end_ad_regions.append((frag['start'], prev_duration))
                    # Регион для начала текущего сегмента (корректируем временные метки)
                    next_start_ad_regions.append((0, frag['end'] - prev_duration))

        return prev_end_ad_regions, next_start_ad_regions

    def process_ts_segment(self, input_file: str, output_file: str, segment_index: int) -> bool:
        """
        Обрабатывает сегменты медиа-контента.
        Возвращает True, если сегмент был обработан и сохранен.
        """
        logging.debug(f"\n{'=' * 50}")
        logging.debug(f"Обработка сегмента {segment_index}")

        try:
            # Извлекаем аудио текущего сегмента
            current_audio_file = extract_audio_from_ts(input_file)
            current_audio = AudioSegment.from_wav(current_audio_file)
            current_duration = len(current_audio) / 1000  # в секундах

            # Если это первый сегмент или нет предыдущего, сохраняем его для следующей итерации
            if self.prev_segment_data is None:
                # Распознаем речь в первом сегменте
                current_transcriptions = transcribe_audio(current_audio_file, self.speech_model)
                current_fragments = self._combine_fragments(current_transcriptions)

                # Проверяем наличие рекламы в первом сегменте
                ad_regions = self._detect_ads_in_fragments(current_fragments)

                # ОТЛИЧИЕ: Для первого сегмента уже применяем обработку рекламы внутри
                processed_audio = current_audio
                for start, end in ad_regions:
                    processed_audio = replace_audio_segment(processed_audio, start, end)

                # Сохраняем данные сегмента для следующей итерации
                self.prev_segment_data = {
                    'audio': processed_audio,
                    'file': current_audio_file,
                    'index': segment_index,
                    'input': input_file,
                    'fragments': current_fragments
                }

                logging.debug(f"Сегмент {segment_index} обработан и сохранен в буфер")
                return False  # Сегмент не был сохранен

            # Извлекаем данные предыдущего сегмента из буфера
            prev_audio = self.prev_segment_data['audio']
            prev_audio_file = self.prev_segment_data['file']
            prev_index = self.prev_segment_data['index']
            prev_input = self.prev_segment_data['input']
            prev_duration = len(prev_audio) / 1000  # длительность предыдущего аудио в секундах

            # Объединяем аудио для анализа стыка сегментов
            combined_audio = prev_audio + current_audio
            combined_audio_file = "temp_combined.wav"
            combined_audio.export(combined_audio_file, format="wav")

            # 1. Обрабатываем рекламу внутри текущего сегмента N+1
            current_transcriptions = transcribe_audio(current_audio_file, self.speech_model)
            current_fragments = self._combine_fragments(current_transcriptions)
            current_ad_regions = self._detect_ads_in_fragments(current_fragments)

            # 2. Обнаруживаем рекламу на стыке сегментов
            prev_end_ad_regions, next_start_ad_regions = self._detect_cross_segment_ads(
                combined_audio_file, prev_duration
            )

            # 3. Применяем изменения к предыдущему сегменту (N)
            # Если в конце предыдущего сегмента есть часть рекламы, мьютим её
            final_prev_audio = prev_audio
            for start, end in prev_end_ad_regions:
                logging.debug(f"Мьютим фрагмент рекламы на стыке в сегменте {prev_index}: {start}-{end}")
                final_prev_audio = replace_audio_segment(final_prev_audio, start, end)

            # 4. Применяем изменения к текущему сегменту (N+1)
            processed_current_audio = current_audio
            # Мьютим начало сегмента, если оно часть рекламы на стыке
            for start, end in next_start_ad_regions:
                logging.debug(f"Мьютим фрагмент рекламы на стыке в сегменте {segment_index}: {start}-{end}")
                processed_current_audio = replace_audio_segment(processed_current_audio, start, end)

            # Мьютим рекламу внутри текущего сегмента
            for start, end in current_ad_regions:
                logging.debug(f"Мьютим рекламу внутри сегмента {segment_index}: {start}-{end}")
                processed_current_audio = replace_audio_segment(processed_current_audio, start, end)

            # 5. Сохраняем обработанный предыдущий сегмент (N)
            prev_output = output_file.replace(f"{segment_index}", f"{prev_index}")
            self._save_processed_segment(
                prev_input,
                prev_audio_file,
                final_prev_audio,
                prev_output
            )

            # 6. Обновляем буфер для следующей итерации
            self.prev_segment_data = {
                'audio': processed_current_audio,
                'file': current_audio_file,
                'index': segment_index,
                'input': input_file,
                'fragments': current_fragments
            }

            # Удаляем временный файл
            if os.path.exists(combined_audio_file):
                os.remove(combined_audio_file)

            logging.debug(f"Сегмент {prev_index} обработан и сохранен: {prev_output}")
            return True

        except Exception as e:
            logging.error(f"Ошибка при обработке сегмента {segment_index}: {str(e)}")
            return False

    def finalize_processing(self, output_file: str) -> None:
        """Обрабатывает последний сегмент из буфера"""
        if self.prev_segment_data:
            buf = self.prev_segment_data
            self._save_processed_segment(
                buf['input'],
                buf['file'],
                buf['audio'],
                output_file
            )
            logging.debug(f"Финальный сегмент {buf['index']} обработан и сохранен")
