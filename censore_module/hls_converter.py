import subprocess
from pathlib import Path
import m3u8
from typing import Optional

def convert_mp4_to_hls(input_file: str, output_dir: str) -> Optional[Path]:
    """
    Конвертирует MP4 файл в HLS формат
    
    Args:
        input_file: путь к входному MP4 файлу
        output_dir: директория для сохранения HLS
        
    Returns:
        путь к созданному плейлисту или None в случае ошибки
    """
    try:
        output_dir = Path(output_dir)
        output_dir.mkdir(parents=True, exist_ok=True)
        
        command = [
            'ffmpeg',
            '-y',
            '-i', input_file,
            '-c:v', 'h264',
            '-c:a', 'aac',
            '-hls_time', '4',
            '-hls_list_size', '0',
            '-hls_segment_filename', str(output_dir / 'segment_%03d.ts'),
            str(output_dir / 'playlist.m3u8')
        ]
        
        subprocess.run(command, check=True)
        return output_dir / 'playlist.m3u8'
        
    except subprocess.CalledProcessError as e:
        print(f"Ошибка при конвертации в HLS: {e.stderr.decode()}")
        return None

def convert_hls_to_mp4(playlist_path: str, output_file: str) -> bool:
    """
    Конвертирует HLS плейлист в MP4 файл
    
    Args:
        playlist_path: путь к HLS плейлисту
        output_file: путь для сохранения MP4
        
    Returns:
        успешность операции
    """
    try:
        command = [
            'ffmpeg',
            '-y',
            '-fflags', '+genpts',
            '-i', playlist_path,
            '-c:v', 'h264',
            '-c:a', 'aac',
            '-vsync', 'vfr',
            output_file
        ]
        
        subprocess.run(command, check=True)
        return True
        
    except subprocess.CalledProcessError as e:
        print(f"Ошибка при конвертации в MP4: {e.stderr.decode()}")
        return False 