<?php
session_start();
if(isset($_SESSION['username']))
{
//echo $_session['username'];
?>

<html>
<head><div align="left"><u><?php print
 $_SESSION['username'];?></u> | <a href="javascript:ajax('../login/loggedin.php','login')">Home</a> | <a href="javascript:ajax('../login/loggedin.php','login')">Back</a>  | <a href="../login/logout.php">Logout</a> </div>

<body>
<form method="post" enctype="multipart/form-data" action="">
<table id="upload" class="upload">
<tr><td ><input type="hidden" name="MAX_FILE_SIZE" value="6000000"></td></tr>
<tr><td>&nbsp;&nbsp;Upload Photo</td></tr>
<tr><td >&nbsp;&nbsp;<input name="userfile" type="file" id="userfile"></td></tr>
<tr><td >&nbsp;&nbsp;( You can upload a JPG, GIF or PNG file,Maximum size - 2MB)</td></tr>
<tr><td align="center"><input name="upload" type="submit" class="box" id="upload" value=" Upload "></td></tr>
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