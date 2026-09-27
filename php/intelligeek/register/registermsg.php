<?php

    include('../config/config.php');
	//$connect = mysql_connect("ns1","vijayalakshmik","vijayalakshmik") or die (mysql_error());
//mysql_select_db ("vijayalakshmik",$connect) or die (mysql_error());
	//echo("connected to Database");
   //mysql_query("create table register(user_id int Primary key AUTO_INCREMENT,username varchar(25) unique ,password varchar(20),email varchar(30),mobile varchar(15), gender varchar(10), dob varchar(10))") or  die (mysql_error());
   $message;
   $username = mysql_real_escape_string($_POST['username1']);
  	$password =  mysql_real_escape_string($_POST['password1']);
   $email=   mysql_real_escape_string($_POST['email']);
   $mailquiz= mysql_real_escape_string($_POST['quizmail']);
   $mobile =  mysql_real_escape_string($_POST['mobile']);
   $smsquiz= mysql_real_escape_string($_POST['quizsms']);
   $gender = mysql_real_escape_string($_POST['gender']);
	$dob =  mysql_real_escape_string($_POST['dob']);
	//echo $username;
	$result=  mysql_query("insert into user values('','$username',PASSWORD('$password'),'$email','$mailquiz','$mobile','$smsquiz','$gender','$dob')") or die ("email id already exists");

    if (!$result)
	 {
    $message = ' SORRY ! REGISTRATION FAILED';
echo "<div id='message' class='message'>";
	echo "<p align='center'>$message</p>";
	echo "</p>";
	echo "</div>";
}

    else
	   {
	$message="Thank you for registering with us.";
	echo "<div id='message' class='message'>";
	echo "<p align='center'>$message</p>";

	echo "</div>";
		   }

?>
<html>
	<head>
		<!--<h3>REGISTERATION</h3>-->
<!--<link rel="stylesheet" type="text/css" href="../css/login.css" />-->
	</head>
	<!--<body onload="javascript:ajax("'../register/registermsg.php','content')"">-->


	</body>
</html>