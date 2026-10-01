--Chat Table
DROP TABLE IF EXISTS `chat`;
CREATE TABLE `chat` (
  `chat_id` INT(11) NOT NULL AUTO_INCREMENT,
  `chat_name` VARCHAR(64) DEFAULT NULL,
  `start_time` DATETIME DEFAULT NULL,
  PRIMARY KEY  (`chat_id`)
) ENGINE=INNODB DEFAULT CHARSET=latin1;
  
--Message Table
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
  `message_id` INT(11) NOT NULL AUTO_INCREMENT,
  `chat_id` INT(11) NOT NULL DEFAULT '0',
  `user_id` INT(11) NOT NULL DEFAULT '0',
  `user_name` VARCHAR(64) DEFAULT NULL,
  `message` TEXT,
  `post_time` DATETIME DEFAULT NULL,
  PRIMARY KEY  (`message_id`)
) ENGINE=INNODB DEFAULT CHARSET=latin1;