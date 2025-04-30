<script setup>
defineProps({
  title: {
    type: String,
    required: true
  }
})
</script>

<template>
  <div class="auth-wrapper">
    <v-card class="auth-card" elevation="3">
      <v-card-title class="text-h4 text-center mb-4">
        {{ title }}
      </v-card-title>
      
      <v-card-text>
        <slot></slot>
      </v-card-text>
    </v-card>
  </div>
</template>

<style scoped>
.auth-wrapper {
  min-height: calc(100vh - 160px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

.auth-card {
  width: 100%;
  max-width: 480px;
  border-radius: 16px;
  padding: 24px;
  background: linear-gradient(to bottom right, #ffffff, #f8f9fa);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.05) !important;
}

:deep(.v-text-field) {
  border-radius: 8px;
  margin-bottom: 20px;
}

:deep(.v-btn) {
  border-radius: 8px;
  height: 48px;
  font-weight: 500;
  letter-spacing: 0.5px;
  text-transform: none;
}

:deep(.v-alert) {
  border-radius: 8px;
  margin-bottom: 24px;
}
</style> 