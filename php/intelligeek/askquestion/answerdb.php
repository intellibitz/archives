<?Php
include('../config/config.php');

$question=mysql_real_escape_string($_POST['question']);
//echo $question;
$answer=mysql_real_escape_string($_POST['answer']);
//echo $answer;
$emailidans=mysql_real_escape_string($_POST['email']);
//echo $emailidans;
$result=mysql_query("update faq set answer='$answer',emailidans='$emailidans' where question='$question'")or die(mysql_error());
if($result)
{
echo "Thanks for Answering the Question!";
}
?>