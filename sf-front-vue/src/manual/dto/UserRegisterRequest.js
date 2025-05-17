/**
 * Запрос регистрации пользователя
 */
export default class UserRegisterRequest {
  /**
   * @param {String} username - Имя пользователя
   * @param {String} email - Email пользователя
   * @param {String} password - Пароль пользователя
   * @param {String} [avatarUrl] - URL аватара пользователя (опционально)
   * @param {String} [bio] - Биография пользователя (опционально)
   */
  constructor(username, email, password, avatarUrl = null, bio = null) {
    this.username = username;
    this.email = email;
    this.password = password;
    this.avatarUrl = avatarUrl;
    this.bio = bio;
  }

  /**
   * Преобразует объект в JSON для отправки на сервер
   * @returns {Object} - Объект для сериализации в JSON
   */
  toJson() {
    const result = {
      username: this.username,
      email: this.email,
      password: this.password
    };

    if (this.avatarUrl) {
      result.avatarUrl = this.avatarUrl;
    }

    if (this.bio) {
      result.bio = this.bio;
    }

    return result;
  }
} 