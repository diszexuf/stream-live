/**
 * Запрос обновления данных пользователя
 */
export default class UserUpdateRequest {
  /**
   * @param {Object} data - Объект с данными для обновления
   * @param {String} [data.email] - Новый email пользователя
   * @param {String} [data.avatarUrl] - Новый URL аватара пользователя
   * @param {String} [data.bio] - Новая биография пользователя
   */
  constructor(data = {}) {
    this.email = data.email;
    this.avatarUrl = data.avatarUrl;
    this.bio = typeof data.bio === 'object' ? '' : (data.bio || '');
  }

  /**
   * Преобразует объект в JSON для отправки на сервер
   * @returns {Object} - Объект для сериализации в JSON
   */
  toJson() {
    const result = {};

    if (this.email !== undefined) {
      result.email = this.email;
    }

    if (this.avatarUrl !== undefined) {
      result.avatarUrl = this.avatarUrl;
    }

    // Всегда отправляем bio как строку, даже если это пустая строка
    result.bio = typeof this.bio === 'object' ? '' : (this.bio || '');

    return result;
  }
} 