<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { listBusinesses, listOrders, listUsers } from '@/services/admin'
import { orderStatusMeta } from '@/utils/dict'
import { errorMessage, money } from '@/utils/format'
import StatusBadge from '@/components/StatusBadge.vue'
import TableState from '@/components/TableState.vue'
import type { OrderSummary } from '@/types'

const loading = ref(true)
const error = ref('')

const users = ref<import('@/types').User[]>([])
const businesses = ref<import('@/types').Business[]>([])
const orderTotal = ref(0)
const recentOrders = ref<OrderSummary[]>([])

const customerCount = computed(() => users.value.filter((u) => u.role === 0).length)
const businessUserCount = computed(() => users.value.filter((u) => u.role === 1).length)
const openBusinessCount = computed(() => businesses.value.filter((b) => b.status === 1).length)

const roleBars = computed(() => [
  { label: '顾客', count: customerCount.value, color: 'linear-gradient(135deg,#2563eb,#3b82f6)' },
  { label: '商家', count: businessUserCount.value, color: 'linear-gradient(135deg,#0891b2,#22d3ee)' },
  { label: '管理员', count: users.value.filter((u) => u.role === 2).length, color: 'linear-gradient(135deg,#7c3aed,#a78bfa)' },
])
const maxRoleCount = computed(() => Math.max(1, ...roleBars.value.map((r) => r.count)))

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [userList, businessList, orderPage] = await Promise.all([
      listUsers(),
      listBusinesses(),
      listOrders({ page: 1, page_size: 6 }),
    ])
    users.value = userList
    businesses.value = businessList
    orderTotal.value = orderPage.total
    recentOrders.value = orderPage.records
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div>
    <section class="stat-grid" aria-label="核心指标">
      <article class="stat-card">
        <div class="stat-icon stat-blue">👥</div>
        <div class="stat-label">用户总数</div>
        <div class="stat-value">{{ users.length }}</div>
        <div class="stat-foot">平台注册用户</div>
      </article>
      <article class="stat-card">
        <div class="stat-icon stat-cyan">🏪</div>
        <div class="stat-label">商家总数</div>
        <div class="stat-value">{{ businesses.length }}</div>
        <div class="stat-foot">已入驻商家</div>
      </article>
      <article class="stat-card">
        <div class="stat-icon stat-violet">🧾</div>
        <div class="stat-label">订单总数</div>
        <div class="stat-value">{{ orderTotal }}</div>
        <div class="stat-foot">全平台订单</div>
      </article>
      <article class="stat-card">
        <div class="stat-icon stat-emerald">🟢</div>
        <div class="stat-label">营业中商家</div>
        <div class="stat-value">{{ openBusinessCount }}</div>
        <div class="stat-foot">当前营业</div>
      </article>
    </section>

    <div class="dashboard-grid">
      <section class="card">
        <header class="card-header">
          <div>
            <h2>最近订单</h2>
            <p class="card-sub">平台最新订单动态</p>
          </div>
        </header>
        <div class="card-body" style="padding: 0">
          <div class="table-wrap" style="border: none; border-radius: 0; box-shadow: none">
            <table class="data-table">
              <thead>
                <tr>
                  <th>订单号</th>
                  <th>用户</th>
                  <th>商家</th>
                  <th>实付金额</th>
                  <th>下单时间</th>
                  <th>状态</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in recentOrders" :key="row.order.id">
                  <td class="cell-mono">{{ row.order.orderNo }}</td>
                  <td>{{ row.order.userNickname }}</td>
                  <td>{{ row.order.businessName }}</td>
                  <td class="cell-strong">{{ money(row.order.actualAmount) }}</td>
                  <td class="cell-muted">{{ row.order.orderDate }}</td>
                  <td><StatusBadge :label="orderStatusMeta(row.order.orderStatus).label" :tone="orderStatusMeta(row.order.orderStatus).tone" /></td>
                </tr>
              </tbody>
            </table>
          </div>
          <TableState :loading="loading" :error="error" :empty="!loading && recentOrders.length === 0" empty-text="暂无订单数据" />
        </div>
      </section>

      <section class="card">
        <header class="card-header">
          <div>
            <h2>用户构成</h2>
            <p class="card-sub">按角色统计</p>
          </div>
        </header>
        <div class="card-body">
          <div class="donut-list">
            <div v-for="bar in roleBars" :key="bar.label" class="donut-row">
              <span class="bar-label">{{ bar.label }}</span>
              <span class="bar-track">
                <span class="bar-fill" :style="{ width: `${(bar.count / maxRoleCount) * 100}%`, background: bar.color }" />
              </span>
              <span class="bar-value">{{ bar.count }}</span>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>
