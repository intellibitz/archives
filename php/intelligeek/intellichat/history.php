<?php 
session_start();
include 'lib/config.inc.php';
include 'lib/func.php';
$connect = mysql_pconnect($host,$user,$pass) or die("Can't connect to MySQL");
mysql_select_db($database,$connect); 


	$sql = "DELETE FROM chat_text WHERE unix_timestamp()-timestamp > 3600";
	mysql_query($sql);
	$sql = "SELECT * FROM chat_text ORDER BY no ASC";
	$result = mysql_query($sql);
	$num = mysql_num_rows($result);

?>
   <html>
<head>
<title><?php echo $title; ?></title>
<meta http-equiv="Content-Type" content="text/html; charset=windows-874">
<link href="style.css" rel="stylesheet" type="text/css">
<body bgcolor="#C7DFF3" topmargin="0" leftmargin="0">
<center><font size="2"><u><strong>Message History ( Last 1 Hour  )</strong></u></font></center><br>
<table width="590" border="0" cellspacing="0" cellpadding="0">
  <?php 
while($arr = mysql_fetch_array($result))
{
if($arr['sendto']=="" && $arr['togroup']=="")
{
$i++;
?>
  <tr> 
    <td width="590" height="15" bgcolor="<?php if($i%2==0){echo "#ADD0ED";}else{echo "#C7DFF3";}?>"><font size="1" face="MS Sans Serif, Tahoma, sans-serif"><?php echo "[".$arr['time']."] "."<strong>".$arr['name']."</strong> : "; echo "<font color='".$arr['color']."'>";if($arr['bold']==1){echo "<strong>";} if($arr['underline']==1){echo "<u>";} echo CheckEmo(encode($arr['text'])); if($arr['underline']==1){echo "</u>";} if($arr['bold']==1){echo "</strong>";} echo "</font>"; ?></font></td>
  </tr>
  <?php 
  }
}
?>
</table><a name="bottom"></a>

<?php mysql_close($connect);?>