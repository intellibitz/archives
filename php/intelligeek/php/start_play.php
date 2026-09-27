<?php
session_start();
//the session is for next button
//when i press next button i remember data using session
//$_GET['topic_is'] will no get value during next button so..

if($_GET['topic_is']){
$topic_is=$_SESSION['topic']= $_GET['topic_is'];
$no_of_ques=$_SESSION['nos_quest']=$_GET['nos_quest'];

}else{
    $topic_is=$_SESSION['topic'];
    $no_of_ques=$_SESSION['nos_quest'];
}

// this is number of questions per session
if($topic_is)
{
$sub_id=$topic_is;
start_playing($sub_id,$no_of_ques);
}

function start_playing($sub_id,$no_of_ques)
{
		require_once "../config/config.php";
                //SELECT q,a,b,c,d,ans FROM geek.question where level=1 and sub_id=1 LIMIT 0,1000
                $query ="SELECT q,a,b,c,d,ans FROM geek.question where level=1 and sub_id=$sub_id LIMIT 0, $no_of_ques ";
                
                $result = mysql_query("$query") or die(mysql_error());  
		$res = Array();
		$i=1;
		echo'<h3>Your Game Starts with '.$no_of_ques.'Questions </h3>';
                echo'<p id="play_station">';
		while($row = mysql_fetch_array( $result )) 
			{
				echo"<b>Q.$i:</b> ";
				echo $row['q'];
				echo'<br>';
				echo'<br>';
				echo ' a. <input type="radio" onclick="collect_ans('.$i.',1)" value="1" name="opt'.$i.'" /> ',$row['a'];
				echo'<br>';
				echo ' b. <input type="radio"  onclick="collect_ans('.$i.',2)" value="2" name="opt'.$i.'" /> ',$row['b'];
				echo'<br>';
				echo ' c. <input type="radio" onclick="collect_ans('.$i.',3)" value="3" name="opt'.$i.'" /> ',$row['c'];
			
				echo'<br>';
				echo ' d. <input type="radio" onclick="collect_ans('.$i.',4)" value="4" name="opt'.$i.'" /> ',$row['d'];
				echo'<br>';

				echo'<br>';
				//Answer this is encripted and send to client where again compared with the click 
				array_push($res,$row['ans']);
				$i++;
			}
			
			$res_str=join('&',$res);
			echo '<input class="button" value="Answer" onclick="check_ans(\''.$res_str.'\') & score_board()" >';
			
                         echo '<input type="button" class="button" value="Next" onclick= javascript:ajax(\'../php/start_play.php\',\'content\') >';
    			//echo '<input type="button" value="Quit the game" onClick="window.location.reload( true )" >';
                        //data  from js - from check_ans() function
			echo '<br><br><div id="message"></div>';
                        echo'</p>';
}

?>








