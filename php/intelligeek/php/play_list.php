<?php
play_list();
function play_list()
	{
	      require_once "../config/config.php";
              echo'<h3>Choose the Topic From List</h3>';
              echo'<p id="play_station" class="capitals">';
              echo'<label class="left">Choose Number Of Question Per Session:</label>';    
              echo'<select class="combo" name="choose_number_of_question" id="nos" size="1" onChange="no_of_questions(document.getElementById(\'nos\'))">
                                        <option value=1>One</option>
                                        <option value=2>Two</option>
                                        <option value=3>Three</option>
                                        <option value=4>Four</option>
                                        <option value=5>Five</option>
                                        <option value=10>Ten</option>
                                </select><br><br>';
		//$result = mysql_query("show tables from geek" ) or die(mysql_error());  
                 $query ='SELECT * FROM subject';
                 $result = mysql_query($query) or die(mysql_error());  
                 
                 echo'List of Topics To Play:<br>';
                 while($row = mysql_fetch_array( $result )) 
			{                     
                        echo $row['0'].') <a  id="topic_list" href="javascript:ajax(\'../php/start_play.php?topic_is='.$row['0'].'&nos_quest=\'+ sel_tag '.',\'content\')">'.$row[1].'</a>'.'<br>';
			}
                echo'</p>';
          
	}



?>
