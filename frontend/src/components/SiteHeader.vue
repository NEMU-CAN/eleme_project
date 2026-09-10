<script setup lang="ts">
import UiIcon from '@/components/UiIcon.vue'

// 统一页面头部，支持标题、返回按钮和右侧扩展区。
withDefaults(
  defineProps<{
    title: string
    backable?: boolean
  }>(),
  {
    backable: false,
  },
)

const emit = defineEmits<{
  back: []
}>()
</script>

<template>
  <header class="site-header">
    <button v-if="backable" type="button" class="site-header__back" @click="emit('back')">
      <UiIcon name="chevronLeft" :size="20" />
    </button>
    <span v-else style="width: 32px; margin-left: -6px" />
    <h1 class="site-header__title">{{ title }}</h1>
    <div class="site-header__actions" style="width: 32px; justify-content: flex-end">
      <slot name="actions" />
    </div>
  </header>
</template>
