import logging

from vosk import Model


class SpeechRecognitionModel:
    def __init__(self, model_path):
        try:
            self.model = Model(model_path)
            logging.info("Model VOSK loaded")
        except Exception as e:
            logging.error(f"Ошибка загрузки модели Vosk из {model_path}: {e}")
            raise
