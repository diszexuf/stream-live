<template>
  <v-footer class="footer">
    <div class="d-flex flex-column align-center w-100">
      <div class="d-flex justify-center">
        <v-btn
          v-for="link in links"
          :key="link.text"
          :href="link.href"
          variant="text"
          class="mx-2"
          color="text"
        >
          {{ link.text }}
        </v-btn>
      </div>
      <div class="text-caption mt-2 copyright">
        © {{ currentYear }} StreamFusion. Все права защищены.
      </div>
    </div>
  </v-footer>
</template>

<script setup>
import { computed } from 'vue'

const links = [
  { text: 'О нас', href: '#' },
  { text: 'Правила', href: '#' },
  { text: 'Конфиденциальность', href: '#' },
  { text: 'Поддержка', href: '#' }
]

const currentYear = computed(() => new Date().getFullYear())
</script>

<style scoped>
.footer {
  position: relative;
  flex-shrink: 0;
  padding: 16px;
  background-color: var(--color-background) !important;
  border-top: 1px solid var(--color-border);
  color: var(--color-text) !important;
}

.copyright {
  color: var(--color-text-soft) !important;
}

:deep(.v-footer) {
  background-color: var(--color-background) !important;
  color: var(--color-text) !important;
}
</style> 