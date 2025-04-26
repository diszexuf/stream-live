<template>
  <v-container>
    <v-row>
      <v-col cols="12" md="8" offset-md="2">
        <v-card>
          <v-card-title class="text-h4 mb-4">Начать стрим</v-card-title>
          
          <v-card-text v-if="!userStore.isAuthenticated">
            <v-alert type="warning">
              Для создания стрима необходимо авторизоваться
            </v-alert>
            <v-btn color="primary" block class="mt-4" to="/login">
              Войти в аккаунт
            </v-btn>
          </v-card-text>
          
          <v-card-text v-else>
            <v-form @submit.prevent="startStream">
              <v-text-field
                v-model="streamTitle"
                label="Название стрима"
                required
                :rules="[v => !!v || 'Введите название стрима']"
                class="mb-4"
              ></v-text-field>
              
              <v-select
                v-model="selectedCategoryId"
                :items="categories"
                item-title="name"
                item-value="id"
                label="Категория"
                required
                :rules="[v => !!v || 'Выберите категорию']"
                class="mb-4"
              ></v-select>
              
              <v-textarea
                v-model="streamDescription"
                label="Описание"
                rows="3"
                class="mb-4"
              ></v-textarea>
              
              <v-switch
                v-model="isPrivate"
                label="Приватный стрим"
                class="mb-4"
              ></v-switch>
              
              <v-btn
                color="primary"
                type="submit"
                block
                :loading="isLoading"
              >
                Начать стрим
              </v-btn>
            </v-form>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { useUserStore } from '@/stores/user'

export default {
  name: 'CreateStreamView',
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    const streamTitle = ref('')
    const selectedCategoryId = ref(null)
    const streamDescription = ref('')
    const isPrivate = ref(false)
    const isLoading = ref(false)
    const categories = ref([])
    const apiUrl = 'http://localhost:8080/api'
    
    const fetchCategories = async () => {
      try {
        const response = await axios.get(`${apiUrl}/categories`)
        categories.value = response.data
      } catch (error) {
        console.error('Ошибка при получении категорий:', error)
        alert('Не удалось загрузить категории. Пожалуйста, обновите страницу.')
      }
    }
    
    const startStream = async () => {
      if (!streamTitle.value || !selectedCategoryId.value) {
        alert('Пожалуйста, заполните все обязательные поля')
        return
      }
      
      isLoading.value = true
      try {
        if (!userStore.user?.id) {
          await userStore.fetchUser()
          if (!userStore.user?.id) {
            throw new Error('Пользователь не авторизован')
          }
        }
        
        const response = await axios.post(`${apiUrl}/streams`, {
          title: streamTitle.value,
          description: streamDescription.value,
          isPrivate: isPrivate.value
        }, {
          params: {
            userId: userStore.user.id,
            categoryId: selectedCategoryId.value
          }
        })
        
        if (response.data && response.data.id) {
          await axios.post(`${apiUrl}/streams/${response.data.id}/start`)
          router.push(`/stream/${response.data.id}`)
        } else {
          throw new Error('Неверный ответ от сервера')
        }
      } catch (error) {
        console.error('Ошибка при создании стрима:', error)
        alert('Не удалось создать стрим. Пожалуйста, попробуйте еще раз.')
      } finally {
        isLoading.value = false
      }
    }
    
    onMounted(async () => {
      if (!userStore.user) {
        await userStore.fetchUser()
      }
      await fetchCategories()
    })
    
    return {
      streamTitle,
      selectedCategoryId,
      streamDescription,
      isPrivate,
      isLoading,
      categories,
      startStream,
      userStore
    }
  }
}
</script>
