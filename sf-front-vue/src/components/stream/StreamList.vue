<script setup>
import StreamCard from './StreamCard.vue'
import { computed } from 'vue'

const props = defineProps({
  streams: {
    type: Array,
    required: true,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  emptyMessage: {
    type: String,
    default: 'Стримы не найдены'
  },
  isSearchResults: {
    type: Boolean,
    default: false
  }
})

const isSearchResults = computed(() => {
  return props.emptyMessage.includes('запросу');
})

defineEmits(['stream-click'])
</script>

<template>
  <div class="stream-list-container">
    <v-progress-linear v-if="loading" indeterminate color="primary" class="mb-4"></v-progress-linear>

    <v-row v-if="streams.length > 0">
      <v-col v-for="stream in streams" 
             :key="stream.id" 
             cols="12" sm="6" md="4" lg="3">
        <stream-card 
          :stream="stream" 
          @click="$emit('stream-click', stream.id)" 
        />
      </v-col>
    </v-row>
    
    <div v-else class="empty-state-container">
      <v-card variant="outlined" class="text-center py-8 empty-state-card">
        <v-card-text>
          <v-icon size="64" color="grey" class="mb-4">
            {{ isSearchResults ? 'mdi-magnify-close' : 'mdi-television-off' }}
          </v-icon>
          <h3 class="text-h6 mb-2">
            {{ isSearchResults ? 'Поиск не дал результатов' : 'Стримов не найдено' }}
          </h3>
          <p class="text-body-1">
            {{ emptyMessage }}
          </p>
        </v-card-text>
      </v-card>
    </div>
  </div>
</template>

<style scoped>
.stream-list-container {
  display: flex;
  flex-direction: column;
  flex-grow: 1;
}

.empty-state-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  flex-grow: 1;
}

.empty-state-card {
  max-width: 500px;
  width: 100%;
}
</style> 