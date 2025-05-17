<script setup>
import { toRaw } from 'vue';

const props = defineProps({
  stream: {
    type: Object,
    required: true
  }
})

defineEmits(['click'])

const getImageUrl = (stream) => {
  const rawStream = toRaw(stream);
  
  // Проверяем, есть ли thumbnailUrl и является ли он строкой
  if (rawStream.thumbnailUrl && typeof rawStream.thumbnailUrl === 'string') {
    return rawStream.thumbnailUrl;
  }
  
  // Проверяем, является ли thumbnailUrl объектом с полем url
  if (rawStream.thumbnailUrl && typeof rawStream.thumbnailUrl === 'object' && rawStream.thumbnailUrl !== null) {
    if (rawStream.thumbnailUrl.url && typeof rawStream.thumbnailUrl.url === 'string') {
      return rawStream.thumbnailUrl.url;
    }
    
    // Проверяем другие возможные поля
    const possibleFields = ['path', 'src', 'href', 'link'];
    for (const field of possibleFields) {
      if (rawStream.thumbnailUrl[field] && typeof rawStream.thumbnailUrl[field] === 'string') {
        return rawStream.thumbnailUrl[field];
      }
    }
  }

  return `https://picsum.photos/seed/${rawStream.id || Math.random()}/300/200`;
}

// Форматирование даты
const formatDate = (dateString) => {
  if (!dateString) return 'Нет данных';

  // Проверяем, не является ли dateString объектом
  if (typeof dateString === 'object' && dateString !== null) {
    // Проверяем, есть ли в объекте поле present
    if (dateString.present === true) {
      return 'Сейчас';
    }
    return 'Нет данных';
  }

  try {
    const date = new Date(dateString);
    // Проверяем валидность даты
    if (isNaN(date.getTime())) {
      return 'Нет данных';
    }

    return date.toLocaleString('ru-RU', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch (error) {
    return 'Нет данных';
  }
}
</script>

<template>
  <v-card @click="$emit('click')" hover class="stream-card h-100">
    <v-img
      :src="`https://i.pravatar.cc/150?u=${stream.userId + 10}`"
      height="200"
      cover
      class="align-end"
    >
      <v-chip
        v-if="stream.isLive"
        color="error"
        size="small"
        class="ma-2"
      >
        <v-icon start size="x-small">mdi-access-point</v-icon>
        В ЭФИРЕ
      </v-chip>
    </v-img>
    
    <v-card-title class="text-truncate">{{ stream.title }}</v-card-title>
    
    <v-card-text>
      <div class="d-flex align-center mb-2">
        <v-avatar size="32" class="mr-2">
          <v-img :src="`https://i.pravatar.cc/150?u=${stream.userId}`"></v-img>
        </v-avatar>
        <span class="text-subtitle-2">{{ stream.userName || 'Пользователь' }}</span>
      </div>
      
      <p class="text-caption text-truncate">{{ stream.descriptions || '' }}</p>
      
      <div class="d-flex align-center mt-2">
        <v-icon size="small" color="red" class="mr-1">mdi-eye</v-icon>
        <span class="text-caption mr-3">{{ stream.viewerCount || 0 }}</span>
      </div>
    </v-card-text>
  </v-card>
</template>


<style scoped>
.stream-card {
  transition: transform 0.2s;
  cursor: pointer;
}

.stream-card:hover {
  transform: translateY(-4px);
}
</style> 