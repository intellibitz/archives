

<html>
<head><div align="right"> <a href=""></a> </div>
<script type="text/javascript" src="../js/fpswd.js"/>
<!--<link rel="stylesheet" type="text/css" href="../css/login.css" />-->
</head>
<body>
<form id="fpswd" name="fpswd" method="post" action="javascript:getforgotpswd(document.getElementById('fpswd'));" onsubmit="return validatefpswd();">
<table id="fpswd"  class="fpswd" >
<tr align="left"><th>Password-Recovery</th></tr>
<tr><td><font color="red"><blink><label id="faq"></label></blink></font></td></tr>
<?php
if ($errorMessage != '') {
?>
<tr><td><p align="center"><font color="red"><blink><?php echo $errorMessage; ?></blink></font></p></td></tr>
<?php
}
?>
<tr><td>
&nbsp;&nbsp;E-Mail:&nbsp;<input type="text" id="emailfp" name="emailfp" ></td></tr>
<tr align="center"><td align="center"><input type="submit" name="submit" value="SUBMIT">&nbsp;<input type="reset" name="reset" value="CLEAR"></td></tr>
</table>
</form>
</body>
</html>
