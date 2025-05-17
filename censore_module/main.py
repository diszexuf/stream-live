from pathlib import Path
from AdRecognitionModel import AdRecognitionModel
from SpeechRecognitionModel import SpeechRecognitionModel
from segment_processor import SegmentProcessor
from hls_converter import convert_mp4_to_hls, convert_hls_to_mp4
import m3u8
import shutil
import logging


def process_hls_playlist(input_playlist: Path, output_dir: Path,
                         speech_model: SpeechRecognitionModel, ad_model: AdRecognitionModel) -> Path:
    """
    Обрабатывает HLS-плейлист с учетом контекста между сегментами
    """
    # Инициализация обработчика сегментов
    processor = SegmentProcessor(speech_model.model, ad_model)

    playlist = m3u8.load(str(input_playlist))
    output_dir.mkdir(parents=True, exist_ok=True)
    segments_dir = output_dir / "processed_segments"
    segments_dir.mkdir(exist_ok=True)

    # Создаем новый плейлист
    new_playlist = m3u8.M3U8()
    new_playlist.version = playlist.version
    new_playlist.target_duration = playlist.target_duration
    new_playlist.media_sequence = playlist.media_sequence

    # Список для отслеживания обработанных сегментов
    processed_segments = set()

    # Обрабатываем каждый сегмент
    for idx, segment in enumerate(playlist.segments):
        input_ts = input_playlist.parent / segment.uri
        output_ts = segments_dir / Path(segment.uri).name

        logging.info(f"Обработка сегмента {idx}: {input_ts} -> {output_ts}")

        # Пытаемся обработать текущий и следующий сегмент
        processed = processor.process_ts_segment(str(input_ts), str(output_ts), idx)

        # Если сегмент обработан, отмечаем его как обработанный
        if processed:
            processed_segments.add(idx - 1)  # Обработан предыдущий сегмент

        # Создаем новый сегмент для плейлиста
        rel_path = f"processed_segments/{Path(segment.uri).name}"
        new_segment = m3u8.model.Segment(
            uri=rel_path,
            duration=segment.duration,
            title=segment.title
        )
        new_playlist.segments.append(new_segment)

    # Обрабатываем последний сегмент
    if len(playlist.segments) > 0:
        last_idx = len(playlist.segments) - 1
        if last_idx not in processed_segments:
            last_segment = playlist.segments[last_idx]
            last_input_ts = input_playlist.parent / last_segment.uri
            last_output_ts = segments_dir / Path(last_segment.uri).name

            # Финализируем последний сегмент
            processor.finalize_processing(str(last_output_ts))

    # Сохраняем плейлист
    playlist_path = output_dir / "processed_playlist.m3u8"
    with open(playlist_path, "w") as f:
        f.write(new_playlist.dumps())

    return playlist_path


def main():
    # Настройка логирования
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(levelname)s - %(message)s',
        handlers=[
            logging.FileHandler('processing.log', encoding='utf-8'),
            logging.StreamHandler()
        ]
    )

    # Пути к моделям и файлам
    speech_model_path = "../models/vosk-model-small-ru-0.22"
    ad_model_path = "../models/ad_recognizer"
    input_mp4_path = "../input_data/input_video_gera.mp4"
    output_hls_folder_path = "original_hls"
    output_hls_process_dir = "processed_hls"

    logging.info("Начало обработки видео")
    logging.info(f"Входной файл: {input_mp4_path}")

    # Инициализация моделей
    logging.info("Инициализация моделей...")
    speech_model = SpeechRecognitionModel(speech_model_path)
    ad_model = AdRecognitionModel(ad_model_path)

    # Конвертация MP4 в HLS
    logging.info("Конвертация MP4 в HLS...")
    playlist_path = convert_mp4_to_hls(input_mp4_path, output_hls_folder_path)
    if not playlist_path:
        logging.error("Ошибка при конвертации в HLS")
        return

    # ----------------------------------------------------------------------------------

    # Обработка HLS
    logging.info("Обработка HLS...")
    processed_playlist = process_hls_playlist(
        playlist_path,
        Path(output_hls_process_dir),
        speech_model,
        ad_model
    )

    # ----------------------------------------------------------------------------------

    logging.info(f"Обработанный плейлист: {processed_playlist}")

    # Конвертация обратно в MP4
    logging.info("Конвертация обратно в MP4...")
    if convert_hls_to_mp4(str(processed_playlist), 'processed.mp4'):
        logging.info("Обработка завершена успешно!")
    else:
        logging.error("Ошибка при конвертации в MP4")


# PTS (Presentation Time Stamp) когда нужно показать кадр
# DTS (Decoding Time Stamp) когда нужно расшифровать кадр

if __name__ == "__main__":
    main()
