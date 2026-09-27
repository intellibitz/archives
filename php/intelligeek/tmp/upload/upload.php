<?php
$ftmp = $_FILES['image']['tmp_name'];
$oname = $_FILES['image']['name'];
$fname = 'upload/'.$_FILES['image']['name'];
if(move_uploaded_file($ftmp, $fname)){
	?>
	<html><head><script>
	var par = window.parent.document;
	var images = par.getElementById('images');
	var imgdiv = images.getElementsByTagName('div')[<?=(int)$_POST['imgnum']?>];
	var image = imgdiv.getElementsByTagName('img')[0];
	imgdiv.removeChild(image);
	var image_new = par.createElement('img');
	image_new.src = 'resize.php?pic=<?=$oname?>';
	image_new.className = 'loaded';
	imgdiv.appendChild(image_new);
	</script></head>
	</html>
	<?php
	exit();
}
?><html><head>
<script language="javascript">
function upload(){
	// hide old iframe
	var par = window.parent.document;
	var num = par.getElementsByTagName('iframe').length - 1;
	var iframe = par.getElementsByTagName('iframe')[num];
	iframe.className = 'hidden';
	
	// create new iframe
	var new_iframe = par.createElement('iframe');
	new_iframe.src = 'upload.php';
	new_iframe.frameBorder = '0';
	par.getElementById('iframe').appendChild(new_iframe);
	
	// add image progress
	var images = par.getElementById('images');
	var new_div = par.createElement('div');
	var new_img = par.createElement('img');
	new_img.src = 'indicator.gif';
	new_img.className = 'load';
	new_div.appendChild(new_img);
	images.appendChild(new_div);
	
	// send
	var imgnum = images.getElementsByTagName('div').length - 1;
	document.iform.imgnum.value = imgnum;
	setTimeout(document.iform.submit(),5000);
}
</script>
<style>
#file {
	width: 350px;
}
</style>
<head><body><center>
<form name="iform" action="" method="post" enctype="multipart/form-data">
<input id="file" type="file" name="image" onchange="upload()" />
<input type="hidden" name="imgnum" />
</form>
</center></html>