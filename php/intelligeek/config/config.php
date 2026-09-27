<?php
//$server ='192.168.1.6';
//$db='geek';
//$usr ='geek';
//$ps ='geek';
$connect=mysql_connect('192.168.1.6','geek','geek') or die(mysql_error());
mysql_select_db ('geek',$connect) or die (mysql_error());
?>
