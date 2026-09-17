<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getOrder, listOrders } from '@/services/admin'
import { genderMeta, orderStatusMeta } from '@/utils/dict'
import { errorMessage, money } from '@/utils/format'
import StatusBadge from '@/components/StatusBadge.vue'
import SidePanel from '@/components/SidePanel.vue'
import TableState from '@/components/TableState.vue'
import type { OrderDetail, OrderSummary } from '@/types'

const loading = ref(true)
const error = ref('')
const records = ref<OrderSummary[]>([])

const orderStatus = ref<number | null>(null)
const page = ref(1)
const pageSize = 10
const total = ref(0)

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)))

const pageItems = computed<(number | '…')[]>(() => {
  const tp = totalPages.value
  if (tp <= 1) return []
  const cur = page.value
  const nums = new Set<number>([1, tp])
  for (let i = cur - 2; i <= cur + 2; i++) if (i >= 1 && i <= tp) nums.add(i)
  const sorted = [...nums].sort((a, b) => a - b)
  const out: (number | '…')[] = []
  let prev = 0
  for (const n of sorted) {
    if (prev && n - prev > 1) out.push('…')
    out.push(n)
    prev = n
  }
  return out
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    const result = await listOrders({
      order_status: orderStatus.value,
      page: page.value,
      page_size: pageSize,
    })
    records.value = result.records
    total.value = result.total
    if (result.records.length === 0 && page.value > 1) {
      page.value = Math.max(1, totalPages.value)
      return load()
    }
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    loading.value = false
  }
}

function go(p: number) {
  if (p < 1 || p > totalPages.value || p === page.value) return
  page.value = p
  load()
}

function applyFilter() {
  page.value = 1
  load()
}

function reset() {
  orderStatus.value = null
  applyFilter()
}

// ---------- 详情 ----------
const panelOpen = ref(false)
const detailLoading = ref(false)
const detailError = ref('')
const detail = ref<OrderDetail | null>(null)

async function openDetail(id: number) {
  panelOpen.value = true
  detailLoading.value = true
  detailError.value = ''
  detail.value = null
  try {
    detail.value = await getOrder(id)
  } catch (e) {
    detailError.value = errorMessage(e)
  } finally {
    detailLoading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-toolbar">
      <div class="toolbar-left filter-bar">
        <select v-model="orderStatus" class="select" @change="applyFilter">
          <option :value="null">全部状态</option>
          <option :value="-1">已取消</option>
          <option :value="0">待支付</option>
          <option :value="1">已支付</option>
          <option :value="2">已完成</option>
        </select>
        <button class="text-button" type="button" @click="reset">重置</button>
      </div>
      <div class="toolbar-right">
        <span class="cell-muted">共 {{ total }} 笔订单（只读）</span>
      </div>
    </div>

    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>订单号</th>
            <th>用户</th>
            <th>商家</th>
            <th>实付金额</th>
            <th>下单时间</th>
            <th>状态</th>
            <th style="text-align: right">操作</th>
          </tr>
        </thead>
        <tbody v-if="!loading && !error">
          <tr v-for="row in records" :key="row.order.id">
            <td class="cell-mono">{{ row.order.orderNo }}</td>
            <td>
              <div class="cell-meta">
                <span>{{ row.order.userNickname }}</span>
                <span class="cell-faint cell-mono">{{ row.order.userPhone }}</span>
              </div>
            </td>
            <td>{{ row.order.businessName }}</td>
            <td class="cell-strong">{{ money(row.order.actualAmount) }}</td>
            <td class="cell-muted">{{ row.order.orderDate }}</td>
            <td><StatusBadge :label="orderStatusMeta(row.order.orderStatus).label" :tone="orderStatusMeta(row.order.orderStatus).tone" /></td>
            <td>
              <div class="cell-actions">
                <button class="btn btn-ghost btn-sm" type="button" @click="openDetail(row.order.id)">详情</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <TableState :loading="loading" :error="error" :empty="!loading && !error && records.length === 0" empty-text="暂无订单数据" />
    </div>

    <div v-if="!loading && !error && totalPages > 1" class="pagination">
      <span>第 {{ page }} / {{ totalPages }} 页</span>
      <div class="pages">
        <button type="button" :disabled="page <= 1" @click="go(page - 1)">上一页</button>
        <template v-for="(item, idx) in pageItems" :key="idx">
          <span v-if="item === '…'" class="cell-muted" style="padding: 0 4px">…</span>
          <button v-else type="button" :class="{ current: item === page }" @click="go(item)">{{ item }}</button>
        </template>
        <button type="button" :disabled="page >= totalPages" @click="go(page + 1)">下一页</button>
      </div>
    </div>

    <SidePanel title="订单详情" :subtitle="detail ? `订单号 ${detail.order.orderNo}` : '加载中…'" :open="panelOpen" @close="panelOpen = false">
      <div v-if="detailLoading" class="table-state"><p>正在加载订单明细…</p></div>
      <div v-else-if="detailError" class="table-state state-error"><strong>加载失败</strong><p>{{ detailError }}</p></div>
      <template v-else-if="detail">
        <h3 class="section-title">订单信息</h3>
        <div class="desc-list">
          <div class="desc-item"><span class="desc-label">订单状态</span><span class="desc-value"><StatusBadge :label="orderStatusMeta(detail.order.orderStatus).label" :tone="orderStatusMeta(detail.order.orderStatus).tone" /></span></div>
          <div class="desc-item"><span class="desc-label">下单时间</span><span class="desc-value">{{ detail.order.orderDate }}</span></div>
          <div class="desc-item"><span class="desc-label">商品总额</span><span class="desc-value">{{ money(detail.order.totalAmount) }}</span></div>
          <div class="desc-item"><span class="desc-label">配送费</span><span class="desc-value">{{ money(detail.order.deliveryPrice) }}</span></div>
          <div class="desc-item"><span class="desc-label">实付金额</span><span class="desc-value cell-strong">{{ money(detail.order.actualAmount) }}</span></div>
          <div class="desc-item"><span class="desc-label">商家</span><span class="desc-value">{{ detail.order.businessName }}</span></div>
          <div class="desc-item"><span class="desc-label">商家地址</span><span class="desc-value">{{ detail.order.businessAddress }}</span></div>
          <div class="desc-item"><span class="desc-label">下单用户</span><span class="desc-value">{{ detail.order.userNickname }}（{{ detail.order.userPhone }}）</span></div>
        </div>

        <h3 class="section-title" style="margin-top: 24px">收货信息</h3>
        <div class="desc-list">
          <div class="desc-item"><span class="desc-label">收货人</span><span class="desc-value">{{ detail.order.receiverName }}</span></div>
          <div class="desc-item"><span class="desc-label">联系电话</span><span class="desc-value">{{ detail.order.receiverTel }}</span></div>
          <div class="desc-item"><span class="desc-label">性别</span><span class="desc-value">{{ genderMeta(detail.order.receiverGender).label }}</span></div>
          <div class="desc-item span-2"><span class="desc-label">收货地址</span><span class="desc-value">{{ detail.order.receiverAddress }}</span></div>
        </div>

        <h3 class="section-title" style="margin-top: 24px">商品明细</h3>
        <div class="table-wrap" style="box-shadow: none">
          <table class="data-table">
            <thead>
              <tr><th>商品</th><th>单价</th><th>数量</th><th style="text-align: right">小计</th></tr>
            </thead>
            <tbody>
              <tr v-for="item in detail.details" :key="item.id">
                <td>{{ item.foodName }}</td>
                <td class="cell-mono">{{ money(item.foodPrice) }}</td>
                <td class="cell-mono">×{{ item.quantity }}</td>
                <td class="cell-strong" style="text-align: right">{{ money(item.subtotal) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </template>
    </SidePanel>
  </div>
</template>
