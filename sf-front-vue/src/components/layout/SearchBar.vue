<template>
  <v-text-field
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    placeholder="Поиск стримов..."
    prepend-inner-icon="mdi-magnify"
    variant="outlined"
    density="compact"
    hide-details
  ></v-text-field>
</template>

<script setup>
defineProps({
  modelValue: {
    type: String,
    default: ''
  }
})

defineEmits(['update:modelValue'])
</script> 