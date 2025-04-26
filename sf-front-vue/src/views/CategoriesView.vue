<template>
  <v-container>
    <h1 class="text-h4 mb-4">Категории</h1>
    
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
            {{ category.viewers }} зрителей
          </v-card-subtitle>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

export default {
  name: 'CategoriesView',
  setup() {
    const router = useRouter()
    const categories = ref([])
    
    const fetchCategories = async () => {
      // В реальном приложении здесь будет API запрос
      categories.value = [
        {
          id: 1,
          name: 'Игры',
          thumbnail: 'https://i.imgur.com/YS9T0MN.jpg',
          viewers: 15000
        },
        {
          id: 2,
          name: 'Программирование',
          thumbnail: 'https://i.imgur.com/M9RF0QA.jpg',
          viewers: 8000
        },
        {
          id: 3,
          name: 'Музыка',
          thumbnail: 'https://i.imgur.com/BSXPAWN.jpg',
          viewers: 12000
        },
        {
          id: 4,
          name: 'Творчество',
          thumbnail: 'https://i.imgur.com/iFIKMsC.jpg',
          viewers: 5000
        }
      ]
    }
    
    const navigateToCategory = (categoryId) => {
      router.push(`/category/${categoryId}`)
    }
    
    onMounted(() => {
      fetchCategories()
    })
    
    return {
      categories,
      navigateToCategory
    }
  }
}
</script> 