<?php
session_start();

include 'lib/config.inc.php';

$connect = mysql_pconnect($host,$user,$pass) or die("Can't connect to MySQL");
mysql_select_db($database,$connect);

$sql = "SELECT * FROM chat_online WHERE name='".$_SESSION['login']."' and ip='".getenv("REMOTE_ADDR")."'";
$result = mysql_query($sql);

$num = mysql_num_rows($result);
if($num>0)
{
?>
<html>
<head>
<title><?php echo $title; ?></title>
<meta http-equiv="Content-Type" content="text/html; charset=windows-874">
<LINK href="style.css" type=text/css rel=stylesheet>
<script language='JavaScript' src="lib/mess.js"></script>
<script src="lib/rainbow.js"></script>
</head>
<body bgcolor="#DBEAF7" background="bg.gif" onLoad="Initial()">
<form name="message" method="post" action="Data.php" onSubmit="SendData(); return false">
<br>
<table width="755" border="0" align="center" cellpadding="0" cellspacing="0" class="outofarea">
  <tr>
    <td><table width="755" height="648" border="0" align="center" cellpadding="0" cellspacing="0" bordercolor="#666666" class="border">
        <tr bgcolor="#E8E8E8">
          <td height="35" colspan="2"><img src="images/header1.jpg" width="755" height="120"></td>
        </tr>
        <tr>
          <td width="586" height="400" rowspan="3" align="center" valign="top" bgcolor="#ADD0ED" class="chatloadarea">
            <table width="586" border="0" cellspacing="0">
              <tr>
                <td width="586" height="24" bgcolor="#64A7DD" align="center"><div id="messhint"><strong><font size="1" face="MS Sans Serif, Tahoma, sans-serif" color="#FFFFFF">This
                        Chat Is Property Of Intellibitz Please Do Not Remove
                        Our Brand </font></strong><font face="MS Sans Serif, Tahoma, sans-serif"><a href="#" onClick="History()">[History]</a></font></div></td>
              </tr>
            </table>
            <div id="mess" class="outerchatarea" align="justify"> </div>
            <table width="586" height="25" border="0" cellpadding="0" cellspacing="0" class="status">
              <tr>
                <td align="center" valign="middle"><font size="2" color="#2E88D1">
                  <div id="status">
                    <div align="center">
                      <p><strong>Status :</strong> MessChat Loading ..</p>
                    </div>
                  </div>
                  </font></td>
              </tr>
            </table></td>
          <td width="169" height="225" valign="top" bgcolor="#92C2E7" class="rightout"><div class="onlineareas"><div id="useronline"></div></div></td>
        </tr>
        <tr>
          <td valign="middle" bgcolor="#CBE2F3" class="menuareas"><br>
            <table width="157" border="0" align="center" cellpadding="0" cellspacing="0">
					   <tr>
                  <td width="21" height="20"><div align="left"><font color="#154062" size="2" face="MS Sans Serif, Tahoma, sans-serif"><img src="images/buttons/status.gif" width="15" height="15"></font></div></td>
                  <td width="129" height="20">
  <select name="status" onChange="SetStatus(<?php echo $_SESSION['memno'];?>)">
				  <option value="Online">Online</option>
				  <option value="Busy">Busy</option>
				  <option value="Away">Away</option>
				  <option value="Personal">Personal Status</option>
				  </select>
				  <input id="statushide" name="statushide" type="hidden" value="Online">
				  </td>
                </tr>
			    <tr>
                  <td width="21" height="20"><div align="left"><font color="#154062" size="2" face="MS Sans Serif, Tahoma, sans-serif"><img src="images/buttons/groupchat.gif" width="15" height="15"></font></div></td>
                  <td width="129" height="20"><font color="#154062" size="2" face="MS Sans Serif, Tahoma, sans-serif"><a target="_parent" onClick="GroupMode()" onMouseOver="this.style.cursor='hand';this.style.color='#000000'" onMouseOut="this.style.color='#154062'" title="Click To Create Or Join In GroupMode"><strong>GroupChat</strong></a></font></td>
                </tr>
                <tr>
                  <td height="20"><img src="images/buttons/sound.gif" width="15" height="15">&nbsp;</td>
                  <td height="20">
							  <table cellspacing="0" cellpadding="0" width="135">
							  <tr>
							  <td width="63">
							  <font color="#154062" size="2" face="MS Sans Serif, Tahoma, sans-serif"><a target="_parent" onClick="SoundState()" onMouseOver="this.style.cursor='hand';this.style.color='#000000'" onMouseOut="this.style.color='#154062'" title="Click To Activate Or Deactivate Sound System"><strong>
                    <div id="soundch">Sound Off</div>
                    </strong></a></font>
							  </td>
							  <td width="9"><center>/&nbsp;</center></td>
							  <td width="61">
							  <font color="#154062" size="2" face="MS Sans Serif, Tahoma, sans-serif"><a target="_parent" onClick="NudgeState()" onMouseOver="this.style.cursor='hand';this.style.color='#000000'" onMouseOut="this.style.color='#154062'" title="Click To Activate Or Deactivate Nudge System"><strong>
                    <div id="nudgech">Nudge On</div>
                    </strong></a></font>
							  </td>
							  </tr>
				    </table>
				  </td>
                </tr>
			    <tr>
                  <td width="21" height="20"><div align="left"><font color="#154062" size="2" face="MS Sans Serif, Tahoma, sans-serif"><img src="images/buttons/winks.gif" width="15" height="15"></font></div></td>
                  <td width="129" height="20"><font color="#154062" size="2" face="MS Sans Serif, Tahoma, sans-serif"><a target="_parent" onClick="WinkState()" onMouseOver="this.style.cursor='hand';this.style.color='#000000'" onMouseOut="this.style.color='#154062'" title="Click To Activate Or Deactivate Winks System"><strong>
				   <div id="winkch">Winks Off</div>
                    </strong></a></font></td>
                </tr>
                <tr>
                  <td width="21" height="20"><div align="left"><font color="#154062" size="2" face="MS Sans Serif, Tahoma, sans-serif"><img src="images/buttons/logout.gif" width="15" height="15"></font></div></td>
                  <td width="129" height="20"><font color="#154062" size="2" face="MS Sans Serif, Tahoma, sans-serif"><strong><a target="_parent" onClick="LogOut()" onMouseOver="this.style.cursor='hand';this.style.color='#000000'" onMouseOut="this.style.color='#154062'"  title="Logging Of This Chat">LogOut</a></strong></font></td>
                </tr>
                <tr>
                  <td height="20"><div align="left"><img src="images/buttons/about.gif" width="15" height="15"></div></td>
                  <td height="20"><font color="#154062" size="2" face="MS Sans Serif, Tahoma, sans-serif"><a target="_parent" onClick="About()" onMouseOver="this.style.cursor='help';this.style.color='#000000'" onMouseOut="this.style.color='#154062'"  title="About MessChat"><strong>About
                          Us</strong></a></font></td>
                </tr>
              </table>
              <div align="center"><br><object  classid="CLSID:22D6F312-B0F6-11D0-94AB-0080C74C7E95"
codebase="http://activex.microsoft.com/activex/controls/mplayer/en/nsmp2inf.cab#Version=6,0,02,0902"
       	 	type="application/x-oleobject" width="148" height="50" hspace="0" vspace="0"
        	standby="Loading Microsoft? Windows Media? Player components..." id="MediaPlayer">
        <param name="FileName" value="http://203.146.140.216:8010" />
        <param name="AutoStart" value="true" />
        <param name="ShowPositionControls" value="false" />
        <param name="ShowDisplay" value="false" />
        <param name="ShowTracker" value="false" />
        <param name="ShowCaptioning" value="false" />
        <param name="ShowGotoBar" value="false" />
        <param name="ShowStatusBar" value="true" />
        <param name="EnableContextMenu" value="false" />
        <param name="Volume" value="-200" />
        <param name="displaySize" value="0" />
        <param name="autoSize" value="false" />
        <embed
				//src="http://www.thaimess.com:8010"
				width="148"
				height="50" hspace="0" vspace="0"
				autostart="True" type="application/x-mplayer2"
				pluginspage =" http://www.microsoft.com/Windows/MediaPlayer/"
				showpositioncontrols="false"
				showdisplay="false"
				showtracker="false"
				showcaptioning="false"
				showgotobar="false"
				showstatusbar="true"
				enablecontextmenu="false"
				volume="-200"
				displaysize="0"
				autosize="false"> </embed>
    </object></div></td></tr>
        <tr>
          <td valign="middle" bgcolor="#AFD2ED" class="menuareas">
            <font size="1" face="MS Sans Serif, Tahoma, sans-serif"><br>
            </font>
            <table width="147" border="0" align="center" cellpadding="0" cellspacing="0">
                    <tr>
                      <td width="127"><font color="#154062" size="1" face="MS Sans Serif, Tahoma, sans-serif">
                      <div id="sendtoshow"><font size="1" face="MS Sans Serif, Tahoma, sans-serif"><strong>Message
                            To</strong><font color="#154062"><br>
              All</font></font></div>
                          <strong>Now Playing</strong><font size="1" face="MS Sans Serif, Tahoma, sans-serif">
                          <div id="songshow"></div></font></font></td>
                    </tr>
              </table>
            <font size="1" face="MS Sans Serif, Tahoma, sans-serif">&nbsp;            </font>            </td>
        </tr>
        <tr valign="middle" bgcolor="4f99d8">
          <td height="64" colspan="2" align="center">
              <table width="748" border="0" cellspacing="0" cellpadding="0">
                <tr>
                  <td width="742" height="38" align="center" valign="middle"><font size="2" face="MS Sans Serif, Tahoma, sans-serif"><br><strong><font color="#FFFFFF"><?php echo $_COOKIE['login'];?>
                    :</font></strong>
                    <input name="nname" type="hidden" value="<?php echo $_SESSION['login'];?>">
                    <input name="memno" type="hidden" id="memno" value="<?php echo $_SESSION['memno'];?>">
                    <input name="data" type="text" class="chattext" size="65" maxlength="98">
                    <input name="color" type="hidden" size="100">
                    <input type="submit" name="Submit" value="Send" class="button">
                    &nbsp;
                    <input type="hidden" name="colorval">
                    <input name="sendto" type="hidden" id="sendto" value="">
					<input name="group" type="hidden" id="group" value="">
                    <input name="sendtoip" type="hidden" id="sendtoip" value="no">
                    <input type="hidden" name="underlineval" value="0">
                    <input type="hidden" name="boldval" value="0">
					<input type="hidden" name="mystatus" value="Online">

                    <input name="sex" type="hidden" value="<?php echo $_SESSION['sex'];?>"><input type="button" name="emoticons" value="Emo" style="height: 25; width: 40;background-color:FFFFFF;font-weight : bold;border:1px solid #000000" onClick="show_emo()">
					<input type="button" name="nudge" value="Nudge!" style="height: 25; width: 60;background-color:FFFFFF;font-weight : bold;border:1px solid #000000" onClick="Nudge()">
					<input type="button" name="wink" value="Winks" style="height: 25; width: 50;background-color:FFFFFF;font-weight : bold;border:1px solid #000000" onClick="wink_select()">
					<br>
                    <input type="button" name="b" value="B" style="height: 25; width: 20;background-color:#FFFFFF;font-weight : bold;border:1px solid #000000" onClick="SetBold()">
                    <input type="button" name="u" value="U" style="height: 25; width: 20;background-color:#FFFFFF;font-weight : bold;text-decoration:underline;border:1px solid #000000" onClick="SetUnderline()">
                    <input type="button" name="colors" value="C" style="height: 25; width: 20;background-color:FFFFFF;font-weight : bold;border:1px solid #000000" onClick="show_color()">
                    </font></td>
                </tr>
              </table>
              <div id="plsnd"></div>
              <br>
              <font color="#FFFFFF" size="1" face="MS Sans Serif, Tahoma, sans-serif"><strong>Copyright
              (c) 2006 intellibitz.com All Right Reserved.</strong></font> <br>              <br>
          </td>
        </tr>
      </table></td>
  </tr>
</table>
<div id="select_color" style="position:absolute; left: 757px; top: 530px; visibility: hidden;" class="box"></div>
<div align="center"></div>
<div id="select_emo" style="position:absolute; left: 692px; top: 615px; visibility: hidden;" class="box"></div>
<div id="select_wink" style="position:absolute; left: 692px; top: 500px; visibility: hidden;" class="box"></div>
<div align="center"></div>
<div id="wink_show" style="position:absolute; left: 165px; top: 180px; visibility: hidden;" class="box"></div>
<div id="groupchat" style="position:absolute; left: 470px; top: 450px; visibility: hidden;"></div>
</form>
</body>
</html>
<?php
}
else
{
?>
<SCRIPT LANGUAGE="JavaScript">
window.location="index.php";
</script>
<?php
}
?>
