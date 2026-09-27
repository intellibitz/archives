<?php
session_start();
$user=$_SESSION['username'];
include('../config/config.php');

//$connect = mysql_connect('192.168.1.6','geek','geek') or die("excuted");
//mysql_select_db('geek',$connect) or die("not done");
//echo ("connected");
$errorMessage='';
$oldpass=$_POST['oldpwsd'];
//echo $oldpass;
$newpass=$_POST['newpwsd'];
$select=mysql_query("select * from user where password=PASSWORD('$oldpass')") or die("not done");
	//echo $select;

if (mysql_num_rows($select)!=0)
   		{

$up="update user SET password=PASSWORD('$newpass') where username='$user'";
//echo 'connected';
mysql_query($up) or die(mysql_error());

 $errorMessage = 'Password Changed sucessfully';
 echo $errorMessage;
 }
else
{
$errorMessage = 'Enter The Old Password Correctly!';
	echo $errorMessage;
	}
?>
