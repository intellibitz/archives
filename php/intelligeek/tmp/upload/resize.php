<?php
if($_GET['pic']){
	$img = new img('upload/'.$_GET['pic']);
	$img->resize();
	$img->show();
}

class img {
	
	var $image = '';
	var $temp = '';
	
	function img($sourceFile){
		if(file_exists($sourceFile)){
			$this->image = ImageCreateFromJPEG($sourceFile);
		} else {
			$this->errorHandler();
		}
		return;
	}
	
	function resize($width = 100, $height = 100, $aspectradio = true){
		$o_wd = imagesx($this->image);
		$o_ht = imagesy($this->image);
		if(isset($aspectradio)&&$aspectradio) {
			$w = round($o_wd * $height / $o_ht);
			$h = round($o_ht * $width / $o_wd);
			if(($height-$h)<($width-$w)){
				$width =& $w;
			} else {
				$height =& $h;
			}
		}
		$this->temp = imageCreateTrueColor($width,$height);
		imageCopyResampled($this->temp, $this->image,
		0, 0, 0, 0, $width, $height, $o_wd, $o_ht);
		$this->sync();
		return;
	}
	
	function sync(){
		$this->image =& $this->temp;
		unset($this->temp);
		$this->temp = '';
		return;
	}
	
	function show(){
		$this->_sendHeader();
		ImageJPEG($this->image);
		return;
	}
	
	function _sendHeader(){
		header('Content-Type: image/jpeg');
	}
	
	function errorHandler(){
		echo "error";
		exit();
	}
	
	function store($file){
		ImageJPEG($this->image,$file);
		return;
	}
	
	function watermark($pngImage, $left = 0, $top = 0){
		ImageAlphaBlending($this->image, true);
		$layer = ImageCreateFromPNG($pngImage); 
		$logoW = ImageSX($layer); 
		$logoH = ImageSY($layer); 
		ImageCopy($this->image, $layer, $left, $top, 0, 0, $logoW, $logoH); 
	}
}
?>
