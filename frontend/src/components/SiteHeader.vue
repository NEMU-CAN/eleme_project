<script setup lang="ts">
import UiIcon from '@/components/UiIcon.vue'

// 统一页面头部，支持主标题、副标题、返回按钮和右侧扩展区。
withDefaults(
  defineProps<{
    title: string
    eyebrow?: string
    backable?: boolean
    compact?: boolean
  }>(),
  {
    backable: false,
    compact: false,
  },
)

const emit = defineEmits<{
  back: []
}>()
</script>

<template>
  <header class="site-header" :class="{ 'site-header--compact': compact }">
    <button v-if="backable" type="button" class="icon-button icon-button--soft" @click="emit('back')">
      <UiIcon name="chevronLeft" :size="18" />
    </button>
    <div class="site-header__copy">
      <p v-if="eyebrow" class="site-header__eyebrow">{{ eyebrow }}</p>
      <h1 class="site-header__title">{{ title }}</h1>
    </div>
    <div class="site-header__actions">
      <slot name="actions" />
    </div>
  </header>
</template>
