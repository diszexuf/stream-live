<script setup>
import { ref, onMounted } from 'vue';
import { UsersApi } from '@/api/src/index.js';

const props = defineProps({
  stream: {
    type: Object,
    required: true
  }
});

defineEmits(['click']);

const user = ref(null);
const usersService = new UsersApi();

onMounted(async () => {
  try {
    const data = await usersService.getUserById(props.stream.userId);
    user.value = data;
  } catch (error) {
    console.error('Ошибка при загрузке пользователя:', error);
  }
});
</script>

<template>
  <v-card @click="$emit('click')" hover class="stream-card h-100">
    <v-img
        :src="stream.thumbnailUrl"
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
          <v-img
              v-if="user?.avatarUrl"
              :src="user.avatarUrl"
          ></v-img>
          <v-icon v-else>mdi-account-circle</v-icon>
        </v-avatar>
        <span class="text-subtitle-2">{{ user?.username || 'Пользователь' }}</span>
      </div>

      <p class="text-caption text-truncate">{{ stream.description || '' }}</p>

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