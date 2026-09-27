<?php
session_start();
include 'lib/config.inc.php';
$connect = mysql_pconnect($host,$user,$pass) or die("<font size=2><strong>Error</strong> : Cannot Connect To Your Database Please Check Variable In  lib/config.inc.php</font>");
mysql_select_db($database,$connect);


			$sql = "DELETE FROM chat_online WHERE (name='".$_COOKIE['login']."' and ip='".$_COOKIE['ip']."') OR (unix_timestamp() - utimestmp > 300)";
			$result = mysql_query($sql) or die("<font size=2><strong>Error</strong> : The First Time Of MessChat Please Go To <a href=\"install.php\">install.php</a> For Installaion Step</font>");


if($_POST['name'])
{

	if(eregi("^[-a-zA-Z0-9�-��-�. %$&^*+=\.?]+$",$_POST['name']))
	{
		$sql = "SELECT * FROM chat_online WHERE name='".$_POST['name']."'";
		$result = mysql_query($sql);

		$num = mysql_num_rows($result);

		mysql_free_result($result);

		if($num>0)
		{
			$statustext = "<strong>Error</strong> : Found This Name In The Room Already.";
		}
		else
		{
			$time = time();
			$sql = "SELECT * FROM chat_online ORDER BY no DESC LIMIT 0,1";
			$result = mysql_query($sql);
			$row = mysql_num_rows($result);

			if($row>0)
			{
			$arr = mysql_fetch_array($result);
			mysql_free_result($result);
			$memid  = $arr['no']+1;
			}
			else
			{
			$memid = 1;
			}

			$sql = "INSERT INTO `chat_online` ( `no` ,`name` , `ip` , `utimestmp` , `avatar` , `mystatus` ) VALUES ('$memid','".$_POST['name']."','".getenv("REMOTE_ADDR")."',$time,'".$_POST['sex']."','Online')";
			mysql_query($sql) or die("<font size=2><strong>Error</strong> : Please Use A-Z Character <a href=\"index.php\">[Back To Login Page]</a></font>");
			$sql = "SELECT * FROM chat_online WHERE ip='".getenv("REMOTE_ADDR")."' and avatar='".$_POST['sex']."'";
			$result = mysql_query($sql);
			$arr = mysql_fetch_array($result);

			setcookie('login',$arr['name'],time()+3600*24*30);
			setcookie('sex',$arr['avatar'],time()+3600*24*30);
			setcookie('ip',getenv("REMOTE_ADDR"),time()+3600*24*30);

			$_SESSION['login'] = $arr['name'];
			$_SESSION['sex'] = $arr['avatar'];
			$_SESSION['memno'] = $arr['no'];

			$filename = "./data/online.dat";
			$handle = @fopen($filename, "w");
			@fwrite($handle,time());
			@fclose($handle);

			$sql = "INSERT INTO chat_text(name,sendto,timestamp,text,bold,underline,color,time) VALUES ('".$arr['name']."',NULL,unix_timestamp(),'takein',0,0,'FFFFFF','".date("H:i")."')";
			mysql_query($sql);


			header("Location: mess.php");

		}
	}
	else
	{
		$statustext = "<strong>Error</strong> : Can Use Only Thai Or English Alphabet  and %$&^*+=-.?";
	}


}

	$sql = "SELECT * FROM chat_online ORDER BY utimestmp DESC LIMIT 0,5";
	$result = mysql_query($sql);
	$num = mysql_num_rows($result);

?>
<script language='JavaScript' src="lib/func.js"></script>
   <script type="text/javascript">


   function ChooseSex(sex)
   {
  	 document.sex.src="./images/" + sex + ".gif";
	 document.login.sex.value = sex;
   }

function setOnload()
{
	if(GetCookie('sex')!=null)
	{
	ChooseSex(GetCookie('sex'));
	}
}
   </script>
   <html>
<head>
<title><?php echo $title; ?></title>
<meta http-equiv="Content-Type" content="text/html; charset=tis-620">
<link href="style.css" rel="stylesheet" type="text/css">
<body bgcolor="#E0E0E0" background="bg.gif" onLoad="setOnload()">
<p>&nbsp;</p>
<p>&nbsp;</p>
<p>&nbsp;</p>
<br>
<br>
<br>
<table width="351" border="1" align="center" cellpadding="0" cellspacing="0" bordercolor="#000000">
  <tr>
    <td width="347" align="center" valign="middle"><table width="100%" border="0" cellpadding="5" cellspacing="1" bgcolor="#BDD9F0">
        <tr>
          <td align="center" background="images/header_login.gif" bgcolor="#153C5B"><font color="#FFFFFF" size="2" face="MS Sans Serif, Tahoma, sans-serif"><strong><?php echo $title; ?></strong></font></td>
        </tr>
        <tr>
          <td><form name="login" method="post" action="login.php">
              <table width="334" border="0" align="center" cellpadding="1" cellspacing="1">
                <tr>
                  <td width="87" height="30" valign="middle" nowrap><div align="right"><font color="#153C5B" size="2" face="MS Sans Serif, Tahoma, sans-serif"><b>
                      NickName :&nbsp;</b></font></div></td>
                  <td width="119" align="center" valign="middle"><div align="left"><font size="2" face="MS Sans Serif, Tahoma, sans-serif">
                      <input type="text" size="19" name="name" maxlength="17" class="chattext" value="<?php echo $_COOKIE['login']; ?>">
                      </font></div></td>
                  <td width="118" valign="middle"> <font size="2" face="MS Sans Serif, Tahoma, sans-serif">
                    <input name="submit" type="submit" value="Join" class="button">
                    <input type="hidden" name="sex" value="f1">
                    &nbsp; <img src="./images/f1.gif" width="11" height="14" name="sex">
                    </font></td>
                </tr>
                <tr>
                  <td colspan="3" valign="top" nowrap> <div align="center"><font size="2" face="MS Sans Serif, Tahoma, sans-serif"><a href="#" onClick="ChooseSex('f1');return false"><img src="images/f1.gif" width="11" height="14" vspace="6" border="0" alt=""></a>
                      <a href="#" onClick="ChooseSex('m1');return false"><img src="images/m1.gif" width="11" height="14" vspace="6" border="0" alt=""></a>
                      <a href="#" onClick="ChooseSex('f2');return false"><img src="images/f2.gif" width="11" height="14" vspace="6" border="0" alt=""></a>
                      <a href="#" onClick="ChooseSex('m2');return false"><img src="images/m2.gif" width="11" height="14" vspace="6" border="0" alt=""></a>
                      <a href="#" onClick="ChooseSex('f3');return false"><img src="images/f3.gif" width="11" height="14" vspace="6" border="0" alt=""></a>
                      <a href="#" onClick="ChooseSex('m3');return false"><img src="images/m3.gif" width="11" height="14" vspace="6" border="0" alt=""></a>
                      <a href="#" onClick="ChooseSex('f4');return false"><img src="images/f4.gif" width="11" height="14" vspace="6" border="0" alt=""></a>
                      <a href="#" onClick="ChooseSex('m4');return false"><img src="images/m4.gif" width="11" height="14" vspace="6" border="0" alt=""></a>
                      <a href="#" onClick="ChooseSex('f5');return false"><img src="images/f5.gif" width="11" height="14" vspace="6" border="0" alt=""></a>
                      <a href="#" onClick="ChooseSex('m5');return false"><img src="images/m5.gif" width="11" height="14" vspace="6" border="0" alt=""></a>
                      <a href="#" onClick="ChooseSex('f6');return false"><img src="images/f6.gif" width="11" height="14" vspace="6" border="0" alt=""></a>
                      <a href="#" onClick="ChooseSex('m6');return false"><img src="images/m6.gif" width="11" height="14" vspace="6" border="0" alt=""></a>
                      <a href="#" onClick="ChooseSex('f7');return false"><img src="images/f7.gif" width="11" height="14" vspace="6" border="0" alt=""></a>
                      <a href="#" onClick="ChooseSex('m7');return false"><img src="images/m7.gif" width="11" height="14" vspace="6" border="0" alt=""></a>
                      <br>
                      <?php echo $statustext;?> </font></div></td>
                </tr>
              </table>
            </form></td>
        </tr>
        <tr>
          <td align="center" background="images/header_login.gif" bgcolor="#153C5B"><font color="#FFFFFF" size="2" face="MS Sans Serif, Tahoma, sans-serif"><strong>Online
                User
            ( Last 5 Online )</strong></font></td>
        </tr>
        <tr>
          <td align="center"> <strong><font size="2" face="MS Sans Serif, Tahoma, sans-serif"><?php $i=0; if($num>0){while($arr = mysql_fetch_array($result)){$i++; echo $arr['name']; if($i<$num){echo ", ";} }}else{echo "Not Found";
		  $sql = "DELETE FROM chat_group";
			mysql_query($sql);
			} ?></font></strong></td>
        </tr>
      </table></td>
  </tr>
</table>
<div align="center"><br>
  </div>
<?php mysql_close($connect);?>