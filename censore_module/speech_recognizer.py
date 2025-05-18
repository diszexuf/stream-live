import wave
import json
from vosk import KaldiRecognizer
from typing import List, Dict


def transcribe_audio(wav_path: str, model) -> List[Dict]:
    """
    Распознает речь в аудиофайле
    
    Args:
        wav_path: путь к WAV файлу
        model: модель распознавания речи
        
    Returns:
        список словарей с распознанными словами и их временными метками
    """
    transcriptions = []
    try:
        with wave.open(wav_path, 'rb') as wf:
            audio_data = wf.readframes(wf.getnframes())
            rec = KaldiRecognizer(model, wf.getframerate())
            rec.SetWords(True)

            if rec.AcceptWaveform(audio_data):
                result = json.loads(rec.Result())
                _process_result(result, transcriptions)
            else:
                final_result = json.loads(rec.FinalResult())
                _process_result(final_result, transcriptions)

        # Убираем вывод в консоль
        # print("Транскрипции:", transcriptions)
    except Exception as e:
        print(f"Ошибка при распознавании аудио: {str(e)}")

    return transcriptions


def _process_result(result: Dict, transcriptions: List[Dict]) -> None:
    """
    Обрабатывает результат распознавания речи
    
    Args:
        result: результат распознавания
        transcriptions: список для сохранения транскрипций
    """
    if 'result' in result and result['result']:
        words = result['result']
        for word in words:
            transcriptions.append({
                "start": word['start'],
                "end": word['end'],
                "word": word['word']
            })
