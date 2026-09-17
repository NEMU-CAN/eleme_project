<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  createBusiness,
  listBusinesses,
  listTastes,
  setBusinessStatus,
  updateBusiness,
} from '@/services/admin'
import { BUSINESS_STATUS_OPTIONS, businessStatusMeta } from '@/utils/dict'
import { errorMessage, money } from '@/utils/format'
import { toast } from '@/composables/useToast'
import StatusBadge from '@/components/StatusBadge.vue'
import SidePanel from '@/components/SidePanel.vue'
import TableState from '@/components/TableState.vue'
import type { Business, Taste } from '@/types'

const loading = ref(true)
const error = ref('')
const businesses = ref<Business[]>([])
const tastes = ref<Taste[]>([])

const keyword = ref('')
const tasteId = ref<number | null>(null)
const status = ref<number | null>(null)

const tasteMap = computed(() => new Map(tastes.value.map((t) => [t.id, t.name])))

async function load() {
  loading.value = true
  error.value = ''
  try {
    businesses.value = await listBusinesses({
      keyword: keyword.value || undefined,
      tasteId: tasteId.value,
      status: status.value,
    })
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    loading.value = false
  }
}

async function loadTastes() {
  try {
    tastes.value = await listTastes()
  } catch {
    /* 口味加载失败不阻塞商家列表 */
  }
}

function reset() {
  keyword.value = ''
  tasteId.value = null
  status.value = null
  load()
}

// ---------- 新建 / 编辑 ----------
const panelOpen = ref(false)
const editing = ref<Business | null>(null)
const saving = ref(false)
const formError = ref('')

const form = reactive({
  name: '',
  address: '',
  description: '',
  image: '',
  tasteId: 0,
  startPrice: '0',
  deliveryPrice: '0',
  status: 1,
})

function openCreate() {
  editing.value = null
  form.name = ''
  form.address = ''
  form.description = ''
  form.image = ''
  form.tasteId = tastes.value[0]?.id ?? 0
  form.startPrice = '0'
  form.deliveryPrice = '0'
  form.status = 1
  formError.value = ''
  panelOpen.value = true
}

function openEdit(business: Business) {
  editing.value = business
  form.name = business.name ?? ''
  form.address = business.address ?? ''
  form.description = business.description ?? ''
  form.image = business.image ?? ''
  form.tasteId = business.tasteId ?? 0
  form.startPrice = String(business.startPrice ?? 0)
  form.deliveryPrice = String(business.deliveryPrice ?? 0)
  form.status = business.status ?? 1
  formError.value = ''
  panelOpen.value = true
}

function closePanel() {
  if (!saving.value) panelOpen.value = false
}

function payload() {
  return {
    name: form.name.trim(),
    address: form.address.trim(),
    description: form.description.trim(),
    image: form.image.trim(),
    tasteId: form.tasteId,
    startPrice: parseFloat(form.startPrice) || 0,
    deliveryPrice: parseFloat(form.deliveryPrice) || 0,
    status: form.status,
  }
}

async function save() {
  if (saving.value) return
  formError.value = ''
  if (!form.name.trim()) {
    formError.value = '商家名称不能为空'
    return
  }
  if (!form.address.trim()) {
    formError.value = '商家地址不能为空'
    return
  }
  if (!form.tasteId) {
    formError.value = '请选择口味分类'
    return
  }
  saving.value = true
  try {
    if (editing.value) {
      await updateBusiness(editing.value.id, payload())
      toast('商家信息已更新', 'success')
    } else {
      await createBusiness(payload())
      toast('商家已创建', 'success')
    }
    panelOpen.value = false
    await load()
  } catch (e) {
    formError.value = errorMessage(e)
  } finally {
    saving.value = false
  }
}

async function toggleStatus(business: Business) {
  const next = business.status === 1 ? 0 : 1
  try {
    await setBusinessStatus(business.id, next)
    toast(next === 1 ? '商家已营业' : '商家已停业', 'success')
    await load()
  } catch (e) {
    toast(errorMessage(e), 'error')
  }
}

onMounted(() => {
  load()
  loadTastes()
})
</script>

<template>
  <div>
    <div class="page-toolbar">
      <div class="toolbar-left filter-bar">
        <input v-model="keyword" class="input" type="search" placeholder="搜索名称 / 地址" @keyup.enter="load" />
        <select v-model="tasteId" class="select" @change="load">
          <option :value="null">全部口味</option>
          <option v-for="t in tastes" :key="t.id" :value="t.id">{{ t.name }}</option>
        </select>
        <select v-model="status" class="select" @change="load">
          <option :value="null">全部状态</option>
          <option v-for="o in BUSINESS_STATUS_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</option>
        </select>
        <button class="btn btn-ghost btn-sm" type="button" @click="load">查询</button>
        <button class="text-button" type="button" @click="reset">重置</button>
      </div>
      <div class="toolbar-right">
        <button class="btn btn-primary btn-sm" type="button" @click="openCreate">+ 新建商家</button>
      </div>
    </div>

    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>商家</th>
            <th>地址</th>
            <th>口味</th>
            <th>起送价</th>
            <th>配送费</th>
            <th>状态</th>
            <th style="text-align: right">操作</th>
          </tr>
        </thead>
        <tbody v-if="!loading && !error">
          <tr v-for="b in businesses" :key="b.id">
            <td>
              <div class="cell-with-avatar">
                <img v-if="b.image" class="table-thumb" :src="b.image" :alt="b.name" />
                <span v-else class="table-thumb" style="display: grid; place-items: center; color: var(--text-faint)">🍜</span>
                <div class="cell-meta">
                  <span class="cell-strong">{{ b.name }}</span>
                  <span class="cell-faint truncate" :title="b.description || ''">{{ b.description || '—' }}</span>
                </div>
              </div>
            </td>
            <td class="cell-muted truncate" :title="b.address" style="max-width: 220px">{{ b.address }}</td>
            <td>{{ tasteMap.get(b.tasteId) || '—' }}</td>
            <td class="cell-mono">{{ money(b.startPrice) }}</td>
            <td class="cell-mono">{{ money(b.deliveryPrice) }}</td>
            <td><StatusBadge :label="businessStatusMeta(b.status).label" :tone="businessStatusMeta(b.status).tone" /></td>
            <td>
              <div class="cell-actions">
                <button class="btn btn-ghost btn-sm" type="button" @click="toggleStatus(b)">
                  {{ b.status === 1 ? '停业' : '营业' }}
                </button>
                <button class="btn btn-ghost btn-sm" type="button" @click="openEdit(b)">编辑</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <TableState :loading="loading" :error="error" :empty="!loading && !error && businesses.length === 0" empty-text="暂无匹配的商家" />
    </div>

    <SidePanel :title="editing ? '编辑商家' : '新建商家'" :subtitle="editing ? `商家 #${editing.id}` : '录入新的商家资料'" :open="panelOpen" @close="closePanel">
      <div class="form-grid">
        <div class="field">
          <label class="field-label">商家名称 <span class="req">*</span></label>
          <input v-model="form.name" class="input" maxlength="40" placeholder="商家名称" />
        </div>
        <div class="field">
          <label class="field-label">口味分类 <span class="req">*</span></label>
          <select v-model="form.tasteId" class="select">
            <option v-for="t in tastes" :key="t.id" :value="t.id">{{ t.name }}</option>
          </select>
        </div>
        <div class="field span-2">
          <label class="field-label">商家地址 <span class="req">*</span></label>
          <input v-model="form.address" class="input" maxlength="100" placeholder="商家地址" />
        </div>
        <div class="field span-2">
          <label class="field-label">商家简介</label>
          <textarea v-model="form.description" class="textarea" maxlength="255" placeholder="商家简介（可选）" />
        </div>
        <div class="field span-2">
          <label class="field-label">商家图片地址</label>
          <input v-model="form.image" class="input" placeholder="https://…" />
        </div>
        <div class="field">
          <label class="field-label">起送价（元）</label>
          <input v-model="form.startPrice" class="input" type="number" min="0" step="0.01" />
        </div>
        <div class="field">
          <label class="field-label">配送费（元）</label>
          <input v-model="form.deliveryPrice" class="input" type="number" min="0" step="0.01" />
        </div>
        <div class="field">
          <label class="field-label">营业状态</label>
          <select v-model="form.status" class="select">
            <option v-for="o in BUSINESS_STATUS_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</option>
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
