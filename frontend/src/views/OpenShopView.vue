<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import SiteHeader from '@/components/SiteHeader.vue'
import { categories } from '@/data/categories'

const router = useRouter()

const form = reactive({
  name: '',
  address: '',
  description: '',
  image: null as File | null,
  tasteId: '',
  startPrice: 0,
  deliveryPrice: 0,
  status: 1,
})

function selectImage(event: Event) {
  const input = event.target as HTMLInputElement
  form.image = input.files?.[0] ?? null
}
</script>

<template>
  <div class="open-shop-page">
    <SiteHeader title="我要开店" backable @back="router.push('/me')" />

    <section class="open-shop-hero">
      <img class="open-shop-hero__image" src="/eleme/merchant-onboarding.png" alt="饿了么商家入驻引导" />
      <div class="open-shop-hero__copy">
        <h2>欢迎入驻饿了么！</h2>
        <p>填写下列内容，即可获得门店账号</p>
      </div>
    </section>

    <section class="open-shop-form">
      <h3 class="open-shop-form__title">门店信息</h3>

      <div class="field">
        <label class="field__label" for="shop-name">门店名称<span class="field__required">*</span></label>
        <input id="shop-name" v-model="form.name" class="field__control" type="text" maxlength="40" placeholder="请输入门店名称" />
      </div>

      <div class="field field--top">
        <label class="field__label" for="shop-address">门店地址<span class="field__required">*</span></label>
        <textarea id="shop-address" v-model="form.address" class="field__control" rows="2" maxlength="100" placeholder="请输入详细地址" />
      </div>

      <div class="field field--top">
        <label class="field__label" for="shop-description">门店简介</label>
        <textarea id="shop-description" v-model="form.description" class="field__control" rows="3" maxlength="255" placeholder="介绍一下你的门店（选填）" />
      </div>

      <div class="field">
        <label class="field__label" for="shop-image">门店图片</label>
        <div class="file-picker">
          <label class="file-picker__button" for="shop-image">选择图片</label>
          <span class="file-picker__name">{{ form.image?.name || '未选择图片' }}</span>
          <input id="shop-image" class="file-picker__input" type="file" accept="image/*" @change="selectImage" />
        </div>
      </div>

      <div class="field">
        <label class="field__label" for="shop-taste">口味分类<span class="field__required">*</span></label>
        <select id="shop-taste" v-model="form.tasteId" class="field__control open-shop-select">
          <option value="" disabled>请选择口味分类</option>
          <option v-for="category in categories" :key="category.id" :value="category.id">{{ category.name }}</option>
        </select>
      </div>

      <div class="field">
        <label class="field__label" for="shop-start-price">起送价</label>
        <div class="open-shop-price">
          <span>¥</span>
          <input id="shop-start-price" v-model.number="form.startPrice" class="field__control" type="number" min="0" step="0.01" />
        </div>
      </div>

      <div class="field">
        <label class="field__label" for="shop-delivery-price">配送费</label>
        <div class="open-shop-price">
          <span>¥</span>
          <input id="shop-delivery-price" v-model.number="form.deliveryPrice" class="field__control" type="number" min="0" step="0.01" />
        </div>
      </div>

      <div class="field">
        <span class="field__label">营业状态</span>
        <div class="seg">
          <button type="button" class="seg__item" :class="{ 'seg__item--active': form.status === 1 }" @click="form.status = 1">营业中</button>
          <button type="button" class="seg__item" :class="{ 'seg__item--active': form.status === 0 }" @click="form.status = 0">休息中</button>
        </div>
      </div>
    </section>

    <p class="open-shop-warning">开店后，当前账号将变为商家账号，不能再用于点餐或购物车。</p>

    <div class="form-actions">
      <button type="button" class="primary-button">提交入驻</button>
    </div>
  </div>
</template>
