<?php

$host = '192.168.1.6';
$user = 'geek'; // DB User
$pass = 'geek'; // DB Pass
$database = 'geek'; // DB Name
$version = 'V.1.7.10F';
$title = "Mess Chat System ".$version;

$connect = mysql_pconnect($host,$user,$pass) or die("<font size=2><strong>Error</strong> : Cannot Connect To Your Database Please Check Variable In  lib/config.inc.php</font>");
mysql_select_db($database,$connect);

?>
