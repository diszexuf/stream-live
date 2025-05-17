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

    // Проверяем, есть ли контент в ответе
    const contentType = response.headers.get('content-type');
    const contentLength = response.headers.get('content-length');
    
    // Если контент отсутствует или его длина равна 0, возвращаем null
    if (!contentType || contentLength === '0') {
      return null;
    }
    
    // Если контент-тип не JSON, возвращаем текст или null
    if (!contentType.includes('application/json')) {
      return response.text().catch(() => null);
    }

    // Парсим JSON с обработкой ошибок
    try {
      return await response.json();
    } catch (error) {
      console.error('Ошибка при разборе JSON:', error);
      // Возвращаем пустой объект вместо ошибки
      return {};
    }
  }
} 