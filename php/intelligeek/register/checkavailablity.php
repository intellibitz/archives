<?php
  $user = $_REQUEST['user'];

include('../config/config.php');

//$connect = mysql_connect("ns1","vijayalakshmik","vijayalakshmik") or die (mysql_error());
//mysql_select_db ("vijayalakshmik",$connect) or die (mysql_error());
$query = mysql_query("select * from user where username='$user'");
$result = mysql_fetch_row($query);
//var_dump($result);
$str=NULL;
if ($result[1]==$user)
{
$str="Username Already Exist";
echo $str;
}
else
{

//$str="Username Available";
//echo $str;

}

?>