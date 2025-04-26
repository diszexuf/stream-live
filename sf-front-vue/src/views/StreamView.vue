<template>
  <v-container fluid class="pa-0">
    <v-row no-gutters>
      <v-col v-if="stream" cols="12">
        <v-card>
          <v-card-title class="d-flex align-center">
            <div>
              <span class="text-h5">{{ stream.title }}</span>
              <v-chip
                v-if="stream.live"
                color="error"
                size="small"
                class="ml-2"
              >
                LIVE
              </v-chip>
              <v-chip
                v-else
                color="grey"
                size="small"
                class="ml-2"
              >
                OFFLINE
              </v-chip>
            </div>
            <v-spacer></v-spacer>
            <v-btn
              v-if="isStreamer && stream.live"
              color="error"
              @click="endStream"
              :loading="isLoading"
              class="ml-2"
            >
              Завершить стрим
            </v-btn>
            <v-btn 
              v-if="isStreamer && !stream.live"
              color="success"
              to="/create-stream"
              class="ml-2"
            >
              Создать новый стрим
            </v-btn>
          </v-card-title>
          
          <v-card-item class="pa-0">
            <div v-if="stream.live && (stream.streamKey || manualStreamKey)" class="video-container">
              <video ref="videoPlayer" class="video-js vjs-fluid vjs-big-play-centered" controls autoplay></video>
            </div>
            <v-img
              v-else
              :src="stream.thumbnail"
              aspect-ratio="16/9"
              cover
            >
              <template v-slot:placeholder>
                <v-row class="fill-height ma-0" align="center" justify="center">
                  <v-progress-circular indeterminate color="primary"></v-progress-circular>
                </v-row>
              </template>
              <div class="stream-offline-overlay d-flex flex-column align-center justify-center">
                <div class="text-h5 text-white">Стрим не активен</div>
                <div class="text-body-1 text-white">Трансляция была завершена или еще не началась</div>
                <div v-if="stream.streamKey" class="mt-4 text-body-2 text-white">Ключ стрима: {{ stream.streamKey }}</div>
              </div>
            </v-img>
          </v-card-item>
          
          <v-card-subtitle>
            <v-row align="center" no-gutters>
              <v-col cols="auto">
                <v-avatar size="32" class="mr-2">
                  <v-img :src="stream.user.avatar" cover></v-img>
                </v-avatar>
                {{ stream.user.username }}
              </v-col>
              <v-col cols="auto" class="ml-4">
                <v-icon start>mdi-eye</v-icon>
                {{ stream.viewersCount }}
              </v-col>
              <v-col cols="auto" class="ml-4">
                <v-icon start>mdi-tag</v-icon>
                {{ stream.category ? stream.category.name : 'Без категории' }}
              </v-col>
            </v-row>
          </v-card-subtitle>
          
          <v-card-text v-if="stream.description">
            <v-divider class="mb-4"></v-divider>
            <p>{{ stream.description }}</p>
          </v-card-text>
          
          <v-card-text v-if="stream.startedAt">
            <v-icon start>mdi-clock-outline</v-icon>
            Начало: {{ new Date(stream.startedAt).toLocaleString() }}
          </v-card-text>
          
          <v-card-text v-if="isStreamer && stream.streamKey">
            <v-alert type="info" title="Информация для стримера">
              <p><strong>URL RTMP:</strong> rtmp://localhost:1935/live</p>
              <p><strong>Ключ стрима:</strong> {{ stream.streamKey }}</p>
              <p class="mt-2">Настройте OBS Studio или другую программу для стриминга с этими параметрами.</p>
            </v-alert>
          </v-card-text>
          
          <v-card-text v-if="stream.live">
            <v-expansion-panels>
              <v-expansion-panel>
                <v-expansion-panel-title>
                  Ручная настройка стрима
                </v-expansion-panel-title>
                <v-expansion-panel-text>
                  <v-form @submit.prevent="updateStreamKey">
                    <v-text-field
                      v-model="manualStreamKey"
                      label="Введите ключ стрима вручную"
                      hint="Используйте, если автоматическое определение не работает"
                      persistent-hint
                    ></v-text-field>
                    
                    <div class="d-flex align-center mt-3">
                      <v-btn 
                        color="primary" 
                        @click="updateStreamKey"
                        :disabled="!manualStreamKey"
                        class="mr-2"
                      >
                        Применить ключ
                      </v-btn>
                      <v-btn 
                        color="success" 
                        @click="tryTestStream"
                        class="ml-2"
                      >
                        Попробовать тестовый стрим
                      </v-btn>
                    </div>
                    
                    <div class="mt-4">
                      <p class="text-body-2">Видимые директории на RTMP сервере:</p>
                      <pre class="bg-grey-lighten-4 pa-2">{{ availableStreamKeys }}</pre>
                    </div>
                  </v-form>
                </v-expansion-panel-text>
              </v-expansion-panel>
            </v-expansion-panels>
          </v-card-text>
        </v-card>
      </v-col>
      
      <v-col v-else cols="12">
        <v-alert type="error" class="mt-4">
          Стрим не найден или недоступен
        </v-alert>
      </v-col>
      
      <!-- Закомментированная секция чата
      <v-col cols="12" md="4">
        <v-card height="calc(100vh - 120px)" class="d-flex flex-column">
          <v-card-title class="d-flex align-center">
            <span>Чат</span>
            <v-spacer></v-spacer>
            <v-btn icon="mdi-dots-vertical" variant="text"></v-btn>
          </v-card-title>
          
          <v-card-text class="pa-0 overflow-y-auto flex-grow-1">
            <v-list class="h-100">
              <v-list-item
                v-for="message in chatMessages"
                :key="message.id"
                :title="message.username"
                :subtitle="message.text"
                class="border-b"
              >
                <template v-slot:prepend>
                  <v-avatar size="32" class="mr-2">
                    <v-img :src="message.avatar" cover></v-img>
                  </v-avatar>
                </template>
              </v-list-item>
            </v-list>
          </v-card-text>
          
          <v-card-actions class="pa-4 border-t">
            <v-text-field
              v-model="newMessage"
              placeholder="Напишите сообщение..."
              variant="outlined"
              density="compact"
              hide-details
              class="mr-2"
            ></v-text-field>
            <v-btn color="primary" icon="mdi-send"></v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
      -->
    </v-row>
  </v-container>
</template>

<script>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { useUserStore } from '@/stores/user'
import videojs from 'video.js'
import 'video.js/dist/video-js.css'
import '@videojs/http-streaming'

export default {
  name: 'StreamView',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const userStore = useUserStore()
    const stream = ref(null)
    const updateInterval = ref(null)
    const isLoading = ref(false)
    const videoPlayer = ref(null)
    const player = ref(null)
    const apiUrl = 'http://localhost:8080/api'
    const manualStreamKey = ref('')
    const availableStreamKeys = ref('')
    
    const isStreamer = computed(() => {
      if (!userStore.user || !stream.value || !stream.value.user) {
        return false
      }
      return userStore.user.id === stream.value.user.id
    })
    
    /* Закомментированная логика чата
    const chatMessages = ref([
      {
        id: 1,
        username: 'User1',
        text: 'Привет всем!',
        avatar: 'https://i.imgur.com/Q9WPlWA.jpg'
      },
      {
        id: 2,
        username: 'User2',
        text: 'Какой классный стрим!',
        avatar: 'https://i.imgur.com/OxWWm2g.jpg'
      }
    ])
    
    const newMessage = ref('')
    */
    
    const fetchStream = async (id) => {
      try {
        const response = await axios.get(`${apiUrl}/streams/${id}`)
        stream.value = response.data
        
        // Если стрим активен, есть ключ и инициализирован плеер
        if (stream.value.live && stream.value.streamKey && player.value) {
          const hlsUrl = `http://localhost:8088/hls/${stream.value.streamKey}.m3u8`
          player.value.src({ src: hlsUrl, type: 'application/x-mpegURL' })
          player.value.play().catch(error => {
            console.error('Ошибка автовоспроизведения:', error)
          })
        }
      } catch (error) {
        console.error('Ошибка при получении стрима:', error)
        stream.value = null
      }
    }
    
    // Инкрементируем счетчик зрителей, когда пользователь заходит на страницу
    const incrementViewerCount = async (id) => {
      try {
        await axios.post(`${apiUrl}/streams/${id}/viewers?delta=1`)
      } catch (error) {
        console.error('Ошибка при обновлении счетчика зрителей:', error)
      }
    }
    
    // Декрементируем счетчик зрителей, когда пользователь покидает страницу
    const decrementViewerCount = async (id) => {
      try {
        await axios.post(`${apiUrl}/streams/${id}/viewers?delta=-1`)
      } catch (error) {
        console.error('Ошибка при обновлении счетчика зрителей:', error)
      }
    }
    
    // Обновить используемый ключ стрима
    const updateStreamKey = () => {
      if (!manualStreamKey.value) return;
      
      console.log(`Установлен ручной ключ стрима: ${manualStreamKey.value}`);
      
      // Если плеер уже инициализирован, обновим источник
      if (player.value) {
        const hlsUrl = `http://localhost:8088/hls/${manualStreamKey.value}/index.m3u8`;
        player.value.src({ src: hlsUrl, type: 'application/x-mpegURL' });
        player.value.play().catch(error => {
          console.error('Ошибка автовоспроизведения:', error);
        });
      } else {
        // Иначе инициализируем заново
        initVideoPlayer();
      }
    };
    
    // Попробовать использовать тестовый ключ стрима
    const tryTestStream = async () => {
      manualStreamKey.value = 'live_12345678_AbCdEfGhIjKlMnOpQrStUvWxYz';
      updateStreamKey();
      
      // Получить список доступных потоков (для отладки)
      try {
        const response = await fetch('http://localhost:8088/stat');
        const text = await response.text();
        if (text) {
          const regex = /hls\/([^"]+)/g;
          const matches = [...text.matchAll(regex)];
          if (matches.length) {
            availableStreamKeys.value = matches.map(m => m[1]).join('\n');
          } else {
            availableStreamKeys.value = 'Активные потоки не найдены';
          }
        }
      } catch (error) {
        console.error('Ошибка при получении статистики:', error);
        availableStreamKeys.value = 'Ошибка при получении статистики';
      }
    };
    
    // Модифицированная инициализация видеоплеера
    const initVideoPlayer = () => {
      // Убеждаемся, что player еще не инициализирован
      if (player.value) {
        return;
      }
      
      if (!videoPlayer.value) {
        console.warn('Не удалось инициализировать видеоплеер: отсутствует элемент проигрывателя');
        return;
      }
      
      // Используем либо ключ с сервера, либо ручной ввод
      const streamKey = manualStreamKey.value || (stream.value && stream.value.streamKey);
      if (!streamKey) {
        console.warn('Не удалось инициализировать видеоплеер: отсутствует ключ стрима');
        return;
      }
      
      // Добавляем /index.m3u8 к пути для более надежного соединения
      const hlsUrl = `http://localhost:8088/hls/${streamKey}/index.m3u8`;
      console.log('Инициализация плеера с URL:', hlsUrl);
      
      const options = {
        fluid: true,
        responsive: true,
        sources: [{
          src: hlsUrl,
          type: 'application/x-mpegURL'
        }],
        autoplay: true,
        controls: true,
        preload: 'auto',
        liveui: true
      };
      
      player.value = videojs(videoPlayer.value, options, () => {
        console.log('Плеер VideoJS инициализирован');
        player.value.play().catch(error => {
          console.error('Ошибка автовоспроизведения:', error);
        });
      });
    };
    
    // Завершение стрима
    const endStream = async () => {
      if (!isStreamer.value || !stream.value || !stream.value.live) {
        return
      }
      
      isLoading.value = true
      try {
        await axios.post(`${apiUrl}/streams/${stream.value.id}/end`)
        await fetchStream(stream.value.id)
        alert('Стрим успешно завершен')
        
        // Останавливаем плеер
        if (player.value) {
          player.value.pause()
        }
      } catch (error) {
        console.error('Ошибка при завершении стрима:', error)
        alert('Не удалось завершить стрим. Попробуйте еще раз.')
      } finally {
        isLoading.value = false
      }
    }
    
    onMounted(async () => {
      const streamId = route.params.id
      await fetchStream(streamId)
      
      if (!userStore.user) {
        await userStore.fetchUser()
      }
      
      if (stream.value) {
        await incrementViewerCount(streamId)
        
        // Периодическое обновление информации о стриме
        updateInterval.value = setInterval(async () => {
          await fetchStream(streamId)
        }, 30000) // Обновляем каждые 30 секунд
        
        // Инициализируем видеоплеер, если стрим активен
        if (stream.value.live) {
          initVideoPlayer()
        }
      }
    })
    
    onUnmounted(async () => {
      if (updateInterval.value) {
        clearInterval(updateInterval.value)
      }
      
      if (stream.value) {
        await decrementViewerCount(route.params.id)
      }
      
      // Освобождаем ресурсы плеера
      if (player.value) {
        player.value.dispose()
        player.value = null
      }
    })
    
    return {
      stream,
      isStreamer,
      isLoading,
      endStream,
      videoPlayer,
      manualStreamKey,
      updateStreamKey,
      tryTestStream,
      availableStreamKeys
      // chatMessages,
      // newMessage
    }
  }
}
</script>

<style scoped>
.video-container {
  width: 100%;
  aspect-ratio: 16/9;
  background-color: #000;
}

.stream-offline-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);
}
</style> 