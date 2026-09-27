<?php

    include('../config/config.php');

	//$connect = mysql_connect("ns1","vijayalakshmik","vijayalakshmik") or die (mysql_error());
	//mysql_select_db ("vijayalakshmik",$connect) or die (mysql_error());

	$errorMessage = '';

	if(isset($_POST['emailfp']))
		{


		$email=$_POST['emailfp'];
		$db ="select email,username from user where email= '$email'";
		$result=mysql_query($db) or die("not avail");

   		if (mysql_num_rows($result) == 1)
   		{
			function createPassword($length) {
			$chars = "234567890abcdefghijkmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
			$i = 0;
			$password = "";
			while ($i <= $length) {
			$password .= $chars{mt_rand(0,strlen($chars))};
			$i++;
			}
			return $password;
			}
			$a=mysql_fetch_row($result)or die("retrive error");;
			//echo $a[1];
			$password = createPassword(8);
			$mailmsg= "Your password is: $password";
			$mailmsg1="Your username is: $a[1]";
			//echo $mailmsg;
			$up="update user SET password=PASSWORD('$password') where email='$email'";
			mysql_query($up) or die(mysql_error);
			$to = "$email";
			//echo $to;
			$subject = "IntelliGame password";

			$message="Hi! $a[1] now you can use this password to login to your account,you may change your password later.";
			$message.="<br>";
			$message.= "$mailmsg1";
			$message.="<br>";
			$message.="$mailmsg";
			$message.="<br><br>";
			$message.="Thank You";
			//echo $message;
			$from = "admin@intelligame.com";
			//$headers = "From: $from";
         $headers  = 'MIME-Version: 1.0' . "\r\n";
			$headers .= 'Content-type: text/html; charset=iso-8859-1' . "\r\n";
			$errormsg='';
			if(mail($to, $subject, $message, $headers ,$from))
          {
 	    $errormsg=("<p>Message successfully sent!</p>");
		echo $errormsg;
           }
		   else
		   {
      	$errormsg=("<p>Message delivery failed...</p>");
	  echo $errormsg;
 	       }

      	exit;

		}
		else
		{
      $errormsg = 'Enter The Correct Email-Address!';
		echo $errormsg;
   		}

			}
//session_destroy();
?>