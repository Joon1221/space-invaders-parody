-- MySQL dump 10.13  Distrib 5.6.13, for osx10.7 (x86_64)
--
-- Host: localhost    Database: space_invaders
-- ------------------------------------------------------
-- Server version	5.6.13

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `inventory`
--

DROP TABLE IF EXISTS `inventory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inventory` (
  `inv_id` int(11) NOT NULL AUTO_INCREMENT,
  `owner` int(11) DEFAULT NULL,
  `item_id` int(11) DEFAULT NULL,
  `uuid` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`inv_id`),
  KEY `user_num_id_idx` (`owner`),
  KEY `id_idx` (`item_id`),
  CONSTRAINT `id` FOREIGN KEY (`item_id`) REFERENCES `item` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `user_num_id` FOREIGN KEY (`owner`) REFERENCES `users` (`user_num_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory`
--

LOCK TABLES `inventory` WRITE;
/*!40000 ALTER TABLE `inventory` DISABLE KEYS */;
INSERT INTO `inventory` (`inv_id`, `owner`, `item_id`, `uuid`) VALUES (3,1,2,'a1'),(4,1,4,'a2');
/*!40000 ALTER TABLE `inventory` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `item`
--

DROP TABLE IF EXISTS `item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `item` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) DEFAULT NULL,
  `class_name` varchar(45) DEFAULT NULL,
  `price` int(11) DEFAULT NULL,
  `image_file_name` varchar(45) DEFAULT NULL,
  `description` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `item`
--

LOCK TABLES `item` WRITE;
/*!40000 ALTER TABLE `item` DISABLE KEYS */;
INSERT INTO `item` (`id`, `name`, `class_name`, `price`, `image_file_name`, `description`) VALUES (0,'Empty','EmptyItem',0,'empty.png','Empty'),(1,'Skin Changing Item Red','SkinChangingItemRed',1,'skin_changing_item_red.png','Skin Changing Item Red'),(2,'Skin Changing Item Green','SkinChangingItemGreen',1,'skin_changing_item_green.png','Skin Changing Item Green'),(3,'Skin Changing Item Blue','SkinChangingItemBlue',1,'skin_changing_item_blue.png','Skin Changing Item Blue'),(4,'Double Point Item','DoublePointItem',5,'double_point_item.png','Double Point Item');
/*!40000 ALTER TABLE `item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slot`
--

DROP TABLE IF EXISTS `slot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `slot` (
  `slot_id` int(11) NOT NULL AUTO_INCREMENT,
  `owner` int(11) DEFAULT NULL,
  `item_id` int(11) DEFAULT NULL,
  `slot_index` int(11) DEFAULT NULL,
  `uuid` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`slot_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slot`
--

LOCK TABLES `slot` WRITE;
/*!40000 ALTER TABLE `slot` DISABLE KEYS */;
/*!40000 ALTER TABLE `slot` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `user_num_id` int(11) NOT NULL AUTO_INCREMENT,
  `id` varchar(45) DEFAULT NULL,
  `pwd` varchar(45) DEFAULT NULL,
  `gold` int(10) unsigned zerofill DEFAULT NULL,
  `level` int(10) unsigned zerofill DEFAULT NULL,
  `exp` int(10) unsigned zerofill DEFAULT NULL,
  `high_score` int(10) unsigned zerofill DEFAULT NULL,
  `num_played` int(10) unsigned zerofill DEFAULT NULL,
  `num_win` int(10) unsigned zerofill DEFAULT NULL,
  `num_tie` int(10) unsigned zerofill DEFAULT NULL,
  PRIMARY KEY (`user_num_id`),
  UNIQUE KEY `user_num_id_UNIQUE` (`user_num_id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` (`user_num_id`, `id`, `pwd`, `gold`, `level`, `exp`, `high_score`, `num_played`, `num_win`, `num_tie`) VALUES (1,'aaa','111',0000001000,0000000000,0000000000,0000000000,0000000000,0000000000,0000000000),(2,'bbb','222',0000001000,0000000000,0000000000,0000000000,0000000000,0000000000,0000000000),(3,'ccc','333',0000001000,0000000000,0000000000,0000000000,0000000000,0000000000,0000000000),(4,'ddd','444',0000001000,0000000000,0000000000,0000000000,0000000000,0000000000,0000000000),(9,'eee','555',0000001000,0000000000,0000000000,0000000000,0000000000,0000000000,0000000000);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-10-18 22:16:30
