<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useStreamStore } from '@/stores/stream';
import { useUserStore } from '@/stores/user';
import { setAuthToken } from '@/stores/authHelpers';

const route = useRoute();
const router = useRouter();
const streamStore = useStreamStore();
const userStore = useUserStore();

const streamId = computed(() => route.params.id);
const isLoading = ref(true);
const errorMessage = ref('');
const player = ref(null);
const latency = ref(0);
const isPlayerInitialized = ref(false);
const showObsInstructions = ref(false);
const isStreamKeyVisible = ref(false);

const isAuthenticated = computed(() => userStore.isAuthenticated);
const isStreamOwner = computed(() => {
  if (!userStore.user || !streamStore.currentStream) return false;
  return streamStore.currentStream.userId === userStore.user.id;
});

const getHlsUrl = (stream) => {
  if (!stream || !stream.streamKey) return '';
  return `http://127.0.0.1:8088/hls/${stream.streamKey}/index.m3u8`;
};

const updateVideoSource = (stream) => {
  if (!player.value || !stream || !stream.streamKey) return;

  const newSource = {
    src: getHlsUrl(stream),
    type: 'application/x-mpegURL',
  };

  console.log('Обновление источника видео:', newSource);

  player.value.src(newSource);
  player.value.load();
  player.value.play().catch(error => {
    console.warn('Автовоспроизведение невозможно:', error);
  });
};

const loadVideoJs = async () => {
  const videojs = (await import('video.js')).default;
  const style = document.createElement('link');
  style.rel = 'stylesheet';
  style.href = '/node_modules/video.js/dist/video-js.css';
  document.head.appendChild(style);
  return videojs;
};

const checkStreamAvailability = async (hlsUrl) => {
  try {
    const response = await fetch(hlsUrl, { method: 'HEAD' });
    return response.ok;
  } catch (error) {
    console.warn('Плейлист недоступен:', error);
    return false;
  }
};

const initPlayer = async () => {
  if (isPlayerInitialized.value || player.value) {
    console.log('Плеер уже инициализирован, пропускаем');
    return;
  }

  if (!streamStore.currentStream || !streamStore.currentStream.isLive) {
    return;
  }

  const videoElement = document.getElementById('stream-video');
  if (!videoElement) {
    console.error('Элемент video не найден');
    return;
  }

  const hlsUrl = getHlsUrl(streamStore.currentStream);
  console.log('Инициализация плеера с URL:', hlsUrl);

  const isAvailable = await checkStreamAvailability(hlsUrl);
  if (!isAvailable) {
    console.log('Стрим недоступен, повторная попытка через 3 секунды...');
    setTimeout(initPlayer, 3000);
    return;
  }

  const videojs = await loadVideoJs();

  const options = {
    autoplay: true,
    muted: true,
    controls: true,
    fluid: true,
    responsive: true,
    liveui: true,
    loadingSpinner: true,
    errorDisplay: false,
    sources: [{
      src: hlsUrl,
      type: 'application/x-mpegURL',
    }],
    html5: {
      hls: {
        overrideNative: false,
        withCredentials: false,
        smoothQualityChange: true,
        startLevel: -1,
        capLevelToPlayerSize: true,
        maxBufferLength: 10,
        maxMaxBufferLength: 15,
      },
    },
  };

  isPlayerInitialized.value = true;
  player.value = videojs(videoElement, options, function onPlayerReady() {
    console.log('Плеер готов к использованию', this);

    let retryCount = 0;
    const maxRetries = 5;

    this.on('error', function () {
      const error = this.error();
      console.log('Ошибка плеера:', error);

      if (error && error.code === 4) {
        console.log('Формат потока не поддерживается, завершаем попытки');
        return;
      }

      if (retryCount < maxRetries) {
        retryCount++;
        const delay = Math.min(1000 * Math.pow(2, retryCount), 10000);
        console.log(`Попытка ${retryCount} из ${maxRetries} через ${delay} мс...`);
        setTimeout(() => {
          this.load();
          this.play().catch(err => console.warn('Ошибка воспроизведения:', err));
        }, delay);
      } else {
        console.log('Превышено максимальное количество попыток');
        retryCount = 0;
      }
    });

    this.on('playing', function () {
      console.log('Воспроизведение началось');
      retryCount = 0;
      const updateLatency = () => {
        latency.value = this.liveTracker ? (this.liveTracker.seekableEnd() - this.currentTime()).toFixed(1) : 0;
      };
      updateLatency();
      setInterval(updateLatency, 1000);
    });

    this.on('waiting', function () {
      console.log('Буферизация...');
    });
  });
};

const destroyPlayer = () => {
  if (player.value) {
    player.value.dispose();
    player.value = null;
    isPlayerInitialized.value = false;
  }
};

const loadStream = async () => {
  if (!streamId.value) {
    errorMessage.value = 'ID стрима не указан';
    router.push('/');
    return;
  }

  isLoading.value = true;
  errorMessage.value = '';

  try {
    if (userStore.isAuthenticated && userStore.token) {
      setAuthToken(userStore.token);
    }

    await streamStore.fetchStreamById(streamId.value);

    if (!streamStore.currentStream) {
      errorMessage.value = streamStore.error || 'Стрим не найден';
      console.error('Стрим не найден или произошла ошибка:', streamStore.error);
    } else {
      console.log('Стрим успешно загружен:', streamStore.currentStream);
    }
  } catch (error) {
    console.error('Ошибка при загрузке стрима:', error);
    errorMessage.value = error.message || 'Не удалось загрузить стрим';
  } finally {
    isLoading.value = false;
  }
};

const endStream = async () => {
  if (!confirm('Вы уверены, что хотите завершить стрим?')) return;

  isLoading.value = true;
  errorMessage.value = '';

  try {
    await streamStore.endStream();
    alert('Стрим успешно завершен');
    await loadStream();
  } catch (error) {
    console.error('Ошибка при завершении стрима:', error);
    errorMessage.value = error.message || 'Произошла ошибка при завершении стрима';
  } finally {
    isLoading.value = false;
  }
};

const editStream = () => {
  if (streamStore.currentStream && streamStore.currentStream.id) {
    streamStore.setActiveStream(streamStore.currentStream);
    router.push('/streams/dashboard');
  }
};

const getTags = (stream) => {
  if (!stream || !stream.tags || !Array.isArray(stream.tags)) return [];
  return stream.tags;
};

const formatDate = (dateString) => {
  if (!dateString) return 'Нет данных';

  try {
    const date = new Date(dateString);
    if (isNaN(date.getTime())) {
      console.log('Invalid date from string:', dateString);
      return 'Нет данных';
    }

    return date.toLocaleString('ru-RU', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch (error) {
    console.error('Error formatting date:', error);
    return 'Нет данных';
  }
};

const copyStreamKey = () => {
  if (!streamStore.currentStream || !streamStore.currentStream.streamKey) return;

  navigator.clipboard.writeText(streamStore.currentStream.streamKey)
      .then(() => {
        alert('Ключ стрима скопирован в буфер обмена');
      })
      .catch(err => {
        console.error('Ошибка при копировании ключа стрима:', err);
      });
};

const toggleStreamKeyVisibility = () => {
  isStreamKeyVisible.value = !isStreamKeyVisible.value;
};

const getMaskedStreamKey = (key) => {
  if (!key) return '';
  if (isStreamKeyVisible.value) return key;

  const visiblePart = 4;
  if (key.length <= visiblePart * 2) {
    return '*'.repeat(key.length);
  }

  const start = key.substring(0, visiblePart);
  const end = key.substring(key.length - visiblePart);
  const masked = '*'.repeat(key.length - visiblePart * 2);

  return `${start}${masked}${end}`;
};

watch(() => streamStore.currentStream?.isLive, (isLive) => {
  if (isLive === undefined) return;
  console.log('Статус стрима изменился:', isLive);
  if (isLive) {
    setTimeout(() => initPlayer(), 1000);
  } else {
    destroyPlayer();
  }
});

onMounted(async () => {
  await loadStream();
});

onUnmounted(() => {
  destroyPlayer();
});
</script>

<template>
  <v-container>
    <v-row v-if="isLoading">
      <v-col cols="12" class="text-center">
        <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
        <div class="mt-4">Загрузка стрима...</div>
      </v-col>
    </v-row>

    <v-row v-else-if="errorMessage">
      <v-col cols="12" md="8" offset-md="2">
        <v-alert type="error" variant="tonal" class="mb-4">
          {{ errorMessage }}
        </v-alert>
        <div class="text-center">
          <v-btn color="primary" to="/" class="mt-4">
            Вернуться на главную
          </v-btn>
        </div>
      </v-col>
    </v-row>

    <template v-else-if="!isLoading && streamStore.currentStream">
      <v-row>
        <v-col cols="12" md="10" offset-md="1">
          <div class="stream-player mb-4">
            <div v-if="streamStore.currentStream.isLive" class="video-container">
              <video
                  id="stream-video"
                  class="video-js vjs-default-skin vjs-big-play-centered"
                  controls
                  preload="auto"
                  :poster="streamStore.currentStream.thumbnailUrl || ''"
                  data-setup="{}"
              >
                <p class="vjs-no-js">
                  Для просмотра видео необходимо включить JavaScript и обновить браузер до поддерживающего HTML5 видео.
                </p>
              </video>
              <div class="latency-indicator">
                Задержка: {{ latency }} секунд
              </div>
            </div>
            <div v-else class="video-container">
              <div class="video-placeholder d-flex align-center justify-center bg-grey-darken-3">
                <div class="text-center">
                  <v-icon size="64" color="white">mdi-television-off</v-icon>
                  <div class="text-h6 mt-2">Трансляция завершена</div>
                </div>
              </div>
            </div>
          </div>

          <v-card-title>
            <div class="d-flex align-center">
              {{ streamStore.currentStream.title }}
              <v-chip
                  v-if="streamStore.currentStream.isLive"
                  color="error"
                  size="small"
                  class="ml-2"
              >
                <v-icon start size="x-small">mdi-access-point</v-icon>
                В ЭФИРЕ
              </v-chip>
            </div>
          </v-card-title>

          <v-card-text>
            <div class="d-flex align-center mb-4">
              <v-avatar size="40" class="mr-2">
                <v-img :src="`https://i.pravatar.cc/150?u=${streamStore.currentStream.userId}`"></v-img>
              </v-avatar>
              <div>
                <div class="text-subtitle-1 font-weight-bold">{{ streamStore.currentStream.userName || 'Пользователь' }}</div>
                <div class="text-caption">{{ formatDate(streamStore.currentStream.startedAt) }}</div>
              </div>

              <v-spacer></v-spacer>

              <div class="d-flex align-center">
                <v-icon color="red" class="mr-1">mdi-eye</v-icon>
                <span class="text-body-2">{{ streamStore.currentStream.viewerCount || 0 }} зрителей</span>
              </div>
            </div>

            <p v-if="streamStore.currentStream.description" class="text-body-1 mb-4">
              {{ streamStore.currentStream.description }}
            </p>
            <p v-else class="text-body-1 text-grey mb-4">
              Описание отсутствует
            </p>

            <div class="d-flex flex-wrap gap-1 mb-4">
              <v-chip
                  v-for="tag in getTags(streamStore.currentStream)"
                  :key="tag"
                  size="small"
                  class="mr-1"
              >
                {{ tag }}
              </v-chip>
            </div>

            <div v-if="isStreamOwner" class="d-flex gap-2">
              <v-btn class="mr-2" color="primary" @click="editStream">
                <v-icon start>mdi-pencil</v-icon>
                Редактировать
              </v-btn>
              <v-btn class="mr-2" v-if="streamStore.currentStream.isLive" color="error" @click="endStream" :loading="isLoading">
                <v-icon start>mdi-stop</v-icon>
                Завершить стрим
              </v-btn>
            </div>

            <div v-if="isStreamOwner && streamStore.currentStream.streamKey" class="mt-4 pt-4 border-top">
              <div class="d-flex align-center mb-2">
                <h3 class="text-subtitle-1 font-weight-bold mr-2">Ключ стрима (RTMP)</h3>
                <v-tooltip text="Этот ключ нужен для настройки OBS или другого ПО для стриминга">
                  <template v-slot:activator="{ props }">
                    <v-icon v-bind="props" size="small">mdi-help-circle-outline</v-icon>
                  </template>
                </v-tooltip>
              </div>

              <div class="d-flex align-center">
                <v-text-field
                    :value="getMaskedStreamKey(streamStore.currentStream.streamKey)"
                    readonly
                    variant="outlined"
                    density="compact"
                    hide-details
                    class="flex-grow-1 mr-2"
                    :append-icon="isStreamKeyVisible ? 'mdi-eye-off' : 'mdi-eye'"
                    @click:append="toggleStreamKeyVisibility"
                >
                  <template v-slot:prepend>
                    <v-tooltip :text="isStreamKeyVisible ? 'Скрыть ключ' : 'Показать ключ'">
                      <template v-slot:activator="{ props }">
                        <v-icon
                            v-bind="props"
                            :color="isStreamKeyVisible ? 'warning' : 'primary'"
                            @click="toggleStreamKeyVisibility"
                        >
                          {{ isStreamKeyVisible ? 'mdi-lock-open' : 'mdi-lock' }}
                        </v-icon>
                      </template>
                    </v-tooltip>
                  </template>
                </v-text-field>
                <v-btn
                    icon="mdi-content-copy"
                    variant="text"
                    density="compact"
                    @click="copyStreamKey"
                ></v-btn>
              </div>

              <div class="mt-2 text-caption">
                RTMP URL: rtmp://127.0.0.1:1935/live
              </div>

              <v-btn
                  color="primary"
                  variant="text"
                  class="mt-2 px-0"
                  @click="showObsInstructions = true"
              >
                <v-icon start>mdi-information-outline</v-icon>
                Инструкция по настройке OBS
              </v-btn>

              <v-dialog v-model="showObsInstructions" max-width="600px">
                <v-card class="p-4">
                  <v-card-title class="text-h5">
                    Настройка OBS для стриминга
                  </v-card-title>
                  <v-card-text>
                    <p class="mb-4">Для начала стриминга через OBS Studio выполните следующие шаги:</p>

                    <ol class="mb-4">
                      <li class="mb-2">Откройте OBS Studio</li>
                      <li class="mb-2">Перейдите в меню <strong>Настройки</strong> → <strong>Вещание</strong></li>
                      <li class="mb-2">Выберите <strong>Сервис</strong>: Custom</li>
                      <li class="mb-2">В поле <strong>Сервер</strong> введите: <code>rtmp://127.0.0.1:1935/live</code></li>
                      <li class="mb-2">В поле <strong>Ключ потока</strong> введите ключ потока</li>
                      <li class="mb-2">Нажмите <strong>OK</strong> для сохранения настроек</li>
                      <li class="mb-2">Нажмите кнопку <strong>Начать трансляцию</strong> в главном окне OBS</li>
                    </ol>
                  </v-card-text>
                  <v-card-actions>
                    <v-spacer></v-spacer>
                    <v-btn color="primary" @click="showObsInstructions = false">
                      Закрыть
                    </v-btn>
                  </v-card-actions>
                </v-card>
              </v-dialog>
            </div>
          </v-card-text>

          <v-card>
            <v-card-title>Рекомендуемые стримы</v-card-title>
            <v-card-text>
              <div class="text-center my-4 text-grey">
                <v-icon>mdi-television</v-icon>
                <p>Рекомендации будут доступны в следующей версии</p>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </template>
  </v-container>
</template>

<style scoped>
.video-container {
  width: 100%;
  max-width: 960px;
  margin: 0 auto;
  aspect-ratio: 16 / 9;
  background-color: #000;
  overflow: hidden;
  border-radius: 8px;
  position: relative;
}

.video-js {
  width: 100% !important;
  height: 100% !important;
}

.video-placeholder {
  width: 100%;
  height: 100%;
  background-color: #1e1e1e;
  color: white;
}

.latency-indicator {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 0.875rem;
}
</style>