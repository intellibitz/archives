<?php
if(isset($_GET['id']))
{
	include('../config/config.php');
	$id      = $_GET['id'];
	$query   = "SELECT name, type, size, content FROM upload WHERE id = '$id'";
	$result  = mysql_query($query) or die('Error, query failed');
	list($name, $type, $size, $content) = mysql_fetch_array($result);

	header("Content-Disposition: attachment; filename=$name");
	header("Content-length: $size");
	header("Content-type: $type");
	echo $content;


	 exit;
}

?>
<html>
<head>


</head>

<body>
<?php
include('../config/config.php');
$query  = "SELECT id, name FROM upload";
$result = mysql_query($query) or die('Error, query failed');
if(mysql_num_rows($result) == 0)
{
	echo "Database is empty <br>";
}
else
{
	while(list($id, $name) = mysql_fetch_array($result))
	{
?>
	Here is your picture:<br>
<img src="../login/checkimage.php?id=<?=$id;?>"><br>
<?php
	}
}

?>
</body>
</html>