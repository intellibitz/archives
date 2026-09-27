<?php
include('../config/config.php');

	$sub_id = mysql_real_escape_string($_POST['sub_id']);
	$level = mysql_real_escape_string($_POST['level']);
    $q = mysql_real_escape_string($_POST['q']);
	$a = mysql_real_escape_string($_POST['a']);
	$b = mysql_real_escape_string($_POST['b']);
	$c = mysql_real_escape_string($_POST['c']);
	$d = mysql_real_escape_string($_POST['d']);
	$ans = mysql_real_escape_string($_POST['ans']);



	mysql_query("insert into question(sub_id,level,q,a,b,c,d,ans)value('$sub_id','$level','$q','$a','$b','$c','$d','$ans')") or die(mysql_error());


?>