from vosk import Model

class SpeechRecognitionModel:
    def __init__(self, model_path):
        self.model = Model(model_path)
