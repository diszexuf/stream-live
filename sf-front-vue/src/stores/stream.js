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
  
  /**
   * Обогащает объект стрима дополнительными методами
   * @param {Object} stream - Объект стрима
   * @returns {Object} - Обогащенный объект стрима
   */
  const enrichStream = (stream) => {
    if (!stream) return null;
    
    // Создаем копию стрима, чтобы избежать проблем с реактивностью
    const enrichedStream = { ...stream };
    
    // Проверяем поля, которые могут быть объектами
    if (enrichedStream.tags && typeof enrichedStream.tags === 'object' && !Array.isArray(enrichedStream.tags)) {
      enrichedStream.tags = [];
    } else if (Array.isArray(enrichedStream.tags)) {
      enrichedStream.tags = [...enrichedStream.tags];
    } else {
      enrichedStream.tags = [];
    }
    
    // Добавляем методы для форматирования времени и длительности
    enrichedStream.getFormattedStartTime = function() {
      if (!this.startedAt) return 'Нет данных';
      try {
        const date = new Date(this.startedAt);
        return date.toLocaleString('ru-RU');
      } catch (error) {
        return 'Нет данных';
      }
    };
    
    enrichedStream.getDuration = function() {
      if (!this.startedAt) return 'Нет данных';
      
      const start = new Date(this.startedAt);
      const end = this.endedAt ? new Date(this.endedAt) : new Date();
      
      const durationMs = end - start;
      const hours = Math.floor(durationMs / (1000 * 60 * 60));
      const minutes = Math.floor((durationMs % (1000 * 60 * 60)) / (1000 * 60));
      
      return `${hours}ч ${minutes}м`;
    };
    
    return enrichedStream;
  };
  
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
      
      // Преобразуем объекты в обычные JS объекты и обогащаем их
      allStreams.value = streams.map(stream => enrichStream(stream));
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
      
      // Преобразуем объекты в обычные JS объекты и обогащаем их
      liveStreams.value = streams.map(stream => enrichStream(stream));
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
      const streams = await streamClient.getStreamsByUser(userStore.user.id);
      currentUserStreams.value = streams.map(stream => enrichStream(stream));
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
    if (!streamId) {
      console.error('fetchStreamById: streamId не указан');
      error.value = 'ID стрима не указан';
      isLoading.value = false;
      return;
    }
    
    isLoading.value = true;
    error.value = null;
    
    try {
      console.log(`fetchStreamById: Загрузка стрима с ID ${streamId}`);

      const stream = await streamClient.getStreamById(streamId);
      currentStream.value = enrichStream(stream);
      console.log('fetchStreamById: Стрим успешно загружен', currentStream.value);
    } catch (err) {
      console.error('Ошибка при загрузке стрима:', err);
      
      if (err.status === 403) {
        error.value = 'Доступ запрещен.';
      } else if (err.status === 404) {
        error.value = 'Стрим не найден';
      } else {
        error.value = 'Не удалось загрузить стрим';
      }
      
      currentStream.value = null;
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
      console.error('createStream: Пользователь не авторизован');
      error.value = 'Необходимо авторизоваться';
      return false;
    }
    
    isLoading.value = true;
    error.value = null;
    
    try {
      console.log('createStream: Создание нового стрима', streamData);
      
      // Проверяем наличие токена авторизации
      if (!userStore.token) {
        console.warn('createStream: Токен авторизации отсутствует');
        await userStore.checkAuth();
        
        if (!userStore.token) {
          throw new Error('Отсутствует токен авторизации');
        }
      }
      
      const request = new StreamRequest(streamData);
      console.log('createStream: Отправка запроса на создание стрима', request.toJson());
      
      const newStream = await streamClient.createStream(request);
      console.log('createStream: Стрим успешно создан', newStream);
      
      // Добавляем новый стрим в список стримов пользователя
      currentUserStreams.value.push(enrichStream(newStream));
      
      return true;
    } catch (err) {
      console.error('Ошибка при создании стрима:', err);
      
      if (err.status === 401) {
        error.value = 'Требуется авторизация';
      } else if (err.status === 400) {
        error.value = 'Некорректные данные стрима';
      } else {
        error.value = err.message || 'Не удалось создать стрим';
      }
      
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
      const enrichedStream = enrichStream(updatedStream);
      
      // Обновляем стрим в списке стримов пользователя
      const index = currentUserStreams.value.findIndex(s => s.id === updatedStream.id);
      if (index !== -1) {
        currentUserStreams.value[index] = enrichedStream;
      }
      
      // Обновляем текущий стрим, если он открыт
      if (currentStream.value && currentStream.value.id === updatedStream.id) {
        currentStream.value = enrichedStream;
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
    if (!userStore.isAuthenticated) {
      console.error('endStream: Пользователь не авторизован');
      error.value = 'Необходимо авторизоваться';
      return false;
    }
    
    isLoading.value = true;
    error.value = null;
    
    try {
      console.log('endStream: Завершение активного стрима');
      
      // Проверяем наличие токена авторизации
      if (!userStore.token) {
        console.warn('endStream: Токен авторизации отсутствует');
        await userStore.checkAuth();
        
        if (!userStore.token) {
          throw new Error('Отсутствует токен авторизации');
        }
      }
      
      // Явно устанавливаем токен авторизации перед запросом
      const { setAuthToken } = await import('@/api/manual');
      setAuthToken(userStore.token);
      
      // Завершаем стрим
      await streamClient.endStream();
      console.log('endStream: Стрим успешно завершен');
      
      // Обновляем список стримов пользователя
      await fetchCurrentUserStreams();
      
      return true;
    } catch (err) {
      console.error('Ошибка при завершении стрима:', err);
      error.value = err.message || 'Не удалось завершить стрим';
      return false;
    } finally {
      isLoading.value = false;
    }
  };
  
  /**
   * Завершает стрим по ID
   * @param {String} streamId - ID стрима
   * @returns {Promise<Boolean>} - Результат операции
   */
  const endStreamById = async (streamId) => {
    if (!userStore.isAuthenticated) {
      console.error('endStreamById: Пользователь не авторизован');
      error.value = 'Необходимо авторизоваться';
      return false;
    }
    
    if (!streamId) {
      console.error('endStreamById: ID стрима не указан');
      error.value = 'ID стрима не указан';
      return false;
    }
    
    isLoading.value = true;
    error.value = null;
    
    try {
      console.log(`endStreamById: Завершение стрима с ID ${streamId}`);
      
      // Проверяем наличие токена авторизации
      if (!userStore.token) {
        console.warn('endStreamById: Токен авторизации отсутствует');
        await userStore.checkAuth();
        
        if (!userStore.token) {
          throw new Error('Отсутствует токен авторизации');
        }
      }
      
      await streamClient.endStreamById(streamId);
      console.log('endStreamById: Стрим успешно завершен');
      
      // Обновляем список стримов пользователя
      await fetchCurrentUserStreams();
      
      // Если текущий стрим - это тот, который мы завершили, обновляем его
      if (currentStream.value && currentStream.value.id === streamId) {
        await fetchStreamById(streamId);
      }
      
      return true;
    } catch (err) {
      console.error('Ошибка при завершении стрима по ID:', err);
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
      
      // Преобразуем объекты в обычные JS объекты и обогащаем их
      const processedStreams = streams.map(stream => enrichStream(stream));
      
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
  
  /**
   * Устанавливает стрим как активный для редактирования
   * @param {Object} stream - Стрим для установки активным
   */
  const setActiveStream = (stream) => {
    if (!stream) return;
    
    // Обогащаем стрим дополнительными методами
    const enrichedStream = enrichStream(stream);
    
    // Находим стрим в списке стримов пользователя и обновляем его
    const index = currentUserStreams.value.findIndex(s => s.id === stream.id);
    if (index !== -1) {
      currentUserStreams.value[index] = enrichedStream;
    } else {
      // Если стрим не найден в списке, добавляем его
      // Учитываем, что у пользователя может быть только один активный стрим
      if (enrichedStream.isLive) {
        // Если добавляемый стрим активный, убедимся что других активных нет
        const existingActiveIndex = currentUserStreams.value.findIndex(s => s.isLive && s.id !== stream.id);
        if (existingActiveIndex !== -1) {
          // Если есть другой активный стрим, деактивируем его
          currentUserStreams.value[existingActiveIndex].isLive = false;
        }
      }
      currentUserStreams.value.push(enrichedStream);
    }
    
    console.log('Установлен активный стрим:', enrichedStream);
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
    endStreamById,
    searchStreams,
    setActiveStream
  };
}); 