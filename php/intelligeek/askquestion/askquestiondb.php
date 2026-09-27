
<?php
include('../config/config.php');

//mysql_query("create table faq(sno int primary key AUTO_INCREMENT,question varchar(350),answer varchar(400),emailid varchar(40),emailidans varchar(40),date varchar(20))")or die(mysql_error());
$question=($_POST['question']);
$emailid=($_POST['email']);
//$question=mysql_real_escape_string($_POST['question']);

//$emailid= mysql_real_escape_string($_POST['email']);
//echo $emailid;
$date=date("d-m-Y");
$answer;
$emailidans;
//echo $date;
$result=mysql_query("insert into faq values ('','$question','$answer','$emailid','$emailidans','$date')")or die(mysql_error());

if($result)
{
echo ('Thanks For Post Your Question!');
}


?>
