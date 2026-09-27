<?php
include 'lib/config.inc.php';
include 'lib/func.php';

$connect = mysql_pconnect($host,$user,$pass) or die("Can't connect to MySQL");
mysql_select_db($database,$connect); 

switch($_GET['action'])
{

// Send Data()
case 'write':

$name = utf8_to_tis620($_POST['nname']);
$sendto = utf8_to_tis620($_POST['sendto']);
$data = utf8_to_tis620($_POST['data']);
$togroup = utf8_to_tis620($_POST['group']);
$input = "";

	if($data)
	{
	
	if($sendto==""){$sendto='NULL';}else{$sendto="'$sendto'";}
	if($togroup==""){$togroup='NULL';}else{$togroup="'$togroup'";}
	
	$sql = "INSERT INTO chat_text(name,sendto,timestamp,text,bold,underline,color,time,avatar,togroup) VALUES ('".$name."',".$sendto.",unix_timestamp(),'".$data."',".$_POST['boldval'].",".$_POST['underlineval'].",'".$_POST['color']."','".date("H:i")."','".$_POST['sex']."',".$togroup.")";
	mysql_query($sql);
	
	if(mysql_insert_id()>0)
	{

			echo mysql_insert_id();
			$sql = "UPDATE chat_online SET utimestmp=".time()." WHERE name='$name'";
			mysql_query($sql);

	}
	else
	{
	
		if($togroup!='NULL')
			{
			echo -2;
			}
		mysql_query("INSERT INTO `chat_online` ( `no` ,`name` , `ip` , `utimestmp` , `avatar` , `mystatus` ) VALUES (".$_POST['memno'].",'".$_POST['nname']."','".getenv("REMOTE_ADDR")."',".time().",'".$_POST['sex']."','".$_POST['status']."')");
		
		$sql = "INSERT INTO chat_text(name,sendto,timestamp,text,bold,underline,color,time,avatar) VALUES ('".$name."',".$sendto.",unix_timestamp(),'".$data."',".$_POST['boldval'].",".$_POST['underlineval'].",'".$_POST['color']."','".date("H:i")."','".$_POST['sex']."')";
		mysql_query($sql);
		
		
			if(mysql_insert_id()>0)
			{
			echo mysql_insert_id();
			}
			else
			{
			echo -1;
			}
	}
	
	}

break;
// End Send Data()


case 'online':
$sql = "select * from chat_online order by no asc";
$result = mysql_query($sql);
$num = mysql_num_rows($result);
$first = 0;

if($num>0)
{
	while($arr = mysql_fetch_array($result))
	{

	if($first==0){$first = 1;}else{echo "|@|";}
	$name = tis2utf8($arr['name']);
	echo $name."|#|".$arr['avatar']."|#|".$arr['mystatus']."|#|".$arr['no']."|#|".$arr['whisper']."|#|".tis2utf8($arr['love']);

	}
}

mysql_free_result($result);

break;

case 'setstatus':


	$sql = "UPDATE chat_online SET mystatus='".$_POST['statushide']."' WHERE no=".$_POST['memno'];
	$result = mysql_query($sql);
	
	echo $_POST['statushide'];
	
break;

case 'logout':

	$name = utf8_to_tis620($_POST['nname']);
	$sql = "DELETE FROM chat_online WHERE name ='$name' and ip='".getenv("REMOTE_ADDR")."'";
	mysql_query($sql);
	
	$filename = "./data/online.dat";
	$handle = @fopen($filename, "w");
	@fwrite($handle,time());
	@fclose($handle);

break;

case 'updateonline':
	$name = utf8_to_tis620($_POST['nname']);
	$sql = "UPDATE chat_online SET utimestmp=".time()." WHERE name='$name'";
	mysql_query($sql);
	
	if(mysql_affected_rows()>0)
	{
	echo 1;
	}
	else
	{
	echo $sql;
	}
	
break;

case 'userinfo':
	$sql = "SELECT * FROM chat_online WHERE no='".$_GET['no']."'";
	$result = mysql_query($sql);
	$row = mysql_num_rows($result);
	
	if($row>0)
	{
		$arr = mysql_fetch_array($result);
		echo tis2utf8($arr['name'])."|#|".$arr['ip']."|#|".$arr['avatar']."|#|".$arr['no'];
	}
	else
	{
	echo "no";
	}
break;

case 'delonline':
$sql = "DELETE FROM chat_online WHERE ".time()." - utimestmp > 300";
mysql_query($sql);

	if(mysql_affected_rows()>0)
	{
	$filename = "./data/online.dat";
	$handle = @fopen($filename, "w");
	@fwrite($handle,time());
	@fclose($handle);
	}
	
$sql = "DELETE FROM chat_text WHERE unix_timestamp()-timestamp > 3600";
mysql_query($sql);
break;

case 'color':
echo "<TABLE border='1' cellpadding=\"0\" cellspacing=\"0\">
<TR>
	<TD><!-- note: this version of the color picker is optimized for IE 5.5+ only -->


<table border=0 cellspacing=0 cellpadding=4>
 <tr>
  <td bgcolor=\"buttonface\" valign=center><div style=\"background-color: #000000; padding: 1; height: 21px; width: 50px\"><div id=\"ColorPreview\" style=\"height: 100%; width: 100%\"></div></div></td>
  <td bgcolor=\"buttonface\" valign=center><input type=\"text\" name=\"ColorHex\" value=\"\" size=15 style=\"font-size: 12px\"></td>
  <td bgcolor=\"buttonface\" width=100%></td>
 </tr>
</table>

<table border=0 cellspacing=1 cellpadding=0 bgcolor=#000000 style=\"cursor: hand;\">
<tr>
<td bgcolor=#000000 onMouseOver=View('000000') onClick=Set('000000') height=10 width=10></td>
<td bgcolor=#000000 onMouseOver=View('000000') onClick=Set('000000') height=10 width=10></td>
<td bgcolor=#000000 onMouseOver=View('000000') onClick=Set('000000') height=10 width=10></td>
<td bgcolor=#000000 onMouseOver=View('000000') onClick=Set('000000') height=10 width=10></td>
<td bgcolor=#003300 onMouseOver=View('003300') onClick=Set('003300') height=10 width=10></td>
<td bgcolor=#006600 onMouseOver=View('006600') onClick=Set('006600') height=10 width=10></td>
<td bgcolor=#009900 onMouseOver=View('009900') onClick=Set('009900') height=10 width=10></td>
<td bgcolor=#00CC00 onMouseOver=View('00CC00') onClick=Set('00CC00') height=10 width=10></td>
<td bgcolor=#00FF00 onMouseOver=View('00FF00') onClick=Set('00FF00') height=10 width=10></td>
<td bgcolor=#330000 onMouseOver=View('330000') onClick=Set('330000') height=10 width=10></td>
<td bgcolor=#333300 onMouseOver=View('333300') onClick=Set('333300') height=10 width=10></td>
<td bgcolor=#336600 onMouseOver=View('336600') onClick=Set('336600') height=10 width=10></td>
<td bgcolor=#339900 onMouseOver=View('339900') onClick=Set('339900') height=10 width=10></td>
<td bgcolor=#33CC00 onMouseOver=View('33CC00') onClick=Set('33CC00') height=10 width=10></td>
<td bgcolor=#33FF00 onMouseOver=View('33FF00') onClick=Set('33FF00') height=10 width=10></td>
<td bgcolor=#660000 onMouseOver=View('660000') onClick=Set('660000') height=10 width=10></td>
<td bgcolor=#663300 onMouseOver=View('663300') onClick=Set('663300') height=10 width=10></td>
<td bgcolor=#666600 onMouseOver=View('666600') onClick=Set('666600') height=10 width=10></td>
<td bgcolor=#669900 onMouseOver=View('669900') onClick=Set('669900') height=10 width=10></td>
<td bgcolor=#66CC00 onMouseOver=View('66CC00') onClick=Set('66CC00') height=10 width=10></td>
<td bgcolor=#66FF00 onMouseOver=View('66FF00') onClick=Set('66FF00') height=10 width=10></td>
</tr>
<tr>
<td bgcolor=#000000 onMouseOver=View('000000') onClick=Set('000000') height=10 width=10></td>
<td bgcolor=#333333 onMouseOver=View('333333') onClick=Set('333333') height=10 width=10></td>
<td bgcolor=#000000 onMouseOver=View('000000') onClick=Set('000000') height=10 width=10></td>
<td bgcolor=#000033 onMouseOver=View('000033') onClick=Set('000033') height=10 width=10></td>
<td bgcolor=#003333 onMouseOver=View('003333') onClick=Set('003333') height=10 width=10></td>
<td bgcolor=#006633 onMouseOver=View('006633') onClick=Set('006633') height=10 width=10></td>
<td bgcolor=#009933 onMouseOver=View('009933') onClick=Set('009933') height=10 width=10></td>
<td bgcolor=#00CC33 onMouseOver=View('00CC33') onClick=Set('00CC33') height=10 width=10></td>
<td bgcolor=#00FF33 onMouseOver=View('00FF33') onClick=Set('00FF33') height=10 width=10></td>
<td bgcolor=#330033 onMouseOver=View('330033') onClick=Set('330033') height=10 width=10></td>
<td bgcolor=#333333 onMouseOver=View('333333') onClick=Set('333333') height=10 width=10></td>
<td bgcolor=#336633 onMouseOver=View('336633') onClick=Set('336633') height=10 width=10></td>
<td bgcolor=#339933 onMouseOver=View('339933') onClick=Set('339933') height=10 width=10></td>
<td bgcolor=#33CC33 onMouseOver=View('33CC33') onClick=Set('33CC33') height=10 width=10></td>
<td bgcolor=#33FF33 onMouseOver=View('33FF33') onClick=Set('33FF33') height=10 width=10></td>
<td bgcolor=#660033 onMouseOver=View('660033') onClick=Set('660033') height=10 width=10></td>
<td bgcolor=#663333 onMouseOver=View('663333') onClick=Set('663333') height=10 width=10></td>
<td bgcolor=#666633 onMouseOver=View('666633') onClick=Set('666633') height=10 width=10></td>
<td bgcolor=#669933 onMouseOver=View('669933') onClick=Set('669933') height=10 width=10></td>
<td bgcolor=#66CC33 onMouseOver=View('66CC33') onClick=Set('66CC33') height=10 width=10></td>
<td bgcolor=#66FF33 onMouseOver=View('66FF33') onClick=Set('66FF33') height=10 width=10></td>
</tr>
<tr>
<td bgcolor=#000000 onMouseOver=View('000000') onClick=Set('000000') height=10 width=10></td>
<td bgcolor=#666666 onMouseOver=View('666666') onClick=Set('666666') height=10 width=10></td>
<td bgcolor=#000000 onMouseOver=View('000000') onClick=Set('000000') height=10 width=10></td>
<td bgcolor=#000066 onMouseOver=View('000066') onClick=Set('000066') height=10 width=10></td>
<td bgcolor=#003366 onMouseOver=View('003366') onClick=Set('003366') height=10 width=10></td>
<td bgcolor=#006666 onMouseOver=View('006666') onClick=Set('006666') height=10 width=10></td>
<td bgcolor=#009966 onMouseOver=View('009966') onClick=Set('009966') height=10 width=10></td>
<td bgcolor=#00CC66 onMouseOver=View('00CC66') onClick=Set('00CC66') height=10 width=10></td>
<td bgcolor=#00FF66 onMouseOver=View('00FF66') onClick=Set('00FF66') height=10 width=10></td>
<td bgcolor=#330066 onMouseOver=View('330066') onClick=Set('330066') height=10 width=10></td>
<td bgcolor=#333366 onMouseOver=View('333366') onClick=Set('333366') height=10 width=10></td>
<td bgcolor=#336666 onMouseOver=View('336666') onClick=Set('336666') height=10 width=10></td>
<td bgcolor=#339966 onMouseOver=View('339966') onClick=Set('339966') height=10 width=10></td>
<td bgcolor=#33CC66 onMouseOver=View('33CC66') onClick=Set('33CC66') height=10 width=10></td>
<td bgcolor=#33FF66 onMouseOver=View('33FF66') onClick=Set('33FF66') height=10 width=10></td>
<td bgcolor=#660066 onMouseOver=View('660066') onClick=Set('660066') height=10 width=10></td>
<td bgcolor=#663366 onMouseOver=View('663366') onClick=Set('663366') height=10 width=10></td>
<td bgcolor=#666666 onMouseOver=View('666666') onClick=Set('666666') height=10 width=10></td>
<td bgcolor=#669966 onMouseOver=View('669966') onClick=Set('669966') height=10 width=10></td>
<td bgcolor=#66CC66 onMouseOver=View('66CC66') onClick=Set('66CC66') height=10 width=10></td>
<td bgcolor=#66FF66 onMouseOver=View('66FF66') onClick=Set('66FF66') height=10 width=10></td>
</tr>
<tr>
<td bgcolor=#000000 onMouseOver=View('000000') onClick=Set('000000') height=10 width=10></td>
<td bgcolor=#999999 onMouseOver=View('999999') onClick=Set('999999') height=10 width=10></td>
<td bgcolor=#000000 onMouseOver=View('000000') onClick=Set('000000') height=10 width=10></td>
<td bgcolor=#000099 onMouseOver=View('000099') onClick=Set('000099') height=10 width=10></td>
<td bgcolor=#003399 onMouseOver=View('003399') onClick=Set('003399') height=10 width=10></td>
<td bgcolor=#006699 onMouseOver=View('006699') onClick=Set('006699') height=10 width=10></td>
<td bgcolor=#009999 onMouseOver=View('009999') onClick=Set('009999') height=10 width=10></td>
<td bgcolor=#00CC99 onMouseOver=View('00CC99') onClick=Set('00CC99') height=10 width=10></td>
<td bgcolor=#00FF99 onMouseOver=View('00FF99') onClick=Set('00FF99') height=10 width=10></td>
<td bgcolor=#330099 onMouseOver=View('330099') onClick=Set('330099') height=10 width=10></td>
<td bgcolor=#333399 onMouseOver=View('333399') onClick=Set('333399') height=10 width=10></td>
<td bgcolor=#336699 onMouseOver=View('336699') onClick=Set('336699') height=10 width=10></td>
<td bgcolor=#339999 onMouseOver=View('339999') onClick=Set('339999') height=10 width=10></td>
<td bgcolor=#33CC99 onMouseOver=View('33CC99') onClick=Set('33CC99') height=10 width=10></td>
<td bgcolor=#33FF99 onMouseOver=View('33FF99') onClick=Set('33FF99') height=10 width=10></td>
<td bgcolor=#660099 onMouseOver=View('660099') onClick=Set('660099') height=10 width=10></td>
<td bgcolor=#663399 onMouseOver=View('663399') onClick=Set('663399') height=10 width=10></td>
<td bgcolor=#666699 onMouseOver=View('666699') onClick=Set('666699') height=10 width=10></td>
<td bgcolor=#669999 onMouseOver=View('669999') onClick=Set('669999') height=10 width=10></td>
<td bgcolor=#66CC99 onMouseOver=View('66CC99') onClick=Set('66CC99') height=10 width=10></td>
<td bgcolor=#66FF99 onMouseOver=View('66FF99') onClick=Set('66FF99') height=10 width=10></td>
</tr>
<tr>
<td bgcolor=#000000 onMouseOver=View('000000') onClick=Set('000000') height=10 width=10></td>
<td bgcolor=#CCCCCC onMouseOver=View('CCCCCC') onClick=Set('CCCCCC') height=10 width=10></td>
<td bgcolor=#000000 onMouseOver=View('000000') onClick=Set('000000') height=10 width=10></td>
<td bgcolor=#0000CC onMouseOver=View('0000CC') onClick=Set('0000CC') height=10 width=10></td>
<td bgcolor=#0033CC onMouseOver=View('0033CC') onClick=Set('0033CC') height=10 width=10></td>
<td bgcolor=#0066CC onMouseOver=View('0066CC') onClick=Set('0066CC') height=10 width=10></td>
<td bgcolor=#0099CC onMouseOver=View('0099CC') onClick=Set('0099CC') height=10 width=10></td>
<td bgcolor=#00CCCC onMouseOver=View('00CCCC') onClick=Set('00CCCC') height=10 width=10></td>
<td bgcolor=#00FFCC onMouseOver=View('00FFCC') onClick=Set('00FFCC') height=10 width=10></td>
<td bgcolor=#3300CC onMouseOver=View('3300CC') onClick=Set('3300CC') height=10 width=10></td>
<td bgcolor=#3333CC onMouseOver=View('3333CC') onClick=Set('3333CC') height=10 width=10></td>
<td bgcolor=#3366CC onMouseOver=View('3366CC') onClick=Set('3366CC') height=10 width=10></td>
<td bgcolor=#3399CC onMouseOver=View('3399CC') onClick=Set('3399CC') height=10 width=10></td>
<td bgcolor=#33CCCC onMouseOver=View('33CCCC') onClick=Set('33CCCC') height=10 width=10></td>
<td bgcolor=#33FFCC onMouseOver=View('33FFCC') onClick=Set('33FFCC') height=10 width=10></td>
<td bgcolor=#6600CC onMouseOver=View('6600CC') onClick=Set('6600CC') height=10 width=10></td>
<td bgcolor=#6633CC onMouseOver=View('6633CC') onClick=Set('6633CC') height=10 width=10></td>
<td bgcolor=#6666CC onMouseOver=View('6666CC') onClick=Set('6666CC') height=10 width=10></td>
<td bgcolor=#6699CC onMouseOver=View('6699CC') onClick=Set('6699CC') height=10 width=10></td>
<td bgcolor=#66CCCC onMouseOver=View('66CCCC') onClick=Set('66CCCC') height=10 width=10></td>
<td bgcolor=#66FFCC onMouseOver=View('66FFCC') onClick=Set('66FFCC') height=10 width=10></td>
</tr>
<tr>
<td bgcolor=#000000 onMouseOver=View('000000') onClick=Set('000000') height=10 width=10></td>
<td bgcolor=#FFFFFF onMouseOver=View('FFFFFF') onClick=Set('FFFFFF') height=10 width=10></td>
<td bgcolor=#000000 onMouseOver=View('000000') onClick=Set('000000') height=10 width=10></td>
<td bgcolor=#0000FF onMouseOver=View('0000FF') onClick=Set('0000FF') height=10 width=10></td>
<td bgcolor=#0033FF onMouseOver=View('0033FF') onClick=Set('0033FF') height=10 width=10></td>
<td bgcolor=#0066FF onMouseOver=View('0066FF') onClick=Set('0066FF') height=10 width=10></td>
<td bgcolor=#0099FF onMouseOver=View('0099FF') onClick=Set('0099FF') height=10 width=10></td>
<td bgcolor=#00CCFF onMouseOver=View('00CCFF') onClick=Set('00CCFF') height=10 width=10></td>
<td bgcolor=#00FFFF onMouseOver=View('00FFFF') onClick=Set('00FFFF') height=10 width=10></td>
<td bgcolor=#3300FF onMouseOver=View('3300FF') onClick=Set('3300FF') height=10 width=10></td>
<td bgcolor=#3333FF onMouseOver=View('3333FF') onClick=Set('3333FF') height=10 width=10></td>
<td bgcolor=#3366FF onMouseOver=View('3366FF') onClick=Set('3366FF') height=10 width=10></td>
<td bgcolor=#3399FF onMouseOver=View('3399FF') onClick=Set('3399FF') height=10 width=10></td>
<td bgcolor=#33CCFF onMouseOver=View('33CCFF') onClick=Set('33CCFF') height=10 width=10></td>
<td bgcolor=#33FFFF onMouseOver=View('33FFFF') onClick=Set('33FFFF') height=10 width=10></td>
<td bgcolor=#6600FF onMouseOver=View('6600FF') onClick=Set('6600FF') height=10 width=10></td>
<td bgcolor=#6633FF onMouseOver=View('6633FF') onClick=Set('6633FF') height=10 width=10></td>
<td bgcolor=#6666FF onMouseOver=View('6666FF') onClick=Set('6666FF') height=10 width=10></td>
<td bgcolor=#6699FF onMouseOver=View('6699FF') onClick=Set('6699FF') height=10 width=10></td>
<td bgcolor=#66CCFF onMouseOver=View('66CCFF') onClick=Set('66CCFF') height=10 width=10></td>
<td bgcolor=#66FFFF onMouseOver=View('66FFFF') onClick=Set('66FFFF') height=10 width=10></td>
</tr>
<tr>
<td bgcolor=#000000 onMouseOver=View('000000') onClick=Set('000000') height=10 width=10></td>
<td bgcolor=#FF0000 onMouseOver=View('FF0000') onClick=Set('FF0000') height=10 width=10></td>
<td bgcolor=#000000 onMouseOver=View('000000') onClick=Set('000000') height=10 width=10></td>
<td bgcolor=#990000 onMouseOver=View('990000') onClick=Set('990000') height=10 width=10></td>
<td bgcolor=#993300 onMouseOver=View('993300') onClick=Set('993300') height=10 width=10></td>
<td bgcolor=#996600 onMouseOver=View('996600') onClick=Set('996600') height=10 width=10></td>
<td bgcolor=#999900 onMouseOver=View('999900') onClick=Set('999900') height=10 width=10></td>
<td bgcolor=#99CC00 onMouseOver=View('99CC00') onClick=Set('99CC00') height=10 width=10></td>
<td bgcolor=#99FF00 onMouseOver=View('99FF00') onClick=Set('99FF00') height=10 width=10></td>
<td bgcolor=#CC0000 onMouseOver=View('CC0000') onClick=Set('CC0000') height=10 width=10></td>
<td bgcolor=#CC3300 onMouseOver=View('CC3300') onClick=Set('CC3300') height=10 width=10></td>
<td bgcolor=#CC6600 onMouseOver=View('CC6600') onClick=Set('CC6600') height=10 width=10></td>
<td bgcolor=#CC9900 onMouseOver=View('CC9900') onClick=Set('CC9900') height=10 width=10></td>
<td bgcolor=#CCCC00 onMouseOver=View('CCCC00') onClick=Set('CCCC00') height=10 width=10></td>
<td bgcolor=#CCFF00 onMouseOver=View('CCFF00') onClick=Set('CCFF00') height=10 width=10></td>
<td bgcolor=#FF0000 onMouseOver=View('FF0000') onClick=Set('FF0000') height=10 width=10></td>
<td bgcolor=#FF3300 onMouseOver=View('FF3300') onClick=Set('FF3300') height=10 width=10></td>
<td bgcolor=#FF6600 onMouseOver=View('FF6600') onClick=Set('FF6600') height=10 width=10></td>
<td bgcolor=#FF9900 onMouseOver=View('FF9900') onClick=Set('FF9900') height=10 width=10></td>
<td bgcolor=#FFCC00 onMouseOver=View('FFCC00') onClick=Set('FFCC00') height=10 width=10></td>
<td bgcolor=#FFFF00 onMouseOver=View('FFFF00') onClick=Set('FFFF00') height=10 width=10></td>
</tr>
<tr>
<td bgcolor=#000000 onMouseOver=View('000000') onClick=Set('000000') height=10 width=10></td>
<td bgcolor=#00FF00 onMouseOver=View('00FF00') onClick=Set('00FF00') height=10 width=10></td>
<td bgcolor=#000000 onMouseOver=View('000000') onClick=Set('000000') height=10 width=10></td>
<td bgcolor=#990033 onMouseOver=View('990033') onClick=Set('990033') height=10 width=10></td>
<td bgcolor=#993333 onMouseOver=View('993333') onClick=Set('993333') height=10 width=10></td>
<td bgcolor=#996633 onMouseOver=View('996633') onClick=Set('996633') height=10 width=10></td>
<td bgcolor=#999933 onMouseOver=View('999933') onClick=Set('999933') height=10 width=10></td>
<td bgcolor=#99CC33 onMouseOver=View('99CC33') onClick=Set('99CC33') height=10 width=10></td>
<td bgcolor=#99FF33 onMouseOver=View('99FF33') onClick=Set('99FF33') height=10 width=10></td>
<td bgcolor=#CC0033 onMouseOver=View('CC0033') onClick=Set('CC0033') height=10 width=10></td>
<td bgcolor=#CC3333 onMouseOver=View('CC3333') onClick=Set('CC3333') height=10 width=10></td>
<td bgcolor=#CC6633 onMouseOver=View('CC6633') onClick=Set('CC6633') height=10 width=10></td>
<td bgcolor=#CC9933 onMouseOver=View('CC9933') onClick=Set('CC9933') height=10 width=10></td>
<td bgcolor=#CCCC33 onMouseOver=View('CCCC33') onClick=Set('CCCC33') height=10 width=10></td>
<td bgcolor=#CCFF33 onMouseOver=View('CCFF33') onClick=Set('CCFF33') height=10 width=10></td>
<td bgcolor=#FF0033 onMouseOver=View('FF0033') onClick=Set('FF0033') height=10 width=10></td>
<td bgcolor=#FF3333 onMouseOver=View('FF3333') onClick=Set('FF3333') height=10 width=10></td>
<td bgcolor=#FF6633 onMouseOver=View('FF6633') onClick=Set('FF6633') height=10 width=10></td>
<td bgcolor=#FF9933 onMouseOver=View('FF9933') onClick=Set('FF9933') height=10 width=10></td>
<td bgcolor=#FFCC33 onMouseOver=View('FFCC33') onClick=Set('FFCC33') height=10 width=10></td>
<td bgcolor=#FFFF33 onMouseOver=View('FFFF33') onClick=Set('FFFF33') height=10 width=10></td>
</tr>
<tr>
<td bgcolor=#000000 onMouseOver=View('000000') onClick=Set('000000') height=10 width=10></td>
<td bgcolor=#0000FF onMouseOver=View('0000FF') onClick=Set('0000FF') height=10 width=10></td>
<td bgcolor=#000000 onMouseOver=View('000000') onClick=Set('000000') height=10 width=10></td>
<td bgcolor=#990066 onMouseOver=View('990066') onClick=Set('990066') height=10 width=10></td>
<td bgcolor=#993366 onMouseOver=View('993366') onClick=Set('993366') height=10 width=10></td>
<td bgcolor=#996666 onMouseOver=View('996666') onClick=Set('996666') height=10 width=10></td>
<td bgcolor=#999966 onMouseOver=View('999966') onClick=Set('999966') height=10 width=10></td>
<td bgcolor=#99CC66 onMouseOver=View('99CC66') onClick=Set('99CC66') height=10 width=10></td>
<td bgcolor=#99FF66 onMouseOver=View('99FF66') onClick=Set('99FF66') height=10 width=10></td>
<td bgcolor=#CC0066 onMouseOver=View('CC0066') onClick=Set('CC0066') height=10 width=10></td>
<td bgcolor=#CC3366 onMouseOver=View('CC3366') onClick=Set('CC3366') height=10 width=10></td>
<td bgcolor=#CC6666 onMouseOver=View('CC6666') onClick=Set('CC6666') height=10 width=10></td>
<td bgcolor=#CC9966 onMouseOver=View('CC9966') onClick=Set('CC9966') height=10 width=10></td>
<td bgcolor=#CCCC66 onMouseOver=View('CCCC66') onClick=Set('CCCC66') height=10 width=10></td>
<td bgcolor=#CCFF66 onMouseOver=View('CCFF66') onClick=Set('CCFF66') height=10 width=10></td>
<td bgcolor=#FF0066 onMouseOver=View('FF0066') onClick=Set('FF0066') height=10 width=10></td>
<td bgcolor=#FF3366 onMouseOver=View('FF3366') onClick=Set('FF3366') height=10 width=10></td>
<td bgcolor=#FF6666 onMouseOver=View('FF6666') onClick=Set('FF6666') height=10 width=10></td>
<td bgcolor=#FF9966 onMouseOver=View('FF9966') onClick=Set('FF9966') height=10 width=10></td>
<td bgcolor=#FFCC66 onMouseOver=View('FFCC66') onClick=Set('FFCC66') height=10 width=10></td>
<td bgcolor=#FFFF66 onMouseOver=View('FFFF66') onClick=Set('FFFF66') height=10 width=10></td>
</tr>
<tr>
<td bgcolor=#000000 onMouseOver=View('000000') onClick=Set('000000') height=10 width=10></td>
<td bgcolor=#FFFF00 onMouseOver=View('FFFF00') onClick=Set('FFFF00') height=10 width=10></td>
<td bgcolor=#000000 onMouseOver=View('000000') onClick=Set('000000') height=10 width=10></td>
<td bgcolor=#990099 onMouseOver=View('990099') onClick=Set('990099') height=10 width=10></td>
<td bgcolor=#993399 onMouseOver=View('993399') onClick=Set('993399') height=10 width=10></td>
<td bgcolor=#996699 onMouseOver=View('996699') onClick=Set('996699') height=10 width=10></td>
<td bgcolor=#999999 onMouseOver=View('999999') onClick=Set('999999') height=10 width=10></td>
<td bgcolor=#99CC99 onMouseOver=View('99CC99') onClick=Set('99CC99') height=10 width=10></td>
<td bgcolor=#99FF99 onMouseOver=View('99FF99') onClick=Set('99FF99') height=10 width=10></td>
<td bgcolor=#CC0099 onMouseOver=View('CC0099') onClick=Set('CC0099') height=10 width=10></td>
<td bgcolor=#CC3399 onMouseOver=View('CC3399') onClick=Set('CC3399') height=10 width=10></td>
<td bgcolor=#CC6699 onMouseOver=View('CC6699') onClick=Set('CC6699') height=10 width=10></td>
<td bgcolor=#CC9999 onMouseOver=View('CC9999') onClick=Set('CC9999') height=10 width=10></td>
<td bgcolor=#CCCC99 onMouseOver=View('CCCC99') onClick=Set('CCCC99') height=10 width=10></td>
<td bgcolor=#CCFF99 onMouseOver=View('CCFF99') onClick=Set('CCFF99') height=10 width=10></td>
<td bgcolor=#FF0099 onMouseOver=View('FF0099') onClick=Set('FF0099') height=10 width=10></td>
<td bgcolor=#FF3399 onMouseOver=View('FF3399') onClick=Set('FF3399') height=10 width=10></td>
<td bgcolor=#FF6699 onMouseOver=View('FF6699') onClick=Set('FF6699') height=10 width=10></td>

<td bgcolor=#FF9999 onMouseOver=View('FF9999') onClick=Set('FF9999') height=10 width=10></td>
<td bgcolor=#FFCC99 onMouseOver=View('FFCC99') onClick=Set('FFCC99') height=10 width=10></td>
<td bgcolor=#FFFF99 onMouseOver=View('FFFF99') onClick=Set('FFFF99') height=10 width=10></td>
</tr>
<tr>
<td bgcolor=#000000 onMouseOver=View('000000') onClick=Set('000000') height=10 width=10></td>
<td bgcolor=#00FFFF onMouseOver=View('00FFFF') onClick=Set('00FFFF') height=10 width=10></td>
<td bgcolor=#000000 onMouseOver=View('000000') onClick=Set('000000') height=10 width=10></td>
<td bgcolor=#9900CC onMouseOver=View('9900CC') onClick=Set('9900CC') height=10 width=10></td>
<td bgcolor=#9933CC onMouseOver=View('9933CC') onClick=Set('9933CC') height=10 width=10></td>
<td bgcolor=#9966CC onMouseOver=View('9966CC') onClick=Set('9966CC') height=10 width=10></td>
<td bgcolor=#9999CC onMouseOver=View('9999CC') onClick=Set('9999CC') height=10 width=10></td>
<td bgcolor=#99CCCC onMouseOver=View('99CCCC') onClick=Set('99CCCC') height=10 width=10></td>
<td bgcolor=#99FFCC onMouseOver=View('99FFCC') onClick=Set('99FFCC') height=10 width=10></td>
<td bgcolor=#CC00CC onMouseOver=View('CC00CC') onClick=Set('CC00CC') height=10 width=10></td>
<td bgcolor=#CC33CC onMouseOver=View('CC33CC') onClick=Set('CC33CC') height=10 width=10></td>
<td bgcolor=#CC66CC onMouseOver=View('CC66CC') onClick=Set('CC66CC') height=10 width=10></td>
<td bgcolor=#CC99CC onMouseOver=View('CC99CC') onClick=Set('CC99CC') height=10 width=10></td>
<td bgcolor=#CCCCCC onMouseOver=View('CCCCCC') onClick=Set('CCCCCC') height=10 width=10></td>
<td bgcolor=#CCFFCC onMouseOver=View('CCFFCC') onClick=Set('CCFFCC') height=10 width=10></td>
<td bgcolor=#FF00CC onMouseOver=View('FF00CC') onClick=Set('FF00CC') height=10 width=10></td>
<td bgcolor=#FF33CC onMouseOver=View('FF33CC') onClick=Set('FF33CC') height=10 width=10></td>
<td bgcolor=#FF66CC onMouseOver=View('FF66CC') onClick=Set('FF66CC') height=10 width=10></td>
<td bgcolor=#FF99CC onMouseOver=View('FF99CC') onClick=Set('FF99CC') height=10 width=10></td>
<td bgcolor=#FFCCCC onMouseOver=View('FFCCCC') onClick=Set('FFCCCC') height=10 width=10></td>
<td bgcolor=#FFFFCC onMouseOver=View('FFFFCC') onClick=Set('FFFFCC') height=10 width=10></td>
</tr>
<tr>
<td bgcolor=#000000 onMouseOver=View('000000') onClick=Set('000000') height=10 width=10></td>
<td bgcolor=#FF00FF onMouseOver=View('FF00FF') onClick=Set('FF00FF') height=10 width=10></td>
<td bgcolor=#000000 onMouseOver=View('000000') onClick=Set('000000') height=10 width=10></td>
<td bgcolor=#9900FF onMouseOver=View('9900FF') onClick=Set('9900FF') height=10 width=10></td>
<td bgcolor=#9933FF onMouseOver=View('9933FF') onClick=Set('9933FF') height=10 width=10></td>
<td bgcolor=#9966FF onMouseOver=View('9966FF') onClick=Set('9966FF') height=10 width=10></td>
<td bgcolor=#9999FF onMouseOver=View('9999FF') onClick=Set('9999FF') height=10 width=10></td>
<td bgcolor=#99CCFF onMouseOver=View('99CCFF') onClick=Set('99CCFF') height=10 width=10></td>
<td bgcolor=#99FFFF onMouseOver=View('99FFFF') onClick=Set('99FFFF') height=10 width=10></td>
<td bgcolor=#CC00FF onMouseOver=View('CC00FF') onClick=Set('CC00FF') height=10 width=10></td>
<td bgcolor=#CC33FF onMouseOver=View('CC33FF') onClick=Set('CC33FF') height=10 width=10></td>
<td bgcolor=#CC66FF onMouseOver=View('CC66FF') onClick=Set('CC66FF') height=10 width=10></td>
<td bgcolor=#CC99FF onMouseOver=View('CC99FF') onClick=Set('CC99FF') height=10 width=10></td>
<td bgcolor=#CCCCFF onMouseOver=View('CCCCFF') onClick=Set('CCCCFF') height=10 width=10></td>
<td bgcolor=#CCFFFF onMouseOver=View('CCFFFF') onClick=Set('CCFFFF') height=10 width=10></td>
<td bgcolor=#FF00FF onMouseOver=View('FF00FF') onClick=Set('FF00FF') height=10 width=10></td>
<td bgcolor=#FF33FF onMouseOver=View('FF33FF') onClick=Set('FF33FF') height=10 width=10></td>
<td bgcolor=#FF66FF onMouseOver=View('FF66FF') onClick=Set('FF66FF') height=10 width=10></td>
<td bgcolor=#FF99FF onMouseOver=View('FF99FF') onClick=Set('FF99FF') height=10 width=10></td>
<td bgcolor=#FFCCFF onMouseOver=View('FFCCFF') onClick=Set('FFCCFF') height=10 width=10></td>
<td bgcolor=#FFFFFF onMouseOver=View('FFFFFF') onClick=Set('FFFFFF') height=10 width=10></td>
</tr>
</table>
</TD>
</TR>
</TABLE>";

break;

case 'emotable':
echo "<table bgcolor=\"#7BB4E1\" border=\"1\">
<tr>
<td>
<table width=\"160\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">
                      <tr> 
                        <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('10')\"><img src=\"images/emoticons/10.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
                        <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('11')\"><img src=\"images/emoticons/11.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
                        <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('12')\"><img src=\"images/emoticons/12.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
                        <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('13')\"><img src=\"images/emoticons/13.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
                        <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('14')\"><img src=\"images/emoticons/14.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
                        <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('15')\"><img src=\"images/emoticons/15.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
                        <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('16')\"><img src=\"images/emoticons/16.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
                        <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('17')\"><img src=\"images/emoticons/17.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
                        </tr>
                        <tr>
                        <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('18')\"><img src=\"images/emoticons/18.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
                        <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('19')\"><img src=\"images/emoticons/19.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
                        <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('20')\"><img src=\"images/emoticons/20.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
                        <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('21')\"><img src=\"images/emoticons/21.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
                        <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('22')\"><img src=\"images/emoticons/22.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
                        <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('23')\"><img src=\"images/emoticons/23.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
                        <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('24')\"><img src=\"images/emoticons/24.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
                        <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('25')\"><img src=\"images/emoticons/25.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
                        </tr>
                        <tr>
                        <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('26')\"><img src=\"images/emoticons/26.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
                        <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('27')\"><img src=\"images/emoticons/27.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
                        <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('28')\"><img src=\"images/emoticons/28.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
                        <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('29')\" ><img src=\"images/emoticons/29.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
                        <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('30')\"><img src=\"images/emoticons/30.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
                        <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('31')\"><img src=\"images/emoticons/31.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
                        <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('32')\"><img src=\"images/emoticons/32.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
                        <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('41')\"><img src=\"images/emoticons/41.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
                        </tr>
                        <tr>
                        <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('34')\"><img src=\"images/emoticons/34.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
                        <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('35')\"><img src=\"images/emoticons/35.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
                        <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('36')\"><img src=\"images/emoticons/36.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
                        <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('37')\"><img src=\"images/emoticons/37.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
                        <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('38')\"><img src=\"images/emoticons/38.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
                        <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('39')\"><img src=\"images/emoticons/39.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
                        <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('40')\"><img src=\"images/emoticons/40.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
	      <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('50')\"><img src=\"images/emoticons/50.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
                      </tr>
	    <tr>
	     <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('stu')\"><img src=\"images/emoticons/stu.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
		 <td width=\"20\"><a onMouseOver=\"this.style.cursor='hand'\" onClick=\"InsertEmo('valentine')\"><img src=\"images/emoticons/valentine.gif\" width=\"19\" height=\"19\" border=\"0\"></a></td>
	     <td width=\"20\"></td>
	   </tr>
                    </table>
</td>
</tr>
</table>";
break;

case 'wink':
echo "
<table width=\"188\" height=\"180\" border=\"1\" bgcolor=\"#7BB4E1\">
<tr>
<td>
<table width=\"175\" border=\"0\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\">
        <tr> 
          <td width=\"35\"><div align=\"center\"> <img src=\"images/winks/1.gif\" width=\"32\" height=\"32\" border=\"1\" onClick=\"insertWink('1')\"></div></td>
          <td width=\"35\"><div align=\"center\"> <img src=\"images/winks/2.gif\" width=\"32\" height=\"32\" border=\"1\" onClick=\"insertWink('2')\"> 
            </div></td>
          <td width=\"35\"><div align=\"center\"> <img src=\"images/winks/3.gif\" width=\"32\" height=\"32\" border=\"1\" onClick=\"insertWink('3')\"> 
            </div></td>
          <td width=\"35\"><div align=\"center\"> <img src=\"images/winks/4.gif\" width=\"32\" height=\"32\" border=\"1\" onClick=\"insertWink('4')\"> 
            </div></td>
          <td width=\"35\"><div align=\"center\"> <img src=\"images/winks/5.gif\" width=\"32\" height=\"32\" border=\"1\" onClick=\"insertWink('5')\"> 
            </div></td>
        </tr>
        <tr> 
          <td width=\"35\"><div align=\"center\"> <img src=\"images/winks/6.gif\" width=\"32\" height=\"32\" border=\"1\" onClick=\"insertWink('6')\"> 
            </div></td>
          <td width=\"35\"><div align=\"center\"> <img src=\"images/winks/7.gif\" width=\"32\" height=\"32\" border=\"1\" onClick=\"insertWink('7')\"> 
            </div></td>
          <td width=\"35\"><div align=\"center\"> <img src=\"images/winks/8.gif\" width=\"32\" height=\"32\" border=\"1\" onClick=\"insertWink('8')\"> 
            </div></td>
          <td width=\"35\"><div align=\"center\"> <img src=\"images/winks/9.gif\" width=\"32\" height=\"32\" border=\"1\" onClick=\"insertWink('9')\"> 
            </div></td>
          <td width=\"35\"><div align=\"center\"> <img src=\"images/winks/10.gif\" width=\"32\" height=\"32\" border=\"1\" onClick=\"insertWink('10')\"> 
            </div></td>
        </tr>
        <tr> 
          <td width=\"35\"><div align=\"center\"> <img src=\"images/winks/11.gif\" width=\"32\" height=\"32\" border=\"1\" onClick=\"insertWink('11')\"> 
            </div></td>
          <td width=\"35\"><div align=\"center\"> <img src=\"images/winks/12.gif\" width=\"32\" height=\"32\" border=\"1\" onClick=\"insertWink('12')\"> 
            </div></td>
          <td width=\"35\"><div align=\"center\"> <img src=\"images/winks/13.gif\" width=\"32\" height=\"32\" border=\"1\" onClick=\"insertWink('13')\"> 
            </div></td>
          <td width=\"35\"><div align=\"center\"> <img src=\"images/winks/14.gif\" width=\"32\" height=\"32\" border=\"1\" onClick=\"insertWink('14')\"> 
            </div></td>
          <td width=\"35\"><div align=\"center\"> <img src=\"images/winks/15.gif\" width=\"32\" height=\"32\" border=\"1\" onClick=\"insertWink('15')\"> 
            </div></td>
        </tr>
        <tr> 
          <td width=\"35\"><div align=\"center\"> <img src=\"images/winks/16.gif\" width=\"32\" height=\"32\" border=\"1\" onClick=\"insertWink('16')\"> 
            </div></td>
          <td width=\"35\"><div align=\"center\"> <img src=\"images/winks/17.gif\" width=\"32\" height=\"32\" border=\"1\" onClick=\"insertWink('17')\"> 
            </div></td>
          <td width=\"35\"><div align=\"center\"> <img src=\"images/winks/18.gif\" width=\"32\" height=\"32\" border=\"1\" onClick=\"insertWink('18')\"> 
            </div></td>
          <td width=\"35\"><div align=\"center\"> <img src=\"images/winks/19.gif\" width=\"32\" height=\"32\" border=\"1\" onClick=\"insertWink('19')\"> 
            </div></td>
          <td width=\"35\"><div align=\"center\"> <img src=\"images/winks/20.gif\" width=\"32\" height=\"32\" border=\"1\" onClick=\"insertWink('20')\"> 
            </div></td>
        </tr>
        <tr> 
          <td><div align=\"center\"> <img src=\"images/winks/21.gif\" width=\"32\" height=\"32\" border=\"1\" onClick=\"insertWink('21')\"> 
            </div></td>
          <td><div align=\"center\"></div></td>
          <td><div align=\"center\"></div></td>
          <td><div align=\"center\"></div></td>
          <td><div align=\"center\"></div></td>
        </tr>
      </table>
</td>
</tr>
</table>
";
break;

case 'groupchat':
echo "<table width=\"244\" class=\"outofarea\" height=\"55\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" bordercolor=\"#336699\">
  <tr bgcolor=\"#42A2F0\">
    <td height=\"30\" colspan=\"2\" align=\"center\"><font color=\"#000000\" size=\"2\"><strong>Chat System (GroupMode)</strong></font></td>
  </tr>
  <tr>
    <td width=\"120\" align=\"center\" valign=\"middle\" bgcolor=\"#6cbdec\"><div align=\"center\"><img src=\"images/buttons/create_group.png\" width=\"120\" height=\"50\" alt=\"Click To Create New Group\" onMouseOver=\"this.style.cursor='hand'\" onClick=\"CreateGroup()\"> </div></td>
    <td width=\"120\" align=\"center\" valign=\"middle\" bgcolor=\"#6cbdec\"><div align=\"center\"><img src=\"images/buttons/choose_group.png\" width=\"120\" height=\"50\" alt=\"Click To Join In GroupMode\" onMouseOver=\"this.style.cursor='hand'\" onClick=\"GroupList()\"></div></td>
  </tr>
</table>";
break;

case 'creategroup':

if(eregi("^[a-zA-Z0-9]{1,10}$",$_GET['groupname']) && eregi("^[a-zA-Z0-9]{1,10}$",$_GET['grouppass']) && $_GET['groupname']!="null" && $_GET['grouppass']!="null")
{
$sql = "INSERT INTO chat_group(groupname,grouppass) VALUES('".$_GET['groupname']."','".$_GET['grouppass']."')";
mysql_query($sql);

	if(mysql_insert_id()>0)
	{
	echo 1;
	}
}
else
{
echo 0;
}
	
break;

case 'grouplist':

$sql = "SELECT * FROM chat_group";
$result = mysql_query($sql);


echo "<table width=\"244\" class=\"outofarea\" height=\"125\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" bordercolor=\"#336699\">
  <tr bgcolor=\"#42A2F0\">
    <td height=\"30\" colspan=\"2\" align=\"center\"><font color=\"#000000\" size=\"2\"><strong>Chat System (GroupMode)</strong></font></td>
  </tr>
  <tr>
    <td width=\"75\" height=\"30\" align=\"center\" valign=\"middle\" bgcolor=\"#6cbdec\"><div align=\"center\"><font size=\"2\"><strong>เลือกห้อง</strong></font></div></td>
    <td width=\"169\" align=\"center\" valign=\"middle\" bgcolor=\"#6cbdec\"><div align=\"left\">
      <select name=\"logingroupname\" id=\"logingroupname\" class=\"button\">";
	  
	  while($dbarr = mysql_fetch_array($result))
	  {
	  echo "<option value=\"".$dbarr['id']."\">[".$dbarr['id']."] ".$dbarr['groupname']."</option>";
	  }
	  echo "
      </select>
    </div></td>
  </tr>
  <tr>
    <td align=\"center\" valign=\"middle\" bgcolor=\"#6cbdec\"><font size=\"2\"><strong>Password</strong></font></td>
    <td align=\"center\" valign=\"middle\" bgcolor=\"#6cbdec\"><div align=\"left\">
      <input type=\"text\" id=\"logingrouppass\" name=\"logingrouppass\"  class=\"button\">
    </div></td>
  </tr>
  <tr>
    <td align=\"center\" valign=\"middle\" bgcolor=\"#6cbdec\">&nbsp;</td>
    <td align=\"center\" valign=\"middle\" bgcolor=\"#6cbdec\"><div align=\"left\">
      <input type=\"button\" name=\"Submit5\" value=\"Enter\"  class=\"button\" onClick=\"SelectGroup()\">
      <input type=\"button\" name=\"Submit2\" value=\"Cancel\"  class=\"button\" onClick=\"GroupMode()\">
    </div></td>
  </tr>
</table>
";
break;

case 'checkgroup':

$sql = "SELECT * FROM chat_group WHERE id=".$_POST['logingroupname']." AND grouppass='".$_POST['logingrouppass']."'";
$result = mysql_query($sql);
$num = mysql_num_rows($result);

if($num>0)
{
$dbarr = mysql_fetch_array($result);
echo $dbarr['groupname']."|#|";
}
else
{
echo 0;
}
break;
}
mysql_close($connect);
?>