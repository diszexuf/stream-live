import logging

from transformers import AutoTokenizer, AutoModelForSequenceClassification
import torch


class AdRecognitionModel:
    def __init__(self, model_path):
        try:
            self.tokenizer = AutoTokenizer.from_pretrained(model_path)
            self.model = AutoModelForSequenceClassification.from_pretrained(model_path)
        except Exception as e:
            logging.error(f"Ошибка загрузки модели Transformers из {model_path}: {e}")
            raise

    def predict(self, statement):
        inputs = self.tokenizer(statement, return_tensors="pt", truncation=True, padding=True, max_length=512)
        with torch.no_grad():
            outputs = self.model(**inputs)
            logits = outputs.logits
            probabilities = torch.nn.functional.softmax(logits, dim=-1)
            predicted_label = torch.argmax(probabilities, dim=1).item()
            return predicted_label, probabilities.numpy()
