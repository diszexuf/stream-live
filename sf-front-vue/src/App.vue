<template>
  <v-app>
    <app-header />

    <v-main class="main-content">
      <v-container fluid>
        <router-view />
      </v-container>
    </v-main>

    <app-footer />
  </v-app>
</template>

<script setup>
import { onMounted } from 'vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

onMounted(async () => {
  await userStore.checkAuth()
})
</script>

<style>
.v-main.main-content {
  min-height: calc(100vh - 128px);
  display: flex;
  flex-direction: column;
}

.v-main.main-content .v-container {
  flex: 1;
  display: flex;
  flex-direction: column;
}
</style>
