<script setup lang="ts">
defineProps<{ loading?: boolean; error?: string; empty?: boolean; emptyText?: string }>()
</script>

<template>
  <div v-if="loading" class="table-state" aria-live="polite">
    <div class="skeleton-lines" aria-hidden="true"><i /><i /><i /></div>
    <p>正在同步数据</p>
  </div>
  <div v-else-if="error" class="table-state state-error" role="alert">
    <strong>数据加载失败</strong>
    <p>{{ error }}</p>
    <slot />
  </div>
  <div v-else-if="empty" class="table-state">
    <strong>暂无匹配数据</strong>
    <p>{{ emptyText || '调整筛选条件后重试' }}</p>
  </div>
</template>
