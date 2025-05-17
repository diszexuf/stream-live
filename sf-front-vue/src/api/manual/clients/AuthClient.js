import AuthResponse from '../dto/AuthResponse';
import UserAuthRequest from '../dto/UserAuthRequest';
import UserRegisterRequest from '../dto/UserRegisterRequest';

/**
 * Клиент для работы с API аутентификации
 */
export default class AuthClient {
  /**
   * @param {String} baseUrl - Базовый URL API
   */
  constructor(baseUrl = 'http://localhost:8080/api') {
    this.baseUrl = baseUrl;
  }

  /**
   * Формирует заголовки запроса
   * @returns {Object} - Заголовки запроса
   */
  getHeaders() {
    return {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    };
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

    // Проверяем, есть ли контент в ответе
    const contentType = response.headers.get('content-type');
    
    // Если контент-тип отсутствует или не JSON, выбрасываем ошибку
    if (!contentType || !contentType.includes('application/json')) {
      throw new Error('Неверный формат ответа от сервера');
    }

    try {
      const text = await response.text();
      
      // Если тело ответа пустое, выбрасываем ошибку
      if (!text || text.trim() === '') {
        throw new Error('Пустой ответ от сервера');
      }

      // Парсим JSON
      return JSON.parse(text);
    } catch (error) {
      console.error('Ошибка при обработке JSON ответа:', error);
      throw error;
    }
  }

  /**
   * Аутентификация пользователя
   * @param {UserAuthRequest} userAuthRequest - Данные для аутентификации
   * @returns {Promise<AuthResponse>} - Ответ с токеном
   */
  async login(userAuthRequest) {
    const url = `${this.baseUrl}/auth/login`;
    
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getHeaders(),
      credentials: 'include',
      body: JSON.stringify(userAuthRequest.toJson())
    });

    const data = await this.handleResponse(response);
    return AuthResponse.fromJson(data);
  }

  /**
   * Регистрация нового пользователя
   * @param {UserRegisterRequest} userRegisterRequest - Данные для регистрации
   * @returns {Promise<AuthResponse>} - Ответ с токеном
   */
  async register(userRegisterRequest) {
    const url = `${this.baseUrl}/auth/register`;
    
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getHeaders(),
      credentials: 'include',
      body: JSON.stringify(userRegisterRequest.toJson())
    });

    const data = await this.handleResponse(response);
    return AuthResponse.fromJson(data);
  }
} 