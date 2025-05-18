/**
 * Ответ аутентификации, содержащий JWT токен
 */
export default class AuthResponse {
  /**
   * @param {String} token - JWT токен для аутентификации
   */
  constructor(token) {
    this.token = token;
  }

  /**
   * Создает экземпляр AuthResponse из объекта данных
   * @param {Object} data - Объект с данными
   * @returns {AuthResponse} - Новый экземпляр AuthResponse
   */
  static fromJson(data) {
    if (!data) {
      return new AuthResponse('');
    }
    return new AuthResponse(data.token || '');
  }
} 