<?php
$faqid=$_GET['questionno'];

include('../config/config.php');
$result= mysql_query("select question from faq where faqid='$faqid'") or die (mysql_error());
$rows=mysql_fetch_row($result);

$question=$rows[0];

//echo "hai";
//$question='How to play this game';

?>
<html>
<head>

</head>
<body>
<form action="javascript:ansget(document.getElementById('ansfaqform'));" id="ansfaqform" name="ansfaqform" method="post">
<table id="faq" class="faq">
<tr>
<td>Question</td>
<td>
<textarea name="question" id="question" cols="45" rows="6" value="<?php echo $question; ?>" /><?php echo $question; ?></textarea>
</td>
</tr>

<tr>
<td>Answer
</td>
<td><textarea name="answer" id="answer" cols="45" rows="6" /></textarea></td></tr>
<tr>
<td>Email</td><td><input type="text"  class="field" name="email" id="email" size="40"></td>
</tr>
<tr>
<td></td><td align="center"><input type="submit" name="submitbut" id="submitbut" value="ADD"/><input type="submit" name="submitbut" id="submitbut" value="RESET"/></td></tr>
</table>
</form>
</body>

</html>