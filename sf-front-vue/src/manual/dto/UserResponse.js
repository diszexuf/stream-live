/**
 * Ответ с данными пользователя
 */
export default class UserResponse {
  /**
   * @param {String} id - ID пользователя
   * @param {String} username - Имя пользователя
   * @param {String} email - Email пользователя
   * @param {String|null} avatarUrl - URL аватара пользователя
   * @param {String|null} bio - Биография пользователя
   * @param {Number} followerCount - Количество подписчиков
   * @param {String|null} streamKey - Ключ стрима (только для своего профиля или админов)
   */
  constructor(id, username, email, avatarUrl, bio, followerCount, streamKey) {
    this.id = id;
    this.username = username;
    this.email = email;
    this.avatarUrl = avatarUrl;
    this.bio = bio;
    this.followerCount = followerCount;
    this.streamKey = streamKey;
  }

  /**
   * Создает экземпляр UserResponse из объекта данных
   * @param {Object} data - Объект с данными
   * @returns {UserResponse} - Новый экземпляр UserResponse
   */
  static fromJson(data) {
    return new UserResponse(
      data.id,
      data.username,
      data.email,
      data.avatarUrl || null,
      data.bio || null,
      data.followerCount || 0,
      data.streamKey || null
    );
  }
} 