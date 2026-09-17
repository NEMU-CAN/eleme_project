<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { listUsers, updateUser } from '@/services/admin'
import { currentUser } from '@/services/auth'
import {
  GENDER_OPTIONS,
  USER_ROLE_OPTIONS,
  USER_STATUS_OPTIONS,
  genderMeta,
  userRoleMeta,
  userStatusMeta,
} from '@/utils/dict'
import { avatarFallback, errorMessage } from '@/utils/format'
import { toast } from '@/composables/useToast'
import StatusBadge from '@/components/StatusBadge.vue'
import SidePanel from '@/components/SidePanel.vue'
import TableState from '@/components/TableState.vue'
import type { User } from '@/types'

const loading = ref(true)
const error = ref('')
const users = ref<User[]>([])

const keyword = ref('')
const role = ref<number | null>(null)
const status = ref<number | null>(null)

async function load() {
  loading.value = true
  error.value = ''
  try {
    users.value = await listUsers({
      keyword: keyword.value || undefined,
      role: role.value,
      status: status.value,
    })
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    loading.value = false
  }
}

function reset() {
  keyword.value = ''
  role.value = null
  status.value = null
  load()
}

// ---------- 编辑 ----------
const panelOpen = ref(false)
const editing = ref<User | null>(null)
const saving = ref(false)
const formError = ref('')

const form = reactive({
  nickname: '',
  phone: '',
  avatar: '',
  gender: 0,
  role: 0,
  status: 0,
})

const isSelf = computed(() => editing.value?.id != null && editing.value.id === currentUser.value?.id)

function openEdit(user: User) {
  editing.value = user
  form.nickname = user.nickname ?? ''
  form.phone = user.phone ?? ''
  form.avatar = user.avatar ?? ''
  form.gender = user.gender ?? 0
  form.role = user.role ?? 0
  form.status = user.status ?? 0
  formError.value = ''
  panelOpen.value = true
}

function closePanel() {
  if (!saving.value) panelOpen.value = false
}

async function save() {
  if (saving.value || !editing.value) return
  formError.value = ''
  if (!form.nickname.trim()) {
    formError.value = '昵称不能为空'
    return
  }
  saving.value = true
  try {
    await updateUser(editing.value.id, {
      nickname: form.nickname.trim(),
      phone: form.phone.trim(),
      avatar: form.avatar.trim(),
      gender: form.gender,
      role: form.role,
      status: form.status,
    })
    toast('用户信息已更新', 'success')
    panelOpen.value = false
    await load()
  } catch (e) {
    formError.value = errorMessage(e)
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-toolbar">
      <div class="toolbar-left filter-bar">
        <input v-model="keyword" class="input" type="search" placeholder="搜索昵称 / 手机号" @keyup.enter="load" />
        <select v-model="role" class="select" @change="load">
          <option :value="null">全部角色</option>
          <option v-for="o in USER_ROLE_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</option>
        </select>
        <select v-model="status" class="select" @change="load">
          <option :value="null">全部状态</option>
          <option v-for="o in USER_STATUS_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</option>
        </select>
        <button class="btn btn-ghost btn-sm" type="button" @click="load">查询</button>
        <button class="text-button" type="button" @click="reset">重置</button>
      </div>
    </div>

    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>用户</th>
            <th>手机号</th>
            <th>性别</th>
            <th>角色</th>
            <th>状态</th>
            <th style="text-align: right">操作</th>
          </tr>
        </thead>
        <tbody v-if="!loading && !error">
          <tr v-for="user in users" :key="user.id">
            <td>
              <div class="cell-with-avatar">
                <img v-if="user.avatar" class="table-avatar" :src="user.avatar" :alt="user.nickname" />
                <span v-else class="table-avatar">{{ avatarFallback(user.nickname) }}</span>
                <div class="cell-meta">
                  <span class="cell-strong">{{ user.nickname }}</span>
                  <span class="cell-mono cell-faint">#{{ user.id }}</span>
                </div>
              </div>
            </td>
            <td class="cell-mono">{{ user.phone || '—' }}</td>
            <td><StatusBadge :label="genderMeta(user.gender).label" :tone="genderMeta(user.gender).tone" /></td>
            <td><StatusBadge :label="userRoleMeta(user.role).label" :tone="userRoleMeta(user.role).tone" /></td>
            <td><StatusBadge :label="userStatusMeta(user.status).label" :tone="userStatusMeta(user.status).tone" /></td>
            <td>
              <div class="cell-actions">
                <button class="btn btn-ghost btn-sm" type="button" @click="openEdit(user)">编辑</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <TableState :loading="loading" :error="error" :empty="!loading && !error && users.length === 0" empty-text="暂无匹配的用户" />
    </div>

    <SidePanel title="编辑用户" :subtitle="`用户 #${editing?.id ?? ''}`" :open="panelOpen" @close="closePanel">
      <div v-if="isSelf" class="inline-error">当前为系统管理员账号，登录手机号、角色与状态不可修改。</div>
      <div class="form-grid">
        <div class="field">
          <label class="field-label">昵称 <span class="req">*</span></label>
          <input v-model="form.nickname" class="input" maxlength="20" placeholder="用户昵称" />
        </div>
        <div class="field">
          <label class="field-label">手机号</label>
          <input v-model="form.phone" class="input" maxlength="20" placeholder="手机号" :disabled="isSelf" />
        </div>
        <div class="field span-2">
          <label class="field-label">头像地址</label>
          <input v-model="form.avatar" class="input" placeholder="https://…" />
          <span class="field-hint">留空则使用默认占位头像</span>
        </div>
        <div class="field">
          <label class="field-label">性别</label>
          <select v-model="form.gender" class="select">
            <option v-for="o in GENDER_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</option>
          </select>
        </div>
        <div class="field">
          <label class="field-label">角色</label>
          <select v-model="form.role" class="select" :disabled="isSelf">
            <option v-for="o in USER_ROLE_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</option>
          </select>
        </div>
        <div class="field">
          <label class="field-label">状态</label>
          <select v-model="form.status" class="select" :disabled="isSelf">
            <option v-for="o in USER_STATUS_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</option>
          </select>
        </div>
      </div>
      <div v-if="formError" class="inline-error" style="margin-top: 18px">{{ formError }}</div>
      <template #footer>
        <button class="btn btn-ghost" type="button" :disabled="saving" @click="closePanel">取消</button>
        <button class="btn btn-primary" type="button" :disabled="saving" @click="save">
          {{ saving ? '保存中…' : '保存' }}
        </button>
      </template>
    </SidePanel>
  </div>
</template>
