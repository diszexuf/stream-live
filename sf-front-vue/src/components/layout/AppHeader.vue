<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useStreamStore } from '@/stores/stream'
import SearchBar from './SearchBar.vue'
import NavigationMenu from './NavigationMenu.vue'

const router = useRouter()
const userStore = useUserStore()
const streamStore = useStreamStore()
const searchQuery = ref('')

const isAuthenticated = computed(() => userStore.isAuthenticated)

const handleLogout = () => {
  userStore.logout()
  router.push('/')
}

const handleSearch = async () => {
  if (searchQuery.value.trim()) {
    // Перенаправляем на главную страницу с параметром поиска
    router.push({
      path: '/',
      query: { search: searchQuery.value.trim() }
    })
  }
}
</script>

<template>
  <v-app-bar>
    <v-app-bar-title>
      <router-link to="/" class="text-decoration-none">StreamFusion</router-link>
    </v-app-bar-title>

    <search-bar 
      v-model="searchQuery" 
      class="mx-4" 
      @search="handleSearch"
    />

    <v-spacer></v-spacer>

    <navigation-menu 
      :is-authenticated="isAuthenticated"
      @logout="handleLogout"
    />
  </v-app-bar>
</template>