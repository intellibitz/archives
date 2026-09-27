<html>
	<head>
		<h3>FAQ</h3>
		<!--<script language="javascript" type="text/javascript" src="../js/questionpost.js"></script>-->
	</head>

<body>

<?php
include('../config/config.php');
$result=mysql_query("select faqid,question,answer,date from faq ") or die(mysql_error());
if (mysql_num_rows($result) > 0) {
// yes
// print them one after another


while($row = mysql_fetch_row($result))
{

	echo "<br>";
echo "<div class='faqdiv' id='faqdiv'>";
echo "<br>";
echo "$row[3]";
echo "<br>";

echo '<a id="topic_list" href="javascript:ajax(\'../askquestion/answerquestion?questionno='.$row[0].'\',\'content\')" >'. $row[1].'</a>';

echo "<br>";
echo "<span id'ans' class='ans'>(click the question to Answer)</span>";
echo "<br>";
echo "$row[2]";
echo "<br>";
echo "</div>";

}

}
else {
// no
// print status message
echo "No rows found!";
}

// free result set memory
mysql_free_result($result);

 mysql_close($connect);
?>

<form action="javascript:get(document.getElementById('faqform'));" name="faqform" id="faqform"  method="post" onsubmit="postTextbox();">
	<br>
<table id="faq" class="faq">
<tr>
<td>Put Your Question</td>
<td><textarea name="question" id="question" cols="45" rows="6" /></textarea> </td>
</tr>
<tr>
	<td height=10px></td>
</tr>
<tr>
<td>Email Id</td>
<td><input type="text"  class="field" name="email" id="email" size="40"></td>
</tr>
<tr>
	<td height=10px></td>
</tr>
<tr>
<td></td><td align="center"><input type="submit" name="submitbut" id="submitbut" value="ADD"/><input type="submit" name="submitbut" id="submitbut" value="RESET"/></td></tr>
</table>

</form>
</body>
</html>