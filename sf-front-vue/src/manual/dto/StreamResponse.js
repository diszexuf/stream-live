/**
 * Ответ с данными стрима
 */
export default class StreamResponse {
  /**
   * @param {String} id - ID стрима
   * @param {String} userId - ID пользователя-владельца стрима
   * @param {String} title - Название стрима
   * @param {String|null} description - Описание стрима
   * @param {String|null} thumbnailUrl - URL миниатюры стрима
   * @param {String} streamKey - Ключ стрима (только для своих стримов)
   * @param {Array<String>} tags - Теги стрима
   * @param {Boolean} isLive - Флаг активности стрима
   * @param {String|null} startedAt - Время начала стрима
   * @param {String|null} endedAt - Время окончания стрима
   * @param {Number} viewerCount - Количество зрителей
   */
  constructor(
    id,
    userId,
    title,
    description,
    thumbnailUrl,
    streamKey,
    tags,
    isLive,
    startedAt,
    endedAt,
    viewerCount
  ) {
    this.id = id;
    this.userId = userId;
    this.title = title;
    this.description = description;
    this.thumbnailUrl = thumbnailUrl;
    this.streamKey = streamKey;
    this.tags = tags;
    this.isLive = isLive;
    this.startedAt = startedAt;
    this.endedAt = endedAt;
    this.viewerCount = viewerCount;
  }

  /**
   * Создает экземпляр StreamResponse из объекта данных
   * @param {Object} data - Объект с данными
   * @returns {StreamResponse} - Новый экземпляр StreamResponse
   */
  static fromJson(data) {
    return new StreamResponse(
      data.id,
      data.userId,
      data.title,
      data.description || null,
      data.thumbnailUrl || null,
      data.streamKey || null,
      data.tags || [],
      data.isLive || false,
      data.startedAt || null,
      data.endedAt || null,
      data.viewerCount || 0
    );
  }

  /**
   * Проверяет, принадлежит ли стрим текущему пользователю
   * @param {String} currentUserId - ID текущего пользователя
   * @returns {Boolean} - true, если стрим принадлежит текущему пользователю
   */
  isOwnedByUser(currentUserId) {
    return this.userId === currentUserId;
  }

  /**
   * Возвращает форматированное время начала стрима
   * @returns {String} - Форматированное время или пустая строка
   */
  getFormattedStartTime() {
    if (!this.startedAt) return '';
    return new Date(this.startedAt).toLocaleString();
  }

  /**
   * Возвращает длительность стрима
   * @returns {String} - Форматированная длительность или "В эфире"
   */
  getDuration() {
    if (!this.startedAt) return '';
    if (!this.endedAt && this.isLive) return 'В эфире';
    
    const start = new Date(this.startedAt);
    const end = this.endedAt ? new Date(this.endedAt) : new Date();
    
    const durationMs = end - start;
    const hours = Math.floor(durationMs / (1000 * 60 * 60));
    const minutes = Math.floor((durationMs % (1000 * 60 * 60)) / (1000 * 60));
    
    return `${hours}ч ${minutes}м`;
  }
} 