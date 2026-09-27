<?php
include_once("settings.php");

if (!$_POST)
{
  include("form.php");
}
else if(!$_POST['formname'] || $_POST['formname'] != "invite")
{
  include_once($scripts[$iscript]['filename']);
  $login = $_POST['username'];
  $password = $_POST['password'];
  
  $resultarray = get_contacts($login, $password);
	
	
	#if contacts were retreived successfully:
  if(is_array($resultarray))
	{
    #the first array_shift of the result will give you the names in an array
		$names = array_shift($resultarray);
		#the second array_shift of the result will give you the emails
		$emails = array_shift($resultarray);
		
				   if (!eregi("@", $login))
				   {
				   		$login = $login . "@" . strtolower($iscript) . ".com";
				   }

                   echo '<div align="center" style="padding:5; background-color: #EEEEEE; width:350;">';
                   echo '<form method="POST" action="'.$formaction .'" name="inviteform" id="inviteform"><table style="background-color:white; border:black solid thin;">';
                   echo '<SCRIPT LANGUAGE="JavaScript">' ."\n"
                           .'    function togglechecked(){ ' . "\n"
                           .'      for (var i = 0; i < document.inviteform.elements.length; i++) {' . "\n"
                           .'        var e = document.inviteform.elements[i];' . "\n"
                           ."        if ((e.disabled == false) && (e.name != 'allbox') && (e.type == 'checkbox')) {" ."\n"
                           .'    e.checked = document.inviteform.allbox.checked;' . "\n"
                           .'        }' . "\n"
                           .'      }' . "\n"
                           .'    }' . "\n"
                           .'    function toggleselect(){ ' . "\n"
                           .'      document.inviteform.allbox.checked = !document.inviteform.allbox.checked;' . "\n"
                           .'        togglechecked();}' . "\n"

                           .'    </SCRIPT>'  . "\n";
                   echo "<tr bgcolor=\"#FFFFFF\"><td colspan=\"3\" align=\"center\"><h1 align=\"center\">Invite Contacts</h1>$logo</td></tr>";
                   echo "<tr bgcolor=\"#CCCCCC\"><td>" . "<input type=\"checkbox\" name=\"allbox\" id=\"allbox\" value=\"nothing\" onClick=\"togglechecked()\" checked>" . '</td><td><b>Name</b></td><td><b>Email</b></td></tr>';
                   echo '<input type="hidden" name="formname" value="invite">';
                   echo "<input type=\"hidden\" name=\"sender\" value=\"$login\">";
                   
                   $maxin = count($names);


                   for ($i=0; $i<$maxin; ++$i)
                   {
                     $emails[$i] = trim($emails[$i]);
                     if ($emails[$i]!="" && eregi("@", $emails[$i]))
                     {
          					   $emails[$i] = strtolower($emails[$i]);
                       echo "<tr><td>" . "<input type=\"checkbox\" name=\"addresses[]\" value=\"$emails[$i]\" checked>" . "</td><td>$names[$i]</td><td>$emails[$i]</td></tr>";
                     }
                   }
                   echo <<< _end_this
               <tr>
               <tr><td><input type="checkbox" name="allbox2" value="nothing" onClick="toggleselect()" checked></td><td><a href="javascript:toggleselect()">Select/Deselect All</a></td><td></td></tr>

             <td colspan="3" style="padding:4"><input name="submit" type="submit" value="Invite Selected" style="width:100%"></td>
           </tr>
          </table></form>
  <div align="center" style="color:#444444; font-size:11.5px; font-family:Arial, sans-serif; align:center; width:100%;">&copy; 
    2006 Address Book Import Scripts Developed by <a href="http://svetlozar.net" target="_blank" style="color:#333333;">Svetlozar 
    Petrov</a></div> 
</div>
_end_this;
	}
  else #else print out the form with the error message
  {
    switch ($resultarray)
    {
      case 1: #invalid login
        $formdisclaimer = "<br><b style=\"color:red\">Invalid Login</b><br>";
        break;
      case 2: #empty username or password
        $formdisclaimer = "<br><b style=\"color:red\">Enter Your Username and Password</b><br>";
        break;
    }
  	include("form.php");
  }
}
else if ($_POST['formname'] == "invite")
{
  $message = file_get_contents($basedir . $slash . "email.html");
  $subject = file_get_contents($basedir . $slash . "emailsubject.txt");


  $addressesStr = implode(",", $_POST['addresses']);

  $headers = "";
  
  if ($fromfield && $fromfield!="")
  {
    $from = $fromfield;
  }
  else
  {
  	$from = trim($_POST['sender']);
  	$headers .= "From: $from\r\n";
  }
  
  $headers .= "Bcc: $addressesStr" . "\r\n";

  $message = str_replace("[[[sender]]]", $_POST['sender'], $message);
  $subject = str_replace("[[[sender]]]", $_POST['sender'], $subject);

  $headers .= "MIME-Version: 1.0\r\n" .
         "Content-Type: text/html;\r\n";
  
 	if(mail ($tofield, $subject, "\r\n". $message, $headers))
 	  $msg = "Invites Sent";
 	else
 	  $msg = "Error occured";
 	
echo <<< _end_sent
<style type="text/css">
<!--
.formheading
{
	color:black;
	font-size:24px;
	font-family:Arial, sans-serif;
	font-weight:bolder;
}
.scriptlinks a
{
	color:blue;
  text-decoration:none;
}

-->
</style> 
  <div style="padding:5; background-color: #EEEEEE; width:350;">
  <div style="width:340; border: black thin solid; background-color:white;" align="center"><h1>$msg</h1>
  <p class="scriptlinks">Invite more from $selects</p><br><br><br></div>
  <div align="center" style="color:#444444; font-size:11.5px; font-family:Arial, sans-serif; align:center;">&copy; 
    2006 Address Book Import Scripts Developed by <a href="http://svetlozar.net" target="_blank" style="color:#333333;">Svetlozar 
    Petrov</a></div> 
</div> 	
_end_sent;

}

?>
