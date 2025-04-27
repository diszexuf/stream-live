<template>
  <div class="d-flex align-center">
    <v-btn to="/" class="mx-2">Главная</v-btn>
    <template v-if="!isAuthenticated">
      <v-btn to="/login" class="mx-2">Войти</v-btn>
      <v-btn to="/register" class="mx-2">Зарегистрироваться</v-btn>
    </template>
    
    <template v-else>
      <v-btn to="/profile" class="mx-2">Мой профиль</v-btn>
      <v-btn @click="$emit('logout')" class="mx-2">Выйти</v-btn>
      <v-btn 
        variant="elevated" 
        class="mx-2" 
        to="/create-stream"
      >
        Начать стрим
      </v-btn>
    </template>
  </div>
</template>

<script setup>
defineProps({
  isAuthenticated: {
    type: Boolean,
    required: true
  }
})

defineEmits(['logout'])
</script> 