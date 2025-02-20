
CREATE DATABASE yufengerp;

USE yufengerp;

DROP TABLE IF EXISTS t_customer;

CREATE TABLE t_customer (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `address` varchar(300) DEFAULT NULL,
  `contact` varchar(50) DEFAULT NULL,
  `name` varchar(200) DEFAULT NULL,
  `number` varchar(50) DEFAULT NULL,
  `remarks` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

/*Data for the table `t_customer` */

insert  into `t_customer`(`id`,`address`,`contact`,`name`,`number`,`remarks`) values (1,'福岡市博多区東公園7番7号','小林','福岡エマスーパー','812-8577',''),(2,'新宿区西新宿2-8-1','山田','東京王大チェーンホテル','163-8001','優良顧客'),(3,'横浜市中区日本大通1','高橋','大山谷希望小学校','231-8588','顧客ケア2'),(4,'名古屋市中区三の丸三丁目1番2号','田中','名古屋総合グループ','460-8501','');

CREATE TABLE `t_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `bz` varchar(1000) DEFAULT NULL,
  `password` varchar(50) DEFAULT NULL,
  `true_name` varchar(50) DEFAULT NULL,
  `user_name` varchar(50) DEFAULT NULL,
  `remarks` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

insert  into `t_user`(`id`,`bz`,`password`,`true_name`,`user_name`,`remarks`) values (1,'管理者','1','山本大地','admin',NULL),(2,'主任','123','鈴木大輔','jack','2'),(3,'営業マネージャー','123','佐藤マリ','marry','33');

/*Table structure for table `t_customer_return_list` */

CREATE TABLE `t_customer_return_list` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `amount_paid` float NOT NULL,
  `amount_payable` float NOT NULL,
  `customer_return_date` datetime DEFAULT NULL,
  `customer_return_number` varchar(100) DEFAULT NULL,
  `remarks` varchar(1000) DEFAULT NULL,
  `state` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `customer_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKd55ju83f0ntixagdvdrsmw10c` (`user_id`),
  KEY `FKl0ahdwa8slkocbfe57opembfx` (`customer_id`),
  CONSTRAINT `FKd55ju83f0ntixagdvdrsmw10c` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`),
  CONSTRAINT `FKl0ahdwa8slkocbfe57opembfx` FOREIGN KEY (`customer_id`) REFERENCES `t_customer` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

insert  into `t_customer_return_list`(`id`,`amount_paid`,`amount_payable`,`customer_return_date`,`customer_return_number`,`remarks`,`state`,`user_id`,`customer_id`) values (2,2200,2200,'2017-10-27 00:00:00','XT201710270001','cc',1,1,3),(3,4514,4514,'2017-10-28 00:00:00','XT201710280001','cc',1,1,3),(4,4400,4400,'2017-10-30 00:00:00','XT201710300001','cc',1,1,3),(5,139,139,'2017-10-30 00:00:00','XT201710300002','cc',1,1,2),(6,38,38,'2017-11-03 00:00:00','XT201711030001','cc',1,1,2);

/*Table structure for table `t_sale_list` */

CREATE TABLE `t_sale_list` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `amount_paid` float NOT NULL,
  `amount_payable` float NOT NULL,
  `remarks` varchar(1000) DEFAULT NULL,
  `sale_date` datetime DEFAULT NULL,
  `sale_number` varchar(100) DEFAULT NULL,
  `state` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `customer_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK34bnujemrdqimbhg133enp8k8` (`user_id`),
  KEY `FKox4qfs87eu3fvwdmrvelqhi8e` (`customer_id`),
  CONSTRAINT `FK34bnujemrdqimbhg133enp8k8` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`),
  CONSTRAINT `FKox4qfs87eu3fvwdmrvelqhi8e` FOREIGN KEY (`customer_id`) REFERENCES `t_customer` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8;

/*Data for the table `t_sale_list` */

insert  into `t_sale_list`(`id`,`amount_paid`,`amount_payable`,`remarks`,`sale_date`,`sale_number`,`state`,`user_id`,`customer_id`) values (4,5060,5060,'cc','2017-01-27 00:00:00','XS201701270001',1,1,2),(6,4889,4889,'dd','2017-02-28 00:00:00','XS201702280002',1,1,2),(7,4400,4400,'cccc','2017-03-30 00:00:00','XS201703300001',1,1,2),(8,860,860,'cc','2017-04-30 00:00:00','XS201704300002',1,1,2),(11,83,83,'ccc','2017-05-01 00:00:00','XS201705100003',1,1,2),(12,6626,6626,'cccc','2017-06-03 00:00:00','XS201706300001',1,1,2),(13,76,76,'cc','2017-06-03 00:00:00','XS201706300002',1,1,1),(14,127,127,'cc','2017-07-03 00:00:00','XS201707300003',2,1,2),(15,1579.5,1579.5,'cc','2017-08-03 00:00:00','XS201708300004',1,1,2),(16,76,76,'cc','2017-09-03 00:00:00','XS201709300005',1,1,2),(17,111,111,'cc','2017-10-28 00:00:00','XS201710280006',1,1,2),(18,40,40,'cc','2017-10-29 00:00:00','XS201710290007',1,1,1),(19,45,45,'cc','2017-10-30 00:00:00','XS201710300008',1,1,1),(20,10,10,'cc','2017-10-31 00:00:00','XS201710310009',1,1,1),(21,202,202,'cc','2017-11-01 00:00:00','XS201711010010',1,1,2),(22,3650,3650,'11','2017-11-02 00:00:00','XS201711020011',1,1,2),(23,20,20,'cc','2017-11-03 00:00:00','XS201711030012',1,1,1),(24,59,59,'cc','2016-12-03 00:00:00','XS201712030013',1,1,2),(25,146,146,'cc','2016-11-03 00:00:00','XS201711030014',1,1,1);

/*Table structure for table `t_goodstype` */

CREATE TABLE `t_goodstype` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) DEFAULT NULL,
  `p_id` int(11) DEFAULT NULL,
  `state` int(11) DEFAULT NULL,
  `icon` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8;

/*Data for the table `t_goodstype` */

insert  into `t_goodstype`(`id`,`name`,`p_id`,`state`,`icon`) values (1,'全カテゴリー',-1,1,'icon-folderOpen'),(2,'アパレル',1,1,'icon-folder'),(3,'食品',1,1,'icon-folder'),(4,'家電',1,1,'icon-folder'),(5,'デジタル',1,1,'icon-folder'),(6,'ワンピース',2,0,'icon-folder'),(7,'メンズスーツ',2,0,'icon-folder'),(8,'ジーンズ',2,0,'icon-folder'),(9,'輸入食品',3,0,'icon-folder'),(10,'地方特産品',3,0,'icon-folder'),(11,'スナック',3,0,'icon-folder'),(12,'テレビ',4,0,'icon-folder'),(13,'洗濯機',4,0,'icon-folder'),(14,'冷蔵庫',4,0,'icon-folder'),(15,'カメラ',5,0,'icon-folder'),(16,'携帯電話',5,0,'icon-folder'),(17,'スピーカー',5,0,'icon-folder');

/*Table structure for table `t_customer_return_list_goods` */

CREATE TABLE `t_customer_return_list_goods` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(50) DEFAULT NULL,
  `model` varchar(50) DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `num` int(11) NOT NULL,
  `price` float NOT NULL,
  `total` float NOT NULL,
  `unit` varchar(10) DEFAULT NULL,
...(about 297 lines omitted)...

/*Table structure for table `t_menu` */

DROP TABLE IF EXISTS `t_menu`;

CREATE TABLE `t_menu` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `icon` varchar(100) DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `state` int(11) DEFAULT NULL,
  `url` varchar(200) DEFAULT NULL,
  `p_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKsni20f28wjqrmpp44uawa2ky4` (`p_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6051 DEFAULT CHARSET=utf8;

/*Data for the table `t_menu` */

insert  into `t_menu`(`id`,`icon`,`name`,`state`,`url`,`p_id`) values (1,'menu-plugin','システムメニュー',1,NULL,-1),(10,'menu-1','仕入管理',1,NULL,1),(20,'menu-2','販売管理',1,NULL,1),(30,'menu-3','在庫管理',1,NULL,1),(40,'menu-4','統計レポート',1,NULL,1),(50,'menu-5','基本データ',1,NULL,1),(60,'menu-6','システム管理',1,NULL,1),(1010,'menu-11','仕入入庫',0,'/purchase/purchase.html',10),(1020,'menu-12','返品出庫',0,'/purchase/return.html',10),(1030,'menu-13','仕入伝票照会',0,'/purchase/purchaseSearch.html',10),(1040,'menu-14','返品伝票照会',0,'/purchase/returnSearch.html',10),(1050,'menu-15','現在庫照会',0,'/common/stockSearch.html',10),(2010,'menu-21','販売出庫',0,'/sale/saleout.html',20),(2020,'menu-22','顧客返品',0,'/sale/salereturn.html',20),(2030,'menu-23','販売伝票照会',0,'/sale/saleSearch.html',20),(2040,'menu-24','顧客返品照会',0,'/sale/returnSearch.html',20),(2050,'menu-25','現在庫照会',0,'/common/stockSearch.html',20),(3010,'menu-31','商品損失',0,'/stock/damage.html',30),(3020,'menu-32','商品過剰',0,'/stock/overflow.html',30),(3030,'menu-33','在庫警告',0,'/stock/alarm.html',30),(3040,'menu-34','損失過剰照会',0,'/stock/damageOverflowSearch.html',30),(3050,'menu-35','現在庫照会',0,'/common/stockSearch.html',30),(4010,'menu-41','仕入先統計',0,'/count/supplier.html',40),(4020,'menu-42','顧客統計',0,'/count/customer.html',40),(4030,'menu-43','商品仕入統計',0,'/count/purchase.html',40),(4040,'menu-44','商品販売統計',0,'/count/sale.html',40),(4050,'menu-45','日別統計分析',0,'/count/saleDay.html',40),(4060,'menu-46','月別統計分析',0,'/count/saleMonth.html',40),(5010,'menu-51','仕入先管理',0,'/basicData/supplier.html',50),(5020,'menu-52','顧客管理',0,'/basicData/customer.html',50),(5030,'menu-53','商品管理',0,'/basicData/goods.html',50),(5040,'menu-54','期首在庫',0,'/basicData/stock.html',50),(6010,'menu-61','ロール管理',0,'/power/role.html',60),(6020,'menu-62','ユーザー管理',0,'/power/user.html',60),(6030,'menu-65','システムログ',0,'/power/log.html',60),(6040,'menu-63','パスワード変更',0,NULL,60),(6050,'menu-64','安全にログアウト',0,NULL,60);



/*Table structure for table `t_role` */

DROP TABLE IF EXISTS `t_role`;

CREATE TABLE `t_role` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `bz` varchar(1000) DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `remarks` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8;

/*Data for the table `t_role` */

insert  into `t_role`(`id`,`bz`,`name`,`remarks`) values (1,'システム管理者 最高権限','管理者',NULL),(2,'主任','主任',NULL),(4,'購買担当','購買担当',NULL),(5,'営業マネージャー','営業マネージャー','22'),(7,'倉庫管理者','倉庫管理者',NULL),(9,'総支配人','総支配人',NULL);

/*Table structure for table `t_role_menu` */

DROP TABLE IF EXISTS `t_role_menu`;

CREATE TABLE `t_role_menu` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `menu_id` int(11) DEFAULT NULL,
  `role_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKsonb0rbt2u99hbrqqvv3r0wse` (`role_id`),
  KEY `FKhayg4ib6v7h1wyeyxhq6xlddq` (`menu_id`),
  CONSTRAINT `FKhayg4ib6v7h1wyeyxhq6xlddq` FOREIGN KEY (`menu_id`) REFERENCES `t_menu` (`id`),
  CONSTRAINT `FKsonb0rbt2u99hbrqqvv3r0wse` FOREIGN KEY (`role_id`) REFERENCES `t_role` (`id`),
  CONSTRAINT `t_role_menu_ibfk_1` FOREIGN KEY (`menu_id`) REFERENCES `t_menu` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=79 DEFAULT CHARSET=utf8;

/*Data for the table `t_role_menu` */

insert  into `t_role_menu`(`id`,`menu_id`,`role_id`) values (2,1,1),(3,10,1),(4,20,1),(5,30,1),(6,40,1),(7,50,1),(8,60,1),(9,1010,1),(10,1020,1),(11,1030,1),(12,1040,1),(13,1050,1),(14,2010,1),(15,2020,1),(16,2030,1),(17,2040,1),(18,2050,1),(19,3010,1),(20,3020,1),(21,3030,1),(22,3040,1),(23,3050,1),(24,4010,1),(25,4020,1),(26,4030,1),(27,4040,1),(28,4050,1),(29,4060,1),(30,5010,1),(31,5020,1),(32,5030,1),(33,5040,1),(34,6010,1),(35,6020,1),(36,10,2),(37,1010,2),(38,1020,2),(39,1030,2),(40,1040,2),(41,1050,2),(42,1,2),(43,6030,1),(44,6040,1),(45,1,4),(46,20,4),(47,2010,4),(48,1,5),(49,30,5),(50,3010,5),(55,1,9),(56,30,9),(57,3040,9),(58,3050,9),(59,50,9),(60,5010,9),(61,5020,9),(62,5030,9),(63,5040,9),(64,6050,1),(65,1,7),(66,10,7),(67,1010,7),(68,1020,7),(69,1030,7),(70,1040,7),(71,1050,7),(72,20,7),(73,2010,7),(74,2020,7),(75,2030,7),(76,40,7),(77,4010,7),(78,4020,7);

/*Table structure for table `t_sale_list_goods` */

DROP TABLE IF EXISTS `t_sale_list_goods`;

CREATE TABLE `t_sale_list_goods` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(50) DEFAULT NULL,
  `model` varchar(50) DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `num` int(11) NOT NULL,
  `price` float NOT NULL,
  `total` float NOT NULL,
  `unit` varchar(10) DEFAULT NULL,
  `sale_list_id` int(11) DEFAULT NULL,
  `type_id` int(11) DEFAULT NULL,
  `goods_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK20ehd6ta9geyql4hxtdsnhbox` (`sale_list_id`),
  KEY `FKn9i5p1d8f0gek5x7q45cq8ibw` (`type_id`),
  CONSTRAINT `FK20ehd6ta9geyql4hxtdsnhbox` FOREIGN KEY (`sale_list_id`) REFERENCES `t_sale_list` (`id`),
  CONSTRAINT `FKn9i5p1d8f0gek5x7q45cq8ibw` FOREIGN KEY (`type_id`) REFERENCES `t_goodstype` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8;

/*Data for the table `t_sale_list_goods` */

insert  into `t_sale_list_goods`(`id`,`code`,`model`,`name`,`num`,`price`,`total`,`unit`,`sale_list_id`,`type_id`,`goods_id`) values (7,'0002','Note8','ファーウェイ Honor Note8',2,2200,4400,'台',4,16,2),(8,'0010','250g入り','小魚干し',33,20,660,'袋',4,11,18),(11,'0003','500g入り','天然黒きくらげ',100,38,3800,'袋',6,11,11),(12,'0009','240g入り','ピスタチオ スナック',33,33,1089,'袋',6,11,17),(13,'0002','Note8','ファーウェイ Honor Note8',2,2200,4400,'台',7,16,2),(14,'0003','500g入り','天然黒きくらげ',22,38,836,'袋',8,11,11),(15,'0014','250g入り','グリーンピース ガーリック味',3,8,24,'袋',8,11,22),(20,'0003','500g入り','天然黒きくらげ',1,38,38,'袋',11,11,11),(21,'0005','500g入り','オートミール チョコレート',3,15,45,'袋',11,11,13),(22,'0002','Note8','ファーウェイ Honor Note8',3,2200,6600,'台',12,16,2),(23,'0006','300g入り','金柑キャンディー',2,13,26,'箱',12,11,14),(24,'0003','500g入り','天然黒きくらげ',2,38,76,'袋',13,11,11),(25,'0004','1kg入り','新疆ナツメ',3,25,75,'袋',14,10,12),(26,'0006','300g入り','金柑キャンディー',4,13,52,'箱',14,11,14),(27,'0001','赤パック','ラー油',33,8.5,280.5,'本',15,10,1),(28,'0018','IXUS 285 HS','キヤノン IXUS 285 HS デジタルカメラ 2020万画素高画質撮影',1,1299,1299,'台',15,15,27),(29,'0003','500g入り','天然黒きくらげ',2,38,76,'袋',16,11,11),(30,'0005','500g入り','オートミール チョコレート',3,15,45,'袋',17,11,13),(31,'0009','240g入り','ピスタチオ スナック',2,33,66,'袋',17,11,17),(32,'0010','250g入り','小魚干し',1,20,20,'袋',18,11,18),(33,'0008','128g入り','桃ドライフルーツ',2,10,20,'箱',18,11,16),(34,'0004','1kg入り','新疆ナツメ',1,25,25,'袋',19,10,12),(35,'0008','128g入り','桃ドライフルーツ',2,10,20,'箱',19,11,16),(36,'0007','500g入り','牛肉味ケーキ',1,10,10,'袋',20,11,15),(37,'0003','500g入り','天然黒きくらげ',2,38,76,'袋',21,11,11),(38,'0007','500g入り','牛肉味ケーキ',2,10,20,'袋',21,11,15),(39,'0009','240g入り','ピスタチオ スナック',2,33,66,'袋',21,11,17),(40,'0010','250g入り','小魚干し',2,20,40,'袋',21,11,18),(41,'0017','ILCE-A6000L','ソニー ILCE-A6000L WiFiミラーレスデジタルカメラ',1,3650,3650,'台',22,15,26),(42,'0010','250g入り','小魚干し',1,20,20,'袋',23,11,18),(43,'0009','240g入り','ピスタチオ スナック',1,33,33,'袋',24,11,17),(44,'0006','300g入り','金柑キャンディー',2,13,26,'箱',24,11,14),(45,'0009','240g入り','ピスタチオ スナック',2,33,66,'袋',25,11,17),(46,'0014','250g入り','グリーンピース ガーリック味',10,8,80,'袋',25,11,22);

/*Table structure for table `t_user_role` */

DROP TABLE IF EXISTS `t_user_role`;

CREATE TABLE `t_user_role` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `role_id` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKa9c8iiy6ut0gnx491fqx4pxam` (`role_id`),
  KEY `FKq5un6x7ecoef5w1n39cop66kl` (`user_id`),
  CONSTRAINT `FKa9c8iiy6ut0gnx491fqx4pxam` FOREIGN KEY (`role_id`) REFERENCES `t_role` (`id`),
  CONSTRAINT `FKq5un6x7ecoef5w1n39cop66kl` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8;

/*Data for the table `t_user_role` */

insert  into `t_user_role`(`id`,`role_id`,`user_id`) values (1,1,1),(19,2,2),(20,4,2),(21,5,2),(28,2,3),(29,4,3),(30,5,3),(31,7,3);
