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
        console.log('updateCurrentUser: Начало обновления профиля');
        console.log('Данные для обновления:', updateData);

        try {
            // Убедимся, что токен установлен перед запросом
            if (token.value) {
                setAuthToken(token.value);
                console.log('updateCurrentUser: Токен авторизации установлен');
            } else {
                console.warn('updateCurrentUser: Токен авторизации отсутствует');
            }
            
            const dto = new UserUpdateRequest(updateData);
            console.log('updateCurrentUser: Создан объект UserUpdateRequest');
            
            try {
                console.log('updateCurrentUser: Вызов userClient.updateUser');
                await userClient.updateUser(dto);
                console.log('updateCurrentUser: Запрос на обновление профиля выполнен успешно');
                
                // Обновляем локальные поля сразу, чтобы пользователь видел изменения
                console.log('updateCurrentUser: Обновление локальных полей');
                if (updateData.email) user.value.email = updateData.email;
                if (updateData.avatarUrl) user.value.avatarUrl = updateData.avatarUrl;
                if (updateData.bio !== undefined) user.value.bio = updateData.bio;
                
                // После успешного обновления получаем актуальные данные пользователя
                try {
                    console.log('updateCurrentUser: Получение актуальных данных пользователя');
                    await fetchCurrentUser();
                    console.log('updateCurrentUser: Актуальные данные пользователя получены');
                } catch (fetchError) {
                    console.error('updateCurrentUser: Ошибка при получении актуальных данных:', fetchError);
                    // Не перебрасываем эту ошибку, так как основная операция уже выполнена успешно
                    // и локальные данные уже обновлены
                }
                
                console.log('updateCurrentUser: Обновление профиля завершено успешно');
                return true;
            } catch (updateError) {
                // Проверяем, не связана ли ошибка с пустым ответом JSON
                if (updateError instanceof SyntaxError && 
                    updateError.message.includes('Unexpected end of JSON input')) {
                    
                    console.log('updateCurrentUser: Получен пустой ответ от сервера, но это нормально');
                    
                    // Обновляем локальные поля, так как запрос скорее всего выполнился успешно
                    if (updateData.email) user.value.email = updateData.email;
                    if (updateData.avatarUrl) user.value.avatarUrl = updateData.avatarUrl;
                    if (updateData.bio !== undefined) user.value.bio = updateData.bio;
                    
                    // Пытаемся получить актуальные данные
                    try {
                        await fetchCurrentUser();
                    } catch (fetchError) {
                        console.error('updateCurrentUser: Ошибка при получении актуальных данных после пустого ответа:', fetchError);
                        // Игнорируем эту ошибку
                    }
                    
                    return true; // Считаем операцию успешной
                }
                
                console.error('updateCurrentUser: Ошибка при вызове userClient.updateUser:', updateError);
                throw updateError; // Перебрасываем ошибку дальше
            }
        } catch (error) {
            console.error('updateCurrentUser: Критическая ошибка при обновлении профиля:', error);
            // Добавляем больше информации об ошибке
            if (error.status) {
                console.error(`Код ошибки: ${error.status}, Сообщение: ${error.statusText || error.message}`);
            }
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
        // Импортируем функцию setAuthToken из API и очищаем токен
        try {
            const { setAuthToken } = require('@/api/manual');
            setAuthToken(null);
            console.log('logout: Токен успешно очищен');
        } catch (error) {
            console.error('logout: Ошибка при очистке токена:', error);
        }
        
        token.value = null;
        user.value = null;
        isAuthenticated.value = false;
        localStorage.removeItem('token');
    }

    // Проверка аутентификации при старте
    const checkAuth = async () => {
        isAuthenticated.value = !!token.value;
        if (isAuthenticated.value && !user.value) {
            try {
                if (token.value) {
                    // Импортируем функцию setAuthToken из API и устанавливаем токен
                    try {
                        const { setAuthToken } = require('@/api/manual');
                        setAuthToken(token.value);
                        console.log('checkAuth: Токен успешно установлен');
                    } catch (error) {
                        console.error('checkAuth: Ошибка при установке токена:', error);
                    }
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
        if (!newToken) {
            console.error('setToken: Попытка установить пустой токен');
            return;
        }
        
        token.value = newToken;
        isAuthenticated.value = true;
        localStorage.setItem('token', newToken);
        
        // Импортируем функцию setAuthToken из API и устанавливаем токен
        try {
            const { setAuthToken } = require('@/api/manual');
            setAuthToken(newToken);
            console.log('setToken: Токен успешно установлен');
        } catch (error) {
            console.error('setToken: Ошибка при установке токена:', error);
        }
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