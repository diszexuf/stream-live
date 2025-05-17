// Клиенты
import HttpClient from './clients/HttpClient';
import AuthClient from './clients/AuthClient';
import UserClient from './clients/UserClient';
import StreamClient from './clients/StreamClient';

// DTO
import AuthResponse from './dto/AuthResponse';
import UserAuthRequest from './dto/UserAuthRequest';
import UserRegisterRequest from './dto/UserRegisterRequest';
import UserResponse from './dto/UserResponse';
import UserUpdateRequest from './dto/UserUpdateRequest';
import StreamKeyResponse from './dto/StreamKeyResponse';
import StreamRequest from './dto/StreamRequest';
import StreamResponse from './dto/StreamResponse';

// Создаем экземпляры клиентов с общим HttpClient
const httpClient = new HttpClient();
const authClient = new AuthClient(httpClient);
const userClient = new UserClient(httpClient);
const streamClient = new StreamClient(httpClient);

// Экспортируем клиенты и классы DTO
export {
  // Клиенты
  httpClient,
  authClient,
  userClient,
  streamClient,
  
  // Классы клиентов (для создания новых экземпляров)
  HttpClient,
  AuthClient,
  UserClient,
  StreamClient,
  
  // DTO
  AuthResponse,
  UserAuthRequest,
  UserRegisterRequest,
  UserResponse,
  UserUpdateRequest,
  StreamKeyResponse,
  StreamRequest,
  StreamResponse
};

// Функция для установки токена авторизации
export const setAuthToken = (token) => {
  httpClient.setAuthToken(token);
};

// Функция для очистки токена авторизации
export const clearAuthToken = () => {
  httpClient.clearAuthToken();
}; 