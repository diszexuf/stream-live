import StreamResponse from '../dto/StreamResponse';
import StreamRequest from '../dto/StreamRequest';

/**
 * Клиент для работы с API стримов
 */
export default class StreamClient {
  /**
   * @param {String} baseUrl - Базовый URL API
   */
  constructor(baseUrl = 'http://localhost:8080/api') {
    this.baseUrl = baseUrl;
  }

  /**
   * Устанавливает токен авторизации
   * @param {String} token - JWT токен
   */
  setAuthToken(token) {
    this.authToken = token;
  }

  /**
   * Очищает токен авторизации
   */
  clearAuthToken() {
    this.authToken = null;
  }

  /**
   * Формирует заголовки запроса
   * @returns {Object} - Заголовки запроса
   */
  getHeaders() {
    const headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    };

    if (this.authToken) {
      headers['Authorization'] = `Bearer ${this.authToken}`;
    }

    return headers;
  }

  /**
   * Обрабатывает ответ от сервера
   * @param {Response} response - Ответ от сервера
   * @returns {Promise<Object>} - Обработанный результат
   */
  async handleResponse(response) {
    if (!response.ok) {
      const error = {
        status: response.status,
        statusText: response.statusText
      };
      
      try {
        const errorData = await response.json();
        Object.assign(error, errorData);
      } catch (e) {
      }
      
      throw error;
    }

    if (response.status === 204) {
      return null;
    }

    const contentLength = response.headers.get('content-length');
    if (contentLength === '0') {
      return null;
    }

    const contentType = response.headers.get('content-type');
    
    if (!contentType) {
      return null;
    }
    
    if (!contentType.includes('application/json')) {
      try {
        return await response.text();
      } catch (error) {
        return null;
      }
    }

    try {
      const text = await response.text();
      
      if (!text || text.trim() === '') {
        return null;
      }

      return JSON.parse(text);
    } catch (error) {
      return null;
    }
  }

  /**
   * Получает все стримы
   * @returns {Promise<Array<StreamResponse>>} - Список всех стримов
   */
  async getAllStreams() {
    const url = `${this.baseUrl}/streams`;
    
    const response = await fetch(url, {
      method: 'GET',
      headers: this.getHeaders(),
      credentials: 'include'
    });

    const data = await this.handleResponse(response);
    return data ? data.map(StreamResponse.fromJson) : [];
  }

  /**
   * Получает все активные стримы
   * @returns {Promise<Array<StreamResponse>>} - Список активных стримов
   */
  async getLiveStreams() {
    const url = `${this.baseUrl}/streams/live`;
    
    const response = await fetch(url, {
      method: 'GET',
      headers: this.getHeaders(),
      credentials: 'include'
    });

    const data = await this.handleResponse(response);
    return data ? data.map(StreamResponse.fromJson) : [];
  }

  /**
   * Поиск стримов по запросу
   * @param {String} query - Поисковый запрос
   * @returns {Promise<Array<StreamResponse>>} - Результаты поиска
   */
  async searchStreams(query) {
    if (!query) {
      throw new Error('Поисковый запрос не указан');
    }
    
    const url = `${this.baseUrl}/streams/search?query=${encodeURIComponent(query)}`;
    
    const response = await fetch(url, {
      method: 'GET',
      headers: this.getHeaders(),
      credentials: 'include'
    });

    const data = await this.handleResponse(response);
    return data ? data.map(StreamResponse.fromJson) : [];
  }

  /**
   * Получает стрим по ID
   * @param {String} streamId - ID стрима
   * @returns {Promise<StreamResponse>} - Данные стрима
   */
  async getStreamById(streamId) {
    if (!streamId) {
      throw new Error('ID стрима не указан');
    }
    
    const url = `${this.baseUrl}/streams/${streamId}`;
    
    const response = await fetch(url, {
      method: 'GET',
      headers: this.getHeaders(),
      credentials: 'include'
    });

    const data = await this.handleResponse(response);
    return StreamResponse.fromJson(data);
  }

  /**
   * Получает стримы пользователя
   * @param {String} userId - ID пользователя
   * @returns {Promise<Array<StreamResponse>>} - Список стримов пользователя
   */
  async getStreamsByUser(userId) {
    if (!userId) {
      throw new Error('ID пользователя не указан');
    }
    
    const url = `${this.baseUrl}/streams/user/${userId}`;
    
    const response = await fetch(url, {
      method: 'GET',
      headers: this.getHeaders(),
      credentials: 'include'
    });

    const data = await this.handleResponse(response);
    return data ? data.map(StreamResponse.fromJson) : [];
  }

  /**
   * Создает новый стрим
   * @param {StreamRequest} streamRequest - Данные для создания стрима
   * @returns {Promise<StreamResponse>} - Созданный стрим
   */
  async createStream(streamRequest) {
    const url = `${this.baseUrl}/streams/me`;
    
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getHeaders(),
      credentials: 'include',
      body: JSON.stringify(streamRequest.toJson())
    });

    const data = await this.handleResponse(response);
    return StreamResponse.fromJson(data);
  }

  /**
   * Обновляет данные стрима
   * @param {StreamRequest} streamRequest - Данные для обновления стрима
   * @returns {Promise<StreamResponse>} - Обновленный стрим
   */
  async updateStream(streamRequest) {
    const url = `${this.baseUrl}/streams/me`;
    
    const response = await fetch(url, {
      method: 'PUT',
      headers: this.getHeaders(),
      credentials: 'include',
      body: JSON.stringify(streamRequest.toJson())
    });

    const data = await this.handleResponse(response);
    return StreamResponse.fromJson(data);
  }

  /**
   * Завершает текущий стрим
   * @returns {Promise<void>} - Результат операции
   */
  async endStream() {
    const url = `${this.baseUrl}/streams/me`;
    
    const response = await fetch(url, {
      method: 'DELETE',
      headers: this.getHeaders(),
      credentials: 'include'
    });
    
    await this.handleResponse(response);
  }

  /**
   * Завершает стрим по ID
   * @param {String} streamId - ID стрима
   * @returns {Promise<void>} - Результат операции
   */
  async endStreamById(streamId) {
    if (!streamId) {
      throw new Error('ID стрима не указан');
    }
    
    const url = `${this.baseUrl}/streams/${streamId}`;
    
    const response = await fetch(url, {
      method: 'DELETE',
      headers: this.getHeaders(),
      credentials: 'include'
    });
    
    await this.handleResponse(response);
  }
} 