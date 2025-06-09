import { defineStore } from 'pinia';
import { ref } from 'vue';
import { AuthApi, UserAuthRequest, UserRegisterRequest, UsersApi } from "@/api/src/index.js";
import { callProtectedApi, setAuthToken } from './authHelpers';

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || null);
    const isAuthenticated = ref(!!token.value);
    const user = ref(null);
    const authService = new AuthApi();
    const usersService = new UsersApi();

    async function login(username, password) {
        try {
            const authRequest = new UserAuthRequest(username, password);
            const response = await authService.loginUser(authRequest);

            token.value = response.token;
            isAuthenticated.value = true;
            localStorage.setItem('token', response.token);
            setAuthToken(response.token);

            await fetchCurrentUser();
            return true;
        } catch (error) {
            console.error('Ошибка при входе:', error.message);
            if (error.response?.status === 401 || error.response?.status === 403) {
                throw new Error('Неверное имя пользователя или пароль');
            }
            throw new Error('Ошибка при входе в систему');
        }
    }

    async function register(username, email, password) {
        try {
            const registerRequest = new UserRegisterRequest(username, email, password);
            const response = await authService.registerUser(registerRequest);

            token.value = response.token;
            isAuthenticated.value = true;
            localStorage.setItem('token', response.token);
            setAuthToken(response.token);

            await fetchCurrentUser();
            return true;
        } catch (error) {
            console.error('Ошибка при регистрации:', error.message);
            throw new Error(error.response?.status === 409 ? 'Username or email already exists' : 'Registration failed');
        }
    }

    async function fetchCurrentUser() {
        try {
            const userResponse = await callProtectedApi(() => usersService.getCurrentUserProfile());

            user.value = {
                id: userResponse.id,
                username: userResponse.username || '',
                email: userResponse.email || '',
                avatarUrl: userResponse.avatarUrl || '',
                bio: typeof userResponse.bio === 'object' ? '' : (userResponse.bio || ''),
                followerCount: userResponse.followerCount || 0,
                streamKey: userResponse.streamKey || '',
            };
        } catch (error) {
            console.error('Ошибка при загрузке профиля:', error.message);
            user.value = null;
            throw new Error(error.response?.status === 401 ? 'Unauthorized' : 'Failed to fetch user profile');
        }
    }

    async function updateCurrentUser(formData) {
        if (!user.value) {
            throw new Error('Пользователь не авторизован');
        }

        try {
            const opts = {
                email: formData.get('email'),
                avatarUrl: formData.get('avatarUrl'),
                bio: formData.get('bio')
            };

            const response = await callProtectedApi(() => usersService.updateUser(opts));
            await fetchCurrentUser();
            return true;
        } catch (error) {
            console.error('Ошибка при обновлении профиля:', error.message);
            throw new Error(error.response?.status === 400 ? 'Invalid input' : 'Profile update failed');
        }
    }

    async function regenerateStreamKey() {
        try {
            const response = await callProtectedApi(() => usersService.regenerateCurrentUserStreamKey());

            if (response?.newStreamKey) {
                user.value.streamKey = response.newStreamKey;
                return response.newStreamKey;
            } else {
                throw new Error('Stream key not provided in response');
            }
        } catch (error) {
            console.error('Ошибка при обновлении ключа стрима:', error.message);
            throw new Error('Failed to regenerate stream key');
        }
    }

    function logout() {
        token.value = null;
        user.value = null;
        isAuthenticated.value = false;
        localStorage.removeItem('token');

        setAuthToken(null);
    }

    async function checkAuth() {
        if (isAuthenticated.value && !user.value) {
            try {
                await fetchCurrentUser();
            } catch (error) {
                console.warn('Не удалось проверить авторизацию:', error.message);
                logout();
            }
        }
    }

    async function checkFieldAvailability(fieldType, value) {
        if (!value) return true;

        try {
            let response;
            switch (fieldType) {
                case 'username':
                    response = await usersService.checkUsernameAvailability(value);
                    break;
                case 'email':
                    response = await usersService.checkEmailAvailability(value);
                    break;
                default:
                    console.error('Unknown field type');
            }
            return response;
        } catch (error) {
            console.error(`Ошибка при проверке ${fieldType}:`, error.message);
            return false;
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
        updateCurrentUser,
        regenerateStreamKey,
        checkFieldAvailability,
    };
});
