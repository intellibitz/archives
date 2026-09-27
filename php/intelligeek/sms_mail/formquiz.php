 <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Test</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<meta http-equiv=refresh>
<SCRIPT LANGUAGE="JavaScript">
var born = new Date();

var hr=born.getHours();
//document.write(hr);
var time1=born.getMinutes();
document.write("hai");
if(hr==9)
{
document.write("Sending mail");
<!--
setTimeout('document.test.submit()',3000);
//-->
}
else
{
document.write("Sorry mail timed out");
}
</SCRIPT>
 </head>

<body>
<form name="test" id="form1" method="post" action="sendquiz.php">
  <p>
    <input name="pattern" type="text" id="pattern" />
    <input name="show" type="hidden" id="show" value="quickref" />
  </p>
  <p><input type="submit" name="next" value="Next" />
&nbsp;  </p>
</form>
</body>
</html>