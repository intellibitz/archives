<?php

session_start();
if(isset($_SESSION['username']))
{
//echo $_session['username'];
?>
<?php
//session_start();
$user= $_SESSION['username'];

if(isset($_GET['id']))
{
	include('../config/config.php');

	//mysql_connect("ns1","vijayalakshmik","vijayalakshmik");
    //mysql_select_db("vijayalakshmik");
	$user= $_SESSION['username'];
	echo $user;
	//$user= $_GET['username'];
	$id      = $_GET['id'];
	$query   = "SELECT name, type, size, content FROM upload WHERE username = '$user'";
	$result  = mysql_query($query) or die('Error, query failed');
	list($name, $type, $size, $content) = mysql_fetch_array($result);

	header("Content-Disposition: attachment; filename=$name");
	header("Content-length: $size");
	header("Content-type: $type");
	echo $content;

	//include 'library/closedb.php';
	exit;
}

?>
<html>
<head><div align="left"><u><?php print
$_SESSION['username'];?></u> | <a href="javascript:ajax('../login/changepswd.php','login')">changepswd</a> | <a href="../login/logout.php">Logout</a></div><br>

</head>
<body>
<form name="photofrm" method="post" action=""javascript:getonlinename(document.getElementById('photofrm'));"" onsubmit="">
	<div align="left">
<table id="photo"   class="" width="150" >
<tr><td><?php

  include('../config/config.php');
//mysql_connect("ns1","vijayalakshmik","vijayalakshmik");
//mysql_select_db("vijayalakshmik");
$query  = "SELECT id, name FROM upload where username='$user'";
$result = mysql_query($query) or die('Error, query failed');
if(mysql_num_rows($result) == 0)
{
print "<table  width='100px 'height='100px' border='1'>";
print"</table>";
}
else
{
	 while(list($id, $name) = mysql_fetch_array($result))
	  // while(($result))

	{
?>
<!--
	<a href="imagedownload.php?id=<?=$id;?>"><?=$name;?></a> <br>
	-->

<img src="../login/checkimage.php?id=<?=$id;?>" width="100px" height="100px"><br>
<?
	}
}
//include 'library/closedb.php';
?></td></tr>
</table>

<table id="photo1"  class="photo1" width="150">
<tr><td>&nbsp;&nbsp;&nbsp;<a href="javascript:ajax('../login/upload.php','login')">Upload Photo</a></td></tr>
 <!--<tr><td><a href="dummycheckimage.php">see picture</a></td></tr>-->
 <input type="hidden" id="username" name="username" value="<?php $_session['username']; ?>">
</table>
<label id="msg" class="msg" /><?php
if ($errorMessage != '') {
?>
<font color="red">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<blink><?php echo $errorMessage; ?></blink></font>
<?php
}
?></label>

</div>
</body>
</html>
<?php
}
else
{
session_destroy();
	print " <i>Hello Guest! Your are not logged in</i>";?>
      <a href="javascript:ajax('../login/index.php','login')">Login </a>  Here

	   <?php
}
?>