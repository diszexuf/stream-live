// Клиенты
import HttpClient from './clients/HttpClient';
import AuthClient from './clients/AuthClient';
import UserClient from './clients/UserClient';

// DTO
import AuthResponse from './dto/AuthResponse';
import UserAuthRequest from './dto/UserAuthRequest';
import UserRegisterRequest from './dto/UserRegisterRequest';
import UserResponse from './dto/UserResponse';
import UserUpdateRequest from './dto/UserUpdateRequest';
import StreamKeyResponse from './dto/StreamKeyResponse';

// Создаем экземпляры клиентов с общим HttpClient
const httpClient = new HttpClient();
const authClient = new AuthClient(httpClient);
const userClient = new UserClient(httpClient);

// Экспортируем клиенты и классы DTO
export {
  // Клиенты
  httpClient,
  authClient,
  userClient,
  
  // Классы клиентов (для создания новых экземпляров)
  HttpClient,
  AuthClient,
  UserClient,
  
  // DTO
  AuthResponse,
  UserAuthRequest,
  UserRegisterRequest,
  UserResponse,
  UserUpdateRequest,
  StreamKeyResponse
};

// Функция для установки токена авторизации
export const setAuthToken = (token) => {
  httpClient.setAuthToken(token);
};

// Функция для очистки токена авторизации
export const clearAuthToken = () => {
  httpClient.clearAuthToken();
}; 