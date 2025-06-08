from pathlib import Path
from ad_recognition_model import AdRecognitionModel
from segment_processor import SegmentProcessor
from ad_filter_handler import AdFilterHandler
import logging
from watchdog.observers import Observer
import time
from speech_recognition_model import SpeechRecognitionModel


class StreamManager:
    def __init__(self, speech_model, ad_model):
        self.speech_model = speech_model
        self.ad_model = ad_model
        self.active_streams = {}
        self.observers = {}

    def start_stream_monitoring(self, stream_key):
        """Запускает мониторинг конкретного стрима в отдельном потоке"""
        if stream_key in self.active_streams:
            logging.warning(f"Стрим {stream_key} уже мониторится")
            return

        input_path = Path(f"./hls_data/input/{stream_key}")
        if not input_path.exists():
            logging.info(f"Ожидаем создания директории {input_path}")
            return False

        try:
            event_handler = AdFilterHandler(stream_key, self.speech_model, self.ad_model)
            event_handler.processor = SegmentProcessor(self.speech_model.model, self.ad_model)

            observer = Observer()
            observer.schedule(event_handler, str(input_path), recursive=False)
            observer.start()

            self.active_streams[stream_key] = event_handler
            self.observers[stream_key] = observer

            logging.info(f"Запущен мониторинг стрима: {stream_key}")
            return True

        except Exception as e:
            logging.error(f"Ошибка запуска мониторинга {stream_key}: {e}")
            return False

    def stop_stream_monitoring(self, stream_key):
        """Останавливает мониторинг стрима"""
        if stream_key in self.observers:
            self.observers[stream_key].stop()
            self.observers[stream_key].join()
            del self.observers[stream_key]

        if stream_key in self.active_streams:
            self.active_streams[stream_key].on_stop()
            del self.active_streams[stream_key]

        logging.info(f"Остановлен мониторинг стрима: {stream_key}")

    def discover_and_monitor_streams(self):
        """Непрерывно ищет новые стримы и запускает их мониторинг"""
        input_base_path = Path("./hls_data/input")
        input_base_path.mkdir(parents=True, exist_ok=True)

        while True:
            try:
                current_streams = set()

                if input_base_path.exists():
                    for item in input_base_path.iterdir():
                        if item.is_dir():
                            current_streams.add(item.name)

                for stream_key in current_streams:
                    if stream_key not in self.active_streams:
                        logging.info(f"Обнаружен новый стрим: {stream_key}")
                        self.start_stream_monitoring(stream_key)

                removed_streams = set(self.active_streams.keys()) - current_streams
                for stream_key in removed_streams:
                    logging.info(f"Стрим удален: {stream_key}")
                    self.stop_stream_monitoring(stream_key)

            except Exception as e:
                logging.error(f"Ошибка при сканировании стримов: {e}")

            time.sleep(5)

    def shutdown(self):
        """Корректное завершение работы всех стримов"""
        for stream_key in list(self.active_streams.keys()):
            self.stop_stream_monitoring(stream_key)


def main():
    try:
        logging.basicConfig(
            level=logging.INFO,
            format='%(asctime)s - %(levelname)s - %(name)s - %(funcName)s - %(message)s',
            handlers=[logging.StreamHandler()]
        )

        speech_model_path = "models/other/vosk-model-ru-0.42"
        ad_model_path = "models/other/best-model"
        # speech_model_path = "models/vosk-model-small-ru-0.22"
        # ad_model_path = "models/ad_recognizer"

        logging.debug("Инициализация моделей...")
        speech_model = SpeechRecognitionModel(speech_model_path)
        ad_model = AdRecognitionModel(ad_model_path)
        logging.debug("Модели успешно загружены")

        stream_manager = StreamManager(speech_model, ad_model)

        try:
            stream_manager.discover_and_monitor_streams()
        except KeyboardInterrupt:
            logging.info("Получен сигнал завершения работы")
        finally:
            stream_manager.shutdown()

    except Exception as e:
        logging.error(f"Критическая ошибка: {e}")
        raise


if __name__ == "__main__":
    main()
