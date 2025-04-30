<template>
  <v-container>
    <h1 class="text-h4 mb-4">Профиль пользователя</h1>
    
    <v-card>
      <v-card-item>
        <v-avatar size="100" class="mr-4">
          <v-img :src="userProfile.avatar || 'https://i.imgur.com/XzZDFqy.jpg'" cover></v-img>
        </v-avatar>
        <v-card-title>{{ userProfile.username }}</v-card-title>
        <v-card-subtitle>{{ userProfile.followersCount }} подписчиков</v-card-subtitle>
      </v-card-item>

      <v-tabs v-model="activeTab" align-tabs="start">
        <v-tab value="info">Информация профиля</v-tab>
        <v-tab value="security">Безопасность</v-tab>
        <v-tab value="stream">Настройки стрима</v-tab>
      </v-tabs>

      <v-card-text>
        <v-window v-model="activeTab">
          <v-window-item value="info">
            <v-form @submit.prevent="saveProfile">
              <v-text-field
                v-model="userProfile.username"
                label="Имя пользователя"
                required
                class="mb-4"
              ></v-text-field>
              
              <v-text-field
                v-model="userProfile.email"
                label="Email"
                type="email"
                required
                class="mb-4"
              ></v-text-field>
              
              <v-textarea
                v-model="userProfile.bio"
                label="О себе"
                rows="4"
                class="mb-4"
              ></v-textarea>
              
              <v-btn type="submit">Сохранить изменения</v-btn>
            </v-form>
          </v-window-item>

          <v-window-item value="security">
            <v-form @submit.prevent="changePassword">

              <v-text-field
                v-model="securityData.newPassword"
                label="Новый пароль"
                type="password"
                required
                class="mb-4"
              ></v-text-field>
              
              <v-text-field
                v-model="securityData.confirmPassword"
                label="Подтвердите пароль"
                type="password"
                required
                class="mb-4"
              ></v-text-field>
              
              <v-btn type="submit">Изменить пароль</v-btn>
            </v-form>
          </v-window-item>

          <v-window-item value="stream">
            <v-card variant="outlined" class="mb-4">
              <v-card-title>Ключ стрима</v-card-title>
              <v-card-text>
                <v-row>
                  <v-col cols="12" sm="8">
                    <v-text-field
                      v-model="streamData.streamKey"
                      readonly
                      variant="outlined"
                      density="compact"
                      hide-details
                    ></v-text-field>
                  </v-col>
                  <v-col cols="12" sm="4" class="d-flex gap-2">
                    <v-btn @click="copyStreamKey">
                      <v-icon start>mdi-content-copy</v-icon>
                      Копировать
                    </v-btn>
                    <v-btn color="error" @click="resetStreamKey">
                      <v-icon start>mdi-refresh</v-icon>
                      Сбросить
                    </v-btn>
                  </v-col>
                </v-row>
                <v-alert type="warning" class="mt-2">
                  Никогда не передавайте свой ключ стрима третьим лицам!
                </v-alert>
              </v-card-text>
            </v-card>

            <v-form @submit.prevent="saveStreamSettings">
              <v-text-field
                v-model="streamData.defaultTitle"
                label="Название стрима по умолчанию"
                class="mb-4"
              ></v-text-field>
              
              <v-select
                v-model="streamData.defaultCategory"
                :items="categories"
                label="Категория по умолчанию"
                class="mb-4"
              ></v-select>
              
              <v-btn type="submit">Сохранить настройки</v-btn>
            </v-form>
          </v-window-item>
        </v-window>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'

export default {
  name: 'ProfileView',
  setup() {
    const activeTab = ref('info')
    const userProfile = reactive({
      username: '',
      email: '',
      bio: '',
      avatar: '',
      followersCount: 0
    })
    
    const securityData = reactive({
      newPassword: '',
      confirmPassword: ''
    })
    
    const streamData = reactive({
      streamKey: '',
      defaultTitle: '',
      defaultCategory: ''
    })
    
    const categories = ref([])
    const apiUrl = 'http://localhost:8080/api'
    
    const fetchUserProfile = async () => {
      try {
        const response = await axios.get(`${apiUrl}/users/profile`)
        const userData = response.data
        userProfile.username = userData.username || ''
        userProfile.email = userData.email || ''
        userProfile.bio = userData.bio || ''
        userProfile.avatar = userData.avatar || 'https://i.imgur.com/vLOQdNY.jpg'
        userProfile.followersCount = userData.followersCount || 0
      } catch (error) {
        console.error('Ошибка при получении профиля пользователя:', error)
        // Заглушка с тестовыми данными при ошибке
        userProfile.username = 'streamMaster'
        userProfile.email = 'user@example.com'
        userProfile.bio = 'Я профессиональный стример и геймер. Стримлю каждый день в 20:00.'
        userProfile.avatar = 'https://i.imgur.com/vLOQdNY.jpg'
        userProfile.followersCount = 1024
      }
    }
    
    const fetchStreamKey = async () => {
      try {
        const response = await axios.get(`${apiUrl}/stream/key`)
        streamData.streamKey = response.data
      } catch (error) {
        console.error('Ошибка при получении ключа стрима:', error)
        // Заглушка при ошибке
        streamData.streamKey = 'live_12345678_AbCdEfGhIjKlMnOpQrStUvWxYz'
      }
    }
    
    const fetchStreamSettings = async () => {
      try {
        const response = await axios.get(`${apiUrl}/stream/settings`)
        streamData.defaultTitle = response.data.defaultTitle || ''
        streamData.defaultCategory = response.data.defaultCategory || ''
      } catch (error) {
        console.error('Ошибка при получении настроек стрима:', error)
        // Заглушка при ошибке
        streamData.defaultTitle = 'Ежедневный стрим'
        streamData.defaultCategory = 'Игры'
      }
    }
    
    const fetchCategories = async () => {
      try {
        const response = await axios.get(`${apiUrl}/categories`)
        categories.value = response.data.map(category => category.name)
      } catch (error) {
        console.error('Ошибка при получении категорий:', error)
        // Заглушка при ошибке
        categories.value = [
          'Игры', 'Разговорный стрим', 'Музыка', 'Творчество', 
          'Спорт', 'Наука и технологии', 'Программирование', 'Другое'
        ]
      }
    }
    
    const saveProfile = async () => {
      try {
        await axios.put(`${apiUrl}/users/profile`, userProfile)
        alert('Профиль сохранен')
      } catch (error) {
        console.error('Ошибка при сохранении профиля:', error)
        alert('Не удалось сохранить профиль')
      }
    }
    
    const changePassword = async () => {
      if (securityData.newPassword !== securityData.confirmPassword) {
        alert('Пароли не совпадают')
        return
      }
      
      try {
        await axios.post(`${apiUrl}/users/change-password`, securityData)
        alert('Пароль изменен')
        securityData.currentPassword = ''
        securityData.newPassword = ''
        securityData.confirmPassword = ''
      } catch (error) {
        console.error('Ошибка при изменении пароля:', error)
        alert('Не удалось изменить пароль')
      }
    }
    
    const copyStreamKey = () => {
      navigator.clipboard.writeText(streamData.streamKey)
        .then(() => alert('Ключ стрима скопирован в буфер обмена'))
        .catch(error => console.error('Ошибка при копировании ключа стрима:', error))
    }
    
    const resetStreamKey = async () => {
      if (confirm('Вы уверены, что хотите сбросить ключ стрима? Текущий стрим будет прерван.')) {
        try {
          const response = await axios.post(`${apiUrl}/stream/key/reset`)
          streamData.streamKey = response.data
          alert('Ключ стрима обновлен')
        } catch (error) {
          console.error('Ошибка при сбросе ключа стрима:', error)
          alert('Не удалось сбросить ключ стрима')
        }
      }
    }
    
    const saveStreamSettings = async () => {
      try {
        await axios.put(`${apiUrl}/stream/settings`, {
          defaultTitle: streamData.defaultTitle,
          defaultCategory: streamData.defaultCategory
        })
        alert('Настройки стрима сохранены')
      } catch (error) {
        console.error('Ошибка при сохранении настроек стрима:', error)
        alert('Не удалось сохранить настройки стрима')
      }
    }
    
    onMounted(async () => {
      await Promise.all([
        fetchUserProfile(),
        fetchStreamKey(),
        fetchStreamSettings(),
        fetchCategories()
      ])
    })
    
    return {
      activeTab,
      userProfile,
      securityData,
      streamData,
      categories,
      saveProfile,
      changePassword,
      copyStreamKey,
      resetStreamKey,
      saveStreamSettings
    }
  }
}
</script> 