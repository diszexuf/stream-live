from typing import Tuple
import logging
import numpy as np


def detect_ad(text: str, model) -> Tuple[bool, float]:
    """
    Определяет, является ли текст рекламой
    
    Args:
        text: текст для проверки
        model: объект класса AdRecognitionModel
        
    Returns:
        (является ли рекламой, вероятность)
    """
    try:
        # Используем метод predict модели AdRecognitionModel
        predicted_label, probabilities = model.predict(text)

        # Преобразуем в numpy массив, если требуется
        if not isinstance(probabilities, np.ndarray):
            probabilities = np.array(probabilities)

        # Проверяем размерность выходных данных
        if probabilities.size > 1:
            ad_probability = float(probabilities[0][1])
            is_ad = ad_probability > 0.8
            logging.debug(f"Текст: '{text}' | Вероятность рекламы: {ad_probability}")
            return is_ad, ad_probability
        else:
            logging.error(f"Неверный формат вероятностей: {probabilities}")
            return False, 0.0

    except Exception as e:
        logging.error(f"Ошибка при проверке рекламы: {str(e)}")
        logging.error(f"Текст, вызвавший ошибку: '{text}'")
        return False, 0.0
