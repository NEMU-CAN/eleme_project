-- ============================================================
-- 测试种子数据（elm 库）
-- 用途：重建/清空数据库后执行本脚本，恢复集成测试所需的基础数据。
-- 数据与集成测试断言对应：
--   user 11111111111（张三丰 / 密码 123）—— UserControllerTest / OrderControllerTest 等
--   business 10001、food 1（纯肉鲜肉（水饺），价格 17.50）—— BusinessControllerTest / OrderControllerTest
--   deliveryaddress 1 —— DeliveryAddressControllerTest / OrderControllerTest
-- 说明：若 orders 表为旧库（无 address_contact_* 快照列），请先按 docs/tables.sql 补列或重建表。
-- ============================================================

INSERT INTO `user` (`id`, `password`, `name`, `sex`, `avatar`, `del_flag`) VALUES
('11111111111', '123', '张三丰', 1, NULL, 1);

INSERT INTO `business` (`id`, `name`, `address`, `description`, `image`, `order_type_id`, `start_price`, `delivery_price`, `remark`) VALUES
(10001, '测试商家', '测试地址', '测试介绍', 'data:image/png;base64,iVBORw0KGgo=', 1, 0.00, 0.00, NULL);

INSERT INTO `food` (`id`, `name`, `description`, `image`, `price`, `business_id`, `remark`, `stock`) VALUES
(1, '纯肉鲜肉（水饺）', '测试描述', 'data:image/png;base64,iVBORw0KGgo=', 17.50, 10001, NULL, 100);

INSERT INTO `deliveryaddress` (`id`, `contact_name`, `contact_sex`, `contact_tel`, `address`, `user_id`) VALUES
(1, '张三丰', 1, '13800000000', '测试地址1号', '11111111111');
