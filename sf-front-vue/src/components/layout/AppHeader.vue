<template>
  <v-app-bar>
    <v-app-bar-title>
      <router-link to="/" class="text-decoration-none">StreamFusion</router-link>
    </v-app-bar-title>

    <search-bar v-model="searchQuery" class="mx-4" />

    <v-spacer></v-spacer>

    <navigation-menu 
      :is-authenticated="isAuthenticated"
      @logout="handleLogout"
    />
  </v-app-bar>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import SearchBar from './SearchBar.vue'
import NavigationMenu from './NavigationMenu.vue'

const router = useRouter()
const userStore = useUserStore()
const searchQuery = ref('')

const isAuthenticated = userStore.isAuthenticated

const handleLogout = () => {
  userStore.logout()
  router.push('/')
}
</script> 