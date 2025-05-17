<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStreamStore } from '@/stores/stream'
import StreamList from '@/components/stream/StreamList.vue'

const router = useRouter()
const route = useRoute()
const streamStore = useStreamStore()

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
  return streamStore.liveStreams
})

// Загрузка стримов
const loadStreams = async () => {
  isLoading.value = true

  try {
    await streamStore.fetchLiveStreams()
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

const navigateToStream = (streamId) => {
  console.log("STREAMID", streamId)
  router.push(`/stream/${streamId}`)
}

const getEmptyMessage = () => {
  if (searchQuery.value) {
    return `По запросу "${searchQuery.value}" ничего не найдено`
  }
  return 'В данный момент нет активных стримов'
}

onMounted(async () => {
  if (!searchQuery.value) {
    await loadStreams()
  }
})
</script>

<template>
  <v-container class="home-container">
    <v-row class="mb-4 mt-0 header-row">
      <v-col cols="12">
        <h1 class="text-h4">
          <template v-if="searchQuery">
            Результаты поиска: "{{ searchQuery }}"
          </template>
          <template v-else>
            Активные стримы
          </template>
        </h1>
        
        <v-spacer></v-spacer>
        
        <v-btn v-if="searchQuery" color="primary" variant="text" @click="clearSearch">
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

<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  min-height: 80vh;
}

.header-row {
  flex-shrink: 0;
}

.stream-card {
  transition: transform 0.2s;
  cursor: pointer;
}

.stream-card:hover {
  transform: translateY(-4px);
}
</style>