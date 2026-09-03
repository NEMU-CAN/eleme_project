# 饿了么项目 RESTful 接口文档（阶段1）

> 说明：本文档依据阶段1开发要求与题目给出的表结构整理，面向“前后端分离”的接口协作场景。  
> 设计目标是：逻辑清晰、可落地、便于后续扩展。

## 1. 设计原则

1. 前后端分离：前端只通过 JSON 接口获取和提交数据，不直接依赖数据库。
2. RESTful 风格：资源用名词表达，查询、创建、修改、删除分别使用 `GET`、`POST`、`PUT/PATCH`、`DELETE`。
3. 统一返回：所有接口都使用统一响应结构，便于前端拦截和错误处理。
4. 版本管理：接口统一放在 `/api/v1` 下，后续升级时可平滑扩展。
5. 资源分层：公共店铺展示、商家后台管理、用户中心、购物车、订单各自独立，但共享同一套业务数据。
6. TDD 友好：每个接口都应先明确成功、失败、边界三类场景，再补业务实现。

## 2. 通用约定

### 2.1 基础信息

- Base URL：`/api/v1`
- 数据格式：`application/json`
- 字符编码：`UTF-8`
- 时间格式：`yyyy-MM-dd HH:mm:ss`
- 金额单位：元，统一保留两位小数

### 2.2 请求头

```http
Content-Type: application/json
Authorization: Bearer <token>
```

说明：

- `Authorization` 为登录后携带的令牌，推荐使用 JWT。
- 如果当前阶段还未启用登录态校验，可先保留该字段作为扩展位。

### 2.3 统一响应结构

```json
{
  "code": 0,
  "message": "ok",
  "data": {},
  "timestamp": "2026-09-01T13:00:00+08:00"
}
```

字段说明：

- `code`：业务状态码，`0` 表示成功，非 `0` 表示失败。
- `message`：提示信息。
- `data`：返回数据主体。
- `timestamp`：服务端时间戳，便于排查问题。

### 2.4 分页约定

列表接口统一支持以下参数：

- `page`：页码，从 `1` 开始
- `size`：每页条数，默认 `10`
- `keyword`：关键字查询
- `sortBy`：排序字段
- `sortOrder`：排序方式，`asc` / `desc`

分页响应建议：

```json
{
  "page": 1,
  "size": 10,
  "total": 56,
  "list": []
}
```

### 2.5 通用状态码

| HTTP 状态 | 业务码 | 说明 |
|---|---:|---|
| 200 | 0 | 成功 |
| 400 | 40001 | 请求参数错误 |
| 401 | 40101 | 未登录或令牌失效 |
| 403 | 40301 | 无权限访问 |
| 404 | 40401 | 资源不存在 |
| 409 | 40901 | 资源冲突 |
| 500 | 50000 | 系统异常 |

## 3. 业务字典

### 3.1 商家分类 `orderTypeId`

| 值 | 分类名称 |
|---:|---|
| 1 | 美食 |
| 2 | 早餐 |
| 3 | 跑腿代购 |
| 4 | 汉堡披萨 |
| 5 | 甜品饮品 |
| 6 | 速食简餐 |
| 7 | 地方小吃 |
| 8 | 米粉面馆 |
| 9 | 包子粥铺 |
| 10 | 炸鸡炸串 |

### 3.2 性别枚举

| 值 | 含义 |
|---:|---|
| 0 | 女 |
| 1 | 男 |

### 3.3 订单状态 `orderState`

当前阶段建议先实现以下状态：

| 值 | 含义 |
|---:|---|
| 0 | 未支付 |
| 1 | 已支付 |

后续如果需要退款、取消、配送完成等状态，可以继续扩展，不影响现有接口路径。

### 3.4 商家状态 `businessState`

为了满足“营业 / 休息 / 临时闭店”的需求，建议接口统一返回以下状态：

| 值 | 含义 |
|---:|---|
| 0 | 休息 |
| 1 | 营业中 |
| 2 | 临时闭店 |

说明：如果当前数据库表还未包含该字段，可在商家表中补充，或在服务层做状态扩展，不建议把状态逻辑散落到多个地方。

### 3.5 库存字段说明

题目截图中的 `food` 表没有独立库存字段，但阶段1开发要求包含“库存展示”。建议先在接口层预留 `stock` 字段：

- 当前阶段：可由服务层返回默认库存或模拟库存。
- 后续阶段：推荐在 `food` 表补充 `stock` 字段，或新增库存表独立管理。
- 前端展示：统一读取接口返回的 `stock`，不要在页面中写死库存文案。

## 4. 核心资源接口

### 4.1 登录与注册

#### 4.1.1 创建登录会话

`POST /api/v1/sessions`

请求体：

```json
{
  "userId": "u10001",
  "password": "123456"
}
```

响应示例：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "userId": "u10001",
      "userName": "刘晨",
      "userSex": 1,
      "userImg": "/images/user.png"
    }
  }
}
```

#### 4.1.2 退出登录

`DELETE /api/v1/sessions/current`

说明：当前登录态失效即可，不需要物理删除用户信息。

#### 4.1.3 用户注册

`POST /api/v1/users`

请求体：

```json
{
  "userId": "u10001",
  "password": "123456",
  "userName": "刘晨",
  "userSex": 1,
  "userImg": "/images/user.png"
}
```

#### 4.1.4 获取个人信息

`GET /api/v1/users/{userId}`

#### 4.1.5 修改个人信息

`PUT /api/v1/users/{userId}`

建议更新字段：

- `password`
- `userName`
- `userSex`
- `userImg`

### 4.2 店铺展示与商家管理

说明：

- `shops`：面向前台用户的店铺展示资源。
- `businesses`：面向商家后台的管理资源。
- 两者底层可共用 `business` 表，只是返回字段和权限不同。

#### 4.2.1 店铺列表

`GET /api/v1/shops`

常用查询参数：

- `orderTypeId`：分类筛选
- `keyword`：店铺名称关键字
- `businessState`：营业状态筛选
- `page`、`size`
- `sortBy`：`starPrice`、`deliveryPrice`、`distance`、`monthlySales`

#### 4.2.2 店铺详情

`GET /api/v1/shops/{businessId}`

建议返回字段：

- 店铺基础信息
- 分类信息
- 起送价、配送费
- 营业状态
- 评分、销量、距离、预计送达时间
- 活动信息

#### 4.2.3 商家注册

`POST /api/v1/businesses`

请求体建议：

```json
{
  "businessName": "万家饺子（软件园E18店）",
  "businessAddress": "沈阳市浑南区...",
  "businessExplain": "各种饺子炒菜",
  "businessImg": "/images/business.png",
  "orderTypeId": 1,
  "starPrice": 15.00,
  "deliveryPrice": 3.00,
  "remarks": "新店开业",
  "businessState": 1
}
```

#### 4.2.4 修改商家信息

`PUT /api/v1/businesses/{businessId}`

#### 4.2.5 切换商家状态

`PATCH /api/v1/businesses/{businessId}`

请求体示例：

```json
{
  "businessState": 2
}
```

说明：

- `1`：营业中
- `0`：休息
- `2`：临时闭店

### 4.3 食品资源

#### 4.3.1 某商家的食品列表

`GET /api/v1/businesses/{businessId}/foods`

支持参数：

- `keyword`
- `categoryId`：食品分类，当前数据库未提供时可先保留扩展位
- `page`
- `size`

#### 4.3.2 食品详情

`GET /api/v1/foods/{foodId}`

#### 4.3.3 新增食品

`POST /api/v1/businesses/{businessId}/foods`

请求体：

```json
{
  "foodName": "纯肉鲜肉（水饺）",
  "foodExplain": "新鲜猪肉，皮薄馅足",
  "foodImg": "/images/food.png",
  "foodPrice": 15.00,
  "stock": 100,
  "remarks": "招牌"
}
```

#### 4.3.4 修改食品信息

`PUT /api/v1/foods/{foodId}`

#### 4.3.5 删除食品

`DELETE /api/v1/foods/{foodId}`

说明：推荐优先做逻辑删除，避免订单引用历史数据时出现断链。

#### 4.3.6 修改食品库存

`PATCH /api/v1/foods/{foodId}/stock`

请求体：

```json
{
  "stock": 88
}
```

说明：当前数据库表未包含库存字段时，该接口可以先不实现，只在文档中保留契约，后续扩展时不影响前端调用方式。

### 4.4 购物车资源

购物车以“用户 + 商家”维度管理，更符合实际点餐流程。

#### 4.4.1 获取购物车

`GET /api/v1/users/{userId}/cart-items`

可选参数：

- `businessId`：只看某一家店铺的购物车

#### 4.4.2 加入购物车

`POST /api/v1/users/{userId}/cart-items`

请求体：

```json
{
  "businessId": 1,
  "foodId": 101,
  "quantity": 2
}
```

#### 4.4.3 修改购物车数量

`PATCH /api/v1/cart-items/{cartId}`

请求体：

```json
{
  "quantity": 3
}
```

#### 4.4.4 删除购物车条目

`DELETE /api/v1/cart-items/{cartId}`

#### 4.4.5 清空某商家购物车

`DELETE /api/v1/users/{userId}/cart-items?businessId=1`

### 4.5 收货地址资源

#### 4.5.1 地址列表

`GET /api/v1/users/{userId}/addresses`

#### 4.5.2 新增地址

`POST /api/v1/users/{userId}/addresses`

请求体：

```json
{
  "contactName": "刘晨",
  "contactSex": 1,
  "contactTel": "13656785432",
  "address": "沈阳市浑南区智慧四街1-121号"
}
```

#### 4.5.3 修改地址

`PUT /api/v1/addresses/{daId}`

#### 4.5.4 删除地址

`DELETE /api/v1/addresses/{daId}`

### 4.6 订单资源

#### 4.6.1 创建订单

`POST /api/v1/orders`

请求体建议：

```json
{
  "userId": "u10001",
  "businessId": 1,
  "daId": 2,
  "paymentMethod": "alipay",
  "items": [
    {
      "foodId": 101,
      "quantity": 2
    },
    {
      "foodId": 102,
      "quantity": 1
    }
  ]
}
```

说明：

- 创建订单时，后端应根据食品价格重新计算 `orderTotal`，不要信任前端直接传来的总价。
- `items` 会落到 `orderdetail` 表。

#### 4.6.2 订单列表

`GET /api/v1/orders`

支持参数：

- `userId`
- `businessId`
- `orderState`
- `page`
- `size`

#### 4.6.3 订单详情

`GET /api/v1/orders/{orderId}`

建议同时返回：

- 订单主表信息
- 商家信息
- 收货地址信息
- 订单明细列表

#### 4.6.4 订单明细

`GET /api/v1/orders/{orderId}/items`

#### 4.6.5 更新订单状态

`PATCH /api/v1/orders/{orderId}`

请求体示例：

```json
{
  "orderState": 1
}
```

#### 4.6.6 支付订单

`POST /api/v1/orders/{orderId}/payments`

请求体示例：

```json
{
  "paymentMethod": "wechat"
}
```

说明：支付成功后将订单状态更新为 `1`（已支付）。

### 4.7 字典资源

#### 4.7.1 商家分类字典

`GET /api/v1/dictionaries/order-types`

#### 4.7.2 支付方式字典

`GET /api/v1/dictionaries/payment-methods`

建议返回值：

- `alipay`：支付宝
- `wechat`：微信支付

## 5. 数据库表结构映射

下面字段名根据题目截图整理，接口层建议保持与数据库字段一一对应，便于实现和联调。

### 5.1 business（商家表）

| 字段名 | 类型 | 约束 | 说明 |
|---|---|---|---|
| businessId | int | PK, AI, NN | 商家编号 |
| businessName | varchar(40) | NN | 商家名称 |
| businessAddress | varchar(50) |  | 商家地址 |
| businessExplain | varchar(40) |  | 商家介绍 |
| businessImg | mediumtext | NN | 商家图片 |
| orderTypeId | int | NN | 点餐分类 |
| starPrice | decimal(5,2) | 默认 0.00 | 起送费 |
| deliveryPrice | decimal(5,2) | 默认 0.00 | 配送费 |
| remarks | varchar(40) |  | 备注 |

### 5.2 food（食品表）

| 字段名 | 类型 | 约束 | 说明 |
|---|---|---|---|
| foodId | int | PK, AI, NN | 食品编号 |
| foodName | varchar(30) | NN | 食品名称 |
| foodExplain | varchar(30) | NN | 食品介绍 |
| foodImg | mediumtext | NN | 食品图片 |
| foodPrice | decimal(5,2) | NN | 食品价格 |
| businessId | int | FK, NN | 所属商家编号 |
| remarks | varchar(40) |  | 备注 |

### 5.3 cart（购物车表）

| 字段名 | 类型 | 约束 | 说明 |
|---|---|---|---|
| cartId | int | PK, AI, NN | 无意义编号 |
| foodId | int | FK, NN | 食品编号 |
| businessId | int | FK, NN | 所属商家编号 |
| userId | varchar(20) | FK, NN | 所属用户编号 |
| quantity | int | NN | 同一类型食品的购买数量 |

### 5.4 deliveryaddress（送货地址表）

| 字段名 | 类型 | 约束 | 说明 |
|---|---|---|---|
| daId | int | PK, AI, NN | 送货地址编号 |
| contactName | varchar(20) | NN | 联系人姓名 |
| contactSex | int | NN | 联系人性别 |
| contactTel | varchar(20) | NN | 联系人电话 |
| address | varchar(100) | NN | 送货地址 |
| userId | varchar(20) | FK, NN | 所属用户编号 |

### 5.5 orderinfo（订单表）

| 字段名 | 类型 | 约束 | 说明 |
|---|---|---|---|
| orderId | int | PK, AI, NN | 订单编号 |
| userId | varchar(20) | FK, NN | 所属用户编号 |
| businessId | int | FK, NN | 所属商家编号 |
| orderDate | varchar(20) | NN | 订购日期 |
| orderTotal | decimal(7,2) | 默认 0.00, NN | 订单总价 |
| daId | int | FK, NN | 所属送货地址编号 |
| orderState | int | 默认 0, NN | 订单状态 |

### 5.6 orderdetail（订单明细表）

| 字段名 | 类型 | 约束 | 说明 |
|---|---|---|---|
| odId | int | PK, AI, NN | 订单明细编号 |
| orderId | int | FK, NN | 所属订单编号 |
| foodId | int | FK, NN | 所属食品编号 |
| quantity | int | NN | 数量 |

### 5.7 user（用户表）

| 字段名 | 类型 | 约束 | 说明 |
|---|---|---|---|
| userId | varchar(20) | PK, NN | 用户编号 |
| password | varchar(20) | NN | 密码 |
| userName | varchar(20) | NN | 用户名称 |
| userSex | int | 默认 1, NN | 用户性别 |
| userImg | mediumtext |  | 用户头像 |
| delTag | int | 默认 1, NN | 删除标记 |

说明：接口返回用户信息时不返回 `password`。如果后续允许调整表结构，建议将明文密码字段升级为密码摘要字段，例如 `passwordHash`。

## 6. 关键接口示例

### 6.1 店铺列表示例

请求：

```http
GET /api/v1/shops?page=1&size=10&orderTypeId=1&keyword=饺子
```

响应：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "page": 1,
    "size": 10,
    "total": 1,
    "list": [
      {
        "businessId": 1,
        "businessName": "万家饺子（软件园E18店）",
        "businessAddress": "沈阳市浑南区...",
        "businessExplain": "各种饺子炒菜",
        "businessImg": "/images/business.png",
        "orderTypeId": 1,
        "businessState": 1,
        "starPrice": 15.0,
        "deliveryPrice": 3.0,
        "remarks": "热销"
      }
    ]
  }
}
```

### 6.2 下单示例

请求：

```http
POST /api/v1/orders
```

```json
{
  "userId": "u10001",
  "businessId": 1,
  "daId": 2,
  "paymentMethod": "wechat",
  "items": [
    {
      "foodId": 101,
      "quantity": 2
    }
  ]
}
```

响应：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "orderId": 10001,
    "orderState": 0,
    "orderTotal": 49.0,
    "orderDate": "2026-09-01 13:00:00"
  }
}
```

### 6.3 支付示例

请求：

```http
POST /api/v1/orders/10001/payments
```

```json
{
  "paymentMethod": "alipay"
}
```

响应：

```json
{
  "code": 0,
  "message": "支付成功",
  "data": {
    "orderId": 10001,
    "orderState": 1
  }
}
```

## 7. 前端页面对接建议

| 页面 | 建议调用接口 |
|---|---|
| 首页 | `GET /api/v1/shops`、`GET /api/v1/dictionaries/order-types` |
| 商家列表 | `GET /api/v1/shops` |
| 商家详情 | `GET /api/v1/shops/{businessId}`、`GET /api/v1/businesses/{businessId}/foods` |
| 购物车 | `GET /api/v1/users/{userId}/cart-items`、`POST /api/v1/users/{userId}/cart-items` |
| 确认订单 | `GET /api/v1/users/{userId}/addresses`、`POST /api/v1/orders` |
| 支付页面 | `GET /api/v1/orders/{orderId}`、`POST /api/v1/orders/{orderId}/payments` |
| 订单列表 | `GET /api/v1/orders?userId=...` |
| 个人中心 | `GET /api/v1/users/{userId}`、`GET /api/v1/users/{userId}/addresses` |

## 8. 扩展说明

1. `shops` 与 `businesses` 可以共享同一张商家表，但必须区分“前台展示”和“后台管理”两个视图。
2. `orderdetail` 适合做订单明细子资源，订单主表只保存汇总信息，避免一张表塞入过多冗余数据。
3. 图片字段建议统一返回可访问 URL，前端只关心展示，不直接处理数据库存储格式。
4. 购物车、订单、地址都应基于 `userId` 做归属，后续如果引入多角色系统，也方便继续扩展权限控制。
5. 当前文档以阶段1为主，后续可在不改路径的前提下补充分页、排序、状态流转和权限校验。

## 9. TDD 验收建议

阶段1要求“先写单元测试用例，再开发业务代码”。接口实现时建议每个模块至少覆盖以下场景：

| 模块 | 成功场景 | 失败 / 边界场景 |
|---|---|---|
| 用户 | 注册成功、登录成功、查询个人信息成功 | 用户不存在、密码错误、重复注册、被删除用户不可登录 |
| 商家 | 商家注册成功、查询店铺列表成功、状态切换成功 | 商家不存在、分类非法、状态值非法 |
| 店铺 | 按分类查询、按关键字查询、查询详情 | 空列表、无效 `businessId` |
| 食品 | 新增食品、查询食品列表、价格与库存正常返回 | 食品不存在、价格小于 0、库存小于 0、商家不存在 |
| 购物车 | 加入购物车、修改数量、查询购物车 | 数量小于 1、食品不存在、跨用户修改购物车 |
| 地址 | 新增地址、修改地址、按用户查询地址列表 | 手机号为空、地址为空、用户不存在 |
| 订单 | 创建订单、查询订单列表、查询订单详情、支付成功 | 空购物车下单、库存不足、地址不存在、重复支付 |

建议测试命名使用“业务含义 + 预期结果”，例如：

- `login_shouldReturnToken_whenPasswordCorrect`
- `createOrder_shouldFail_whenCartIsEmpty`
- `updateStock_shouldFail_whenStockLessThanZero`

## 10. 结论

这份接口文档的目标不是一次把所有业务都做满，而是先把资源边界、字段映射和调用方式定清楚。  
这样后端可以按 RESTful 风格逐步落地，前端也能稳定对接，后续新增功能时只需要在现有资源模型上扩展即可。
