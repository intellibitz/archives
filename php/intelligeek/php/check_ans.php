<?php
$no_of_questions = $_GET['no_of_questions'];

for($i=1;$i<=$no_of_questions;){
	$ans[$i]= $_GET["ans.$i"];

}
   print_r ($ans);
?>
