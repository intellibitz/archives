<?php
//$timecheck=date('His');
//echo 'Time stamp: ' . $timecheck;
// if($timecheck==135200)
 //{
 	//echo "connected";
include('../config/config.php');
	//$connect = mysql_connect("ns1","vijayalakshmik","vijayalakshmik") or die (mysql_error());
	 //mysql_select_db ("vijayalakshmik",$connect) or die (mysql_error());
	echo "connected";
	$db ="select email from register where mailquiz= '0' AND username='anuradha'";
	 $result=mysql_query($db) or die("not avail");
	echo $result;
	$nrows = mysql_num_rows($result);
	 if($nrows != 0)
	{
	 	// print "<table border=1>";
		for($j=0;$j<$nrows;$j++)
		{
			$r=mysql_fetch_array($result);
		 	 $uname=$r["email"];
		 	 $mailids = $uname.",";
		 	 print $mailids;
  		 }

	      $to = "$mailids";
			//echo $to;
			print $uname.",";
			$subject = "IntelliGame password";

			$message="Hi! there check this link for new updated quiz in our website.";
			$message.="<br>";
			$message.="To play now click on this link below";
			$message.="<br><br>";
			$message.="Thank You";
			echo $message;
			$from = "admin@intelligame.com";
			//$headers = "From: $from";
         $headers  = 'MIME-Version: 1.0' . "\r\n";
			$headers .= 'Content-type: text/html; charset=iso-8859-1' . "\r\n";
			if(mail($to, $subject, $message, $headers ,$from))
          {
 	   echo("<p>Message successfully sent!</p>");
           }
		   else
		   {
      echo("<p>Message delivery failed...</p>");
 	       }
 	       }
 	       else
 	       {
 	       print "db cannot be fetched";
 	       }

 	      //else
 	      // {
 	     //  print "cannot send mail at this time";
 	     // }
 	       exit;
?>