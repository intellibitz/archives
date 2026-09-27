<?php
session_start();
if(isset($_SESSION['username']))
{
	 //header('Location: ../login/loggedin.php');
	require("../login/loggedin.php");
}
else
	{
		?>
<html>
<head>
	<h3>LOGIN</h3>

	<div align="left"> <a href=""></a> </div>

<!--<link rel="stylesheet" type="text/css" href="../css/login.css" />-->
</head>

<body>
<div id="faq" class="faq">
<form name="rec" id= "rec" method="post" action="javascript:getlog(document.getElementById('regform'));" onsubmit="return validatelogin()">

<table  border="0" align="center" class="box">
	<tr>
<td></td></tr>
<tr><td><font color="red"><blink><label id="loginmsg"></label></blink></font></td></tr>
<tr>
<td>
USERNAME:
<input type="text" id="username" name="username" ></td>&nbsp;
</tr>
<tr>
<td>
PASSWORD:
<input type="password" id="password" name="password" ></td>
</tr></table>
<input type="submit" name="submit" value="SUBMIT">
&nbsp;&nbsp;&nbsp;<input type="reset" value="CLEAR"><br><a href="javascript:ajax('../login/forgotpswd.php','login')">ForgotPassword!</a>&nbsp;New User&nbsp;<a href="javascript:ajax('../register/index.php','content')">Sign Up</a>

</form>
</div>
</body>

</html>
<?php
}
?>