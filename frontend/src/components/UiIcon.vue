<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    name: string
    size?: number
    strokeWidth?: number
  }>(),
  {
    size: 20,
    strokeWidth: 2,
  },
)

// 这里集中维护项目里用到的轻量 SVG 路径，避免再额外引入图标库。
const iconPaths: Record<string, string> = {
  home: 'M3 11.25L12 4l9 7.25M5 10.5V20h14v-9.5',
  compass:
    'M12 21a9 9 0 1 1 0-18a9 9 0 0 1 0 18Zm3.8-11.8-1.6 4.1-4.1 1.6 1.6-4.1 4.1-1.6Z',
  orders:
    'M6.5 3.75h11A1.75 1.75 0 0 1 19.25 5.5v13A1.75 1.75 0 0 1 17.5 20.25h-11A1.75 1.75 0 0 1 4.75 18.5v-13A1.75 1.75 0 0 1 6.5 3.75Zm2 4h7M8.5 11h7M8.5 15h4',
  user:
    'M20 20a8 8 0 1 0-16 0M12 12a4 4 0 1 0 0-8a4 4 0 0 0 0 8Z',
  search: 'M11 18a7 7 0 1 1 0-14a7 7 0 0 1 0 14Zm4.5-1.5L20 21',
  pin: 'M12 21s6-5.7 6-11a6 6 0 0 0-12 0c0 5.3 6 11 6 11Zm0-8a3 3 0 1 1 0-6a3 3 0 0 1 0 6Z',
  chevronDown: 'M6 9l6 6 6-6',
  chevronLeft: 'M15 18l-6-6 6-6',
  chevronRight: 'M9 6l6 6-6 6',
  plus: 'M12 5v14M5 12h14',
  minus: 'M5 12h14',
  cart: 'M5 6h2l1.2 8.5A1.8 1.8 0 0 0 10 16h7.2a1.8 1.8 0 0 0 1.8-1.5L20 8H7.2',
  star: 'M12 4.8l2.8 5.7 6.3.9-4.5 4.4 1.1 6.2L12 18.9 6.3 22l1.1-6.2L2.9 11.4l6.3-.9L12 4.8Z',
  filter:
    'M4 6h16M7 12h10M10 18h4',
  check: 'M5 12.5 9.2 16.7 19 6.8',
  wallet:
    'M4.5 7.5h15A1.5 1.5 0 0 1 21 9v7.5A1.5 1.5 0 0 1 19.5 18H5A1.5 1.5 0 0 1 3.5 16.5V9A1.5 1.5 0 0 1 5 7.5Zm12 3.75h4.5',
  truck:
    'M3.75 7.5h11.5v7.25h-11.5zM15.25 10.25h3.25l2 2v2.5h-5.25M8.25 18.75a1.75 1.75 0 1 0 0-3.5a1.75 1.75 0 0 0 0 3.5Zm9 0a1.75 1.75 0 1 0 0-3.5a1.75 1.75 0 0 0 0 3.5Z',
  phone:
    'M8.5 4.75h7a1.75 1.75 0 0 1 1.75 1.75v11a1.75 1.75 0 0 1-1.75 1.75h-7A1.75 1.75 0 0 1 6.75 17.5v-11A1.75 1.75 0 0 1 8.5 4.75Zm1.75 12.5h3.5',
  clock:
    'M12 6v6l4 2M21 12a9 9 0 1 1-18 0a9 9 0 0 1 18 0Z',
}

// 找不到指定图标时，回退到搜索图标，保证界面不会空白。
const iconPath = computed(() => iconPaths[props.name] ?? iconPaths.search)
</script>

<template>
  <svg
    class="ui-icon"
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    aria-hidden="true"
    fill="none"
    stroke="currentColor"
    :stroke-width="strokeWidth"
    stroke-linecap="round"
    stroke-linejoin="round"
  >
    <path :d="iconPath" />
  </svg>
</template>
