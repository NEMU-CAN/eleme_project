import type { Address, CategoryItem, Merchant, OrderRecord, UserProfile } from '@/types'

// 首页九宫格分类入口，对应课件里的点餐分类区。
export const categories: CategoryItem[] = [
  { id: 'c1', name: '美食', image: '/eleme/dcfl01.png', route: '/businesses' },
  { id: 'c2', name: '早餐', image: '/eleme/dcfl02.png', route: '/businesses' },
  { id: 'c3', name: '跑腿代购', image: '/eleme/dcfl03.png', route: '/businesses' },
  { id: 'c4', name: '汉堡披萨', image: '/eleme/dcfl04.png', route: '/businesses' },
  { id: 'c5', name: '甜品饮品', image: '/eleme/dcfl05.png', route: '/businesses' },
  { id: 'c6', name: '速食简餐', image: '/eleme/dcfl06.png', route: '/businesses' },
  { id: 'c7', name: '地方小吃', image: '/eleme/dcfl07.png', route: '/businesses' },
  { id: 'c8', name: '米粉面馆', image: '/eleme/dcfl08.png', route: '/businesses' },
  { id: 'c9', name: '包子粥铺', image: '/eleme/dcfl09.png', route: '/businesses' },
  { id: 'c10', name: '炸鸡炸串', image: '/eleme/dcfl10.png', route: '/businesses' },
]

// 商家 mock 数据，包含列表、详情、活动和菜单，支撑整条点餐流程。
export const merchants: Merchant[] = [
  {
    id: 'wanjia',
    name: '万家饺子（软件园E18店）',
    image: '/eleme/sj01.png',
    rating: 4.9,
    monthlySales: 345,
    minOrder: 15,
    deliveryFee: 3,
    distanceKm: 3.22,
    etaMinutes: 30,
    cuisine: '饺子 / 家常菜',
    highlight: '各种饺子炒菜',
    tags: ['蜂鸟专送', '新客立减', '热销'],
    banner: '/eleme/index_banner.png',
    promotions: ['饿了么新用户首单立减9元', '特价商品5元起'],
    menuSections: [
      {
        id: 'classic-dumplings',
        title: '招牌水饺',
        items: [
          { id: 'sp01', name: '纯肉鲜肉（水饺）', description: '新鲜猪肉，皮薄馅足', price: 15, image: '/eleme/sp01.png', tag: '招牌' },
          { id: 'sp02', name: '玉米鲜肉（水饺）', description: '玉米清甜，口感扎实', price: 16, image: '/eleme/sp02.png' },
          { id: 'sp03', name: '虾仁三鲜（蒸饺）', description: '弹嫩虾仁，蒸制更鲜', price: 22, image: '/eleme/sp03.png', tag: '热卖' },
        ],
      },
      {
        id: 'snacks',
        title: '热门加点',
        items: [
          { id: 'sp04', name: '素三鲜（蒸饺）', description: '爽口菌菇，清爽不腻', price: 15, image: '/eleme/sp04.png' },
          { id: 'sp05', name: '角瓜鸡蛋（蒸饺）', description: '清新角瓜搭配鸡蛋', price: 16, image: '/eleme/sp05.png' },
          { id: 'sp06', name: '小白菜肉（水饺）', description: '经典北方口味', price: 18, image: '/eleme/sp06.png' },
          { id: 'sp07', name: '芹菜牛肉（水饺）', description: '牛肉鲜香，层次清晰', price: 18, image: '/eleme/sp07.png' },
        ],
      },
    ],
  },
  {
    id: 'xiaoguo',
    name: '小锅饭豆腐馆（全运店）',
    image: '/eleme/sj02.png',
    rating: 4.8,
    monthlySales: 298,
    minOrder: 15,
    deliveryFee: 3,
    distanceKm: 2.58,
    etaMinutes: 26,
    cuisine: '家常菜 / 米饭',
    highlight: '米饭套餐，出餐快',
    tags: ['新品', '下饭菜', '热销'],
    banner: '/eleme/index_banner.png',
    promotions: ['下单满39减8', '午餐时段免配送费'],
    menuSections: [
      {
        id: 'rice-bowls',
        title: '热卖套餐',
        items: [
          { id: 'sp08', name: '锅包豆腐饭', description: '外酥里嫩，配香米饭', price: 19, image: '/eleme/sp08.png', tag: '推荐' },
          { id: 'sp09', name: '番茄牛腩饭', description: '酸甜开胃，汤汁浓郁', price: 24, image: '/eleme/sp09.png' },
          { id: 'sp10', name: '香菇鸡腿饭', description: '鸡腿嫩滑，香菇提鲜', price: 22, image: '/eleme/sp10.png' },
        ],
      },
      {
        id: 'side-dishes',
        title: '搭配小菜',
        items: [
          { id: 'sp11', name: '凉拌土豆丝', description: '清脆爽口', price: 8, image: '/eleme/sp11.png' },
          { id: 'sp12', name: '酸辣海带丝', description: '微辣解腻', price: 7, image: '/eleme/sp12.png' },
        ],
      },
    ],
  },
  {
    id: 'mcdelivery',
    name: '麦当劳麦乐送（全运路店）',
    image: '/eleme/sj03.png',
    rating: 4.9,
    monthlySales: 512,
    minOrder: 20,
    deliveryFee: 4,
    distanceKm: 4.06,
    etaMinutes: 22,
    cuisine: '汉堡 / 炸物',
    highlight: '标准化出餐，稳定配送',
    tags: ['品牌', '秒出餐', '全天候'],
    banner: '/eleme/index_banner.png',
    promotions: ['早餐时段赠饮品', '双人餐立减12元'],
    menuSections: [
      {
        id: 'burgers',
        title: '经典汉堡',
        items: [
          { id: 'sp13', name: '双层牛肉汉堡', description: '双层肉饼，芝士拉丝', price: 28, image: '/eleme/sp04.png' },
          { id: 'sp14', name: '脆鸡腿堡', description: '外脆里嫩，口感清爽', price: 24, image: '/eleme/sp05.png' },
          { id: 'sp15', name: '大薯条分享装', description: '现炸现出，适合搭配', price: 12, image: '/eleme/sp06.png' },
        ],
      },
    ],
  },
  {
    id: 'miyuan',
    name: '米村拌饭（浑南店）',
    image: '/eleme/sj04.png',
    rating: 4.8,
    monthlySales: 264,
    minOrder: 18,
    deliveryFee: 3,
    distanceKm: 3.8,
    etaMinutes: 28,
    cuisine: '拌饭 / 炒菜',
    highlight: '热锅快炒，出餐利落',
    tags: ['热菜', '米饭', '拌饭'],
    banner: '/eleme/index_banner.png',
    promotions: ['新客首单立减6元', '拌饭套餐第二份半价'],
    menuSections: [
      {
        id: 'rice',
        title: '拌饭套餐',
        items: [
          { id: 'sp16', name: '牛肉石锅拌饭', description: '韩式风味，锅底微焦', price: 26, image: '/eleme/sp07.png', tag: '招牌' },
          { id: 'sp17', name: '鸡排芝士拌饭', description: '浓香芝士，饱腹感强', price: 27, image: '/eleme/sp08.png' },
          { id: 'sp18', name: '肥牛金针菇拌饭', description: '鲜香浓郁', price: 25, image: '/eleme/sp09.png' },
        ],
      },
    ],
  },
  {
    id: 'chuan',
    name: '申记串道（中海康城店）',
    image: '/eleme/sj05.png',
    rating: 4.7,
    monthlySales: 189,
    minOrder: 15,
    deliveryFee: 3,
    distanceKm: 3.35,
    etaMinutes: 32,
    cuisine: '烧烤 / 炸串',
    highlight: '夜宵友好，口味偏重',
    tags: ['夜宵', '烧烤', '聚会'],
    banner: '/eleme/index_banner.png',
    promotions: ['夜间时段满49减10', '第二件半价'],
    menuSections: [
      {
        id: 'skewers',
        title: '热门串烤',
        items: [
          { id: 'sp19', name: '秘制羊肉串', description: '孜然香气足', price: 12, image: '/eleme/sp10.png' },
          { id: 'sp20', name: '孜然鸡翅中', description: '外皮焦香', price: 18, image: '/eleme/sp11.png' },
          { id: 'sp21', name: '烤茄子', description: '蒜香浓郁', price: 16, image: '/eleme/sp12.png' },
        ],
      },
    ],
  },
  {
    id: 'guo',
    name: '半亩良田排骨米饭',
    image: '/eleme/sj06.png',
    rating: 4.7,
    monthlySales: 201,
    minOrder: 15,
    deliveryFee: 3,
    distanceKm: 2.96,
    etaMinutes: 24,
    cuisine: '排骨饭 / 盖饭',
    highlight: '肉菜搭配均衡',
    tags: ['盖饭', '排骨', '实惠'],
    banner: '/eleme/index_banner.png',
    promotions: ['套餐加饮品减3元', '午市特惠'],
    menuSections: [
      {
        id: 'set-meals',
        title: '经典盖饭',
        items: [
          { id: 'sp22', name: '红烧排骨饭', description: '酱香浓厚', price: 23, image: '/eleme/sp01.png' },
          { id: 'sp23', name: '香辣鸡丁饭', description: '微辣开胃', price: 21, image: '/eleme/sp02.png' },
          { id: 'sp24', name: '咖喱牛肉饭', description: '咖喱柔和', price: 24, image: '/eleme/sp03.png' },
        ],
      },
    ],
  },
]

// 收货地址 mock，供结算页、订单页和个人页复用。
export const addresses: Address[] = [
  {
    id: 'addr-1',
    name: '刘晨',
    phone: '13656785432',
    detail: '沈阳市浑南区智慧四街1-121号',
    note: '公司楼下自取柜附近',
  },
  {
    id: 'addr-2',
    name: '周岚',
    phone: '13821004512',
    detail: '天津市西青区海泰南道 88 号 4 层',
    note: '午间电话联系',
  },
]

// 默认演示用户，用于登录、注册和个人中心展示。
export const defaultUser: UserProfile = {
  name: '刘晨',
  phone: '13656785432',
  gender: 'male',
  avatar: '/eleme/userImg/userImg.png',
}

// 预置两笔订单，让订单页和支付页一打开就有可看的内容。
export const seededOrders: OrderRecord[] = [
  {
    id: 'order-1001',
    merchantId: 'wanjia',
    merchantName: '万家饺子（软件园E18店）',
    merchantImage: '/eleme/sj01.png',
    status: 'pending',
    paymentMethod: 'alipay',
    addressId: 'addr-1',
    addressName: '刘晨',
    addressPhone: '13656785432',
    addressDetail: '沈阳市浑南区智慧四街1-121号',
    items: [
      { id: 'sp01', name: '纯肉鲜肉（水饺）', price: 15, image: '/eleme/sp01.png', quantity: 2 },
      { id: 'sp02', name: '玉米鲜肉（水饺）', price: 16, image: '/eleme/sp02.png', quantity: 1 },
    ],
    deliveryFee: 3,
    subtotal: 46,
    total: 49,
    createdAt: '2026-08-30T10:20:00',
  },
  {
    id: 'order-1002',
    merchantId: 'xiaoguo',
    merchantName: '小锅饭豆腐馆（全运店）',
    merchantImage: '/eleme/sj02.png',
    status: 'paid',
    paymentMethod: 'wechat',
    addressId: 'addr-1',
    addressName: '刘晨',
    addressPhone: '13656785432',
    addressDetail: '沈阳市浑南区智慧四街1-121号',
    items: [
      { id: 'sp08', name: '锅包豆腐饭', price: 19, image: '/eleme/sp08.png', quantity: 2 },
      { id: 'sp11', name: '凉拌土豆丝', price: 8, image: '/eleme/sp11.png', quantity: 1 },
    ],
    deliveryFee: 3,
    subtotal: 46,
    total: 49,
    createdAt: '2026-08-27T18:40:00',
    paidAt: '2026-08-27T18:43:00',
  },
]
