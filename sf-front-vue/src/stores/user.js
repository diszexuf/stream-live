import {defineStore} from 'pinia'
import {ref} from 'vue'

// Импортируем сгенерированные клиенты
import AuthApi from '@/../src/api/src/api/AuthApi.js'
import UsersApi from '@/../src/api/src/api/UsersApi.js'

// DTO модели
import UserAuthRequest from '@/../src/api/src/model/UserAuthRequest.js'
import UserRegisterRequest from '@/../src/api/src/model/UserRegisterRequest.js'
import UserUpdateRequest from '@/../src/api/src/model/UserUpdateRequest.js'

export const useUserStore = defineStore('user', () => {
    // Стор
    const token = ref(localStorage.getItem('token') || null)
    const isAuthenticated = ref(!!token.value)
    const user = ref(null)

    // Клиенты
    const authApi = new AuthApi()
    const usersApi = new UsersApi()

    // Авторизация
    const login = async (username, password) => {
        try {
            const request = new UserAuthRequest(username, password)
            const response = await authApi.loginUser(request)

            if (response && response.token) {
                setToken(response.token)
                await fetchCurrentUser()
                return true
            }

            return false
        } catch (error) {
            console.error('Ошибка при входе:', error.response?.text || error.message)
            return false
        }
    }

    // Регистрация
    const register = async (username, email, password) => {
        try {
            const request = new UserRegisterRequest(username, email, password)
            const response = await authApi.registerUser(request)

            if (response && response.token) {
                setToken(response.token)
                await fetchCurrentUser()
                return true
            }

            return false
        } catch (error) {
            console.error('Ошибка при регистрации:', error.response?.text || error.message)
            return false
        }
    }

    // Получение текущего пользователя
    const fetchCurrentUser = async () => {
        try {
            user.value = await usersApi.getCurrentUserProfile()
        } catch (error) {
            console.error('Ошибка при загрузке профиля:', error.response?.text || error.message)
            user.value = null
            throw error
        }
    }

    // Обновление профиля
    const updateCurrentUser = async (updateData) => {
        if (!user.value || !user.value.id) throw new Error('Пользователь не загружен')

        try {
            const dto = new UserUpdateRequest(updateData)
            user.value = await usersApi.updateUser(user.value.id, dto)
            return true
        } catch (error) {
            console.error('Ошибка при обновлении профиля:', error.response?.text || error.message)
            return false
        }
    }

    const regenerateStreamKey = async () => {
        try {
            const response = await usersApi.regenerateCurrentUserStreamKey()
            if (response && response.newStreamKey) {
                user.value.streamKey = response.newStreamKey
                return response.newStreamKey
            }
            return null
        } catch (error) {
            console.error('Ошибка при обновлении ключа:', error.response?.text || error.message)
            return null
        }
    }

    // Выход из аккаунта
    const logout = () => {
        token.value = null
        user.value = null
        isAuthenticated.value = false
        localStorage.removeItem('token')
    }

    // Проверка аутентификации при старте
    const checkAuth = async () => {
        isAuthenticated.value = !!token.value
        if (isAuthenticated.value && !user.value) {
            try {
                await fetchCurrentUser()
            } catch (e) {
                console.warn('Не удалось получить профиль', e)
                logout()
            }
        }
    }

    // Установка токена
    const setToken = (newToken) => {
        token.value = newToken
        isAuthenticated.value = true
        localStorage.setItem('token', newToken)
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
        setToken
    }
})