<?php

if ($_POST['username'])
  $username = $_POST['username'];

$logonform = <<< _end_lform
<style type="text/css">
<!--
.formheading
{
	color:black;
	font-size:24px;
	font-family:Arial, sans-serif;
	font-weight:bolder;
}
.scriptlinks a
{
	color:blue;
  text-decoration:none;
}

-->
</style> 
<div style="padding:5; background-color: #EEEEEE; width:350;">
<form name="form1" method="post" action="">
  <table width="340" border="0" cellspacing="0" cellpadding="0" style="border:solid thin black; background-color: #FFFFFF">
    <tr> 
      <td colspan="2" style="padding:6"><div align="right" class="scriptlinks"><font color="#666666"><strong>Select:</strong></font>$selects</div></td>
    </tr>
    <tr> 
      <td colspan="2" style="padding:6"><div align="center" class="formheading"><strong>Import 
            $formname Contacts<br>
           </strong></div></td>
    </tr>
    <tr> 
      <td width="50%" style="padding:6"><div align="left"><strong>$formname Username:</strong></div></td>
      <td width="50%" style="padding:6"><input name="username" value="$username" type="text" class="text" id="username" style="width:100%"></td>
    </tr>
    <tr> 
      <td width="50%" style="padding:6"><div align="left"><strong>$formname Password:</strong></div></td>
      <td width="50%" style="padding:6"><input name="password" type="password" class="text" id="password" style="width:100%"></td>
    </tr>
    <tr> 
      <td colspan="2" style="padding:6">$formdisclaimer</td>
    </tr>
    <tr> 
      <td colspan="2" style="padding:6"><input type="submit" name="Submit" value="Import Contacts" class="button" style="width:100%"></td>
    </tr>
  </table>
</form>
  <div align="center" style="color:#444444; font-size:11.5px; font-family:Arial, sans-serif; align:center;">&copy; 
    2006 Address Book Import Scripts Developed by <a href="http://svetlozar.net" target="_blank" style="color:#333333;">Svetlozar 
    Petrov</a></div> 
</div>
_end_lform;
echo $logonform;

?>
