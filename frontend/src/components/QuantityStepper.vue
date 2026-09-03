<script setup lang="ts">
import UiIcon from '@/components/UiIcon.vue'

// 加减器组件，给商家详情页里的商品数量操作复用。
const props = withDefaults(
  defineProps<{
    quantity: number
    size?: 'small' | 'regular'
    disabled?: boolean
  }>(),
  {
    size: 'regular',
    disabled: false,
  },
)

const emit = defineEmits<{
  add: []
  remove: []
}>()
</script>

<template>
  <div class="quantity-stepper" :class="`quantity-stepper--${size}`">
    <button
      v-if="quantity > 0"
      type="button"
      class="quantity-stepper__button"
      :disabled="props.disabled"
      @click="emit('remove')"
    >
      <UiIcon name="minus" :size="14" />
    </button>
    <span v-if="quantity > 0" class="quantity-stepper__value">{{ quantity }}</span>
    <button
      type="button"
      class="quantity-stepper__button quantity-stepper__button--primary"
      :disabled="props.disabled"
      @click="emit('add')"
    >
      <UiIcon name="plus" :size="14" />
    </button>
  </div>
</template>
