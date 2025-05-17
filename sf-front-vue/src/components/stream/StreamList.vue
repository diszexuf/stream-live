<template>
  <div>
    <v-progress-linear v-if="loading" indeterminate color="primary" class="mb-4"></v-progress-linear>

    <v-row>
      <v-col v-for="stream in streams" 
             :key="stream.id" 
             cols="12" sm="6" md="4" lg="3">
        <stream-card 
          :stream="stream" 
          @click="$emit('stream-click', stream.id)" 
        />
      </v-col>
      
      <v-col v-if="streams.length === 0" cols="12">
        <v-card variant="outlined" class="text-center py-8">
          <v-card-text>
            <v-icon size="64" color="grey" class="mb-4">mdi-television-off</v-icon>
            <h3 class="text-h6 mb-2">Стримов не найдено</h3>
            <p class="text-body-1">
              {{ emptyMessage }}
            </p>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </div>
</template>

<script setup>
import StreamCard from './StreamCard.vue'

defineProps({
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
  }
})

defineEmits(['stream-click'])
</script> 