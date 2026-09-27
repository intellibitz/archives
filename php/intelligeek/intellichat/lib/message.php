<?php 
require_once('func.php');
require_once('config.inc.php');

$first = 0;

if($_GET['messno']==-1)
{
$sql = "SELECT * FROM chat_text ORDER BY no DESC LIMIT 0,1";
}
else
{
$sql = "SELECT * FROM chat_text WHERE no>".$_GET['messno']." ORDER BY no ASC";
}
$result = mysql_query($sql);
	if(mysql_num_rows($result)>0)
	{
		while($arr = mysql_fetch_array($result)){
			if($first==0){$first = 1;}else{echo "|@|";}
			echo tis2utf8($arr['name'])."|#|".tis2utf8(CheckEmo(encode($arr['text'])))."|#|".$arr['bold']."|#|".$arr['underline']."|#|".$arr['color']."|#|".$arr['time']."|#|".tis2utf8($arr['sendto'])."|#|".$arr['no']."|#|".$arr['avatar']."|#|".tis2utf8($arr['togroup']);
		}
	}


?>