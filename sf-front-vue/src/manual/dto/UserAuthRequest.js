/**
 * Запрос аутентификации пользователя
 */
export default class UserAuthRequest {
  /**
   * @param {String} username - Имя пользователя
   * @param {String} password - Пароль пользователя
   */
  constructor(username, password) {
    this.username = username;
    this.password = password;
  }

  /**
   * Преобразует объект в JSON для отправки на сервер
   * @returns {Object} - Объект для сериализации в JSON
   */
  toJson() {
    return {
      username: this.username,
      password: this.password
    };
  }
} 