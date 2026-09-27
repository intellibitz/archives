<html>
<head><title>Post Questions</title></head>
<body>
<form name="postquestion" method="post" action= "post_questiondb.php">
<table id="postquestion" class="postquestion">
<tr><td>Select The Subject </td>
<td>
	<?php
	//$con=mysql_connect("192.168.1.6","geek","geek") or die(mysql_error());
	//mysql_select_db("geek",$con) or die(mysql_error());
         include('../config/config.php');
                $query ='SELECT * FROM subject';
                 $result = mysql_query($query) or die(mysql_error());
                 if ($result)
                 {
                 echo "<select id='sub_id' name='sub_id' >";
                 while($row = mysql_fetch_array( $result ))
			{
                        echo '<option value='.$row[0].'>'.$row[1].'</option>';

                         }
           echo "</select>";
          }


         ?>
	</td></tr>
	<tr><td>Level</td><td><select name ="level">
	<option value="1">simple </option>
	<option value="2">medium </option>
	<option value="3">hard </option>
</select></td></tr>
<tr><td>Question</td><td><textarea name="q" cols="50" rows="2"></textarea></td></tr>
<tr><td>A</td><td><textarea name="a" cols="50" rows="1.5"></textarea></td></tr>
<tr><td>B</td><td><textarea name="b" cols="50" rows="1.5"></textarea></td></tr>
<tr><td>C</td><td><textarea name="c" cols="50" rows="1.5"></textarea></td></tr>
<tr><td>D</td><td><textarea name="d" cols="50" rows="1.5"></textarea></td></tr>
<tr><td>Select Answer</td><td><select name ="ans">
	<option value="1">A </option>
	<option value="2">B </option>
	<option value="3">C </option>
	<option value="4">D </option>
</select></td></tr>
<tr><td height="10px"></td></tr>
<tr><td></td><td align="center"><input type="submit" name="submit" id="submit" value="ADD"/></td></tr>
</table>
</form>
</body>
</html>


