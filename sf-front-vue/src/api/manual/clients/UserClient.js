import UserResponse from '../dto/UserResponse';
import UserUpdateRequest from '../dto/UserUpdateRequest';
import StreamKeyResponse from '../dto/StreamKeyResponse';

/**
 * Клиент для работы с API пользователей
 */
export default class UserClient {
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
    console.log(`Получен ответ от ${response.url}, статус: ${response.status} ${response.statusText}`);
    
    if (!response.ok) {
      console.error(`Ошибка запроса: ${response.status} ${response.statusText}`);
      const error = {
        status: response.status,
        statusText: response.statusText
      };
      
      try {
        const errorData = await response.json();
        Object.assign(error, errorData);
      } catch (e) {
        // Игнорируем ошибку парсинга JSON
      }
      
      throw error;
    }

    // Специальная обработка для PUT запросов на обновление профиля
    if (response.url.includes('/users/me') && response.type === 'basic' && response.method === 'PUT') {
      console.log('Обнаружен PUT запрос на обновление профиля, возвращаем null');
      return null;
    }

    // Для ответов без тела (например, 204 No Content)
    if (response.status === 204) {
      return null;
    }

    // Проверяем заголовок Content-Length
    const contentLength = response.headers.get('content-length');
    if (contentLength === '0') {
      return null;
    }

    // Проверяем, есть ли контент в ответе
    const contentType = response.headers.get('content-type');
    
    // Если контент-тип отсутствует, возвращаем null
    if (!contentType) {
      return null;
    }
    
    // Если контент-тип не JSON, возвращаем текст или null
    if (!contentType.includes('application/json')) {
      try {
        return await response.text();
      } catch (error) {
        return null;
      }
    }

    try {
      const text = await response.text();
      
      // Если тело ответа пустое, возвращаем null
      if (!text || text.trim() === '') {
        return null;
      }

      // Парсим JSON
      return JSON.parse(text);
    } catch (error) {
      console.error('Ошибка при обработке JSON ответа:', error);
      return null;
    }
  }

  /**
   * Получает профиль текущего пользователя
   * @returns {Promise<UserResponse>} - Данные текущего пользователя
   */
  async getCurrentUserProfile() {
    const url = `${this.baseUrl}/users/me`;
    
    const response = await fetch(url, {
      method: 'GET',
      headers: this.getHeaders(),
      credentials: 'include'
    });

    const data = await this.handleResponse(response);
    return UserResponse.fromJson(data);
  }

  /**
   * Обновляет профиль пользователя
   * @param {UserUpdateRequest} userUpdateRequest - Данные для обновления
   * @returns {Promise<void>} - Метод не возвращает данные
   */
  async updateUser(userUpdateRequest) {
    console.log('UserClient.updateUser: Отправка запроса на обновление профиля');
    console.log('Данные для обновления:', userUpdateRequest.toJson());
    
    const url = `${this.baseUrl}/users/me`;
    
    try {
      const response = await fetch(url, {
        method: 'PUT',
        headers: this.getHeaders(),
        credentials: 'include',
        body: JSON.stringify(userUpdateRequest.toJson())
      });

      await this.handleResponse(response);
      console.log('UserClient.updateUser: Профиль успешно обновлен');
    } catch (error) {
      console.error('UserClient.updateUser: Ошибка при обновлении профиля:', error);
      
      // Если ошибка связана с пустым ответом JSON, это не критично для PUT запроса
      if (error instanceof SyntaxError && error.message.includes('Unexpected end of JSON input')) {
        console.log('UserClient.updateUser: Получен пустой ответ, но это нормально для PUT запроса');
        return; // Просто возвращаемся без ошибки
      }
      
      throw error; // Перебрасываем другие ошибки дальше для обработки в store
    }
  }

  /**
   * Получает ключ стрима для текущего пользователя
   * @returns {Promise<StreamKeyResponse>} - Ответ с ключом стрима
   */
  async getStreamKey() {
    const url = `${this.baseUrl}/users/me/streamkey`;
    
    const response = await fetch(url, {
      method: 'GET',
      headers: this.getHeaders(),
      credentials: 'include'
    });

    const data = await this.handleResponse(response);
    return StreamKeyResponse.fromJson(data);
  }

  /**
   * Обновляет ключ стрима для текущего пользователя
   * @returns {Promise<StreamKeyResponse>} - Ответ с новым ключом стрима
   */
  async updateStreamKey() {
    const url = `${this.baseUrl}/users/me/streamkey`;
    
    const response = await fetch(url, {
      method: 'PUT',
      headers: this.getHeaders(),
      credentials: 'include',
      body: JSON.stringify({})
    });

    const data = await this.handleResponse(response);
    return StreamKeyResponse.fromJson(data);
  }

  /**
   * Получает пользователя по ID
   * @param {String} userId - ID пользователя
   * @returns {Promise<UserResponse>} - Данные пользователя
   */
  async getUserById(userId) {
    const url = `${this.baseUrl}/users/${userId}`;
    
    const response = await fetch(url, {
      method: 'GET',
      headers: this.getHeaders(),
      credentials: 'include'
    });

    const data = await this.handleResponse(response);
    return UserResponse.fromJson(data);
  }

  /**
   * Получает пользователя по имени пользователя
   * @param {String} username - Имя пользователя
   * @returns {Promise<UserResponse>} - Данные пользователя
   */
  async getUserByUsername(username) {
    const url = `${this.baseUrl}/users/${username}`;
    
    const response = await fetch(url, {
      method: 'GET',
      headers: this.getHeaders(),
      credentials: 'include'
    });

    const data = await this.handleResponse(response);
    return UserResponse.fromJson(data);
  }
} 