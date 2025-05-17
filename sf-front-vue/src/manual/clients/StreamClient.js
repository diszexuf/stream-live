import HttpClient from './HttpClient';
import StreamResponse from '../dto/StreamResponse';
import StreamRequest from '../dto/StreamRequest';

/**
 * Клиент для работы с API стримов
 */
export default class StreamClient {
  /**
   * @param {HttpClient} httpClient - HTTP-клиент для выполнения запросов
   */
  constructor(httpClient = new HttpClient()) {
    this.httpClient = httpClient;
  }

  /**
   * Получает все стримы
   * @returns {Promise<Array<StreamResponse>>} - Список всех стримов
   */
  async getAllStreams() {
    const data = await this.httpClient.get('/streams');
    return data.map(StreamResponse.fromJson);
  }

  /**
   * Получает все активные стримы
   * @returns {Promise<Array<StreamResponse>>} - Список активных стримов
   */
  async getLiveStreams() {
    const data = await this.httpClient.get('/streams/live');
    return data.map(StreamResponse.fromJson);
  }

  /**
   * Поиск стримов по запросу
   * @param {String} query - Поисковый запрос
   * @returns {Promise<Array<StreamResponse>>} - Результаты поиска
   */
  async searchStreams(query) {
    const data = await this.httpClient.get(`/streams/search?query=${encodeURIComponent(query)}`);
    return data.map(StreamResponse.fromJson);
  }

  /**
   * Получает стрим по ID
   * @param {String} streamId - ID стрима
   * @returns {Promise<StreamResponse>} - Данные стрима
   */
  async getStreamById(streamId) {
    const data = await this.httpClient.get(`/streams/${streamId}`);
    return StreamResponse.fromJson(data);
  }

  /**
   * Получает стримы пользователя
   * @param {String} userId - ID пользователя
   * @returns {Promise<Array<StreamResponse>>} - Список стримов пользователя
   */
  async getStreamsByUser(userId) {
    const data = await this.httpClient.get(`/streams/user/${userId}`);
    return data.map(StreamResponse.fromJson);
  }

  /**
   * Создает новый стрим
   * @param {StreamRequest} streamRequest - Данные для создания стрима
   * @returns {Promise<StreamResponse>} - Созданный стрим
   */
  async createStream(streamRequest) {
    const data = await this.httpClient.post('/streams/me', streamRequest.toJson());
    return StreamResponse.fromJson(data);
  }

  /**
   * Обновляет данные стрима
   * @param {StreamRequest} streamRequest - Данные для обновления стрима
   * @returns {Promise<StreamResponse>} - Обновленный стрим
   */
  async updateStream(streamRequest) {
    const data = await this.httpClient.put('/streams/me', streamRequest.toJson());
    return StreamResponse.fromJson(data);
  }

  /**
   * Завершает текущий стрим
   * @returns {Promise<void>} - Результат операции
   */
  async endStream() {
    await this.httpClient.delete('/streams/me');
  }
} 