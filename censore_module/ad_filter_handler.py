import time
from pathlib import Path
from threading import Timer, Lock
from typing import Optional, Dict

import m3u8
import shutil
import logging
from watchdog.events import FileSystemEventHandler


class AdFilterHandler(FileSystemEventHandler):
    def __init__(self, stream_key, speech_model, ad_model):
        self.stream_key = stream_key
        self.input_path = Path(f"./hls_data/input/{stream_key}")
        self.output_path = Path(f"./hls_data/output/{stream_key}")
        self.processor = None

        self.processed_segments = set()
        self.processing_lock = Lock()
        self.segment_timers = {}

        self.playlist_lock = Lock()
        self.max_segments_in_playlist = 6
        self.segment_timeout = 10.0

        self.output_path.mkdir(parents=True, exist_ok=True)
        self.playlist_path = self.output_path / "index.m3u8"
        self._initialize_playlist()

        logging.info(f"Инициализирован обработчик для stream_key: {stream_key}")

    def _initialize_playlist(self):
        try:
            with self.playlist_lock:
                if not self.playlist_path.exists():
                    playlist_content = (
                        "#EXTM3U\n"
                        "#EXT-X-VERSION:3\n"
                        "#EXT-X-TARGETDURATION:3\n"
                        "#EXT-X-MEDIA-SEQUENCE:0\n"
                    )
                    with open(self.playlist_path, "w", encoding='utf-8') as f:
                        f.write(playlist_content)
                    logging.info(f"Создан новый плейлист: {self.playlist_path}")
                else:
                    logging.info(f"Используется существующий плейлист: {self.playlist_path}")
        except Exception as e:
            logging.error(f"Ошибка инициализации плейлиста: {e}")

    def _get_segment_info_from_input_playlist(self, segment_name: str) -> Optional[Dict]:
        try:
            input_playlist_path = self.input_path / "index.m3u8"
            if not input_playlist_path.exists():
                return None

            playlist = m3u8.load(str(input_playlist_path))
            for segment in playlist.segments:
                if segment.uri == segment_name:
                    duration = segment.duration
                    if not (2.7 <= duration <= 3.3):
                        logging.warning(f"Длительность сегмента {segment_name} не около 3s: {duration:.2f}s")
                    return {
                        'duration': duration,
                        'uri': segment.uri
                    }
            return None
        except Exception as e:
            logging.warning(f"Ошибка чтения входного плейлиста: {e}")
            return None

    def _update_output_playlist(self, segment_name: str, duration: float):
        try:
            with self.playlist_lock:
                if self.playlist_path.exists():
                    with open(self.playlist_path, 'r', encoding='utf-8') as f:
                        lines = f.readlines()
                else:
                    lines = [
                        "#EXTM3U\n",
                        "#EXT-X-VERSION:3\n",
                        "#EXT-X-TARGETDURATION:3\n",
                        "#EXT-X-MEDIA-SEQUENCE:0\n"
                    ]

                segments = []
                media_sequence = 0
                target_duration = 3

                i = 0
                while i < len(lines):
                    line = lines[i].strip()
                    if line.startswith("#EXT-X-MEDIA-SEQUENCE:"):
                        media_sequence = int(line.split(":")[1])
                    elif line.startswith("#EXT-X-TARGETDURATION:"):
                        target_duration = int(line.split(":")[1])
                    elif line.startswith("#EXTINF:"):
                        if i + 1 < len(lines):
                            seg_duration = float(line.split(":")[1].split(",")[0])
                            seg_uri = lines[i + 1].strip()
                            segments.append((seg_duration, seg_uri))
                            i += 1
                    i += 1

                segments.append((duration, segment_name))

                if len(segments) > self.max_segments_in_playlist:
                    segments = segments[-self.max_segments_in_playlist:]
                    if segments:
                        try:
                            first_segment_num = int(segments[0][1].split('.')[0])
                            media_sequence = first_segment_num
                        except:
                            media_sequence += 1

                max_duration = max((seg[0] for seg in segments), default=duration)
                target_duration = max(int(max_duration) + 1, target_duration)

                new_lines = [
                    "#EXTM3U\n",
                    "#EXT-X-VERSION:3\n",
                    f"#EXT-X-TARGETDURATION:{target_duration}\n",
                    f"#EXT-X-MEDIA-SEQUENCE:{media_sequence}\n"
                ]

                for seg_duration, seg_uri in segments:
                    new_lines.append(f"#EXTINF:{seg_duration:.3f},\n")
                    new_lines.append(f"{seg_uri}\n")

                with open(self.playlist_path, "w", encoding='utf-8') as f:
                    f.writelines(new_lines)

                logging.debug(f"Плейлист обновлён: добавлен {segment_name} (длительность: {duration:.3f}s)")

        except Exception as e:
            logging.error(f"Ошибка обновления плейлиста: {e}")

    def _process_segment_delayed(self, input_ts_path: str, segment_index: int):
        try:
            input_ts = Path(input_ts_path)
            if not input_ts.exists():
                logging.warning(f"Сегмент {input_ts} исчез до обработки")
                return
            prev_size = -1
            stable_count = 0
            min_file_size = 10240
            max_wait_time = 7.0
            start_time = time.time()

            while time.time() - start_time < max_wait_time:
                current_size = input_ts.stat().st_size
                logging.debug(f"Проверка сегмента {input_ts.name}: размер {current_size} байт, "
                              f"время ожидания {time.time() - start_time:.1f}s")
                if current_size == prev_size and current_size >= min_file_size:
                    stable_count += 1
                    if stable_count >= 2:
                        logging.debug(f"Сегмент {input_ts.name} стабилен (размер: {current_size} байт) "
                                      f"за {time.time() - start_time:.1f}s")
                        break
                else:
                    stable_count = 0
                prev_size = current_size
                time.sleep(0.3)

            if stable_count < 2:
                logging.warning(f"Сегмент {input_ts.name} не стабилизировался за {time.time() - start_time:.1f}s, "
                                f"размер {current_size} байт, пропускаем")
                return

            output_ts = self.output_path / input_ts.name

            logging.info(f"Начинаем обработку сегмента {segment_index}: {input_ts.name}")

            with self.processing_lock:
                if segment_index in self.processed_segments:
                    logging.debug(f"Сегмент {segment_index} уже обработан, пропускаем")
                    return

                success = self.processor.process_ts_segment(
                    str(input_ts),
                    str(output_ts),
                    segment_index
                )

                if success:
                    self.processed_segments.add(segment_index)

                    segment_info = self._get_segment_info_from_input_playlist(input_ts.name)
                    duration = segment_info['duration'] if segment_info else 3.0

                    self._update_output_playlist(input_ts.name, duration)

                    logging.info(f"Сегмент {segment_index} успешно обработан")
                else:
                    logging.warning(f"Ошибка обработки сегмента {segment_index}, копируем оригинал")
                    try:
                        shutil.copy2(input_ts, output_ts)
                        segment_info = self._get_segment_info_from_input_playlist(input_ts.name)
                        duration = segment_info['duration'] if segment_info else 3.0
                        self._update_output_playlist(input_ts.name, duration)
                        self.processed_segments.add(segment_index)
                    except Exception as copy_error:
                        logging.error(f"Не удалось скопировать сегмент {segment_index}: {copy_error}")

        except Exception as e:
            logging.error(f"Критическая ошибка при обработке сегмента {segment_index}: {e}")
        finally:
            if segment_index in self.segment_timers:
                del self.segment_timers[segment_index]

    def on_created(self, event):
        if event.is_directory:
            return

        file_path = Path(event.src_path)

        if file_path.suffix.lower() == '.ts':
            try:
                segment_index = int(file_path.stem)
            except ValueError:
                segment_index = len(self.processed_segments)

            logging.debug(f"Обнаружен новый сегмент: {file_path.name} (индекс: {segment_index})")

            if segment_index in self.segment_timers:
                self.segment_timers[segment_index].cancel()

            timer = Timer(2.5, self._process_segment_delayed, [str(file_path), segment_index])  # Увеличено до 2.5s
            self.segment_timers[segment_index] = timer
            timer.start()

        elif file_path.suffix.lower() == '.m3u8':
            logging.debug(f"Обновлён входной плейлист: {file_path.name}")

    def on_modified(self, event):
        if not event.is_directory and event.src_path.endswith('.m3u8'):
            logging.debug(f"Изменён плейлист: {event.src_path}")

    def on_stop(self):
        logging.info(f"Завершение работы обработчика для stream_key: {self.stream_key}")

        for timer in self.segment_timers.values():
            if timer.is_alive():
                timer.cancel()
        self.segment_timers.clear()

        if hasattr(self.processor, 'finalize_processing'):
            try:
                last_segment_index = max(self.processed_segments) if self.processed_segments else 0
                output_file = str(self.output_path / f"{last_segment_index + 1}.ts")
                self.processor.finalize_processing(output_file)
            except Exception as e:
                logging.error(f"Ошибка финализации: {e}")

        logging.info(f"Обработчик для {stream_key} успешно остановлен")
