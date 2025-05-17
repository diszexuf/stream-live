import {defineStore} from 'pinia'
import {ref} from 'vue'

// Импортируем самописные клиенты и DTO
import { authClient, userClient, setAuthToken, UserAuthRequest, UserRegisterRequest, UserUpdateRequest } from '@/api/manual';

export const useUserStore = defineStore('user', () => {
    // Стор
    const token = ref(localStorage.getItem('token') || null)
    const isAuthenticated = ref(!!token.value)
    const user = ref(null)

    // Инициализация токена авторизации
    if (token.value) {
        setAuthToken(token.value);
    }

    // Авторизация
    const login = async (username, password) => {
        try {
            const request = new UserAuthRequest(username, password);
            const response = await authClient.login(request);

            if (response && response.token) {
                setToken(response.token);
                await fetchCurrentUser();
                return true;
            }

            return false;
        } catch (error) {
            console.error('Ошибка при входе:', error.statusText || error.message);
            return false;
        }
    }

    // Регистрация
    const register = async (username, email, password) => {
        try {
            const request = new UserRegisterRequest(username, email, password);
            const response = await authClient.register(request);

            if (response && response.token) {
                setToken(response.token);
                await fetchCurrentUser();
                return true;
            }

            return false;
        } catch (error) {
            console.error('Ошибка при регистрации:', error.statusText || error.message);
            return false;
        }
    }

    // Получение текущего пользователя
    const fetchCurrentUser = async () => {
        try {
            // Убедимся, что токен установлен перед запросом
            if (token.value) {
                setAuthToken(token.value);
            }
            
            const userResponse = await userClient.getCurrentUserProfile();
            // Преобразуем UserResponse в обычный объект
            user.value = {
                id: userResponse.id,
                username: userResponse.username || '',
                email: userResponse.email || '',
                avatarUrl: userResponse.avatarUrl || '',
                bio: typeof userResponse.bio === 'object' ? '' : (userResponse.bio || ''),
                followerCount: userResponse.followerCount || 0,
                streamKey: userResponse.streamKey || ''
            };
            
            // Если ключ стрима не получен, запрашиваем его отдельно
            if (!user.value.streamKey) {
                await fetchStreamKey();
            }
        } catch (error) {
            console.error('Ошибка при загрузке профиля:', error);
            user.value = null;
            throw error;
        }
    }

    // Получение ключа стрима
    const fetchStreamKey = async () => {
        if (!user.value) return;
        
        try {
            const response = await userClient.getStreamKey();
            if (response && response.newStreamKey) {
                user.value.streamKey = response.newStreamKey;
            }
        } catch (error) {
            console.error('Ошибка при получении ключа стрима:', error.statusText || error.message);
        }
    }

    // Обновление профиля
    const updateCurrentUser = async (updateData) => {
        if (!user.value) throw new Error('Пользователь не загружен');

        try {
            // Убедимся, что токен установлен перед запросом
            if (token.value) {
                setAuthToken(token.value);
            }
            
            const dto = new UserUpdateRequest(updateData);
            const updatedUser = await userClient.updateUser(dto);
            // Преобразуем UserResponse в обычный объект
            user.value = {
                id: updatedUser.id,
                username: updatedUser.username || '',
                email: updatedUser.email || '',
                avatarUrl: updatedUser.avatarUrl || '',
                bio: updatedUser.bio || '',
                followerCount: updatedUser.followerCount || 0,
                streamKey: updatedUser.streamKey || user.value.streamKey || ''
            };
            return true;
        } catch (error) {
            console.error('Ошибка при обновлении профиля:', error);
            return false;
        }
    }

    const regenerateStreamKey = async () => {
        try {
            const response = await userClient.updateStreamKey();
            if (response && response.newStreamKey) {
                user.value.streamKey = response.newStreamKey;
                return response.newStreamKey;
            }
            return null;
        } catch (error) {
            console.error('Ошибка при обновлении ключа:', error.statusText || error.message);
            return null;
        }
    }

    // Выход из аккаунта
    const logout = () => {
        token.value = null;
        user.value = null;
        isAuthenticated.value = false;
        localStorage.removeItem('token');
        setAuthToken(null);
    }

    // Проверка аутентификации при старте
    const checkAuth = async () => {
        isAuthenticated.value = !!token.value;
        if (isAuthenticated.value && !user.value) {
            try {
                if (token.value) {
                    setAuthToken(token.value);
                }
                await fetchCurrentUser();
            } catch (e) {
                console.warn('Не удалось получить профиль', e);
                logout();
            }
        }
    }

    // Установка токена
    const setToken = (newToken) => {
        token.value = newToken;
        isAuthenticated.value = true;
        localStorage.setItem('token', newToken);
        setAuthToken(newToken);
    }

    return {
        token,
        isAuthenticated,
        user,
        login,
        register,
        logout,
        checkAuth,
        fetchCurrentUser,
        fetchStreamKey,
        updateCurrentUser,
        regenerateStreamKey,
        setToken
    }
})