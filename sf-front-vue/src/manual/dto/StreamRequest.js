/**
 * Запрос на создание или обновление стрима
 */
export default class StreamRequest {
  /**
   * @param {Object} data - Данные стрима
   * @param {String} data.title - Название стрима
   * @param {String} [data.description] - Описание стрима
   * @param {Array<String>} [data.tags] - Теги стрима
   * @param {String} [data.thumbnailUrl] - URL миниатюры стрима
   */
  constructor(data = {}) {
    this.title = data.title;
    this.description = data.description;
    this.tags = data.tags || [];
    this.thumbnailUrl = data.thumbnailUrl;
  }

  /**
   * Преобразует объект в JSON для отправки на сервер
   * @returns {Object} - Объект для сериализации в JSON
   */
  toJson() {
    const result = {
      title: this.title
    };

    if (this.description !== undefined) {
      result.description = this.description;
    }

    if (this.tags && this.tags.length > 0) {
      result.tags = this.tags;
    }

    if (this.thumbnailUrl !== undefined) {
      result.thumbnailUrl = this.thumbnailUrl;
    }

    return result;
  }
} 