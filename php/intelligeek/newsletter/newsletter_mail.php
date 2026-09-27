<?php
include ("../config/config.php");


mysql_query("create table IF NOT EXISTS emailid(eid int(5) primary key AUTO_INCREMENT,emailid varchar(50)");




#day of the week 1-7
			$w =date("w");
			if($w==3)
			{
$result=mysql_query("select emailid from email") or die(mysql_error());
				$nrows = mysql_num_rows($result);
	if($nrows != 0)
	{

		while($r= mysql_fetch_row($result))
                {
		         $to=($r[0].",");
		        //$to=$r[0];

                //$to=join(",",$r[0]);
                //echo ($to);

                  }
    }
	//$email = $_GET['email'];
     // $email = 'sherin_mvs@yahoo.co.in';
	if($to)
	{


			// Sending Mail to user
		   //$to = 'project@intellibitz.com';
		   $subject = "Weekly Newsletter From Intelligeek - from $ucname";

$file = "news_content.php";
	$open = fopen($file, "r");
	$size = filesize($file);
	$names = fread($open, $size);
	 echo $names;

 $message=$names;



			// To send HTML mail, the Content-type header must be set
			$headers  = 'MIME-Version: 1.0' . "\r\n";
			$headers .= 'Content-type: text/html; charset=iso-8859-1' . "\r\n";
        	#day of the week 1-7
			$w =date("w");
		// Mail it


			if(mail($to, $subject, $message, $headers))
          {
 	   echo("<p>Message successfully sent!</p>");
           }
		   else
		   {
      echo("<p>Message delivery failed...</p>");
 	       }

	}
}

?>
