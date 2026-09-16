<script setup lang="ts">
import { nextTick, ref } from 'vue'
import { useRouter } from 'vue-router'
import SiteHeader from '@/components/SiteHeader.vue'
import UiIcon from '@/components/UiIcon.vue'

interface LocalMessage {
  id: number
  text: string
  time: string
}

const router = useRouter()
const draft = ref('')
const messages = ref<LocalMessage[]>([])
const messageList = ref<HTMLElement | null>(null)

function formatTime() {
  return new Intl.DateTimeFormat('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(new Date())
}

async function sendMessage() {
  const text = draft.value.trim()
  if (!text) {
    return
  }

  messages.value.push({
    id: Date.now(),
    text,
    time: formatTime(),
  })
  draft.value = ''

  await nextTick()
  messageList.value?.scrollTo({ top: messageList.value.scrollHeight, behavior: 'smooth' })
}

function handleKeydown(event: KeyboardEvent) {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    void sendMessage()
  }
}
</script>

<template>
  <div class="customer-service-page">
    <SiteHeader title="我的客服" backable @back="router.push('/me')" />

    <div class="customer-service-status">
      <span class="customer-service-status__icon"><UiIcon name="headphone" :size="22" /></span>
      <div>
        <strong>饿了么客服</strong>
        <p><span /> 在线服务</p>
      </div>
    </div>

    <main ref="messageList" class="customer-service-messages">
      <div v-if="!messages.length" class="customer-service-empty">
        <span><UiIcon name="bubble" :size="32" /></span>
        <h2>有什么可以帮你？</h2>
        <p>请在下方输入并发送你的问题</p>
      </div>

      <div v-else class="customer-service-message-list">
        <article v-for="message in messages" :key="message.id" class="customer-service-message">
          <p>{{ message.text }}</p>
          <time>{{ message.time }}</time>
        </article>
      </div>
    </main>

    <footer class="customer-service-composer">
      <textarea
        v-model="draft"
        rows="1"
        maxlength="500"
        placeholder="请输入你的问题"
        aria-label="客服消息"
        @keydown="handleKeydown"
      />
      <button type="button" :disabled="!draft.trim()" @click="sendMessage">发送</button>
    </footer>
  </div>
</template>
