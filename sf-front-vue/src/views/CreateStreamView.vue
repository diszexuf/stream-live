<script setup>
import {ref, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {useUserStore} from '@/stores/user'
import {useStreamStore} from '@/stores/stream'
import ImageUploader from '@/components/stream/ImageUploader.vue'

const router = useRouter()
const userStore = useUserStore()
const streamStore = useStreamStore()
const streamTitle = ref('')
const streamDescription = ref('')
const streamThumbnailUrl = ref('')
const streamTags = ref([])
const newTag = ref('')
const isLoading = ref(false)

const addTag = () => {
  if (newTag.value && !streamTags.value.includes(newTag.value)) {
    streamTags.value.push(newTag.value)
    newTag.value = ''
  }
}

const removeTag = (tag) => {
  streamTags.value = streamTags.value.filter(t => t !== tag)
}

const startStream = async () => {
  if (!streamTitle.value) {
    alert('Введите название стрима')
    return
  }

  isLoading.value = true
  try {
    const streamData = {
      title: streamTitle.value,
      description: streamDescription.value,
      thumbnailUrl: streamThumbnailUrl.value,
      tags: streamTags.value
    }
    
    await streamStore.createStream(streamData)
    router.push('/dashboard')
  } catch (error) {
    console.error('Ошибка при создании стрима:', error)
    alert(`Не удалось создать стрим: ${error.message || 'Неизвестная ошибка'}`)
  } finally {
    isLoading.value = false
  }
}

onMounted(async () => {
  if (!userStore.user) {
    await userStore.fetchCurrentUser()
  }
})

</script>

<template>
  <v-container>
    <v-row>
      <v-col cols="12" md="8" offset-md="2">
        <v-card>
          <v-card-title class="text-h4 mb-4">Начать стрим</v-card-title>

          <v-card-text v-if="!userStore.isAuthenticated">
            <v-alert type="warning">
              Для создания стрима необходимо авторизоваться
            </v-alert>
            <v-btn color="primary" block class="mt-4" to="/login">
              Войти в аккаунт
            </v-btn>
          </v-card-text>

          <v-card-text v-else>
            <v-form @submit.prevent="startStream">
              <v-text-field
                  v-model="streamTitle"
                  label="Название стрима"
                  required
                  :rules="[v => !!v || 'Введите название стрима']"
                  class="mb-4"
              ></v-text-field>

              <v-textarea
                  v-model="streamDescription"
                  label="Описание"
                  rows="3"
                  class="mb-4"
              ></v-textarea>

              <image-uploader
                  v-model="streamThumbnailUrl"
                  label="Загрузить миниатюру стрима"
                  class="mb-4"
              />

              <div class="mb-4">
                <label class="text-subtitle-1 mb-2 d-block">Теги</label>
                <div class="d-flex flex-wrap gap-2 mb-2">
                  <v-chip
                    v-for="tag in streamTags"
                    :key="tag"
                    closable
                    @click:close="removeTag(tag)"
                  >
                    {{ tag }}
                  </v-chip>
                </div>
                <div class="d-flex gap-2">
                  <v-text-field
                    v-model="newTag"
                    label="Новый тег"
                    hide-details
                    density="compact"
                    @keyup.enter="addTag"
                  ></v-text-field>
                  <v-btn @click="addTag" icon="mdi-plus"></v-btn>
                </div>
              </div>

              <v-btn
                  color="primary"
                  type="submit"
                  block
                  :loading="isLoading"
              >
                Начать стрим
              </v-btn>
            </v-form>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>
