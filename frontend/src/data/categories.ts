import type { CategoryItem } from '@/types'

// 后端按 orderTypeId 查询商家；这里仅保留首页入口到后端筛选参数的映射。
export const categories: CategoryItem[] = [
  { id: '1', name: '美食', image: '/eleme/dcfl01.png', route: '/businesses?orderTypeId=1' },
  { id: '2', name: '早餐', image: '/eleme/dcfl02.png', route: '/businesses?orderTypeId=2' },
  { id: '3', name: '跑腿代购', image: '/eleme/dcfl03.png', route: '/businesses?orderTypeId=3' },
  { id: '4', name: '汉堡披萨', image: '/eleme/dcfl04.png', route: '/businesses?orderTypeId=4' },
  { id: '5', name: '甜品饮品', image: '/eleme/dcfl05.png', route: '/businesses?orderTypeId=5' },
  { id: '6', name: '速食简餐', image: '/eleme/dcfl06.png', route: '/businesses?orderTypeId=6' },
  { id: '7', name: '地方小吃', image: '/eleme/dcfl07.png', route: '/businesses?orderTypeId=7' },
  { id: '8', name: '米粉面馆', image: '/eleme/dcfl08.png', route: '/businesses?orderTypeId=8' },
  { id: '9', name: '包子粥铺', image: '/eleme/dcfl09.png', route: '/businesses?orderTypeId=9' },
  { id: '10', name: '炸鸡炸串', image: '/eleme/dcfl10.png', route: '/businesses?orderTypeId=10' },
]
