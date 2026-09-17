<script setup lang="ts">
import { nextTick, ref } from 'vue'
import { useRouter } from 'vue-router'
import SiteHeader from '@/components/SiteHeader.vue'
import UiIcon from '@/components/UiIcon.vue'

interface ChatMessage {
  id: number
  role: 'user' | 'assistant'
  text: string
  time: string
}

const router = useRouter()
const draft = ref('')
const messages = ref<ChatMessage[]>([])
const sending = ref(false)
const messageList = ref<HTMLElement | null>(null)

// deepseek-flash 接入：api 暂时留空，后续自行填入后即可生效。
const DEEPSEEK_API_URL = 'https://api.deepseek.com/v1/chat/completions'
const DEEPSEEK_API_KEY = ''
const DEEPSEEK_MODEL = 'deepseek-flash'

// 本地知识库：api 未配置时，用于回答外卖系统的常见问题。
const SYSTEM_PROMPT =
  '你是饿了么外卖系统的智能客服，请用简洁的中文回答用户关于本系统的问题。' +
  '你可以参考以下信息：成为商家请点击「我的 -> 我要开店」，成为商家后将无法再点餐；' +
  '购物车按商家分组，可一键结算多个商家的订单；' +
  '订单状态包括待支付、配送中、已完成、已取消；' +
  '配送中订单超时 15 分钟会自动标记；' +
  '店铺注销后账号会从商家转为普通用户。'

const LOCAL_QA: Array<{ match: RegExp; answer: string }> = [
  {
    match: /(成为商家|我要开店|开店|成为商户|入驻)/,
    answer: '可以的。点击「我的 → 我要开店」，填写门店信息并提交即可。注意：成为商家后将无法再点餐。',
  },
  {
    match: /(注销|关闭店铺|注销店铺|关店|删除店铺)/,
    answer: '商家可在店铺资料页点击「注销店铺」进行永久注销，注销后账号会从商家转为普通用户。',
  },
  {
    match: /(配送|超时)/,
    answer: '配送中的订单若超过 15 分钟未完成，会按超时处理。',
  },
  {
    match: /(购物车|结算)/,
    answer: '购物车按商家分组展示，右下角「一键结算」可同时结算多个商家的订单。',
  },
  {
    match: /(订单|查订单)/,
    answer: '点击「我的 → 我的订单」即可查看全部、配送中、已完成的订单。',
  },
  {
    match: /(商家|店铺)/,
    answer: '在首页可浏览商家，点击商家查看菜品并下单；上方搜索框既能搜商家，也能搜菜品。',
  },
]

function formatTime() {
  return new Intl.DateTimeFormat('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(new Date())
}

async function localAnswer(text: string): Promise<string> {
  for (const item of LOCAL_QA) {
    if (item.match.test(text)) {
      return item.answer
    }
  }
  return '抱歉，我暂时无法回答这个问题。你可以问我关于开店、购物车、订单、配送等方面的问题。'
}

async function callDeepseek(text: string): Promise<string> {
  if (!DEEPSEEK_API_URL) {
    return localAnswer(text)
  }

  const headers: Record<string, string> = { 'Content-Type': 'application/json' }
  if (DEEPSEEK_API_KEY) {
    headers.Authorization = `Bearer ${DEEPSEEK_API_KEY}`
  }

  const response = await fetch(DEEPSEEK_API_URL, {
    method: 'POST',
    headers,
    body: JSON.stringify({
      model: DEEPSEEK_MODEL,
      messages: [
        { role: 'system', content: SYSTEM_PROMPT },
        ...messages.value.map((message) => ({ role: message.role, content: message.text })),
        { role: 'user', content: text },
      ],
      stream: false,
    }),
  })

  if (!response.ok) {
    return localAnswer(text)
  }

  const payload = (await response.json()) as {
    choices?: Array<{ message?: { content?: string } }>
  }
  const answer = payload.choices?.[0]?.message?.content?.trim()
  return answer || localAnswer(text)
}

async function sendMessage() {
  const text = draft.value.trim()
  if (!text || sending.value) {
    return
  }

  messages.value.push({
    id: Date.now(),
    role: 'user',
    text,
    time: formatTime(),
  })
  draft.value = ''
  sending.value = true
  await scrollToBottom()

  try {
    const answer = await callDeepseek(text)
    messages.value.push({
      id: Date.now(),
      role: 'assistant',
      text: answer,
      time: formatTime(),
    })
  } catch {
    messages.value.push({
      id: Date.now(),
      role: 'assistant',
      text: await localAnswer(text),
      time: formatTime(),
    })
  } finally {
    sending.value = false
    await scrollToBottom()
  }
}

async function scrollToBottom() {
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
        <article
          v-for="message in messages"
          :key="message.id"
          class="customer-service-message"
          :class="{ 'customer-service-message--assistant': message.role === 'assistant' }"
        >
          <p>{{ message.text }}</p>
          <time>{{ message.time }}</time>
        </article>
        <p v-if="sending" class="customer-service-typing">客服正在输入…</p>
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
      <button type="button" :disabled="!draft.trim() || sending" @click="sendMessage">发送</button>
    </footer>
  </div>
</template>
