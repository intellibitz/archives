-- MySQL Administrator dump 1.4
--
-- ------------------------------------------------------
-- Server version	5.0.27


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


--
-- Create schema geek
--

CREATE DATABASE IF NOT EXISTS geek;
USE geek;

--
-- Definition of table `geek`.`emailid`
--

DROP TABLE IF EXISTS `geek`.`emailid`;
CREATE TABLE  `geek`.`emailid` (
  `eid` int(5) NOT NULL auto_increment,
  `emailid` varchar(50) default NULL,
  PRIMARY KEY  (`eid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `geek`.`emailid`
--

/*!40000 ALTER TABLE `emailid` DISABLE KEYS */;
LOCK TABLES `emailid` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `emailid` ENABLE KEYS */;


--
-- Definition of table `geek`.`faq`
--

DROP TABLE IF EXISTS `geek`.`faq`;
CREATE TABLE  `geek`.`faq` (
  `faqid` int(11) NOT NULL auto_increment,
  `question` varchar(350) default NULL,
  `answer` varchar(400) default NULL,
  `emailid` varchar(40) default NULL,
  `emailidans` varchar(40) default NULL,
  `date` varchar(20) default NULL,
  PRIMARY KEY  (`faqid`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `geek`.`faq`
--

/*!40000 ALTER TABLE `faq` DISABLE KEYS */;
LOCK TABLES `faq` WRITE;
INSERT INTO `geek`.`faq` VALUES  (1,'What is php','PHP stands for \n\nHipertext Pre Processor','vinayaga@gmail.com','gunabalans@gmail.com','02-01-2008'),
 (2,'What was the name of php when it was originating\n','','gunabalans@gmail.com','','02-01-2008');
UNLOCK TABLES;
/*!40000 ALTER TABLE `faq` ENABLE KEYS */;


--
-- Definition of table `geek`.`newsfeed`
--

DROP TABLE IF EXISTS `geek`.`newsfeed`;
CREATE TABLE  `geek`.`newsfeed` (
  `id` int(11) NOT NULL auto_increment,
  `category` tinytext,
  `title` tinytext,
  `summary` tinytext,
  `date` varchar(25) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `geek`.`newsfeed`
--

/*!40000 ALTER TABLE `newsfeed` DISABLE KEYS */;
LOCK TABLES `newsfeed` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `newsfeed` ENABLE KEYS */;


--
-- Definition of table `geek`.`newsletter`
--

DROP TABLE IF EXISTS `geek`.`newsletter`;
CREATE TABLE  `geek`.`newsletter` (
  `eid` int(5) NOT NULL auto_increment,
  `emailid` varchar(50) default NULL,
  PRIMARY KEY  (`eid`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `geek`.`newsletter`
--

/*!40000 ALTER TABLE `newsletter` DISABLE KEYS */;
LOCK TABLES `newsletter` WRITE;
INSERT INTO `geek`.`newsletter` VALUES  (1,'gunabalans@gmail.com');
UNLOCK TABLES;
/*!40000 ALTER TABLE `newsletter` ENABLE KEYS */;


--
-- Definition of table `geek`.`onlineuser`
--

DROP TABLE IF EXISTS `geek`.`onlineuser`;
CREATE TABLE  `geek`.`onlineuser` (
  `userid` int(11) NOT NULL auto_increment,
  `username` varchar(30) default NULL,
  `datetime` varchar(50) default NULL,
  PRIMARY KEY  (`userid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `geek`.`onlineuser`
--

/*!40000 ALTER TABLE `onlineuser` DISABLE KEYS */;
LOCK TABLES `onlineuser` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `onlineuser` ENABLE KEYS */;


--
-- Definition of table `geek`.`question`
--

DROP TABLE IF EXISTS `geek`.`question`;
CREATE TABLE  `geek`.`question` (
  `qb_id` bigint(20) unsigned NOT NULL auto_increment,
  `sub_id` int(10) unsigned NOT NULL,
  `level` tinyint(3) unsigned NOT NULL,
  `q` tinytext NOT NULL,
  `a` tinytext NOT NULL,
  `b` tinytext NOT NULL,
  `c` tinytext NOT NULL,
  `d` tinytext NOT NULL,
  `ans` tinyint(3) unsigned NOT NULL,
  PRIMARY KEY  (`qb_id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `geek`.`question`
--

/*!40000 ALTER TABLE `question` DISABLE KEYS */;
LOCK TABLES `question` WRITE;
INSERT INTO `geek`.`question` VALUES  (1,1,1,'Who is the Father of PHP?','Dugles Crooke','Lary Wall','Rasmus Lerdorf','Monty Widenius',3);
UNLOCK TABLES;
/*!40000 ALTER TABLE `question` ENABLE KEYS */;


--
-- Definition of table `geek`.`question_feedback`
--

DROP TABLE IF EXISTS `geek`.`question_feedback`;
CREATE TABLE  `geek`.`question_feedback` (
  `q_id` bigint(20) unsigned NOT NULL,
  `simple` int(10) unsigned default NULL,
  `ok` int(10) unsigned default NULL,
  `hard` int(10) unsigned default NULL,
  `error` int(10) unsigned default NULL,
  PRIMARY KEY  USING BTREE (`q_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `geek`.`question_feedback`
--

/*!40000 ALTER TABLE `question_feedback` DISABLE KEYS */;
LOCK TABLES `question_feedback` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `question_feedback` ENABLE KEYS */;


--
-- Definition of table `geek`.`subject`
--

DROP TABLE IF EXISTS `geek`.`subject`;
CREATE TABLE  `geek`.`subject` (
  `sub_id` int(10) unsigned NOT NULL auto_increment,
  `sub` varchar(50) NOT NULL,
  PRIMARY KEY  (`sub_id`)
) ENGINE=MyISAM AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `geek`.`subject`
--

/*!40000 ALTER TABLE `subject` DISABLE KEYS */;
LOCK TABLES `subject` WRITE;
INSERT INTO `geek`.`subject` VALUES  (1,'php'),
 (2,'java'),
 (3,'javascript'),
 (4,'gendral knowledge'),
 (5,'ajax');
UNLOCK TABLES;
/*!40000 ALTER TABLE `subject` ENABLE KEYS */;


--
-- Definition of table `geek`.`upload`
--

DROP TABLE IF EXISTS `geek`.`upload`;
CREATE TABLE  `geek`.`upload` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(30) NOT NULL,
  `type` varchar(30) NOT NULL,
  `size` int(11) NOT NULL,
  `content` mediumblob NOT NULL,
  `username` varchar(25) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `geek`.`upload`
--

/*!40000 ALTER TABLE `upload` DISABLE KEYS */;
LOCK TABLES `upload` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `upload` ENABLE KEYS */;


--
-- Definition of table `geek`.`user`
--

DROP TABLE IF EXISTS `geek`.`user`;
CREATE TABLE  `geek`.`user` (
  `user_id` int(11) NOT NULL auto_increment,
  `username` varchar(25) NOT NULL,
  `password` varchar(10) NOT NULL,
  `email` varchar(30) NOT NULL,
  `mailquiz` bit(1) default NULL,
  `mobile` varchar(15) default NULL,
  `smsquiz` bit(1) default NULL,
  `gender` varchar(10) default NULL,
  `dob` varchar(10) default NULL,
  PRIMARY KEY  (`user_id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=MyISAM AUTO_INCREMENT=59 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `geek`.`user`
--

/*!40000 ALTER TABLE `user` DISABLE KEYS */;
LOCK TABLES `user` WRITE;
INSERT INTO `geek`.`user` VALUES  (58,'gunabalans','0a830e4354','gunabalans@gmail.com',0x01,'9884442898',0x01,'Male','05-12-75');
UNLOCK TABLES;
/*!40000 ALTER TABLE `user` ENABLE KEYS */;


--
-- Definition of table `geek`.`usl_score`
--

DROP TABLE IF EXISTS `geek`.`usl_score`;
CREATE TABLE  `geek`.`usl_score` (
  `user_id` int(11) NOT NULL,
  `sub_id` int(10) unsigned NOT NULL,
  `level` bit(1) NOT NULL,
  `score_gt` int(10) unsigned default NULL,
  `score_yr` int(10) unsigned default NULL,
  `score_mth` int(10) unsigned default NULL,
  `score_wk` int(10) unsigned default NULL,
  `score_dy` int(10) unsigned default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `geek`.`usl_score`
--

/*!40000 ALTER TABLE `usl_score` DISABLE KEYS */;
LOCK TABLES `usl_score` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `usl_score` ENABLE KEYS */;




/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
