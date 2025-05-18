from pathlib import Path
from AdRecognitionModel import AdRecognitionModel
from SpeechRecognitionModel import SpeechRecognitionModel
from segment_processor import SegmentProcessor
import m3u8
import shutil
import logging
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler
import time
import os

class AdFilterHandler(FileSystemEventHandler):
    def __init__(self, stream_key, speech_model, ad_model):
        self.stream_key = stream_key
        self.input_path = Path(f"/app/hls/input/{stream_key}")
        self.output_path = Path(f"/app/hls/output/{stream_key}")
        self.processor = SegmentProcessor(speech_model.model, ad_model)
        self.processed_segments = set()

        # Создаём output директорию и поддиректорию для сегментов
        self.output_path.mkdir(parents=True, exist_ok=True)
        self.segments_dir = self.output_path / "processed_segments"
        self.segments_dir.mkdir(exist_ok=True)

        # Инициализируем плейлист
        self.playlist_path = self.output_path / "processed_playlist.m3u8"
        self._initialize_playlist()

        logging.info(f"Инициализирован обработчик для stream_key: {stream_key}")

    def _initialize_playlist(self):
        """Инициализирует новый или загружает существующий плейлист."""
        if not self.playlist_path.exists():
            new_playlist = m3u8.M3U8()
            new_playlist.version = 3
            new_playlist.target_duration = 10  # Настройте по вашим данным
            new_playlist.media_sequence = 0
            with open(self.playlist_path, "w") as f:
                f.write(new_playlist.dumps())
        else:
            with open(self.playlist_path, "r") as f:
                self.current_playlist = m3u8.load(f.read())

    def _update_playlist(self, segment_path, duration):
        """Обновляет плейлист новым сегментом."""
        with open(self.playlist_path, "r") as f:
            current_playlist = m3u8.load(f.read())

        rel_path = f"processed_segments/{Path(segment_path).name}"
        new_segment = m3u8.model.Segment(uri=rel_path, duration=duration)
        current_playlist.segments.append(new_segment)
        current_playlist.target_duration = max(current_playlist.target_duration, duration)

        with open(self.playlist_path, "w") as f:
            f.write(current_playlist.dumps())
        logging.info(f"Обновлён плейлист: добавлен сегмент {rel_path}")

    def on_created(self, event):
        """Обрабатывает создание нового файла в input директории."""
        if event.is_directory or not event.src_path.endswith('.ts'):
            return

        input_ts = Path(event.src_path)
        output_ts = self.segments_dir / input_ts.name
        segment_idx = int(input_ts.name.split('.')[0]) if input_ts.name[0].isdigit() else 0

        logging.info(f"Обнаружен новый сегмент: {input_ts} -> {output_ts}")

        # Обрабатываем сегмент
        try:
            processed = self.processor.process_ts_segment(str(input_ts), str(output_ts), segment_idx)
            if processed:
                self.processed_segments.add(segment_idx - 1 if segment_idx > 0 else segment_idx)
                shutil.copy2(input_ts, output_ts)
                self._update_playlist(output_ts, 3)  # Предполагаемая длительность 3 секунды
                logging.info(f"Сегмент {input_ts.name} обработан и скопирован в {output_ts}")
            else:
                logging.warning(f"Сегмент {input_ts.name} содержит рекламу и пропущен")
        except Exception as e:
            logging.error(f"Ошибка при обработке сегмента {input_ts}: {e}")

        # Финализация последнего сегмента (если это последний)
        if segment_idx == len(os.listdir(self.input_path)) - 1:
            self.processor.finalize_processing(str(output_ts))

    def on_modified(self, event):
        """Обрабатывает изменения (например, плейлиста)."""
        if event.src_path.endswith('.m3u8'):
            logging.info(f"Обнаружено изменение плейлиста: {event.src_path}")

def monitor_stream(stream_key, speech_model, ad_model):
    """Мониторит директорию input для заданного stream_key."""
    input_path = Path(f"/app/hls/input/{stream_key}")
    if not input_path.exists():
        logging.info(f"Директория {input_path} не существует, ожидаем её создания...")
        while not input_path.exists():
            time.sleep(1)

    event_handler = AdFilterHandler(stream_key, speech_model, ad_model)
    observer = Observer()
    observer.schedule(event_handler, str(input_path), recursive=False)
    observer.start()

    logging.info(f"Начало мониторинга директории: {input_path}")
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        observer.stop()
    observer.join()

def discover_streams(speech_model, ad_model):
    """Обнаруживает все stream_key в input и запускает мониторинг."""
    input_base_path = Path("/app/hls/input")
    input_base_path.mkdir(parents=True, exist_ok=True)

    while True:
        for stream_key in os.listdir(input_base_path):
            stream_path = input_base_path / stream_key
            if stream_path.is_dir():
                logging.info(f"Обнаружен stream_key: {stream_key}")
                monitor_stream(stream_key, speech_model, ad_model)
        time.sleep(5)

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

        logging.debug(f"Обработка сегмента {idx}: {input_ts} -> {output_ts}")

        # Пытаемся обработать текущий и следующий сегмент
        processed = processor.process_ts_segment(str(input_ts), str(output_ts), idx)

def main():
    # Настройка логирования
    logging.basicConfig(
        level=logging.ERROR,
        format='%(asctime)s - %(levelname)s - %(message)s',
        handlers=[
            logging.FileHandler('/app/processing.log', encoding='utf-8'),
            logging.StreamHandler()
        ]
    )

    # Пути к моделям
    speech_model_path = "/models/vosk-model-small-ru-0.22"
    ad_model_path = "/models/ad_recognizer"

    logging.debug("Начало обработки HLS в реальном времени")

    # Инициализация моделей
    logging.debug("Инициализация моделей...")
    speech_model = SpeechRecognitionModel(speech_model_path)
    ad_model = AdRecognitionModel(ad_model_path)

    # Запуск мониторинга всех стримов
    discover_streams(speech_model, ad_model)

if __name__ == "__main__":
    main()