<?php 

$host = "203.146.140.216";
$port = "8010";

$fp = @fsockopen($host , $port);

if($fp){
fputs($fp,"GET /index.html HTTP/1.0\r\nUser-Agent: SHOUTcast stats XML Parser (Mozilla Compatible)\r\n\r\n");

while(!feof($fp)) {
	$page = fgets($fp, 1024);
	
	
	$page = stristr($page, 'Current Song: </font></td><td><font class=default><b>');
	$page = ltrim($page, 'Current Song: </font></td><td><font class=default><b>');
	
	$page = explode("</b></td></tr>",$page);

	
}
}	

@fclose($fp);

function tis2utf8($tis) {
   for( $i=0 ; $i< strlen($tis) ; $i++ ){
      $s = substr($tis, $i, 1);
      $val = ord($s);
      if( $val < 0x80 ){
         $utf8 .= $s;
      } elseif ( ( 0xA1 <= $val and $val <= 0xDA ) or ( 0xDF <= $val and $val <= 0xFB ) ){
         $unicode = 0x0E00 + $val - 0xA0;
         $utf8 .= chr( 0xE0 | ($unicode >> 12) );
         $utf8 .= chr( 0x80 | (($unicode >> 6) & 0x3F) );
         $utf8 .= chr( 0x80 | ($unicode & 0x3F) );
      }
   }
   return $utf8;
}

if(tis2utf8($page[0]))
{
echo "<marquee direction=\"left\" width=\"95%\" scrollAmount=\"2\">".tis2utf8($page[0])."</marquee>";
}

?>