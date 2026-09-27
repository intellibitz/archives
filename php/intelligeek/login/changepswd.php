<?php
session_start();
if(isset($_SESSION['username']))
{
//echo $_session['username'];
?>
<html>
<head><div align="left"><u><?php print
$_SESSION['username'];?></u> | <a href="javascript:ajax('../login/loggedin.php','login')">Home</a> | <a href="../login/logout.php">Logout</a></div>

</head>
<body>


<form name="chg" id="chg" method="post" action="javascript:getchangepswd(document.getElementById('chg'));" onsubmit="return validatechange();">
<table id="cpswd"  class="cpswd" >
<tr align="left"><th>Change Password</th></tr>
<tr></tr>
<tr><td></td><td><font color="red"><blink><label id="faq" ></label></blink></font></td></tr>
<tr><td>
&nbsp;&nbsp;&nbsp;&nbsp;Old Password<font color="red">*</font></td><td><input type="password" id="oldpwsd" name="oldpwsd" ></td></tr>
<tr><td>
&nbsp;&nbsp;&nbsp;&nbsp;New Password<font color="red">*</font></td><td><input type= "password" id="newpwsd" name="newpwsd" ></td></tr>
<tr align="center"><td align="right"><input type="submit" name="submit" value="SUBMIT"></td><td align="left"><input type="reset" name="reset" value="CLEAR"></td></tr>
</table>
</form>
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
