<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStreamStore } from '@/stores/stream'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const streamStore = useStreamStore()
const userStore = useUserStore()

const streamId = computed(() => route.params.id)
const isLoading = ref(true)
const errorMessage = ref('')

// Вычисляемые свойства
const isAuthenticated = computed(() => userStore.isAuthenticated)
const isStreamOwner = computed(() => {
  if (!userStore.user || !streamStore.currentStream) return false
  return streamStore.currentStream.userId === userStore.user.id
})

// Загрузка данных стрима
const loadStream = async () => {
  if (!streamId.value) {
    router.push('/')
    return
  }

  isLoading.value = true
  errorMessage.value = ''

  try {
    await streamStore.fetchStreamById(streamId.value)
    
    if (!streamStore.currentStream) {
      errorMessage.value = 'Стрим не найден'
      router.push('/')
    }
  } catch (error) {
    console.error('Ошибка при загрузке стрима:', error)
    errorMessage.value = 'Не удалось загрузить стрим'
  } finally {
    isLoading.value = false
  }
}

// Завершение стрима
const endStream = async () => {
  if (!confirm('Вы уверены, что хотите завершить стрим?')) return

  isLoading.value = true
  errorMessage.value = ''

  try {
    const result = await streamStore.endStream()

    if (result) {
      alert('Стрим успешно завершен')
      await loadStream() // Обновляем данные стрима
    } else {
      errorMessage.value = streamStore.error || 'Не удалось завершить стрим'
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
  router.push('/streams/dashboard')
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

// Инициализация
onMounted(async () => {
  await loadStream()
})
</script>

<template>
  <v-container v-if="!isLoading && streamStore.currentStream">
    <v-row>
      <v-col cols="12">
        <!-- Видео плеер -->
        <div class="stream-player mb-4">
          <div v-if="streamStore.currentStream.isLive" class="video-container">
            <!-- Здесь будет встроен видео плеер (например, Hls.js или VideoJS) -->
            <div class="video-placeholder d-flex align-center justify-center">
              <div class="text-center">
                <v-icon size="64" color="white">mdi-play-circle</v-icon>
                <div class="text-h6 mt-2">Трансляция в прямом эфире</div>
              </div>
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
              <v-btn color="primary" @click="editStream">
                <v-icon start>mdi-pencil</v-icon>
                Редактировать
              </v-btn>
              <v-btn v-if="streamStore.currentStream.isLive" color="error" @click="endStream" :loading="isLoading">
                <v-icon start>mdi-stop</v-icon>
                Завершить стрим
              </v-btn>
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
  </v-container>

  <v-container v-else-if="isLoading">
    <div class="d-flex justify-center align-center" style="height: 300px;">
      <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
    </div>
  </v-container>

  <v-container v-else>
    <v-alert type="error" variant="tonal" border="start">
      {{ errorMessage || 'Стрим не найден' }}
    </v-alert>
    <div class="text-center mt-4">
      <v-btn color="primary" to="/">На главную</v-btn>
    </div>
  </v-container>
</template>

<style scoped>
.video-container {
  position: relative;
  width: 100%;
  height: 0;
  padding-bottom: 56.25%; /* 16:9 соотношение сторон */
  background-color: #000;
  overflow: hidden;
  border-radius: 8px;
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