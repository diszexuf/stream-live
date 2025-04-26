<template>
  <v-app>
    <v-app-bar>
      <v-app-bar-title>
        <router-link to="/" class="text-decoration-none">StreamFusion</router-link>
      </v-app-bar-title>

      <v-text-field
        v-model="searchQuery"
        placeholder="Поиск стримов..."
        prepend-inner-icon="mdi-magnify"
        variant="outlined"
        density="compact"
        hide-details
        class="mx-4"
      ></v-text-field>

      <v-spacer></v-spacer>

      <v-btn to="/" class="mx-2">Главная</v-btn>
      <v-btn to="/categories" class="mx-2">Категории</v-btn>
      <v-btn to="/profile" class="mx-2">Мой профиль</v-btn>
      <v-btn variant="elevated" class="mx-2" @click="router.push('/create-stream')">Начать стрим</v-btn>
    </v-app-bar>

    <v-main>
      <v-container fluid>
        <router-view />
      </v-container>
    </v-main>

    <v-footer class="text-center d-flex flex-column">
      <div>
        <v-btn
          v-for="link in footerLinks"
          :key="link.text"
          :href="link.href"
          variant="text"
          class="mx-4"
        >
          {{ link.text }}
        </v-btn>
      </div>
      <div class="text-caption mt-2">
        © 2025 StreamFusion. Все права защищены.
      </div>
    </v-footer>
  </v-app>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const searchQuery = ref('')

const footerLinks = [
  { text: 'О нас', href: '#' },
  { text: 'Правила', href: '#' },
  { text: 'Конфиденциальность', href: '#' },
  { text: 'Поддержка', href: '#' }
]
</script>
