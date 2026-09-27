<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<link rel="alternate" type="application/rss+xml" title="Latest Itelligeek News" href="../rssfeed/index.xml">
<link rel="shortcut icon" href="../images/favicon.ico" />
<head>
 <?require "../include/head_script";?>
</head>

<body>


<div id="header">
<h2 class="header_txt">Learning Made Fun</h2>

</div>


<div id="navcontainer"><?require "../include/top_menu";?></div>

<div id="left">
 <div id="login"></div>
 <label id="msg" class="msg" /><?php
if ($errorMessage != '') {
?>
<p><strong><font color="#990000"><?php echo $errorMessage; ?></font></strong></p>
<?php
}
?></label>
 <!--<?require "../include/star_of_the_day.php";?>-->
 <?require "../newsletter/index.php";?>


</div>



<div id="right">

	<div id="score"></div>

	<?require "../include/chat_box.php";?>
	<?require "../include/who_is_online.php";?>

</div>

<div id="content" class="content">
<h3>Play Station</h3>

<!--<p id="play_station">The default text to display is how to play the game and more..</p>-->
<p id="play_station">
    <?require "../include/how_to_play_more";?>
</p>
</div>



<div id="footer"> <?require "../include/footer";?></div>

<script  type="text/javascript" src="../js/ajax_xmlhttp.js"></script>
<script  type="text/javascript" src="../js/datetimepicker.js"></script>
<script  type="text/javascript" src="../js/registervalid.js"></script>
<script  type = "text/javascript" src="../js/checkavailablity.js"></script>
<script type="text/javascript"  src="../js/intellibitz.js"></script>
<script type="text/javascript"  src="../js/questionpost.js"></script>
<script  type = "text/javascript" src="../js/login.js"></script>
<script  type = "text/javascript" src="../js/loginvalid.js"></script>
<script type="text/javascript" src="../js/cpswd.js"></script>
<script type="text/javascript" src="../js/fpswdvalid.js"></script>

</body>
</html>
 <?php require "../login/uploaddb.php";?>

