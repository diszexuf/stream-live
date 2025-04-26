import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'

export const useUserStore = defineStore('user', () => {
  const user = ref(null)
  const isAuthenticated = ref(false)
  const apiUrl = 'http://localhost:8080/api'

  // Загрузка данных пользователя с сервера
  const fetchUser = async () => {
    try {
      const response = await axios.get(`${apiUrl}/users/profile`)
      if (response.data) {
        user.value = response.data
        isAuthenticated.value = true
      }
    } catch (error) {
      console.error('Ошибка при получении профиля пользователя:', error)
      user.value = null
      isAuthenticated.value = false
    }
  }

  // Авторизация пользователя
  const login = async (username, password) => {
    try {
      const response = await axios.post(`${apiUrl}/users/login`, null, {
        params: { username, password }
      })
      
      if (response.data) {
        user.value = response.data
        isAuthenticated.value = true
        return true
      }
      return false
    } catch (error) {
      console.error('Ошибка при авторизации:', error)
      return false
    }
  }

  // Выход из системы
  const logout = () => {
    user.value = null
    isAuthenticated.value = false
  }

  return {
    user,
    isAuthenticated,
    fetchUser,
    login,
    logout
  }
}) 