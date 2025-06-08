import logging
import tempfile
import subprocess
import os
import wave
import numpy as np
from pydub import AudioSegment


def extract_audio_from_ts(ts_file: str) -> str:
    temp_audio = tempfile.NamedTemporaryFile(delete=False, suffix=".wav")
    temp_audio_path = temp_audio.name
    temp_audio.close()

    try:
        command = [
            'ffmpeg', '-y', '-i', ts_file, '-vn',
            '-af', 'loudnorm=I=-15:TP=-9:LRA=11',
            '-acodec', 'pcm_s16le', '-ar', '16000', '-ac', '1',
            temp_audio_path
        ]
        subprocess.run(command, check=True, capture_output=True)

        audio = AudioSegment.from_wav(temp_audio_path)

        logging.debug(f"Извлечено аудио из {ts_file}: громкость {audio.dBFS:.1f}dB, "
                      f"максимум {audio.max_dBFS:.1f}dB, длительность {len(audio) / 1000.0:.2f}s")
        if audio.max_dBFS > -9.0:
            logging.warning(f"Пики громкости в {ts_file} превышают -9 дБ: {audio.max_dBFS:.1f}dB")
        if not (2.7 <= len(audio) / 1000.0 <= 3.3):
            logging.warning(f"Длительность аудио в {ts_file} не около 3s: {len(audio) / 1000.0:.2f}s")
        return temp_audio_path

    except subprocess.CalledProcessError as e:
        logging.error(f"Ошибка при извлечении аудио: {e.stderr.decode()}")
        try:
            os.unlink(temp_audio_path)
        except:
            pass
        return None


def generate_beep(duration_ms: int, frequency: int = 1000, volume: float = 0.2) -> AudioSegment:
    sample_rate = 16000
    t = np.linspace(0, duration_ms / 1000, int(duration_ms * sample_rate / 1000))
    beep_data = np.sin(2 * np.pi * frequency * t) * volume
    beep_data = (beep_data * 32767).astype(np.int16)

    temp_beep = tempfile.NamedTemporaryFile(delete=False, suffix=".wav")
    temp_beep_path = temp_beep.name
    temp_beep.close()

    try:
        with wave.open(temp_beep_path, 'wb') as wf:
            wf.setnchannels(1)
            wf.setsampwidth(2)
            wf.setframerate(sample_rate)
            wf.writeframes(beep_data.tobytes())

        beep = AudioSegment.from_wav(temp_beep_path)
        return beep
    finally:
        try:
            os.unlink(temp_beep_path)
        except:
            pass


def replace_audio_segment(audio: AudioSegment, start_time: float, end_time: float,
                          beep_frequency: int = 1000, beep_volume: float = 0.2) -> AudioSegment:
    audio_duration_ms = len(audio)
    start_time_ms = int(start_time * 1000)
    end_time_ms = int(end_time * 1000)

    start_time_ms = max(0, min(start_time_ms, audio_duration_ms))
    end_time_ms = max(start_time_ms, min(end_time_ms, audio_duration_ms))

    beep_duration_ms = end_time_ms - start_time_ms
    beep = generate_beep(beep_duration_ms, beep_frequency, beep_volume)

    result = audio[:start_time_ms] + beep + audio[end_time_ms:]

    if abs(len(result) - audio_duration_ms) > 1:
        logging.warning(f"Длительность аудио изменилась! Было {audio_duration_ms}ms, стало {len(result)}ms")
    else:
        logging.debug(f"Длительность аудио сохранена: {audio_duration_ms}ms")

    return result
