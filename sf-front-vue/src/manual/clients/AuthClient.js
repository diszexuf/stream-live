import HttpClient from './HttpClient';
import AuthResponse from '../dto/AuthResponse';
import UserAuthRequest from '../dto/UserAuthRequest';
import UserRegisterRequest from '../dto/UserRegisterRequest';

/**
 * Клиент для работы с API аутентификации
 */
export default class AuthClient {
  /**
   * @param {HttpClient} httpClient - HTTP-клиент для выполнения запросов
   */
  constructor(httpClient = new HttpClient()) {
    this.httpClient = httpClient;
  }

  /**
   * Выполняет вход пользователя
   * @param {UserAuthRequest} userAuthRequest - Данные для входа
   * @returns {Promise<AuthResponse>} - Ответ с токеном авторизации
   */
  async login(userAuthRequest) {
    const data = await this.httpClient.post('/auth/login', userAuthRequest.toJson());
    return AuthResponse.fromJson(data);
  }

  /**
   * Регистрирует нового пользователя
   * @param {UserRegisterRequest} userRegisterRequest - Данные для регистрации
   * @returns {Promise<AuthResponse>} - Ответ с токеном авторизации
   */
  async register(userRegisterRequest) {
    const data = await this.httpClient.post('/auth/register', userRegisterRequest.toJson());
    return AuthResponse.fromJson(data);
  }
} 