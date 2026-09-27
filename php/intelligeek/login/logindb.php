<?php

session_start();


//if(isset($_SESSION['username']))
//{
	//require("loggedin.php");
//}
//else
//	{

    include('../config/config.php');

	//$connect = mysql_connect("ns1","vijayalakshmik","vijayalakshmik") or die (mysql_error());
	//mysql_select_db ("vijayalakshmik",$connect) or die (mysql_error());
	//echo 'connected';
	//$errorMessage = '';

	if (isset($_POST['username']) && isset($_POST['password']))
		{


		$username = mysql_real_escape_string($_POST['username']);
		$pswd = mysql_real_escape_string($_POST['password']);
		//echo $username;
		/*$result = mysql_query("SELECT * FROM register where username= '$username' ")
		or die(mysql_error());*/

     $sql = "SELECT * FROM user WHERE username = '$username'
                 AND password = PASSWORD('$pswd')";
//print $sql;
   $result = @mysql_query($sql)
             or die('Query failed. ' . mysql_error());
//print $result;
   		if (mysql_num_rows($result) == 1)
   		{

       $_SESSION['db_is_logged_in'] = true;
		$_SESSION['auth'] = 1;
      $_SESSION['username'] = $username;
      //echo $_SESSION['username'];
	 //mysql_query("create table onlineuser(sno int primary key auto_increment, username varchar(30), datetime varchar(50))")or die (mysql_error());
//echo "table created";
	//$sqlerror=echo '<a  href="javascript:ajax(\'../login/forgotpswd.php','login')" > click here</a>';
	$sqlerror1=  "you are already loggedin";
 $result1= mysql_query("insert into onlineuser values('','$username',current_timestamp())") or die ($sqlerror1);

      header('Location: ../login/loggedin.php');

      exit;
		}
		else
		{
      $errorMessage = 'Improper Login Please Login Again!';
	  echo $errorMessage;
//echo 'Improper Login Please Login Again!';
   		}

			}
//session_destroy();
?>