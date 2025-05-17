import HttpClient from './HttpClient';
import UserResponse from '../dto/UserResponse';
import UserUpdateRequest from '../dto/UserUpdateRequest';
import StreamKeyResponse from '../dto/StreamKeyResponse';

/**
 * Клиент для работы с API пользователей
 */
export default class UserClient {
  /**
   * @param {HttpClient} httpClient - HTTP-клиент для выполнения запросов
   */
  constructor(httpClient = new HttpClient()) {
    this.httpClient = httpClient;
  }

  /**
   * Получает профиль текущего пользователя
   * @returns {Promise<UserResponse>} - Данные текущего пользователя
   */
  async getCurrentUserProfile() {
    const data = await this.httpClient.get('/users/me');
    return UserResponse.fromJson(data);
  }

  /**
   * Обновляет профиль пользователя
   * @param {String} userId - ID пользователя
   * @param {UserUpdateRequest} userUpdateRequest - Данные для обновления
   * @returns {Promise<UserResponse>} - Обновленные данные пользователя
   */
  async updateUser(userId, userUpdateRequest) {
    const data = await this.httpClient.put(`/users/${userId}`, userUpdateRequest.toJson());
    return UserResponse.fromJson(data);
  }

  /**
   * Получает ключ стрима для текущего пользователя
   * @returns {Promise<StreamKeyResponse>} - Ответ с ключом стрима
   */
  async getStreamKey() {
    const data = await this.httpClient.get('/users/me/streamkey');
    return StreamKeyResponse.fromJson(data);
  }

  /**
   * Обновляет ключ стрима для текущего пользователя
   * @returns {Promise<StreamKeyResponse>} - Ответ с новым ключом стрима
   */
  async updateStreamKey() {
    const data = await this.httpClient.put('/users/me/streamkey', {});
    return StreamKeyResponse.fromJson(data);
  }

  /**
   * Получает пользователя по ID
   * @param {String} userId - ID пользователя
   * @returns {Promise<UserResponse>} - Данные пользователя
   */
  async getUserById(userId) {
    const data = await this.httpClient.get(`/users/${userId}`);
    return UserResponse.fromJson(data);
  }

  /**
   * Получает пользователя по имени пользователя
   * @param {String} username - Имя пользователя
   * @returns {Promise<UserResponse>} - Данные пользователя
   */
  async getUserByUsername(username) {
    const data = await this.httpClient.get(`/users/${username}`);
    return UserResponse.fromJson(data);
  }
} 