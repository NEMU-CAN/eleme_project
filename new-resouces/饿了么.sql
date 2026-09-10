CREATE TABLE `business` (
  `id` int PRIMARY KEY AUTO_INCREMENT COMMENT '商家ID，自增主键',
  `name` varchar(40) NOT NULL COMMENT '商家名称',
  `address` varchar(100) NOT NULL COMMENT '商家地址',
  `description` varchar(255) COMMENT '商家简介',
  `image` mediumtext COMMENT '商家图片（Base64或URL）',
  `taste_id` int NOT NULL COMMENT '口味分类ID，关联taste表',
  `start_price` decimal(5,2) DEFAULT 0 COMMENT '起送价',
  `delivery_price` decimal(5,2) DEFAULT 0 COMMENT '配送费',
  `status` tinyint DEFAULT 0 COMMENT '0关店 1开店 -1注销'
);

CREATE TABLE `user` (
  `id` int PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID，自增主键',
  `nickname` varchar(20) NOT NULL COMMENT '用户昵称',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `phone` varchar(20) UNIQUE NOT NULL COMMENT '手机号（登录账号）',
  `avatar` mediumtext COMMENT '用户头像（Base64或URL）',
  `gender` tinyint DEFAULT 0 COMMENT '0保密 1男 2女',
  `role` tinyint DEFAULT 0 COMMENT '0普通用户 1商家 2管理员',
  `status` tinyint DEFAULT 0 COMMENT '0正常 1禁用 -1注销',
  `current_token_hash` varchar(64) COMMENT '当前有效 token 的哈希（登录写入，注销/删除清空；单会话）'
);

CREATE TABLE `business_admin` (
  `id` int PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID，自增主键',
  `user_id` int NOT NULL COMMENT '用户ID，关联user表',
  `business_id` int NOT NULL COMMENT '商家ID，关联business表'
);

CREATE TABLE `taste` (
  `id` int PRIMARY KEY AUTO_INCREMENT COMMENT '口味ID，自增主键',
  `name` varchar(40) NOT NULL COMMENT '口味名称（如：川菜、粤菜）'
);

CREATE TABLE `food` (
  `id` int PRIMARY KEY AUTO_INCREMENT COMMENT '菜品ID，自增主键',
  `name` varchar(30) NOT NULL COMMENT '菜品名称',
  `description` varchar(255) COMMENT '菜品简介',
  `image` mediumtext COMMENT '菜品图片（Base64或URL）',
  `price` decimal(5,2) NOT NULL DEFAULT 0 COMMENT '菜品单价',
  `business_id` int NOT NULL COMMENT '所属商家ID，关联business表',
  `stock` int NOT NULL DEFAULT 0 COMMENT '库存',
  `reserved_stock` int NOT NULL DEFAULT 0 COMMENT '已预占库存（未支付订单锁定）',
  `status` tinyint DEFAULT 0 COMMENT '0下架 1上架'
);

CREATE TABLE `cart` (
  `id` int PRIMARY KEY AUTO_INCREMENT COMMENT '购物车记录ID，自增主键',
  `user_id` int NOT NULL COMMENT '用户ID，关联user表',
  `business_id` int NOT NULL COMMENT '商家ID，关联business表',
  `food_id` int NOT NULL COMMENT '菜品ID，关联food表',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量'
);

CREATE TABLE `delivery_address` (
  `id` int PRIMARY KEY AUTO_INCREMENT COMMENT '地址ID，自增主键',
  `user_id` int NOT NULL COMMENT '用户ID，关联user表',
  `address` varchar(100) NOT NULL COMMENT '详细收货地址',
  `contact_name` varchar(20) NOT NULL COMMENT '联系人姓名',
  `contact_tel` varchar(20) NOT NULL COMMENT '联系人电话',
  `contact_gender` int DEFAULT 0 COMMENT '0保密 1男 2女',
  `is_deleted` tinyint DEFAULT 0 COMMENT '0正常 1删除'
);

CREATE TABLE `orders` (
  `id` int PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID，自增主键',
  `order_no` varchar(32) UNIQUE NOT NULL COMMENT '订单号（用户可见）',
  `user_id` int NOT NULL COMMENT '下单用户ID，关联user表',
  `business_id` int NOT NULL COMMENT '商家ID，关联business表',
  `user_nickname` varchar(20) NOT NULL COMMENT '下单时用户昵称快照',
  `user_phone` varchar(20) NOT NULL COMMENT '下单时用户手机号快照',
  `business_name` varchar(40) NOT NULL COMMENT '下单时商家名称快照',
  `business_address` varchar(100) NOT NULL COMMENT '下单时商家地址快照',
  `receiver_name` varchar(20) NOT NULL COMMENT '收货人姓名快照',
  `receiver_tel` varchar(20) NOT NULL COMMENT '收货人电话快照',
  `receiver_gender` tinyint DEFAULT 0 COMMENT '收货人性别快照',
  `receiver_address` varchar(100) NOT NULL COMMENT '收货详细地址快照',
  `order_date` datetime DEFAULT (CURRENT_TIMESTAMP) COMMENT '下单时间',
  `delivery_price` decimal(5,2) DEFAULT 0 COMMENT '配送费',
  `total_amount` decimal(8,2) NOT NULL DEFAULT 0 COMMENT '商品总价（不含配送费）',
  `actual_amount` decimal(8,2) NOT NULL DEFAULT 0 COMMENT '实付金额（含配送费）',
  `delivery_address_id` int NOT NULL COMMENT '收货地址ID，关联delivery_address表',
  `order_status` int DEFAULT 0 COMMENT '-1取消 0未支付 1已支付 2已完成'
);

CREATE TABLE `order_detail` (
  `id` int PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID，自增主键',
  `order_id` int NOT NULL COMMENT '订单ID，关联orders表',
  `food_id` int NOT NULL COMMENT '菜品ID，关联food表',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
  `food_name` varchar(30) NOT NULL COMMENT '菜品名称（快照）',
  `food_price` decimal(5,2) NOT NULL DEFAULT 0 COMMENT '菜品单价（快照）',
  `subtotal` decimal(8,2) NOT NULL DEFAULT 0 COMMENT '小计（food_price × quantity）(快照)'
);

CREATE UNIQUE INDEX `cart_index_0` ON `cart` (`user_id`, `business_id`, `food_id`);

-- Disable foreign key checks for INSERT
SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO `user` (`id`, `nickname`, `password`, `phone`, `avatar`, `gender`, `role`, `status`)
VALUES
  (1, '张老板', '123456', '13800001001', NULL, 1, 1, 0),
  (2, '李掌柜', '123456', '13800001002', NULL, 2, 1, 0),
  (3, '王大厨', '123456', '13800001003', NULL, 1, 1, 0),
  (4, '赵总', '123456', '13800001004', NULL, 2, 1, 0),
  (5, '孙经理', '123456', '13800001005', NULL, 1, 1, 0),
  (6, '周八', '123456', '13800001006', NULL, 0, 0, 0),
  (7, '吴九', '123456', '13800001007', NULL, 1, 0, 0),
  (8, '郑十', '123456', '13800001008', NULL, 2, 0, 0),
  (9, '冯七', '123456', '13800001009', NULL, 0, 0, 0),
  (10, '陈六', '123456', '13800001010', NULL, 1, 0, 0),
  (11, '褚五', '123456', '13800001011', NULL, 2, 0, 0),
  (12, '卫四', '123456', '13800001012', NULL, 0, 0, 0),
  (13, '蒋三', '123456', '13800001013', NULL, 1, 0, 0),
  (14, '沈二', '123456', '13800001014', NULL, 2, 0, 0),
  (15, '韩姐', '123456', '13800001015', NULL, 2, 0, 0),
  (16, '杨大哥', '123456', '13800001016', NULL, 1, 0, 0),
  (17, '朱小妹', '123456', '13800001017', NULL, 2, 0, 0),
  (18, '秦叔', '123456', '13800001018', NULL, 1, 0, 0),
  (19, '尤姐', '123456', '13800001019', NULL, 2, 0, 0),
  (20, '许小妹', '123456', '13800001020', NULL, 0, 0, 0);
INSERT INTO `business_admin` (`id`, `user_id`, `business_id`)
VALUES
  (1, 1, 1),
  (2, 2, 2),
  (3, 3, 3),
  (4, 4, 4),
  (5, 5, 5);
INSERT INTO `taste` (`id`, `name`)
VALUES
  (1, '川菜'),
  (2, '粤菜'),
  (3, '湘菜'),
  (4, '东北菜'),
  (5, '日料');
INSERT INTO `business` (`id`, `name`, `address`, `description`, `image`, `taste_id`, `start_price`, `delivery_price`, `status`)
VALUES
  (1, '川香阁', '成都市锦江区春熙路88号', '正宗川菜，麻辣鲜香', NULL, 1, 20, 5, 1),
  (2, '粤味轩', '广州市天河区珠江新城10号', '地道粤菜，清淡鲜美', NULL, 2, 30, 6, 1),
  (3, '湘辣坊', '长沙市岳麓区麓山南路66号', '湘菜经典，无辣不欢', NULL, 3, 25, 4, 1),
  (4, '东北大院', '沈阳市和平区太原街99号', '东北特色，量大实惠', NULL, 4, 15, 3, 1),
  (5, '樱之味', '上海市长宁区古北路77号', '精致日料，新鲜地道', NULL, 5, 50, 8, 1);
INSERT INTO `food` (`id`, `name`, `description`, `image`, `price`, `business_id`, `stock`, `reserved_stock`, `status`)
VALUES
  (1, '麻婆豆腐', '麻辣鲜香，入口即化', NULL, 28, 1, 50, 0, 1),
  (2, '水煮鱼', '鲜嫩滑爽，辣而不燥', NULL, 58, 1, 30, 0, 1),
  (3, '宫保鸡丁', '甜辣适中，花生酥脆', NULL, 38, 1, 45, 0, 1),
  (4, '夫妻肺片', '麻辣开胃，凉菜经典', NULL, 22, 1, 60, 0, 1),
  (5, '担担面', '麻辣鲜香，回味无穷', NULL, 18, 1, 100, 0, 1),
  (6, '白切鸡', '皮滑肉嫩，原汁原味', NULL, 48, 2, 25, 0, 1),
  (7, '叉烧肉', '甜香软嫩，色泽红亮', NULL, 42, 2, 20, 0, 1),
  (8, '虾饺', '晶莹剔透，鲜甜弹牙', NULL, 32, 2, 40, 0, 1),
  (9, '干炒牛河', '镬气十足，牛肉嫩滑', NULL, 28, 2, 35, 0, 1),
  (10, '煲仔饭', '米饭焦香，腊味醇厚', NULL, 26, 2, 30, 0, 1),
  (11, '剁椒鱼头', '鲜辣浓郁，鱼肉细嫩', NULL, 52, 3, 20, 0, 1),
  (12, '小炒黄牛肉', '香辣入味，口感劲道', NULL, 42, 3, 30, 0, 1),
  (13, '臭豆腐', '外酥里嫩，闻臭吃香', NULL, 15, 3, 80, 0, 1),
  (14, '毛氏红烧肉', '肥而不腻，入口即化', NULL, 38, 3, 25, 0, 1),
  (15, '口味虾', '香辣鲜浓，吮指回味', NULL, 55, 3, 15, 0, 1),
  (16, '锅包肉', '酸甜酥脆，外焦里嫩', NULL, 35, 4, 35, 0, 1),
  (17, '猪肉炖粉条', '汤汁浓郁，粉条爽滑', NULL, 30, 4, 40, 0, 1),
  (18, '地三鲜', '茄子软糯，土豆绵香', NULL, 22, 4, 50, 0, 1),
  (19, '杀猪菜', '酸菜爽口，血肠鲜嫩', NULL, 38, 4, 20, 0, 1),
  (20, '大拉皮', '酸辣爽口，清凉开胃', NULL, 16, 4, 60, 0, 1),
  (21, '三文鱼刺身', '新鲜肥美，入口即化', NULL, 68, 5, 15, 0, 1),
  (22, '鳗鱼饭', '蒲烧鳗鱼，酱香浓郁', NULL, 55, 5, 20, 0, 1),
  (23, '天妇罗', '外酥里嫩，清爽不腻', NULL, 42, 5, 25, 0, 1),
  (24, '寿司拼盘', '多种口味，食材新鲜', NULL, 60, 5, 18, 0, 1),
  (25, '味增汤', '鲜香醇厚，温暖暖胃', NULL, 15, 5, 50, 0, 1);
INSERT INTO `delivery_address` (`id`, `user_id`, `address`, `contact_name`, `contact_tel`, `contact_gender`, `is_deleted`)
VALUES
  (1, 6, '北京市朝阳区建国路88号SOHO现代城A座1203', '周八', '13800001006', 0, 0),
  (2, 6, '北京市海淀区中关村大街66号理想国际大厦901', '周八', '13800001006', 0, 0),
  (3, 7, '上海市浦东新区陆家嘴环路1000号恒生银行大厦1502', '吴九', '13800001007', 1, 0),
  (4, 8, '广州市天河区体育西路189号城建大厦2305', '郑十', '13800001008', 2, 0),
  (5, 9, '深圳市南山区科技园南区科苑路15号科兴科学园B3栋401', '冯七', '13800001009', 0, 0),
  (6, 10, '杭州市西湖区文三路478号华星时代广场A座601', '陈六', '13800001010', 1, 0),
  (7, 11, '成都市高新区天府大道中段688号天府软件园E区1栋202', '褚五', '13800001011', 2, 0),
  (8, 12, '武汉市洪山区珞瑜路726号光谷国际广场A座1201', '卫四', '13800001012', 0, 0),
  (9, 13, '南京市鼓楼区汉中路180号星汉大厦1603', '蒋三', '13800001013', 1, 0),
  (10, 14, '西安市雁塔区高新路36号高新国际商务中心802', '沈二', '13800001014', 2, 0),
  (11, 15, '重庆市渝北区金开大道68号协信中心C栋1205', '韩姐', '13800001015', 2, 0),
  (12, 16, '长沙市岳麓区枫林一路66号麓山南路商业街101号', '杨大哥', '13800001016', 1, 0),
  (13, 17, '郑州市郑东新区金水东路88号楷林IFC-A座902', '朱小妹', '13800001017', 2, 0),
  (14, 18, '青岛市市南区香港中路78号海航万邦中心3101', '秦叔', '13800001018', 1, 0),
  (15, 19, '厦门市思明区环岛南路398号临海花园5栋602', '尤姐', '13800001019', 2, 0),
  (16, 20, '天津市和平区南京路188号天津中心写字楼1103', '许小妹', '13800001020', 0, 0),
  (17, 13, '南京市玄武区中山路366号凯润广场2单元1003', '蒋三', '13800001013', 1, 0),
  (18, 16, '长沙市天心区湘江中路36号华远国际中心2102', '杨大哥', '13800001016', 1, 0);
INSERT INTO `cart` (`id`, `user_id`, `business_id`, `food_id`, `quantity`)
VALUES
  (1, 6, 1, 1, 2),
  (2, 6, 1, 3, 1),
  (3, 7, 2, 6, 1),
  (4, 7, 2, 8, 3),
  (5, 8, 3, 11, 1),
  (6, 8, 3, 14, 2),
  (7, 9, 4, 16, 1),
  (8, 9, 4, 17, 2),
  (9, 10, 5, 21, 1),
  (10, 10, 5, 22, 1),
  (11, 11, 1, 2, 1),
  (12, 11, 1, 4, 2),
  (13, 12, 2, 7, 1),
  (14, 12, 2, 9, 2),
  (15, 13, 3, 13, 3),
  (16, 13, 3, 15, 1),
  (17, 14, 4, 18, 2),
  (18, 14, 4, 20, 1),
  (19, 15, 5, 23, 1),
  (20, 15, 5, 24, 1),
  (21, 16, 1, 5, 2),
  (22, 16, 1, 1, 1),
  (23, 17, 2, 10, 1),
  (24, 17, 2, 6, 2),
  (25, 18, 3, 12, 1),
  (26, 18, 3, 14, 1),
  (27, 19, 4, 19, 2),
  (28, 19, 4, 16, 1),
  (29, 20, 5, 25, 3),
  (30, 20, 5, 21, 1);

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;