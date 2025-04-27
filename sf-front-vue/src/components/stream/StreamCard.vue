<template>
  <v-card @click="$emit('click')" hover>
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
      <v-chip
        v-if="stream.live"
        color="error"
        size="small"
        class="ma-2"
      >
        LIVE
      </v-chip>
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
        <v-col cols="auto" class="ml-4" v-if="stream.category">
          <v-icon start size="small">mdi-tag</v-icon>
          {{ stream.category.name }}
        </v-col>
      </v-row>
    </v-card-subtitle>
  </v-card>
</template>

<script setup>
defineProps({
  stream: {
    type: Object,
    required: true,
    validator: (value) => {
      return value.title && value.user && typeof value.live === 'boolean'
    }
  }
})

defineEmits(['click'])
</script> 