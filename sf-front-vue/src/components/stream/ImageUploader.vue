<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  label: {
    type: String,
    default: 'Загрузить изображение'
  }
})

const emit = defineEmits(['update:modelValue'])

const fileInput = ref(null)
const previewUrl = ref(props.modelValue)
const isDragging = ref(false)

watch(() => props.modelValue, (newValue) => {
  previewUrl.value = newValue
})

const handleFileSelect = (event) => {
  const file = event.target.files[0]
  if (file) {
    processFile(file)
  }
}

const handleDrop = (event) => {
  event.preventDefault()
  isDragging.value = false
  
  const file = event.dataTransfer.files[0]
  if (file) {
    processFile(file)
  }
}

const processFile = (file) => {
  if (!file.type.startsWith('image/')) {
    alert('Пожалуйста, загрузите изображение')
    return
  }

  const reader = new FileReader()
  reader.onload = (e) => {
    previewUrl.value = e.target.result
    emit('update:modelValue', e.target.result)
  }
  reader.readAsDataURL(file)
}

const triggerFileInput = () => {
  fileInput.value.click()
}

const removeImage = () => {
  previewUrl.value = ''
  emit('update:modelValue', '')
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}
</script>

<template>
  <div class="image-uploader">
    <input
      ref="fileInput"
      type="file"
      accept="image/*"
      class="d-none"
      @change="handleFileSelect"
    >
    
    <div
      class="upload-area"
      :class="{ 'dragging': isDragging }"
      @dragover.prevent="isDragging = true"
      @dragleave.prevent="isDragging = false"
      @drop="handleDrop"
      @click="triggerFileInput"
    >
      <template v-if="!previewUrl">
        <v-icon size="large" color="primary">mdi-cloud-upload</v-icon>
        <div class="text-center mt-2">
          <div class="text-body-1">{{ label }}</div>
          <div class="text-caption text-medium-emphasis">
            Перетащите изображение сюда или нажмите для выбора
          </div>
        </div>
      </template>
      
      <template v-else>
        <div class="preview-container">
          <img :src="previewUrl" alt="Preview" class="preview-image">
          <div class="preview-overlay">
            <v-btn
              icon="mdi-delete"
              color="error"
              variant="tonal"
              @click.stop="removeImage"
            ></v-btn>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.image-uploader {
  width: 100%;
}

.upload-area {
  border: 2px dashed rgba(var(--v-theme-primary), 0.5);
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  min-height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.upload-area:hover {
  border-color: rgb(var(--v-theme-primary));
  background-color: rgba(var(--v-theme-primary), 0.05);
}

.upload-area.dragging {
  border-color: rgb(var(--v-theme-primary));
  background-color: rgba(var(--v-theme-primary), 0.1);
}

.preview-container {
  position: relative;
  width: 100%;
  height: 100%;
}

.preview-image {
  max-width: 100%;
  max-height: 300px;
  object-fit: contain;
  border-radius: 4px;
}

.preview-overlay {
  position: absolute;
  top: 8px;
  right: 8px;
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  padding: 4px;
}
</style> 