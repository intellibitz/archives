<?php
/*
 session_destroy();
 echo "you have successfully logged out";
  //header('Location: recruiterlogin.php');
  header('Location: home.php');
  */
 ?><!--
 <html>
 <head>
 <script language="javascript" >
history.go(1); /* undo user navigation (ex: IE Back Button) */
</script>
</head>
</html>
-->

<?php
session_start();
$username= $_SESSION['username'];

include('../config/config.php');
//$connect= mysql_connect("ns1","vijayalakshmik","vijayalakshmik") or die (mysql_error());
//echo "connected";
//mysql_select_db("vijayalakshmik",$connect) or die (mysql_error());
if($_REQUEST['logout']='logout')
{
mysql_query("delete from onlineuser where username='$username'") or die (mysql_error());
   session_unset();
  session_destroy();
  //include "log.php";

  echo "you Are Successfully Log Out, Thanks for Visiting<br>";
header('Location: ../welcome/index.php');
 //<a href="javascript:ajax('../welcome/index.php','content')"></a>
}
?>