<script setup lang="ts">
defineProps<{
  label: string
  tone?: 'positive' | 'warning' | 'danger' | 'neutral' | 'info'
}>()
</script>

<template>
  <span class="status-badge" :class="`status-${tone || 'neutral'}`">{{ label }}</span>
</template>
