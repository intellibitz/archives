<HTML>
<HEAD><TITLE>Hangman</TITLE></HEAD>
<BODY><DIV ALIGN = 'center'>
<?php
//hangman.php
$alphabet = array("A","B","C","D","E","F","G","H","I","J","K","L","M",
"N","O","P","Q","R","S","T","U","V","W","X","Y","Z");
$words = array("AARDVARK", "INDIGESTION", "CALCULATOR",
"PERISTALSIS", "VERMILLION", "MNEMONIC");
$wrong = 0;
if (!isset($word_number)) { $word_number = rand(1,count($words)) - 1; }
echo"<H1>";
$word = $words[$word_number];
$finished = 1;
for ($i=0; $i < strlen($word); $i++) {
if (ereg($word[$i], $letters)) {
echo $word[$i];
}
else {
echo "_";
$finished = 0;
}
}
echo"</H1>";
if ($finished) {
echo "<BR><BR>Congratulations! You win!<BR><BR>";
echo "<A HREF=$PHP_SELF>Play again</A>";
}
else {
foreach ($alphabet as $var) {
if (ereg($var, $letters)) {
if (ereg($var, $words[$word_number])) {
$links .= "<B>$var</B> ";
} else {
$links .= "$var ";
$wrong++;
}
}
else {
$links .= "<A HREF=\"$PHP_SELF?letters=$letters$var
&word_number=$word_number\">
$var
</A> ";
}
}
echo "<BR><IMG SRC=\"./hangman$wrong.gif\"><BR>";
if ($wrong == 12) {
echo "<BR>HANGMAN!!<BR><BR>";
echo "The word you were looking for was \"$word\"<BR><BR>";
echo "<A HREF=$PHP_SELF>Play again</A>";
} else {
echo "Tries remaining = ".(12-$wrong)."<BR>";
echo "<BR>Please pick a letter.<BR><BR>";
echo $links;
}
}
?>
</DIV></BODY>
</HTML>
