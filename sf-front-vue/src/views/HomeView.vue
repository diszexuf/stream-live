<template>
  <v-container>
    <v-row class="mb-4">
      <v-col cols="12" class="d-flex align-center">
        <h1 class="text-h4">
          <template v-if="searchQuery">
            Результаты поиска: "{{ searchQuery }}"
          </template>
          <template v-else>
            {{ showOnlyLive ? 'Активные стримы' : 'Все стримы' }}
          </template>
        </h1>
        
        <v-spacer></v-spacer>
        
        <v-btn-toggle v-if="!searchQuery" v-model="showOnlyLive" mandatory @change="toggleLiveStreams">
          <v-btn :value="true" color="success">
            <v-icon start>mdi-access-point</v-icon>
            Активные
          </v-btn>
          <v-btn :value="false" color="primary">
            <v-icon start>mdi-format-list-bulleted</v-icon>
            Все
          </v-btn>
        </v-btn-toggle>
        
        <v-btn v-else color="primary" variant="text" @click="clearSearch">
          <v-icon start>mdi-arrow-left</v-icon>
          Назад к стримам
        </v-btn>
      </v-col>
    </v-row>

    <v-progress-linear v-if="isLoading" indeterminate color="primary" class="mb-4"></v-progress-linear>

    <stream-list
      :streams="displayedStreams"
      :loading="isLoading"
      :empty-message="getEmptyMessage()"
      @stream-click="navigateToStream"
    />
  </v-container>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStreamStore } from '@/stores/stream'
import StreamList from '@/components/stream/StreamList.vue'

const router = useRouter()
const route = useRoute()
const streamStore = useStreamStore()

const showOnlyLive = ref(true)
const isLoading = ref(false)
const searchResults = ref([])
const searchQuery = ref('')

watch(() => route.query.search, async (newQuery) => {
  searchQuery.value = newQuery || ''
  if (searchQuery.value) {
    await performSearch()
  }
}, { immediate: true })

const displayedStreams = computed(() => {
  if (searchQuery.value) {
    return searchResults.value
  }
  return showOnlyLive.value ? streamStore.liveStreams : streamStore.allStreams
})

// Загрузка стримов
const loadStreams = async () => {
  isLoading.value = true

  try {
    if (showOnlyLive.value) {
      await streamStore.fetchLiveStreams()
    } else {
      await streamStore.fetchAllStreams()
    }
  } catch (error) {
    console.error('Ошибка при загрузке стримов:', error)
  } finally {
    isLoading.value = false
  }
}

const performSearch = async () => {
  if (!searchQuery.value) return
  
  isLoading.value = true
  try {
    searchResults.value = await streamStore.searchStreams(searchQuery.value)
  } catch (error) {
    console.error('Ошибка при поиске стримов:', error)
    searchResults.value = []
  } finally {
    isLoading.value = false
  }
}

const clearSearch = () => {
  router.replace({ path: '/' })
  searchQuery.value = ''
  searchResults.value = []
}

// Переключение между всеми и активными стримами
const toggleLiveStreams = async () => {
  await loadStreams()
}

const navigateToStream = (streamId) => {
  router.push(`/stream/${streamId}`)
}

const getEmptyMessage = () => {
  if (searchQuery.value) {
    return `По запросу "${searchQuery.value}" ничего не найдено`
  }
  return showOnlyLive.value ? 'В данный момент нет активных стримов' : 'Стримы не найдены'
}

onMounted(async () => {
  if (!searchQuery.value) {
    await loadStreams()
  }
})
</script>

<style scoped>
.stream-card {
  transition: transform 0.2s;
  cursor: pointer;
}

.stream-card:hover {
  transform: translateY(-4px);
}
</style>