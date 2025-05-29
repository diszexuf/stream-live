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
        self.input_path = Path(f"./hls_data/input/{stream_key}")
        self.output_path = Path(f"./hls_data/output/{stream_key}")
        self.processor = SegmentProcessor(speech_model.model, ad_model)
        self.processed_segments = set()

        # Создаём output директорию и поддиректорию для сегментов
        self.output_path.mkdir(parents=True, exist_ok=True)
        self.segments_dir = self.output_path
        self.segments_dir.mkdir(exist_ok=True)

        # Инициализируем плейлист
        self.playlist_path = self.output_path / "index.m3u8"
        self._initialize_playlist()

        logging.info(f"Инициализирован обработчик для stream_key: {stream_key}")

    def _initialize_playlist(self):
        """Инициализирует новый или загружает существующий плейлист."""
        if not self.playlist_path.exists():
            # Создаем новый плейлист с базовой структурой
            playlist_content = "#EXTM3U\n#EXT-X-VERSION:3\n#EXT-X-TARGETDURATION:10\n#EXT-X-MEDIA-SEQUENCE:0\n"
            with open(self.playlist_path, "w", encoding='utf-8') as f:
                f.write(playlist_content)
            logging.info(f"Создан новый плейлист: {self.playlist_path}")
        else:
            logging.info(f"Загружен существующий плейлист: {self.playlist_path}")

    def _update_playlist(self, segment_path, duration):
        """Обновляет плейлист новым сегментом."""
        try:
            # Правильно загружаем плейлист из файла
            current_playlist = m3u8.load(str(self.playlist_path))

            # Добавляем новый сегмент
            rel_path = Path(segment_path).name
            new_segment = m3u8.model.Segment(uri=rel_path, duration=duration)
            current_playlist.segments.append(new_segment)

            # Обновляем target_duration если нужно
            if duration > current_playlist.target_duration:
                current_playlist.target_duration = int(duration) + 1

            # Сохраняем обновленный плейлист
            with open(self.playlist_path, "w", encoding='utf-8') as f:
                f.write(current_playlist.dumps())

            logging.info(f"Обновлён плейлист: добавлен сегмент {rel_path} (длительность: {duration}s)")

        except Exception as e:
            logging.error(f"Ошибка при обновлении плейлиста: {e}")
            # Fallback - добавляем сегмент вручную
            self._manual_playlist_update(segment_path, duration)

    def _manual_playlist_update(self, segment_path, duration):
        """Ручное обновление плейлиста в случае ошибок с m3u8."""
        try:
            rel_path = Path(segment_path).name
            segment_line = f"#EXTINF:{duration:.3f},\n{rel_path}\n"

            with open(self.playlist_path, "a", encoding='utf-8') as f:
                f.write(segment_line)

            logging.info(f"Плейлист обновлен вручную: добавлен {rel_path}")

        except Exception as e:
            logging.error(f"Критическая ошибка при обновлении плейлиста: {e}")

    def on_created(self, event):
        if event.is_directory or not event.src_path.endswith('.ts'):
            return

        input_ts = Path(event.src_path)
        output_ts = self.segments_dir / input_ts.name

        # Извлекаем индекс сегмента из имени файла
        try:
            segment_idx = int(input_ts.stem)
        except ValueError:
            segment_idx = len(self.processed_segments)

        logging.info(f"Обнаружен новый сегмент: {input_ts} -> {output_ts}")

        try:
            # Обрабатываем сегмент
            processed = self.processor.process_ts_segment(str(input_ts), str(output_ts), segment_idx)

            if processed:
                self.processed_segments.add(segment_idx)

                # Получаем длительность сегмента
                duration = self._get_segment_duration(str(input_ts))

                # Обновляем плейлист
                self._update_playlist(output_ts, duration)

                logging.info(f"Сегмент {input_ts.name} успешно обработан и добавлен в плейлист")
            else:
                logging.warning(f"Сегмент {input_ts.name} содержит рекламу и пропущен")

        except Exception as e:
            logging.error(f"Ошибка при обработке сегмента {input_ts}: {e}")
            # В случае ошибки копируем оригинальный сегмент
            try:
                shutil.copy2(input_ts, output_ts)
                duration = self._get_segment_duration(str(input_ts))
                self._update_playlist(output_ts, duration)
                logging.info(f"Скопирован оригинальный сегмент {input_ts.name} после ошибки обработки")
            except Exception as copy_error:
                logging.error(f"Не удалось скопировать сегмент {input_ts}: {copy_error}")

    def _get_segment_duration(self, ts_file):
        """Получает длительность TS сегмента."""
        try:
            import subprocess
            result = subprocess.run([
                'ffprobe', '-v', 'quiet', '-show_entries', 'format=duration',
                '-of', 'csv=p=0', ts_file
            ], capture_output=True, text=True, timeout=10)

            if result.returncode == 0 and result.stdout.strip():
                return float(result.stdout.strip())
            else:
                logging.warning(f"Не удалось получить длительность {ts_file}, используем значение по умолчанию")
                return 3.0  # Значение по умолчанию

        except Exception as e:
            logging.warning(f"Ошибка при получении длительности {ts_file}: {e}")
            return 3.0  # Значение по умолчанию

    def on_modified(self, event):
        """Обрабатывает изменения (например, плейлиста)."""
        if event.src_path.endswith('.m3u8'):
            logging.info(f"Обнаружено изменение плейлиста: {event.src_path}")


def monitor_stream(stream_key, speech_model, ad_model):
    input_path = Path(f"./hls_data/input/{stream_key}")
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
        logging.info("Получен сигнал остановки, завершаем мониторинг...")
    observer.join()


def discover_streams(speech_model, ad_model):
    """Обнаруживает все stream_key в input и запускает мониторинг."""
    input_base_path = Path("./hls_data/input")
    input_base_path.mkdir(parents=True, exist_ok=True)

    processed_streams = set()

    while True:
        try:
            current_streams = set()
            for stream_key in os.listdir(input_base_path):
                stream_path = input_base_path / stream_key
                if stream_path.is_dir():
                    current_streams.add(stream_key)
                    if stream_key not in processed_streams:
                        logging.info(f"Обнаружен новый stream_key: {stream_key}")
                        processed_streams.add(stream_key)
                        monitor_stream(stream_key, speech_model, ad_model)
                        return

        except Exception as e:
            logging.error(f"Ошибка при сканировании директорий: {e}")

        time.sleep(5)


def process_hls_playlist(input_playlist: Path, output_dir: Path,
                         speech_model: SpeechRecognitionModel, ad_model: AdRecognitionModel) -> Path:
    processor = SegmentProcessor(speech_model.model, ad_model)
    playlist = m3u8.load(str(input_playlist))
    output_dir.mkdir(parents=True, exist_ok=True)
    segments_dir = output_dir
    segments_dir.mkdir(exist_ok=True)

    new_playlist = m3u8.M3U8()
    new_playlist.version = playlist.version
    new_playlist.target_duration = playlist.target_duration
    new_playlist.media_sequence = playlist.media_sequence

    processed_segments = set()
    for idx, segment in enumerate(playlist.segments):
        input_ts = input_playlist.parent / segment.uri
        output_ts = segments_dir / Path(segment.uri).name
        logging.debug(f"Обработка сегмента {idx}: {input_ts} -> {output_ts}")
        processed = processor.process_ts_segment(str(input_ts), str(output_ts), idx)
        if processed:
            processed_segments.add(idx - 1 if idx > 0 else idx)
            new_segment = m3u8.model.Segment(uri=f"{Path(segment.uri).name}",
                                             duration=segment.duration)
            new_playlist.segments.append(new_segment)

    playlist_path = output_dir / "index.m3u8"
    with open(playlist_path, "w", encoding='utf-8') as f:
        f.write(new_playlist.dumps())
    return playlist_path


def main():
    try:
        logging.basicConfig(
            level=logging.DEBUG,
            format='%(asctime)s - %(levelname)s - %(message)s',
            handlers=[
                logging.StreamHandler()
            ]
        )
        speech_model_path = "models/vosk-model-small-ru-0.22"
        ad_model_path = "models/ad_recognizer"

        logging.debug("Начало обработки HLS в реальном времени")

        logging.debug("Инициализация модели Vosk...")
        speech_model = SpeechRecognitionModel(speech_model_path)
        logging.debug("Модель Vosk успешно загружена")

        logging.debug("Инициализация модели AdRecognition...")
        ad_model = AdRecognitionModel(ad_model_path)
        logging.debug("Модель AdRecognition успешно загружена")

        logging.debug("Запуск мониторинга стримов...")
        discover_streams(speech_model, ad_model)

    except KeyboardInterrupt:
        logging.info("Получен сигнал завершения работы")
    except Exception as e:
        logging.error(f"Произошла ошибка в main: {e}")
        raise


if __name__ == "__main__":
    main()