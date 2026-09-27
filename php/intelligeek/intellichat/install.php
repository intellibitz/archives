<?php
require_once 'lib/config.inc.php';
?>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"><head>
<title>Install FirstTime Of <?php echo $title; ?></title>
<meta http-equiv="Content-Type" content="text/html; charset=tis-620" />
<link rel="stylesheet" type="text/css" href="style.css" />
</head><body>
<?php

$connect = mysql_pconnect($host,$user,$pass) or die("<font size=2><strong>Error</strong> : Cannot Connect To Your Database Please Check Variable In  lib/config.inc.php</font>");
mysql_select_db($database,$connect);

$install=array();

$install[]="CREATE TABLE `chat_group` (
  `id` int(11) NOT NULL auto_increment,
  `groupname` text,
  `grouppass` text,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB";

$install[]="CREATE TABLE `chat_online` (
  `no` int(11) default '0',
  `name` varchar(30) NOT NULL default '',
  `ip` varchar(20) NOT NULL default '',
  `utimestmp` int(11) default NULL,
  `avatar` char(3) NOT NULL default '',
  `mystatus` varchar(30) default 'online',
  `onvote` int(3) default '0',
  `nowvote` int(11) default '0',
  `votetimestmp` int(20) default '0',
  `whisper` int(11) NOT NULL default '1',
  `love` varchar(60) NOT NULL default '',
  PRIMARY KEY  (`name`,`ip`)
) ENGINE=InnoDB";

$install[]="CREATE TABLE `chat_text` (
  `no` int(11) NOT NULL auto_increment,
  `name` varchar(40) default NULL,
  `timestamp` int(11) NOT NULL default '0',
  `text` longtext,
  `bold` int(1) NOT NULL default '0',
  `underline` int(1) NOT NULL default '0',<?php
  `color` varchar(11) default NULL,
  `time` varchar(15) NOT NULL default '00:00',
  `sendto` varchar(40) default NULL,
  `avatar` char(3) NOT NULL default 'm1',
  `togroup` text,
  PRIMARY KEY  (`no`),
  KEY `sendto` (`sendto`),
  KEY `name` (`name`),
  CONSTRAINT `chat_text_ibfk_1` FOREIGN KEY (`sendto`) REFERENCES `chat_online` (`name`) ON DELETE CASCADE,
  CONSTRAINT `chat_text_ibfk_2` FOREIGN KEY (`name`) REFERENCES `chat_online` (`name`) ON DELETE CASCADE

) ENGINE=InnoDB";


for($i=0;$i<count($install);$i++){
mysql_query($install[$i]) or die("<font size=2><strong>Error</strong> : Cannot Dump The Sql On Line ".$i."<br><br>".$install[$i]."</font>");}

echo "<font size=2><strong>Setup Status</strong> : Install MessChat System Successfully (<a href=\"index.php\">Click Here</a>) To Go To Your Chat</font>";

?>