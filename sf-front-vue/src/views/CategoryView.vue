<template>
  <v-container>
    <v-row>
      <v-col v-if="category" cols="12">
        <v-card class="mb-6">
          <v-row no-gutters>
            <v-col cols="12" sm="4" md="3">
              <v-img
                :src="category.thumbnail"
                aspect-ratio="16/9"
                cover
                width="100%"
                height="100%"
              ></v-img>
            </v-col>
            <v-col cols="12" sm="8" md="9">
              <v-card-title class="text-h4">{{ category.name }}</v-card-title>
              <v-card-subtitle class="mb-2">
                <v-icon start>mdi-eye</v-icon>
                {{ category.viewersCount }} зрителей
              </v-card-subtitle>
              <v-card-text>
                <p>{{ category.description }}</p>
              </v-card-text>
            </v-col>
          </v-row>
        </v-card>
        
        <v-card>
          <v-card-title class="text-h5">Активные стримы</v-card-title>
          <v-card-text>
            <v-row>
              <v-col v-for="stream in streams" :key="stream.id" cols="12" sm="6" md="4" lg="3">
                <v-card @click="navigateToStream(stream.id)" hover>
                  <v-img
                    :src="stream.thumbnail"
                    aspect-ratio="16/9"
                    cover
                  >
                    <template v-slot:placeholder>
                      <v-row class="fill-height ma-0" align="center" justify="center">
                        <v-progress-circular indeterminate color="primary"></v-progress-circular>
                      </v-row>
                    </template>
                  </v-img>
                  
                  <v-card-title class="text-subtitle-1">{{ stream.title }}</v-card-title>
                  <v-card-subtitle>
                    <v-row align="center" no-gutters>
                      <v-col cols="auto">
                        <v-avatar size="24" class="mr-2">
                          <v-img :src="stream.user.avatar" cover></v-img>
                        </v-avatar>
                        {{ stream.user.username }}
                      </v-col>
                      <v-col cols="auto" class="ml-4">
                        <v-icon start size="small">mdi-eye</v-icon>
                        {{ stream.viewersCount }}
                      </v-col>
                    </v-row>
                  </v-card-subtitle>
                </v-card>
              </v-col>
              
              <v-col v-if="streams.length === 0" cols="12">
                <v-alert type="info">
                  В данной категории пока нет активных стримов.
                </v-alert>
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>
      </v-col>
      
      <v-col v-else cols="12">
        <v-alert type="error">
          Категория не найдена
        </v-alert>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'

export default {
  name: 'CategoryView',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const category = ref(null)
    const streams = ref([])
    const apiUrl = 'http://localhost:8080/api'
    
    const fetchCategory = async (id) => {
      try {
        const response = await axios.get(`${apiUrl}/categories/${id}`)
        category.value = response.data
      } catch (error) {
        console.error('Ошибка при получении категории:', error)
        category.value = null
      }
    }
    
    const fetchStreams = async (categoryId) => {
      try {
        const response = await axios.get(`${apiUrl}/streams/category/${categoryId}`)
        streams.value = response.data
      } catch (error) {
        console.error('Ошибка при получении стримов категории:', error)
        streams.value = []
      }
    }
    
    const navigateToStream = (streamId) => {
      router.push(`/stream/${streamId}`)
    }
    
    onMounted(async () => {
      const categoryId = route.params.id
      await fetchCategory(categoryId)
      await fetchStreams(categoryId)
    })
    
    return {
      category,
      streams,
      navigateToStream
    }
  }
}
</script> 