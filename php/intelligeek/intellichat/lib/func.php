<?php 
function utf8_to_tis620($string) {
  $str = $string;
  $res = "";
  for ($i = 0; $i < strlen($str); $i++) {
    if (ord($str[$i]) == 224) {
      $unicode = ord($str[$i+2]) & 0x3F;
      $unicode |= (ord($str[$i+1]) & 0x3F) << 6;
      $unicode |= (ord($str[$i]) & 0x0F) << 12;
      $res .= chr($unicode-0x0E00+0xA0);
      $i += 2;
    } else {
      $res .= $str[$i];
    }
  }
  return $res;
}

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

function encode($data) {
	$data = htmlspecialchars($data);
	$data = str_replace("\n", "", $data);
	if(ereg("(http|https|ftp)+://[^<>[:space:]]+[[:alnum:]]" , $data))
		{
			$data = ereg_replace("(http|https|ftp)+://[^<>[:space:]]+[[:alnum:]]" , "<span class=\"link\"><a href=\"\\0\" target=\"_blank\"  title=\"Click To Go To This Site\">\\0</a></span>", $data);
		}
	elseif(ereg("(www)+\.+[a-zA-Z]+\.+[a-zA-Z]+$" , $data))
		{
			$data = ereg_replace("(www)+\.+[a-zA-Z]+\.+[a-zA-Z]+$" , "<span class=\"link\"><a href=\"http://\\0\" target=\"_blank\"  title=\"Click To Go To This Site\">\\0</a></span>", $data);
		}
		
	$data = ereg_replace("[0-9a-zA-Z|\_]+@+[0-9a-zA-Z]+\.+[a-zA-Z]+" , "<span class=\"link\"><a href=\"msnim:add?contact=\\0\" title=\"Click To Add This E-mail In Your Windows Live Messenger\">\\0</a></span>" , $data);
		
return $data;
}

function CheckEmo($data) {

	$data = ereg_replace('[emo=+[1]+[0]+]', "<span class=\"emo\"><img src=\"images/emoticons/10.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[1]+[1]+]', "<span class=\"emo\"><img src=\"images/emoticons/11.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[1]+[2]+]', "<span class=\"emo\"><img src=\"images/emoticons/12.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[1]+[3]+]', "<span class=\"emo\"><img src=\"images/emoticons/13.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[1]+[4]+]', "<span class=\"emo\"><img src=\"images/emoticons/14.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[1]+[5]+]', "<span class=\"emo\"><img src=\"images/emoticons/15.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[1]+[6]+]', "<span class=\"emo\"><img src=\"images/emoticons/16.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[1]+[7]+]', "<span class=\"emo\"><img src=\"images/emoticons/17.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[1]+[8]+]', "<span class=\"emo\"><img src=\"images/emoticons/18.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[1]+[9]+]', "<span class=\"emo\"><img src=\"images/emoticons/19.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[2]+[0]+]', "<span class=\"emo\"><img src=\"images/emoticons/20.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[2]+[1]+]', "<span class=\"emo\"><img src=\"images/emoticons/21.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[2]+[2]+]', "<span class=\"emo\"><img src=\"images/emoticons/22.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[2]+[3]+]', "<span class=\"emo\"><img src=\"images/emoticons/23.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[2]+[4]+]', "<span class=\"emo\"><img src=\"images/emoticons/24.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[2]+[5]+]', "<span class=\"emo\"><img src=\"images/emoticons/25.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[2]+[6]+]', "<span class=\"emo\"><img src=\"images/emoticons/26.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[2]+[7]+]', "<span class=\"emo\"><img src=\"images/emoticons/27.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[2]+[8]+]', "<span class=\"emo\"><img src=\"images/emoticons/28.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[2]+[9]+]', "<span class=\"emo\"><img src=\"images/emoticons/29.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[3]+[0]+]', "<span class=\"emo\"><img src=\"images/emoticons/30.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[3]+[1]+]', "<span class=\"emo\"><img src=\"images/emoticons/31.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[3]+[2]+]', "<span class=\"emo\"><img src=\"images/emoticons/32.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[3]+[4]+]', "<span class=\"emo\"><img src=\"images/emoticons/34.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[3]+[5]+]', "<span class=\"emo\"><img src=\"images/emoticons/35.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[3]+[6]+]', "<span class=\"emo\"><img src=\"images/emoticons/36.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[3]+[7]+]', "<span class=\"emo\"><img src=\"images/emoticons/37.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[3]+[8]+]', "<span class=\"emo\"><img src=\"images/emoticons/38.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[3]+[9]+]', "<span class=\"emo\"><img src=\"images/emoticons/39.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[4]+[0]+]', "<span class=\"emo\"><img src=\"images/emoticons/40.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[4]+[1]+]', "<span class=\"emo\"><img src=\"images/emoticons/41.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[5]+[0]+]', "<span class=\"emo\"><img src=\"images/emoticons/50.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[5]+[2]+]', "<span class=\"emo\"><img src=\"images/emoticons/52.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[c]+[h]+]', "<span class=\"emo\"><img src=\"images/emoticons/ch.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[c]+[h]+[1]+]', "<span class=\"emo\"><img src=\"images/emoticons/ch1.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[c]+[h]+[2]+]', "<span class=\"emo\"><img src=\"images/emoticons/ch2.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[l]+[o]+]', "<span class=\"emo\"><img src=\"images/emoticons/lo.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[b]+[a]+[n]+[n]+]', "<span class=\"emo\"><img src=\"images/emoticons/bann.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[s]+[t]+[u]+]', "<span class=\"emo\"><img src=\"images/emoticons/stu.gif\"></span>", $data);
	$data = ereg_replace('[emo=+[v]+[a]+[l]+[e]+[n]+[t][i]+[n]+[e]]', "<span class=\"emo\"><img src=\"images/emoticons/valentine.gif\"></span>", $data);

return $data;
}

?>