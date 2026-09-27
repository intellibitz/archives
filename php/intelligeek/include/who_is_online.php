<?php
session_start();
if(isset($_SESSION['username']))
{
//echo $_session['username'];
?>
<html>
	<head>

<h3>Who is Online</h3>

	</head>
<p>

<?php
session_start();
$currentuser= $_SESSION['username'];
include('../config/config.php');
//$connect= mysql_connect("ns1","vijayalakshmik","vijayalakshmik") or die (mysql_error());
//echo "connected";
//mysql_select_db("vijayalakshmik",$connect) or die (mysql_error());
   $result=mysql_query("select username,datetime from onlineuser where username != '$currentuser'") or die(mysql_error());
   if (mysql_num_rows($result) > 0) {
// yes
// print them one after another


while($row = mysql_fetch_row($result))
{

?>

<img src="../images/onlineicon.gif ">


<?php
echo "<label id='onlinelbl' name='onlinelbl' value='$row[0]'>$row[0]</label>";
echo "<br>";
}

}
?>

</p>
</html>
<?php
}
else
{
session_destroy();
	//print " <i>Hello Guest! Your are not logged in</i>";?>
      <a href="javascript:ajax('../login/index.php','login')"> </a>

	   <?php
}
?>