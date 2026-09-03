DROP TABLE IF EXISTS `business`;
CREATE TABLE `business` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '商家编号',
  `name` varchar(40) NOT NULL COMMENT '商家名称',
  `address` varchar(50) DEFAULT NULL COMMENT '商家地址',
  `description` varchar(40) DEFAULT NULL COMMENT '商家介绍',
  `image` mediumtext DEFAULT NULL COMMENT '商家图片（base64）',
  `order_type_id` int(11) NOT NULL COMMENT '点餐分类',
  `start_price` decimal(5,2) DEFAULT '0.00' COMMENT '起送费',
  `delivery_price` decimal(5,2) DEFAULT '0.00' COMMENT '配送费',
  `remark` varchar(40) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10010 DEFAULT CHARSET=utf8;


CREATE TABLE `cart` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '购物车编号',
  `food_id` int(11) NOT NULL COMMENT '食品编号',
  `business_id` int(11) NOT NULL COMMENT '所属商家编号',
  `user_id` varchar(20) NOT NULL COMMENT '所属用户编号',
  `quantity` int(11) NOT NULL COMMENT '同一类型食品的购买数量',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


CREATE TABLE `deliveryaddress` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '送货地址编号',
  `contact_name` varchar(20) NOT NULL COMMENT '联系人姓名',
  `contact_sex` int(11) DEFAULT NULL COMMENT '联系人性别',
  `contact_tel` varchar(20) NOT NULL COMMENT '联系人电话',
  `address` varchar(100) NOT NULL COMMENT '送货地址',
  `user_id` varchar(20) NOT NULL COMMENT '所属用户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;


CREATE TABLE `food` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '食品编号',
  `name` varchar(30) NOT NULL COMMENT '食品名称',
  `description` varchar(30) NOT NULL COMMENT '食品介绍',
  `image` mediumtext NOT NULL COMMENT '食品图片',
  `price` decimal(5,2) NOT NULL COMMENT '食品价格',
  `business_id` int(11) NOT NULL COMMENT '所属商家编号',
  `remark` varchar(40) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8;


CREATE TABLE `orderdetailet` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '订单明细编号',
  `order_id` int(11) NOT NULL COMMENT '所属订单编号',
  `food_id` int(11) NOT NULL COMMENT '食品编号',
  `quantity` int(11) NOT NULL COMMENT '数量',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


CREATE TABLE `orders` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '订单编号',
  `user_id` varchar(20) NOT NULL COMMENT '用户编号',
  `business_id` int(11) NOT NULL COMMENT '商家编号',
  `order_date` varchar(20) NOT NULL COMMENT '订购日期',
  `order_total` decimal(7,2) NOT NULL DEFAULT '0.00' COMMENT '订单总价',
  `address_id` int(11) NOT NULL COMMENT '送货地址编号',
  `order_status` int(11) NOT NULL DEFAULT '0' COMMENT '订单状态（0：未支付； 1：已支付）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


CREATE TABLE `user` (
  `id` varchar(20) NOT NULL COMMENT '用户编号',
  `password` varchar(20) NOT NULL COMMENT '密码',
  `name` varchar(20) NOT NULL COMMENT '用户名称',
  `sex` int(11) NOT NULL DEFAULT '1' COMMENT '用户性别（1：男； 0：女）',
  `avatar` mediumtext COMMENT '用户头像',
  `del_flag` int(1) NOT NULL DEFAULT '1' COMMENT '删除标记（1：正常； 0：删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
