import os
import tempfile
from pathlib import Path
from typing import Optional


def create_temp_file(suffix: str = None) -> Optional[str]:
    """
    Создает временный файл
    
    Args:
        suffix: расширение файла
        
    Returns:
        путь к временному файлу или None в случае ошибки
    """
    try:
        temp = tempfile.NamedTemporaryFile(delete=False, suffix=suffix)
        path = temp.name
        temp.close()
        return path
    except Exception as e:
        print(f"Ошибка при создании временного файла: {str(e)}")
        return None


def safe_delete_file(file_path: str) -> bool:
    """
    Безопасно удаляет файл
    
    Args:
        file_path: путь к файлу
        
    Returns:
        успешность операции
    """
    try:
        if os.path.exists(file_path):
            os.unlink(file_path)
        return True
    except Exception as e:
        print(f"Ошибка при удалении файла {file_path}: {str(e)}")
        return False


def ensure_dir_exists(dir_path: str) -> bool:
    """
    Создает директорию, если она не существует
    
    Args:
        dir_path: путь к директории
        
    Returns:
        успешность операции
    """
    try:
        Path(dir_path).mkdir(parents=True, exist_ok=True)
        return True
    except Exception as e:
        print(f"Ошибка при создании директории {dir_path}: {str(e)}")
        return False
