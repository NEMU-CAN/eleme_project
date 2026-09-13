-- 将最新版演示库从 5 家扩展到 15 家；可重复执行。
INSERT INTO `taste` (`id`,`name`) VALUES
(6,'面食'),(7,'火锅'),(8,'早茶'),(9,'特色小吃'),(10,'异国料理')
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);

INSERT INTO `business` (`id`,`name`,`address`,`description`,`image`,`taste_id`,`start_price`,`delivery_price`,`status`) VALUES
(6,'老北京炸酱面','天津大学北洋园校区商业街6号','现擀面条，老北京传统酱香','/images/catalog/merchants/merchant-06.jpg',6,18,3,1),
(7,'重庆火锅','天津大学北洋园校区商业街7号','重庆牛油锅底，鲜切食材','/images/catalog/merchants/merchant-07.jpg',7,38,5,1),
(8,'广式早茶','天津大学北洋园校区商业街8号','手工点心，现蒸现卖','/images/catalog/merchants/merchant-08.jpg',8,25,4,1),
(9,'台湾小吃','天津大学北洋园校区商业街9号','地道台式卤肉与特色小吃','/images/catalog/merchants/merchant-09.jpg',9,20,3,1),
(10,'韩式料理','天津大学北洋园校区商业街10号','石锅拌饭、炸鸡与部队锅','/images/catalog/merchants/merchant-10.jpg',10,30,5,1),
(11,'意式西餐','天津大学北洋园校区商业街11号','手工披萨与经典意面','/images/catalog/merchants/merchant-11.jpg',10,35,6,1),
(12,'云南米线','天津大学北洋园校区商业街12号','每日熬汤，米线爽滑入味','/images/catalog/merchants/merchant-12.jpg',6,18,3,1),
(13,'新疆大盘鸡','天津大学北洋园校区商业街13号','鸡肉软嫩，土豆入味，分量十足','/images/catalog/merchants/merchant-13.jpg',3,40,5,1),
(14,'海南椰子鸡','天津大学北洋园校区商业街14号','现开椰青，清甜原汤','/images/catalog/merchants/merchant-14.jpg',7,45,6,1),
(15,'兰州拉面','天津大学北洋园校区商业街15号','一清二白三红四绿，现拉现煮','/images/catalog/merchants/merchant-15.jpg',6,16,3,1)
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`),`address`=VALUES(`address`),`description`=VALUES(`description`),`image`=VALUES(`image`),`taste_id`=VALUES(`taste_id`),`start_price`=VALUES(`start_price`),`delivery_price`=VALUES(`delivery_price`),`status`=VALUES(`status`);

INSERT INTO `food` (`id`,`name`,`description`,`image`,`price`,`business_id`,`stock`,`reserved_stock`,`status`) VALUES
(26,'老北京炸酱面','六必居酱香，配菜丰富','/images/catalog/foods/food-26.jpg',22,6,80,0,1),(27,'番茄鸡蛋面','酸甜浓郁，手擀面筋道','/images/catalog/foods/food-27.jpg',20,6,80,0,1),(28,'京味爆肚','鲜脆爽口，麻酱醇厚','/images/catalog/foods/food-28.jpg',32,6,40,0,1),(29,'糖蒜小菜','清脆解腻','/images/catalog/foods/food-29.jpg',6,6,100,0,1),
(30,'招牌毛肚','七上八下，爽脆鲜香','/images/catalog/foods/food-30.jpg',38,7,50,0,1),(31,'雪花肥牛','纹理均匀，鲜嫩不柴','/images/catalog/foods/food-31.jpg',42,7,50,0,1),(32,'手工虾滑','虾肉饱满，弹嫩鲜甜','/images/catalog/foods/food-32.jpg',36,7,45,0,1),(33,'红糖糍粑','外酥里糯，红糖浓香','/images/catalog/foods/food-33.jpg',16,7,70,0,1),
(34,'水晶虾饺','皮薄馅足，鲜甜弹牙','/images/catalog/foods/food-34.jpg',28,8,60,0,1),(35,'豉汁蒸凤爪','软糯脱骨，豉香浓郁','/images/catalog/foods/food-35.jpg',24,8,60,0,1),(36,'叉烧包','松软香甜，叉烧馅足','/images/catalog/foods/food-36.jpg',18,8,80,0,1),(37,'艇仔粥','料足绵滑，暖胃鲜香','/images/catalog/foods/food-37.jpg',16,8,80,0,1),
(38,'台式卤肉饭','卤汁浓郁，米饭软香','/images/catalog/foods/food-38.jpg',26,9,80,0,1),(39,'盐酥鸡','外酥里嫩，九层塔提香','/images/catalog/foods/food-39.jpg',22,9,80,0,1),(40,'蚵仔煎','鲜香软嫩，酱汁地道','/images/catalog/foods/food-40.jpg',24,9,50,0,1),(41,'珍珠奶茶','茶香浓郁，珍珠Q弹','/images/catalog/foods/food-41.jpg',14,9,100,0,1),
(42,'石锅拌饭','锅巴焦香，时蔬丰富','/images/catalog/foods/food-42.jpg',28,10,70,0,1),(43,'韩式炸鸡','外酥里嫩，甜辣入味','/images/catalog/foods/food-43.jpg',35,10,60,0,1),(44,'部队火锅','芝士浓香，配料丰富','/images/catalog/foods/food-44.jpg',48,10,40,0,1),(45,'辣白菜','酸辣爽脆','/images/catalog/foods/food-45.jpg',8,10,100,0,1),
(46,'经典肉酱意面','番茄肉酱慢熬，酸甜浓郁','/images/catalog/foods/food-46.jpg',36,11,60,0,1),(47,'黑椒牛柳意面','牛柳鲜嫩，黑椒浓香','/images/catalog/foods/food-47.jpg',42,11,50,0,1),(48,'玛格丽特披萨','番茄、芝士与罗勒经典搭配','/images/catalog/foods/food-48.jpg',46,11,40,0,1),(49,'奶油蘑菇汤','奶香顺滑，蘑菇鲜美','/images/catalog/foods/food-49.jpg',18,11,70,0,1),
(50,'过桥米线','高汤滚烫，配菜丰富','/images/catalog/foods/food-50.jpg',28,12,80,0,1),(51,'酸汤肥牛米线','酸辣开胃，肥牛鲜嫩','/images/catalog/foods/food-51.jpg',32,12,70,0,1),(52,'菌菇米线','菌香浓郁，清鲜爽口','/images/catalog/foods/food-52.jpg',25,12,70,0,1),
(53,'招牌大盘鸡','鸡肉鲜嫩，土豆软糯','/images/catalog/foods/food-53.jpg',68,13,40,0,1),(54,'新疆拌面','面条筋道，菜香浓郁','/images/catalog/foods/food-54.jpg',30,13,60,0,1),(55,'烤羊肉串','孜然浓香，肥瘦相间','/images/catalog/foods/food-55.jpg',25,13,80,0,1),
(56,'原味椰子鸡','椰青原汤，文昌鸡鲜嫩','/images/catalog/foods/food-56.jpg',78,14,35,0,1),(57,'竹荪椰子鸡','竹荪爽脆，汤底清甜','/images/catalog/foods/food-57.jpg',88,14,30,0,1),(58,'海南鸡饭','鸡肉嫩滑，米饭油润','/images/catalog/foods/food-58.jpg',32,14,60,0,1),
(59,'兰州牛肉面','清汤醇香，牛肉鲜嫩','/images/catalog/foods/food-59.jpg',22,15,100,0,1),(60,'红烧牛肉面','汤汁浓厚，牛肉软烂','/images/catalog/foods/food-60.jpg',26,15,90,0,1)
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`),`description`=VALUES(`description`),`image`=VALUES(`image`),`price`=VALUES(`price`),`business_id`=VALUES(`business_id`),`stock`=VALUES(`stock`),`reserved_stock`=VALUES(`reserved_stock`),`status`=VALUES(`status`);
