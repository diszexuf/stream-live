// Клиенты API
import AuthClient from './clients/AuthClient';
import UserClient from './clients/UserClient';
import StreamClient from './clients/StreamClient';

// DTO для запросов
import UserAuthRequest from './dto/UserAuthRequest';
import UserRegisterRequest from './dto/UserRegisterRequest';
import UserUpdateRequest from './dto/UserUpdateRequest';
import StreamRequest from './dto/StreamRequest';

// DTO для ответов
import AuthResponse from './dto/AuthResponse';
import UserResponse from './dto/UserResponse';
import StreamResponse from './dto/StreamResponse';
import StreamKeyResponse from './dto/StreamKeyResponse';

// Создаем экземпляры клиентов
const baseUrl = 'http://localhost:8080/api';
const authClient = new AuthClient(baseUrl);
const userClient = new UserClient(baseUrl);
const streamClient = new StreamClient(baseUrl);

// Функция для установки токена авторизации
const setAuthToken = (token) => {
  if (token) {
    userClient.setAuthToken(token);
    streamClient.setAuthToken(token);
  } else {
    userClient.clearAuthToken();
    streamClient.clearAuthToken();
  }
};

// Экспортируем всё необходимое
export {
  // Клиенты
  authClient,
  userClient,
  streamClient,
  
  // Функции
  setAuthToken,
  
  // DTO для запросов
  UserAuthRequest,
  UserRegisterRequest,
  UserUpdateRequest,
  StreamRequest,
  
  // DTO для ответов
  AuthResponse,
  UserResponse,
  StreamResponse,
  StreamKeyResponse
}; 