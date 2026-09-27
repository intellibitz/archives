<?php
session_start();
$user= $_SESSION['username'];
//print $user;

include('../config/config.php');
 //$connect=mysql_connect("ns1","vijayalakshmik","vijayalakshmik") or die(mysql_error());
//mysql_select_db ("vijayalakshmik",$connect) or die (mysql_error());

//mysql_query ("CREATE TABLE upload(id INT NOT NULL AUTO_INCREMENT,name VARCHAR(30) NOT NULL,type VARCHAR(30) NOT NULL,size INT NOT NULL,content MEDIUMBLOB NOT NULL,PRIMARY KEY(id))")or die (mysql_error());

  if(isset ($_FILES['userfile']['size']) > 0)
 {
 $fileName = $_FILES['userfile']['name'];

$tmpName  = $_FILES['userfile']['tmp_name'];
$fileSize = $_FILES['userfile']['size'];
 $fileType = $_FILES['userfile']['type'];

$fp= fopen($tmpName, 'r');
   $content = fread($fp, filesize($tmpName));
$content = addslashes($content);
fclose($fp);

 if(!get_magic_quotes_gpc())
{
     $fileName = addslashes($fileName);
}
$select=mysql_query("select * from upload where username='$user'") or die("not done");

//print $sql;
  // $result = @mysql_query($sql)
    //         or die('Query failed. ' . mysql_error());
//print $result;
$errorMessage=NULL;
   		if (mysql_num_rows($select)!=0)
   		{
   		 $query1 = mysql_query("update upload SET name= '$fileName' , size='$fileSize' , type='$fileType', content='$content' where username='$user'");

			if($query1)
			{
			$errorMessage = "File $fileName updated";
			//echo $errorMessage;
			}
			else
			{
			$errorMessage = "Image Cannot be Updated";

			}

   		}
   		else
   		{
//include 'library/config.php';
//include 'library/opendb.php';
//$username= $_session['username'];
  $query = "INSERT INTO upload (name, size, type, content, username ) ".
"VALUES ('$fileName', '$fileSize', '$fileType', '$content', '$user')";

mysql_query($query) ;

//include 'library/closedb.php';
           if($query1)
			{
			$msg = "File $fileName uploaded";
			echo $msg;
			}

			else
			{
			$msg = "Image Cannot be Updated";

			}

}
}
?>
