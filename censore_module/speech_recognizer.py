import logging
import wave
import json
from vosk import KaldiRecognizer
from typing import List, Dict


def transcribe_audio(wav_path: str, model) -> List[Dict]:
    transcriptions = []
    try:
        with wave.open(wav_path, 'rb') as wf:
            audio_data = wf.readframes(wf.getnframes())
            if not audio_data:
                logging.warning(f"Аудиофайл {wav_path} пустой")
                return transcriptions

            rec = KaldiRecognizer(model, wf.getframerate(), '{"vad_threshold": 0.0}')
            rec.SetWords(True)

            if rec.AcceptWaveform(audio_data):
                result = json.loads(rec.Result())
                logging.debug(f"Полный результат Vosk: {result}")
                _process_result(result, transcriptions)
            else:
                final_result = json.loads(rec.FinalResult())
                logging.debug(f"Финальный результат Vosk: {final_result}")
                _process_result(final_result, transcriptions)

            if transcriptions:
                words = [f"{t['word']} ({t['start']:.2f}-{t['end']:.2f}s)" for t in transcriptions]
                logging.info(f"Распознанный текст в {wav_path}: {' '.join(words)}")
                logging.info(f"Распознано {len(transcriptions)} слов в {wav_path}")
            else:
                logging.warning(f"Нет распознанного текста в {wav_path}")

            return transcriptions

    except Exception as e:
        logging.error(f"Ошибка при распознавании аудио {wav_path}: {str(e)}")
        return transcriptions


def _process_result(result: Dict, transcriptions: List[Dict]) -> None:
    if 'result' in result and result['result']:
        words = result['result']
        for word in words:
            transcriptions.append({
                "start": word['start'],
                "end": word['end'],
                "word": word['word']
            })
        logging.debug(f"Обработано {len(words)} слов")
    else:
        logging.debug(f"Нет слов в результате")
