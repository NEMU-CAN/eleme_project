-- 商家与菜品使用本地精选图片；未找到精确公开图的条目使用项目原有饿了么食物素材兜底。
-- 所有文件均已检查为餐厅或食物内容，不使用随机人物图库。
SET NAMES utf8mb4;

UPDATE business
SET image = CONCAT('/images/catalog/merchants/merchant-', LPAD(id, 2, '0'), '.jpg')
WHERE id BETWEEN 1 AND 15;

UPDATE food
SET image = CONCAT('/images/catalog/foods/food-', LPAD(id, 2, '0'), '.jpg')
WHERE id BETWEEN 1 AND 60;
