<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'

// 图片懒加载组件：只有当图片进入视口时才开始加载，提升页面性能
const props = withDefaults(
  defineProps<{
    src: string
    alt?: string
    fallback?: string
  }>(),
  {
    alt: '',
    fallback: '/eleme/sp01.png',
  },
)

const imgRef = ref<HTMLImageElement | null>(null)
const isLoaded = ref(false)
const isError = ref(false)
const currentSrc = ref<string>('')

let observer: IntersectionObserver | null = null

onMounted(() => {
  if (!imgRef.value) return

  // 使用 IntersectionObserver 监听图片是否进入视口
  observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting && !currentSrc.value) {
          // 图片进入视口，开始加载
          currentSrc.value = props.src
          observer?.unobserve(entry.target)
        }
      })
    },
    {
      rootMargin: '200px', // 提前 200px 开始加载，确保滚动时图片已准备好
      threshold: 0.01, // 只要有一点点进入视口就开始加载
    },
  )

  observer.observe(imgRef.value)
})

onBeforeUnmount(() => {
  observer?.disconnect()
})

const handleLoad = () => {
  isLoaded.value = true
}

const handleError = () => {
  isError.value = true
  if (props.fallback) {
    currentSrc.value = props.fallback
  }
}
</script>

<template>
  <img
    ref="imgRef"
    :src="currentSrc || props.src"
    :alt="alt"
    :class="{ 'lazy-image--loaded': isLoaded, 'lazy-image--error': isError }"
    class="lazy-image"
    @load="handleLoad"
    @error="handleError"
  />
</template>

<style scoped>
.lazy-image {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: opacity 0.3s ease;
  opacity: 0.5;
  background: #eef1f4;
}

.lazy-image--loaded {
  opacity: 1;
}

.lazy-image--error {
  opacity: 0.6;
  background: #f3f4f6;
}
</style>
