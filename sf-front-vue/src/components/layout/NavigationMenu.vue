<script setup>
import { useStreamStore } from '@/stores/stream'
import { computed } from 'vue'

const streamStore = useStreamStore()

defineProps({
  isAuthenticated: {
    type: Boolean,
    required: true
  }
})

defineEmits(['logout'])

const hasActiveStream = computed(() => streamStore.hasActiveStream)
</script>

<template>
  <div class="d-flex align-center">
    <v-btn to="/" class="mx-2">Главная</v-btn>
    <template v-if="!isAuthenticated">
      <v-btn
          to="/login"
          class="mx-2"
      >Войти</v-btn>

      <v-btn
          to="/register"
          class="mx-2"
      >Зарегистрироваться</v-btn>
    </template>

    <template v-else>
      <v-btn to="/profile"
             class="mx-2"
             rounded="lg"
             color="text"
             variant="flat"
      >Мой профиль</v-btn>

      <v-btn
          v-if="!hasActiveStream"
          color="success"
          variant="flat"
          rounded="lg"
          class="mx-2"
          to="/create-stream"
      >Начать стрим
      </v-btn>

      <v-btn
          variant="tonal"
          rounded="lg"
          color="error"
          @click="$emit('logout')"
          class="mx-2"
      >Выйти</v-btn>
    </template>
  </div>
</template>

<style scoped>

</style>