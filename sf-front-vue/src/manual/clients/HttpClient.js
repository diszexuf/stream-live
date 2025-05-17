/**
 * Базовый HTTP-клиент для работы с API
 */
export default class HttpClient {
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
      // Восстанавливаем стандартный формат токена с префиксом Bearer
      headers['Authorization'] = `Bearer ${this.authToken}`;
      console.log('Using token for request:', this.authToken.substring(0, 10) + '...');
    } else {
      console.warn('No auth token available for request');
    }

    return headers;
  }

  /**
   * Выполняет GET-запрос
   * @param {String} path - Путь запроса
   * @returns {Promise<Object>} - Результат запроса
   */
  async get(path) {
    const url = `${this.baseUrl}${path}`;
    console.log(`GET request to: ${url}`);
    
    const response = await fetch(url, {
      method: 'GET',
      headers: this.getHeaders(),
      credentials: 'include'
    });

    return this.handleResponse(response);
  }

  /**
   * Выполняет POST-запрос
   * @param {String} path - Путь запроса
   * @param {Object} data - Данные для отправки
   * @returns {Promise<Object>} - Результат запроса
   */
  async post(path, data) {
    const url = `${this.baseUrl}${path}`;
    console.log(`POST request to: ${url}`, data);
    
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getHeaders(),
      credentials: 'include',
      body: JSON.stringify(data)
    });

    return this.handleResponse(response);
  }

  /**
   * Выполняет PUT-запрос
   * @param {String} path - Путь запроса
   * @param {Object} data - Данные для отправки
   * @returns {Promise<Object>} - Результат запроса
   */
  async put(path, data) {
    const url = `${this.baseUrl}${path}`;
    
    const response = await fetch(url, {
      method: 'PUT',
      headers: this.getHeaders(),
      credentials: 'include',
      body: JSON.stringify(data)
    });

    return this.handleResponse(response);
  }

  /**
   * Выполняет DELETE-запрос
   * @param {String} path - Путь запроса
   * @returns {Promise<Object>} - Результат запроса
   */
  async delete(path) {
    const url = `${this.baseUrl}${path}`;
    
    const response = await fetch(url, {
      method: 'DELETE',
      headers: this.getHeaders(),
      credentials: 'include'
    });

    return this.handleResponse(response);
  }

  /**
   * Обрабатывает ответ от сервера
   * @param {Response} response - Ответ от сервера
   * @returns {Promise<Object>} - Обработанный результат
   */
  async handleResponse(response) {
    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw {
        status: response.status,
        statusText: response.statusText,
        ...error
      };
    }

    // Для ответов без тела (например, 204 No Content)
    if (response.status === 204) {
      return null;
    }

    return response.json();
  }
} 