/**
 * Ответ с новым ключом стрима
 */
export default class StreamKeyResponse {
  /**
   * @param {String} newStreamKey - Новый ключ стрима
   */
  constructor(newStreamKey) {
    this.newStreamKey = newStreamKey;
  }

  /**
   * Создает экземпляр StreamKeyResponse из объекта данных
   * @param {Object} data - Объект с данными
   * @returns {StreamKeyResponse} - Новый экземпляр StreamKeyResponse
   */
  static fromJson(data) {
    if (!data) {
      return new StreamKeyResponse('');
    }
    return new StreamKeyResponse(data.newStreamKey || '');
  }
} 