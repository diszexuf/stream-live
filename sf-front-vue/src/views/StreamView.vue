<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStreamStore } from '@/stores/stream'
import { useUserStore } from '@/stores/user'
// Импортируем VideoJS
import videojs from 'video.js'
import 'video.js/dist/video-js.css'

const route = useRoute()
const router = useRouter()
const streamStore = useStreamStore()
const userStore = useUserStore()

const streamId = computed(() => route.params.id)
const isLoading = ref(true)
const errorMessage = ref('')
const player = ref(null)

// Вычисляемые свойства
const isAuthenticated = computed(() => userStore.isAuthenticated)
const isStreamOwner = computed(() => {
  if (!userStore.user || !streamStore.currentStream) return false
  return streamStore.currentStream.userId === userStore.user.id
})

// Состояние для диалогового окна с инструкциями
const showObsInstructions = ref(false)

// Состояние для отображения streamKey
const isStreamKeyVisible = ref(false)

// Получение URL HLS потока
const getHlsUrl = (stream) => {
  if (!stream || !stream.streamKey) return '';
  
  // URL HLS потока на основе streamKey
  // Используем IP вместо localhost для лучшей совместимости с Docker
  // Убираем расширение .m3u8, так как NGINX-RTMP может автоматически добавлять его
  const url = `http://127.0.0.1:8088/hls/${stream.streamKey}/index.m3u8`;
  console.log('HLS URL:', url);
  return url;
}

// Обновление источника видео
const updateVideoSource = (stream) => {
  if (!player.value || !stream || !stream.streamKey) return
  
  const newSource = {
    src: getHlsUrl(stream),
    type: 'application/x-mpegURL'
  }
  
  console.log('Обновление источника видео:', newSource);
  
  player.value.src(newSource)
  player.value.load()
  player.value.play().catch(error => {
    console.warn('Автовоспроизведение невозможно:', error)
  })
}

// Инициализация VideoJS плеера
const initPlayer = () => {
  if (!streamStore.currentStream || !streamStore.currentStream.isLive) {
    console.log('Не инициализируем плеер: стрим не активен или не загружен', streamStore.currentStream);
    return;
  }
  
  // Находим элемент video
  const videoElement = document.getElementById('stream-video')
  if (!videoElement) {
    console.error('Элемент video не найден');
    return;
  }
  
  // URL для HLS потока
  const hlsUrl = getHlsUrl(streamStore.currentStream);
  console.log('Инициализация плеера с URL:', hlsUrl);
  
  // Опции для VideoJS
  const options = {
    autoplay: true,
    controls: true,
    fluid: true,
    responsive: true,
    liveui: true,
    loadingSpinner: true,
    errorDisplay: true,
    sources: [{
      src: hlsUrl,
      type: 'application/x-mpegURL'
    }]
  }
  
  console.log('Опции плеера:', options);
  
  // Создаем экземпляр плеера
  player.value = videojs(videoElement, options, function onPlayerReady() {
    console.log('Плеер готов к использованию', this)
    
    // Обработка ошибок
    this.on('error', function() {
      const error = this.error();
      console.error('Ошибка воспроизведения видео:', error);
      
      // Пытаемся перезагрузить плеер при ошибке
      if (error && error.code) {
        console.log('Попытка перезагрузить плеер через 3 секунды...');
        setTimeout(() => {
          this.load();
          this.play();
        }, 3000);
      }
    })
    
    // Обработка начала воспроизведения
    this.on('playing', function() {
      console.log('Воспроизведение началось')
    })
    
    // Обработка буферизации
    this.on('waiting', function() {
      console.log('Буферизация...')
    })
  })
}

// Уничтожение плеера при размонтировании компонента
const destroyPlayer = () => {
  if (player.value) {
    player.value.dispose()
    player.value = null
  }
}

// Загрузка данных стрима
const loadStream = async () => {
  if (!streamId.value) {
    errorMessage.value = 'ID стрима не указан'
    router.push('/')
    return
  }

  isLoading.value = true
  errorMessage.value = ''

  try {
    console.log(`Загрузка стрима с ID: ${streamId.value}`)
    
    // Проверяем, авторизован ли пользователь
    if (userStore.isAuthenticated) {
      console.log('Пользователь авторизован, токен:', userStore.token ? 'установлен' : 'не установлен')
      
      // Если токен есть, но не установлен в клиенте, устанавливаем его
      if (userStore.token) {
        // Импортируем функцию setAuthToken из API
        const { setAuthToken } = await import('@/api/manual')
        setAuthToken(userStore.token)
      }
    } else {
      console.log('Пользователь не авторизован')
    }
    
    // Загружаем стрим
    await streamStore.fetchStreamById(streamId.value)
    
    if (!streamStore.currentStream) {
      errorMessage.value = streamStore.error || 'Стрим не найден'
      console.error('Стрим не найден или произошла ошибка:', streamStore.error)
    } else {
      console.log('Стрим успешно загружен:', streamStore.currentStream)
      console.log('Stream key:', streamStore.currentStream.streamKey)
      
      // Инициализируем плеер после загрузки данных стрима
      setTimeout(() => {
        initPlayer()
      }, 100)
    }
  } catch (error) {
    console.error('Ошибка при загрузке стрима:', error)
    errorMessage.value = error.message || 'Не удалось загрузить стрим'
  } finally {
    isLoading.value = false
  }
}

// Завершение стрима
const endStream = async () => {
  if (!confirm('Вы уверены, что хотите завершить стрим?')) return

  console.log('StreamView.endStream: Завершение стрима');
  console.log('StreamView.endStream: Текущий стрим:', streamStore.currentStream);

  isLoading.value = true
  errorMessage.value = ''

  try {
    // Убедимся, что токен авторизации установлен
    if (userStore.token) {
      const { setAuthToken } = await import('@/api/manual')
      setAuthToken(userStore.token)
    }
    
    // Завершаем стрим
    const result = await streamStore.endStream()
    console.log('StreamView.endStream: Результат завершения стрима:', result);

    if (result) {
      alert('Стрим успешно завершен')
      await loadStream() // Обновляем данные стрима
    } else {
      errorMessage.value = streamStore.error || 'Не удалось завершить стрим'
      console.error('StreamView.endStream: Ошибка при завершении стрима:', streamStore.error);
    }
  } catch (error) {
    console.error('Ошибка при завершении стрима:', error)
    errorMessage.value = 'Произошла ошибка при завершении стрима'
  } finally {
    isLoading.value = false
  }
}

// Редактирование стрима
const editStream = () => {
  if (streamStore.currentStream && streamStore.currentStream.id) {
    // Сначала устанавливаем текущий стрим как активный в хранилище
    streamStore.setActiveStream(streamStore.currentStream);
    // Затем переходим на страницу управления стримами
    router.push('/streams/dashboard');
  }
}

// Получение тегов
const getTags = (stream) => {
  if (!stream) return [];
  
  if (stream.tags && Array.isArray(stream.tags)) {
    return stream.tags;
  }
  
  return [];
}

// Форматирование даты
const formatDate = (dateString) => {
  if (!dateString) return 'Нет данных';
  
  // Проверяем, не является ли dateString объектом
  if (typeof dateString === 'object' && dateString !== null) {
    console.log('Date is an object:', dateString);
    return 'Нет данных';
  }
  
  try {
    const date = new Date(dateString);
    // Проверяем валидность даты
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
}

// Копирование ключа стрима
const copyStreamKey = () => {
  if (!streamStore.currentStream || !streamStore.currentStream.streamKey) return
  
  navigator.clipboard.writeText(streamStore.currentStream.streamKey)
    .then(() => {
      alert('Ключ стрима скопирован в буфер обмена')
    })
    .catch(err => {
      console.error('Ошибка при копировании ключа стрима:', err)
    })
}

// Переключение видимости ключа стрима
const toggleStreamKeyVisibility = () => {
  isStreamKeyVisible.value = !isStreamKeyVisible.value
}

// Получение маскированного значения ключа стрима
const getMaskedStreamKey = (key) => {
  if (!key) return ''
  if (isStreamKeyVisible.value) return key
  
  // Показываем только первые 4 и последние 4 символа
  const visiblePart = 4
  if (key.length <= visiblePart * 2) {
    return '*'.repeat(key.length)
  }
  
  const start = key.substring(0, visiblePart)
  const end = key.substring(key.length - visiblePart)
  const masked = '*'.repeat(key.length - visiblePart * 2)
  
  return `${start}${masked}${end}`
}

// Наблюдение за изменением ID стрима
watch(streamId, (newStreamId, oldStreamId) => {
  if (newStreamId !== oldStreamId) {
    console.log(`ID стрима изменился с ${oldStreamId} на ${newStreamId}`)
    destroyPlayer()
    loadStream()
  }
}, { immediate: true })

// Инициализация
onMounted(async () => {
  await loadStream()
})

// Очистка при размонтировании компонента
onUnmounted(() => {
  destroyPlayer()
})
</script>

<template>
  <v-container>
    <!-- Отображение загрузки -->
    <v-row v-if="isLoading">
      <v-col cols="12" class="text-center">
        <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
        <div class="mt-4">Загрузка стрима...</div>
      </v-col>
    </v-row>

    <!-- Отображение ошибки -->
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

    <!-- Отображение стрима -->
    <template v-else-if="!isLoading && streamStore.currentStream">
      <v-row>
        <v-col cols="12" md="10" offset-md="1">
          <!-- Видео плеер -->
          <div class="stream-player mb-4">
            <div v-if="streamStore.currentStream.isLive" class="video-container">
              <!-- VideoJS плеер -->
              <video
                id="stream-video"
                class="video-js vjs-default-skin vjs-big-play-centered"
                controls
                preload="auto"
                width="100%"
                height="100%"
                :poster="streamStore.currentStream.thumbnailUrl || ''"
                data-setup="{}"
              >
                <p class="vjs-no-js">
                  Для просмотра видео необходимо включить JavaScript и обновить браузер до поддерживающего HTML5 видео.
                </p>
              </video>
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

          <!-- Информация о стриме -->
          <v-card variant="outlined" class="mb-4">
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
                  <v-img src="https://i.pravatar.cc/150?img=1"></v-img>
                </v-avatar>
                <div>
                  <div class="text-subtitle-1 font-weight-bold">Пользователь</div>
                  <div class="text-caption">{{ formatDate(streamStore.currentStream.startedAt) }}</div>
                </div>
                
                <v-spacer></v-spacer>
                
                <div class="d-flex align-center">
                  <v-icon color="red" class="mr-1">mdi-eye</v-icon>
                  <span class="text-body-2">{{ streamStore.currentStream.viewerCount }} зрителей</span>
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
              
              <!-- Информация о ключе стрима (только для владельца) -->
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
                
                <!-- Диалоговое окно с инструкциями -->
                <v-dialog v-model="showObsInstructions" max-width="600px">
                  <v-card>
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
                        <li class="mb-2">В поле <strong>Ключ потока</strong> введите: <code>{{ streamStore.currentStream.streamKey }}</code></li>
                        <li class="mb-2">Нажмите <strong>OK</strong> для сохранения настроек</li>
                        <li class="mb-2">Нажмите кнопку <strong>Начать трансляцию</strong> в главном окне OBS</li>
                      </ol>
                      
                      <p>Через несколько секунд после начала трансляции в OBS, ваш стрим станет доступен для просмотра.</p>
                      
                      <div class="mt-4 pa-4 bg-grey-lighten-4 rounded">
                        <p class="text-subtitle-1 font-weight-bold">Важно!</p>
                        <p>Убедитесь, что:</p>
                        <ul>
                          <li>Docker-контейнер с RTMP-сервером запущен</li>
                          <li>Порт 1935 доступен и не заблокирован брандмауэром</li>
                          <li>OBS настроен на правильное разрешение и битрейт (рекомендуется 720p и 2500-3500 Kbps)</li>
                        </ul>
                      </div>
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
          </v-card>
          
          <!-- Рекомендуемые стримы -->
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
  position: relative;
  width: 100%;
  height: 0;
  padding-bottom: 56.25%; /* Стандартное соотношение сторон 16:9 */
  background-color: #000;
  overflow: hidden;
  border-radius: 8px;
}

/* Стили для VideoJS */
.video-container .video-js {
  position: absolute !important;
  top: 0;
  left: 0;
  width: 100% !important;
  height: 100% !important;
}

/* Убедимся, что все элементы внутри плеера также занимают всю доступную высоту */
.video-container .vjs-tech {
  width: 100%;
  height: 100%;
}

.video-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: #1e1e1e;
  color: white;
}
</style> 