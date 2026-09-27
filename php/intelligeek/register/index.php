
<html>
<head>
	<h3>REGISTRATION</h3>

<!--<script language="javascript" type="text/javascript" src="../js/registervalid.js"></script>-->
</head>
<body>

<form name="regform" id="regform" method= "post" action="javascript:getreg(document.getElementById('regform'));" onsubmit= "return validate()">


<table class="register2" id="faq">
<tr><td height="10px"></td></tr>
<tr><td>&nbsp;&nbsp;User Name</td><td><input type="text" class="field"  id="username1" name="username1" size="25px" onblur="updateTextbox();" ></td><td><font color="red"><label id='errormsg'></label></font></td></tr>
<tr><td height="3px"></td></tr>
<tr><td>&nbsp;&nbsp;Password</td><td><input type="password" class="field" id="password1" name="password1" size="25px"  ></td></tr>
<tr><td height="3px"></td></tr>
<tr><td>&nbsp;&nbsp;Email</td><td><input type="text" class="field" id="email" name="email" size="25px" ></td></tr>
<tr><td></td><td><input type="checkbox" class="field" name="quizmail" id="quizmail" />  Receive Quiz through Mail</td></tr>
<tr><td height="3px"></td></tr>
<tr><td>&nbsp;&nbsp;Mobile </td><td><input type="text" class="field" id="mobile" name="mobile" size="25px"></td></tr>
<tr><td></td><td><input type="checkbox" class="field" name="quizsms" id="quizsms"/>  Receive Quiz through SMS</td></tr>
<tr><td height="3px"></td></tr>
<tr><td>&nbsp;&nbsp;Gender</td><td>
<select id="gender" name="gender">
<option value="select">select</option>
<option value="Male">Male</option>
<option value="Female">Female</option>
</select>
</td></tr>
<tr><td height="3px"></td></tr>
<tr><td>&nbsp;&nbsp;Date of Birth</td>
<td><input id="demo1" type="text" class="field"  name="dob" size="25px"><a href="javascript:NewCal('demo1','dd-mm-yyyy')"><img src="../images/cal.gif" width="14" height="16" border="0" alt="Pick a date"></a></td>
</tr>
<tr><td height="10px"></td></tr>
<tr>
<td align="right"><input type="submit"  name="register" value="SUBMIT" ></td><td align="left"><input type="reset"  name="clear" value="CLEAR"><?php $errormsg='';?></td></div>
</tr>
</table>

</form>
</body>
</html>