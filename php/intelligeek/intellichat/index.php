<?php 
include './lib/config.inc.php';
if($_COOKIE['login'])
{

	$connect = mysql_pconnect($host,$user,$pass) or die("Can't connect to MySQL");
	mysql_select_db($database,$connect);
	
	$sql = "DELETE FROM chat_online WHERE (name ='".$_COOKIE['login']."' and ip='".$_COOKIE['ip']."') OR unix_timestamp() - utimestmp > 300";
	mysql_query($sql);

	
	if(mysql_affected_rows()>0)
			{
				$sql = "INSERT INTO chat_text(name,sendto,timestamp,text,bold,underline,color,time) VALUES ('".$_COOKIE['login']."',NULL,unix_timestamp(),'takeout',0,0,'FFFFFF','".date("H:i")."')";
				mysql_query($sql);
			}
	header("Location: login.php");

}
else
{
	header("Location: login.php");
}
?>