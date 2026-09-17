<script setup lang="ts">
defineProps<{ title: string; subtitle?: string; open: boolean }>()
const emit = defineEmits<{ close: [] }>()
</script>

<template>
  <Teleport to="body">
    <Transition name="panel">
      <div v-if="open" class="panel-layer" role="presentation" @click.self="emit('close')">
        <section class="side-panel" role="dialog" aria-modal="true" :aria-label="title">
          <header class="panel-header">
            <div>
              <h2>{{ title }}</h2>
              <p v-if="subtitle">{{ subtitle }}</p>
            </div>
            <button class="icon-button" type="button" aria-label="关闭" title="关闭" @click="emit('close')">×</button>
          </header>
          <div class="panel-body"><slot /></div>
          <footer v-if="$slots.footer" class="panel-footer"><slot name="footer" /></footer>
        </section>
      </div>
    </Transition>
  </Teleport>
</template>
