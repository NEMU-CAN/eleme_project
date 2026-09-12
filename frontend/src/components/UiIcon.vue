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
    strokeWidth: 1.8,
  },
)

// 集中维护轻量 SVG 路径，避免额外引入图标库。
const iconPaths: Record<string, string> = {
  home: 'M3 11.25 12 4l9 7.25M5 10.5V20h14v-9.5',
  compass:
    'M12 21a9 9 0 1 1 0-18a9 9 0 0 1 0 18Zm3.8-11.8-1.6 4.1-4.1 1.6 1.6-4.1 4.1-1.6Z',
  orders:
    'M6.5 3.75h11A1.75 1.75 0 0 1 19.25 5.5v13A1.75 1.75 0 0 1 17.5 20.25h-11A1.75 1.75 0 0 1 4.75 18.5v-13A1.75 1.75 0 0 1 6.5 3.75Zm2 4h7M8.5 11h7M8.5 15h4',
  user:
    'M20 20a8 8 0 1 0-16 0M12 12a4 4 0 1 0 0-8a4 4 0 0 0 0 8Z',
  smile:
    'M12 21a9 9 0 1 1 0-18a9 9 0 0 1 0 18Zm-3.5-11h0M15.5 10h0M8.5 14.5c.7.9 2 1.5 3.5 1.5s2.8-.6 3.5-1.5',
  sad:
    'M12 21a9 9 0 1 1 0-18a9 9 0 0 1 0 18Zm-3.5-11h0M15.5 10h0M8.5 16c.7-.9 2-1.5 3.5-1.5s2.8.6 3.5 1.5',
  search: 'M11 18a7 7 0 1 1 0-14a7 7 0 0 1 0 14Zm4.5-1.5L20 21',
  pin: 'M12 21s6-5.7 6-11a6 6 0 0 0-12 0c0 5.3 6 11 6 11Zm0-8a3 3 0 1 1 0-6a3 3 0 0 1 0 6Z',
  chevronDown: 'M6 9l6 6 6-6',
  chevronLeft: 'M15 18l-6-6 6-6',
  chevronRight: 'M9 6l6 6-6 6',
  plus: 'M12 5v14M5 12h14',
  minus: 'M5 12h14',
  close: 'M6 6l12 12M18 6L6 18',
  cart: 'M5 6h2l1.2 8.5A1.8 1.8 0 0 0 10 16h7.2a1.8 1.8 0 0 0 1.8-1.5L20 8H7.2',
  star: 'M12 4.8l2.8 5.7 6.3.9-4.5 4.4 1.1 6.2L12 18.9 6.3 22l1.1-6.2L2.9 11.4l6.3-.9L12 4.8Z',
  filter: 'M4 6h16M7 12h10M10 18h4',
  check: 'M5 12.5 9.2 16.7 19 6.8',
  wallet:
    'M4.5 7.5h15A1.5 1.5 0 0 1 21 9v7.5A1.5 1.5 0 0 1 19.5 18H5A1.5 1.5 0 0 1 3.5 16.5V9A1.5 1.5 0 0 1 5 7.5Zm12 3.75h4.5',
  truck:
    'M3.75 7.5h11.5v7.25h-11.5zM15.25 10.25h3.25l2 2v2.5h-5.25M8.25 18.75a1.75 1.75 0 1 0 0-3.5a1.75 1.75 0 0 0 0 3.5Zm9 0a1.75 1.75 0 1 0 0-3.5a1.75 1.75 0 0 0 0 3.5Z',
  phone:
    'M8.5 4.75h7a1.75 1.75 0 0 1 1.75 1.75v11a1.75 1.75 0 0 1-1.75 1.75h-7A1.75 1.75 0 0 1 6.75 17.5v-11A1.75 1.75 0 0 1 8.5 4.75Zm1.75 12.5h3.5',
  clock: 'M12 6v6l4 2M21 12a9 9 0 1 1-18 0a9 9 0 0 1 18 0Z',
  trash:
    'M4 7h16M10 11v6M14 11v6M6 7l1 12a2 2 0 0 0 2 2h6a2 2 0 0 0 2-2l1-12M9 7V5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v2',
  note: 'M7 3h7l4 4v14H7zM14 3v4h4M9.5 12h5M9.5 16h5',
  scooter:
    'M6 17a2 2 0 1 0 0-4 2 2 0 0 0 0 4Zm12 0a2 2 0 1 0 0-4 2 2 0 0 0 0 4ZM7 15h6l2.5-6h3M13 15l-1 4H7',
  bubble:
    'M4 6a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2H9l-4 4v-4H6a2 2 0 0 1-2-2zM8 9h8M8 12h5',
  headphone:
    'M4 14v-1a8 8 0 0 1 16 0v1M4 14a2 2 0 0 1 2-2h1v9H6a2 2 0 0 1-2-2zm16 0a2 2 0 0 0-2-2h-1v9h1a2 2 0 0 0 2-2z',
  invoice:
    'M6 3h12v17l-2-1.5L14 20l-2-1.5L10 20l-2-1.5L6 20zM9 8h6M9 12h4',
  edit: 'M4 20h4L19.5 8.5a2.12 2.12 0 0 0-3-3L5 17zM13.5 6.5l3 3',
  ear: 'M4 14v-1a8 8 0 0 1 16 0v1M4 14a2 2 0 0 1 2-2h1v9H6a2 2 0 0 1-2-2zm16 0a2 2 0 0 0-2-2h-1v9h1a2 2 0 0 0 2-2z',
}

// 找不到指定图标时回退到搜索图标，避免界面空白。
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
