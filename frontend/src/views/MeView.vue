<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import BottomNav from '@/components/BottomNav.vue'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'
import { formatGender, maskPhone } from '@/utils/format'
import type { GenderType } from '@/types'

const router = useRouter()
const store = useHungryStore()
const user = computed(() => store.state.user)
const authenticated = computed(() => store.isAuthenticated.value)

const showEdit = ref(false)
const notice = ref('')
const profileError = ref('')
const profileSuccess = ref('')
const profileSaving = ref(false)
const accountDeleting = ref(false)
const avatarFileName = ref('')

const profileForm = reactive({
  nickname: '',
  phone: '',
  avatar: '',
  gender: 0 as GenderType,
})

watch(
  user,
  (value) => {
    if (!value) {
      return
    }
    profileForm.nickname = value.nickname || value.name || ''
    profileForm.phone = value.phone || ''
    profileForm.avatar = value.avatar && !value.avatar.startsWith('/eleme/') ? value.avatar : ''
    profileForm.gender = value.gender
  },
  { immediate: true },
)

onMounted(() => {
  if (authenticated.value) {
    void store.loadSessionData().catch(() => undefined)
  }
})

function flashNotice(text: string) {
  notice.value = text
  window.setTimeout(() => {
    notice.value = ''
  }, 1600)
}

function goOrders(filter?: string) {
  router.push(filter ? `/orders?filter=${filter}` : '/orders')
}

function goAddress() {
  router.push('/addresses')
}

function goOpenShop() {
  router.push(user.value?.role === 1 ? '/merchant-center' : '/open-shop')
}

function goCustomerService() {
  router.push('/customer-service')
}

function comingSoon() {
  flashNotice('敬请期待')
}

function selectAvatar(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]

  avatarFileName.value = file?.name ?? ''
  if (!file) {
    profileForm.avatar = ''
    return
  }

  const reader = new FileReader()
  reader.onload = () => {
    profileForm.avatar = typeof reader.result === 'string' ? reader.result : ''
  }
  reader.onerror = () => {
    profileForm.avatar = ''
    avatarFileName.value = ''
    profileError.value = '头像读取失败，请重新选择'
  }
  reader.readAsDataURL(file)
}

function goLogin() {
  router.push('/login')
}

async function saveProfile() {
  if (!authenticated.value) {
    goLogin()
    return
  }
  if (!profileForm.nickname.trim()) {
    profileError.value = '请输入昵称'
    return
  }
  if (!profileForm.phone.trim()) {
    profileError.value = '请输入手机号'
    return
  }
  try {
    profileSaving.value = true
    profileError.value = ''
    profileSuccess.value = ''
    await store.updateCurrentUser({
      nickname: profileForm.nickname.trim(),
      phone: profileForm.phone.trim(),
      avatar: profileForm.avatar.trim() || null,
      gender: profileForm.gender,
    })
    profileSuccess.value = '资料已更新，请重新登录'
    showEdit.value = false
  } catch (cause) {
    profileError.value = store.messageFromError(cause)
  } finally {
    profileSaving.value = false
  }
}

async function deleteAccount() {
  const confirmed = window.confirm('删除账户后将无法恢复，确定要继续吗？')
  if (!confirmed) {
    return
  }

  try {
    accountDeleting.value = true
    profileError.value = ''
    await store.deleteAccount()
    router.push('/login')
  } catch (cause) {
    profileError.value = store.messageFromError(cause)
  } finally {
    accountDeleting.value = false
  }
}

async function logout() {
  const confirmed = window.confirm('确定要退出当前账户吗？')
  if (!confirmed) {
    return
  }

  await store.logout()
  router.push('/')
}
</script>

<template>
  <div class="page page--with-nav">
    <!-- 顶部头像 + 电话 -->
    <div class="me-header">
      <img v-if="user" class="me-header__avatar" :src="user.avatar" :alt="user.name" />
      <UiIcon v-else name="user" :size="32" style="width: 60px; height: 60px; border-radius: 50%; background: rgba(255,255,255,.2)" />
      <div class="me-header__copy">
        <p v-if="user" class="me-header__phone">{{ maskPhone(user.phone) }}</p>
        <p v-if="user" class="me-header__sub">{{ user.name }} · {{ formatGender(user.gender) }}</p>
        <button v-else type="button" class="me-header__login" @click="goLogin">点击登录</button>
      </div>
      <UiIcon name="chevronRight" :size="20" style="opacity: .7" />
    </div>

    <p v-if="notice" class="auth-form__hint auth-form__hint--success" style="text-align: center">{{ notice }}</p>

    <!-- 我的订单 -->
    <div class="me-card">
      <p class="me-card__title">我的订单</p>
      <div class="me-order-grid">
        <button type="button" class="me-order-item" @click="goOrders()">
          <span class="me-order-item__icon"><UiIcon name="note" :size="28" /></span>
          <span class="me-order-item__label">全部</span>
        </button>
        <button type="button" class="me-order-item" @click="goOrders('paid')">
          <span class="me-order-item__icon me-order-item__icon--badge">
            <UiIcon name="scooter" :size="28" />
            <span v-if="store.paidOrders.value" class="me-order-item__badge">{{ store.paidOrders.value }}</span>
          </span>
          <span class="me-order-item__label">配送中</span>
        </button>
        <button type="button" class="me-order-item" @click="goOrders('completed')">
          <span class="me-order-item__icon"><UiIcon name="bubble" :size="28" /></span>
          <span class="me-order-item__label">已完成</span>
        </button>
      </div>
    </div>

    <!-- 更多功能 -->
    <div class="me-card">
      <p class="me-card__title">更多功能</p>
      <button type="button" class="me-row" @click="goAddress">
        <span class="me-row__icon"><UiIcon name="pin" :size="22" /></span>
        <span class="me-row__label">我的地址</span>
        <UiIcon class="me-row__arrow" name="chevronRight" :size="16" />
      </button>
      <button type="button" class="me-row" @click="goOpenShop">
        <span class="me-row__icon"><UiIcon name="store" :size="22" /></span>
        <span class="me-row__label">{{ user?.role === 1 ? '商家中心' : '我要开店' }}</span>
        <UiIcon class="me-row__arrow" name="chevronRight" :size="16" />
      </button>
      <button type="button" class="me-row" @click="goCustomerService">
        <span class="me-row__icon"><UiIcon name="headphone" :size="22" /></span>
        <span class="me-row__label">我的客服</span>
        <UiIcon class="me-row__arrow" name="chevronRight" :size="16" />
      </button>
      <button type="button" class="me-row" @click="comingSoon">
        <span class="me-row__icon"><UiIcon name="invoice" :size="22" /></span>
        <span class="me-row__label">开发票</span>
        <UiIcon class="me-row__arrow" name="chevronRight" :size="16" />
      </button>
      <button v-if="authenticated" type="button" class="me-row" @click="showEdit = !showEdit">
        <span class="me-row__icon"><UiIcon name="edit" :size="22" /></span>
        <span class="me-row__label">编辑资料</span>
        <UiIcon class="me-row__arrow" name="chevronRight" :size="16" />
      </button>
    </div>

    <!-- 编辑资料 -->
    <div v-if="showEdit" class="form-card">
      <div class="field">
        <span class="field__label">昵称</span>
        <input v-model="profileForm.nickname" class="field__control" type="text" maxlength="20" placeholder="请输入昵称" />
      </div>
      <div class="field">
        <span class="field__label">手机号</span>
        <input v-model="profileForm.phone" class="field__control" type="tel" placeholder="请输入手机号" />
      </div>
      <div class="field">
        <span class="field__label">头像</span>
        <div class="file-picker">
          <label class="file-picker__button" for="profile-avatar">选择图片</label>
          <span class="file-picker__name">{{ avatarFileName || '未选择新头像（保留当前头像）' }}</span>
          <input id="profile-avatar" class="file-picker__input" type="file" accept="image/*" @change="selectAvatar" />
        </div>
      </div>
      <div class="field">
        <span class="field__label">性别</span>
        <div class="seg">
          <button type="button" class="seg__item" :class="{ 'seg__item--active': profileForm.gender === 0 }" @click="profileForm.gender = 0">保密</button>
          <button type="button" class="seg__item" :class="{ 'seg__item--active': profileForm.gender === 1 }" @click="profileForm.gender = 1">先生</button>
          <button type="button" class="seg__item" :class="{ 'seg__item--active': profileForm.gender === 2 }" @click="profileForm.gender = 2">女士</button>
        </div>
      </div>
      <p v-if="profileError" class="auth-form__hint auth-form__hint--danger">{{ profileError }}</p>
      <p v-else-if="profileSuccess" class="auth-form__hint auth-form__hint--success">{{ profileSuccess }}</p>
      <div class="form-actions">
        <button type="button" class="primary-button" :disabled="profileSaving" @click="saveProfile">
          {{ profileSaving ? '保存中' : '保存' }}
        </button>
      </div>
      <button type="button" class="delete-account-button" :disabled="accountDeleting" @click="deleteAccount">
        {{ accountDeleting ? '正在删除' : '删除账户' }}
      </button>
    </div>

    <!-- 退出登录 -->
    <div v-if="authenticated" class="me-card">
      <button type="button" class="me-row me-row--danger" @click="logout">
        <span class="me-row__label" style="text-align: center">退出登录</span>
      </button>
    </div>

    <BottomNav />
  </div>
</template>
