<?php

$mailid = $_GET['e_mail'];


include('../config/config.php');

//run this below code for first time only
 //mysql_query("create table email(sno int primary key AUTO_INCREMENT,emailid varchar(40) unique)") or die (mysql_error());

if(preg_match("/^[a-z0-9\å\ä\ö._-]+@[a-z0-9\å\ä\ö.-]+\.[a-z]{2,6}$/i", $mailid))
{

  $sql = "SELECT emailid FROM newsletter WHERE emailid = '$mailid'";


    $check = mysql_query($sql) or die('Query failed. ' . mysql_error());
    if (mysql_num_rows($check) ==1)
    {
	//echo $check;
    echo("<small>New ID please,it`s already registered</small>");
    }
	else
	{
	$pos = $mailid;
   //echo 	$pos;
	if(!($pos > 0))
	{
		$illegal=array('.com',".net",'.org','.biz','.coop','.info','.museum','.name','.pro','.edu','.gov','.int','.mil','.ac','.ad','.ae','.af','.ag','.ai','.al','.am','.an','.ao','.aq','.ar','.as','.at','.au','.aw','.az',
			'.ba','.bb','.bd','.be','.bf','.bg','.bh','.bi','.bj','.bm','.bn','.bo','.br','.bs','.bt','.bv','.bw','.by','.bz','.ca','.cc','.cd','.cf','.cg','.ch','.ci','.ck','.cl','.cm','.cn','.co','.cr','.cu','.cv','.cx','.cy','.cz',
			'.de','.dj','.dk','.dm','.do','.dz','.ec','.ee','.eg','.eh','.er','.es','.et','.fi','.fj','.fk','.fm','.fo','.fr','.ga','.gd','.ge','.gf','.gg','.gh','.gi','.gl','.gm','.gn','.gp','.gq','.gr','.gs','.gt','.gu','.gv','.gy',
			'.hk','.hm','.hn','.hr','.ht','.hu','.id','.ie','.il','.im','.in','.io','.iq','.ir','.is','.it','.je','.jm','.jo','.jp','.ke','.kg','.kh','.ki','.km','.kn','.kp','.kr','.kw','.ky','.kz','.la','.lb','.lc','.li','.lk','.lr','.ls','.lt','.lu','.lv','.ly',
			'.ma','.mc','.md','.mg','.mh','.mk','.ml','.mm','.mn','.mo','.mp','.mq','.mr','.ms','.mt','.mu','.mv','.mw','.mx','.my','.mz','.na','.nc','.ne','.nf','.ng','.ni','.nl','.no','.np','.nr','.nu','.nz','.om',
			'.pa','.pe','.pf','.pg','.ph','.pk','.pl','.pm','.pn','.pr','.ps','.pt','.pw','.py','.qa','.re','.ro','.rw','.ru','.sa','.sb','.sc','.sd','.se','.sg','.sh','.si','.sj','.sk','.sl','.sm','.sn','.so','.sr','.st','.sv','.sy','.sz',
			'.tc','.td','.tf','.tg','.th','.tj','.tk','.tm','.tn','.to','.tp','.tr','.tt','.tv','.tw','.tz','.ua','.ug','.uk','.um','.us','.uy','.uz','.va','.vc','.ve','.vg','.vi','.vn','.vu','.ws','.wf','.ye','.yt','.yu','.za','.zm','.zw');
		$garb=array();
		$pos =strpos($mailid,"@");
		//echo 	$pos;
		$pos1 =strpos($mailid,".");

		//echo 	$pos1;
		$maild = "wrong";
		foreach($illegal as $k)
		{
		 	if(($j=strpos($mailid,$k))!==false)
 			{
			  $l=strpos($mailid,$k);
			  	if($pos == FALSE || (($l-$pos)<=3) || ($pos1<$pos && $pos==($pos1+1)))
				{
					$maild = "wrong";
				}
				else
				{
					$maild = "correct";
				}
 			}
		}

		if($maild == "correct")
		{
			$result=mysql_query("insert into newsletter values('','$mailid')") or die(mysql_error());

			echo("Dear Subscriber, Thanks you");

		}
		else
		{
			echo("Error :proper mail id please");
		}
	}

	else
	{
		echo "Invalid Mail id ";
	}

}
}



?>
