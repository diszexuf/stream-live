import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { streamClient, StreamRequest } from '@/api/manual';
import { useUserStore } from './user';

export const useStreamStore = defineStore('stream', () => {
  // Стор пользователя для доступа к данным текущего пользователя
  const userStore = useUserStore();
  
  // Состояние
  const allStreams = ref([]);
  const liveStreams = ref([]);
  const currentUserStreams = ref([]);
  const currentStream = ref(null);
  const isLoading = ref(false);
  const error = ref(null);
  
  // Вычисляемые свойства
  const hasActiveStream = computed(() => {
    return currentUserStreams.value.some(stream => stream.isLive);
  });
  
  const activeStream = computed(() => {
    return currentUserStreams.value.find(stream => stream.isLive) || null;
  });
  
  // Действия
  
  /**
   * Загружает все стримы
   */
  const fetchAllStreams = async () => {
    isLoading.value = true;
    error.value = null;
    
    try {
      const streams = await streamClient.getAllStreams();
      
      // Преобразуем объекты в обычные JS объекты
      allStreams.value = streams.map(stream => {
        // Если stream не является простым объектом, преобразуем его
        if (typeof stream === 'object' && stream !== null) {
          const plainStream = { ...stream };
          
          // Проверяем поля, которые могут быть объектами
          if (plainStream.tags && typeof plainStream.tags === 'object' && !Array.isArray(plainStream.tags)) {
            plainStream.tags = [];
          }
          
          return plainStream;
        }
        return stream;
      });
    } catch (err) {
      console.error('Ошибка при загрузке стримов:', err);
      error.value = 'Не удалось загрузить стримы';
    } finally {
      isLoading.value = false;
    }
  };
  
  /**
   * Загружает активные стримы
   */
  const fetchLiveStreams = async () => {
    isLoading.value = true;
    error.value = null;
    
    try {
      const streams = await streamClient.getLiveStreams();
      
      // Преобразуем объекты в обычные JS объекты
      liveStreams.value = streams.map(stream => {
        // Если stream не является простым объектом, преобразуем его
        if (typeof stream === 'object' && stream !== null) {
          const plainStream = { ...stream };
          
          // Проверяем поля, которые могут быть объектами
          if (plainStream.tags && typeof plainStream.tags === 'object' && !Array.isArray(plainStream.tags)) {
            plainStream.tags = [];
          }
          
          return plainStream;
        }
        return stream;
      });
    } catch (err) {
      console.error('Ошибка при загрузке активных стримов:', err);
      error.value = 'Не удалось загрузить активные стримы';
    } finally {
      isLoading.value = false;
    }
  };
  
  /**
   * Загружает стримы текущего пользователя
   */
  const fetchCurrentUserStreams = async () => {
    if (!userStore.user || !userStore.user.id) {
      return;
    }
    
    isLoading.value = true;
    error.value = null;
    
    try {
      currentUserStreams.value = await streamClient.getStreamsByUser(userStore.user.id);
    } catch (err) {
      console.error('Ошибка при загрузке стримов пользователя:', err);
      error.value = 'Не удалось загрузить ваши стримы';
    } finally {
      isLoading.value = false;
    }
  };
  
  /**
   * Загружает стрим по ID
   * @param {String} streamId - ID стрима
   */
  const fetchStreamById = async (streamId) => {
    isLoading.value = true;
    error.value = null;
    
    try {
      const stream = await streamClient.getStreamById(streamId);
      
      // Если stream не является простым объектом, преобразуем его
      if (typeof stream === 'object' && stream !== null) {
        const plainStream = { ...stream };
        
        // Проверяем поля, которые могут быть объектами
        if (plainStream.tags && typeof plainStream.tags === 'object' && !Array.isArray(plainStream.tags)) {
          plainStream.tags = [];
        }
        
        currentStream.value = plainStream;
      } else {
        currentStream.value = stream;
      }
    } catch (err) {
      console.error('Ошибка при загрузке стрима:', err);
      error.value = 'Не удалось загрузить стрим';
    } finally {
      isLoading.value = false;
    }
  };
  
  /**
   * Создает новый стрим
   * @param {Object} streamData - Данные стрима
   * @returns {Promise<Boolean>} - Результат операции
   */
  const createStream = async (streamData) => {
    if (!userStore.isAuthenticated) {
      error.value = 'Необходимо авторизоваться';
      return false;
    }
    
    isLoading.value = true;
    error.value = null;
    
    try {
      const request = new StreamRequest(streamData);
      const newStream = await streamClient.createStream(request);
      
      // Добавляем новый стрим в список стримов пользователя
      currentUserStreams.value.push(newStream);
      
      return true;
    } catch (err) {
      console.error('Ошибка при создании стрима:', err);
      error.value = 'Не удалось создать стрим';
      return false;
    } finally {
      isLoading.value = false;
    }
  };
  
  /**
   * Обновляет данные стрима
   * @param {Object} streamData - Данные для обновления
   * @returns {Promise<Boolean>} - Результат операции
   */
  const updateStream = async (streamData) => {
    if (!userStore.isAuthenticated) {
      error.value = 'Необходимо авторизоваться';
      return false;
    }
    
    isLoading.value = true;
    error.value = null;
    
    try {
      const request = new StreamRequest(streamData);
      const updatedStream = await streamClient.updateStream(request);
      
      // Обновляем стрим в списке стримов пользователя
      const index = currentUserStreams.value.findIndex(s => s.id === updatedStream.id);
      if (index !== -1) {
        currentUserStreams.value[index] = updatedStream;
      }
      
      // Обновляем текущий стрим, если он открыт
      if (currentStream.value && currentStream.value.id === updatedStream.id) {
        currentStream.value = updatedStream;
      }
      
      return true;
    } catch (err) {
      console.error('Ошибка при обновлении стрима:', err);
      error.value = 'Не удалось обновить стрим';
      return false;
    } finally {
      isLoading.value = false;
    }
  };
  
  /**
   * Завершает текущий стрим
   * @returns {Promise<Boolean>} - Результат операции
   */
  const endStream = async () => {
    if (!userStore.isAuthenticated || !hasActiveStream.value) {
      error.value = 'Нет активного стрима';
      return false;
    }
    
    isLoading.value = true;
    error.value = null;
    
    try {
      await streamClient.endStream();
      
      // Обновляем список стримов пользователя
      await fetchCurrentUserStreams();
      
      return true;
    } catch (err) {
      console.error('Ошибка при завершении стрима:', err);
      error.value = 'Не удалось завершить стрим';
      return false;
    } finally {
      isLoading.value = false;
    }
  };
  
  /**
   * Поиск стримов
   * @param {String} query - Поисковый запрос
   */
  const searchStreams = async (query) => {
    if (!query || query.trim().length === 0) {
      return [];
    }
    
    isLoading.value = true;
    error.value = null;
    
    try {
      const streams = await streamClient.searchStreams(query);
      
      // Преобразуем объекты в обычные JS объекты
      const processedStreams = streams.map(stream => {
        // Если stream не является простым объектом, преобразуем его
        if (typeof stream === 'object' && stream !== null) {
          const plainStream = { ...stream };
          
          // Проверяем поля, которые могут быть объектами
          if (plainStream.tags && typeof plainStream.tags === 'object' && !Array.isArray(plainStream.tags)) {
            plainStream.tags = [];
          }
          
          return plainStream;
        }
        return stream;
      });
      
      console.log('Search results:', processedStreams);
      return processedStreams;
    } catch (err) {
      console.error('Ошибка при поиске стримов:', err);
      error.value = 'Не удалось выполнить поиск';
      return [];
    } finally {
      isLoading.value = false;
    }
  };
  
  return {
    // Состояние
    allStreams,
    liveStreams,
    currentUserStreams,
    currentStream,
    isLoading,
    error,
    
    // Вычисляемые свойства
    hasActiveStream,
    activeStream,
    
    // Действия
    fetchAllStreams,
    fetchLiveStreams,
    fetchCurrentUserStreams,
    fetchStreamById,
    createStream,
    updateStream,
    endStream,
    searchStreams
  };
}); 