<template>
  <v-container>
    <v-row>
      <v-col cols="12">
        <v-card>
          <v-card-item>
            <v-card-title class="text-h4">Популярные категории</v-card-title>
          </v-card-item>
          
          <v-card-text>
            <v-row>
              <v-col v-for="category in categories" :key="category.id" cols="12" sm="6" md="4" lg="3">
                <v-card @click="navigateToCategory(category.id)" hover>
                  <v-img
                    :src="category.thumbnail"
                    aspect-ratio="16/9"
                    cover
                  >
                    <template v-slot:placeholder>
                      <v-row class="fill-height ma-0" align="center" justify="center">
                        <v-progress-circular indeterminate color="primary"></v-progress-circular>
                      </v-row>
                    </template>
                  </v-img>
                  
                  <v-card-title class="text-subtitle-1">{{ category.name }}</v-card-title>
                  <v-card-subtitle>
                    <v-icon start size="small">mdi-eye</v-icon>
                    {{ category.viewersCount }} зрителей
                  </v-card-subtitle>
                </v-card>
              </v-col>
              
              <v-col v-if="categories.length === 0" cols="12">
                <v-alert type="info">
                  Категории не найдены. Пожалуйста, обновите страницу.
                </v-alert>
              </v-col>
            </v-row>
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

export default {
  name: 'HomeView',
  setup() {
    const router = useRouter()
    const categories = ref([])
    const apiUrl = 'http://localhost:8080/api'
    
    const fetchCategories = async () => {
      try {
        const response = await axios.get(`${apiUrl}/categories`)
        categories.value = response.data
      } catch (error) {
        console.error('Ошибка при получении категорий:', error)
        categories.value = []
        alert('Не удалось загрузить категории. Пожалуйста, обновите страницу.')
      }
    }
    
    const navigateToCategory = (categoryId) => {
      router.push(`/category/${categoryId}`)
    }
    
    const navigateToStream = (streamId) => {
      router.push(`/stream/${streamId}`)
    }
    
    onMounted(async () => {
      await fetchCategories()
    })
    
    return {
      categories,
      navigateToCategory,
      navigateToStream
    }
  }
}
</script> 