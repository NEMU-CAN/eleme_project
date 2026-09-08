import type { CategoryItem } from '@/types'

// 后端按 orderTypeId 查询商家；这里仅保留首页入口到后端筛选参数的映射。
export const categories: CategoryItem[] = [
  { id: '1', name: '川菜', image: '/eleme/dcfl01.png', route: '/businesses?tasteId=1' },
  { id: '2', name: '粤菜', image: '/eleme/dcfl02.png', route: '/businesses?tasteId=2' },
  { id: '3', name: '湘菜', image: '/eleme/dcfl03.png', route: '/businesses?tasteId=3' },
  { id: '4', name: '东北菜', image: '/eleme/dcfl04.png', route: '/businesses?tasteId=4' },
  { id: '5', name: '日料', image: '/eleme/dcfl05.png', route: '/businesses?tasteId=5' },
]
