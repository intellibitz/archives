<?php
$fromfield = ""; #change this to the address that you want it to appear as the sender
                #leave unchanged if you want the sender address to be the one from the user importing contacts
                
$tofield = ""; #you must enter (your) email address here otherwise mail won't be sent
#the script right now works by putting all invites in the bcc field (to save execution time)
#the to field should not be empty though for the script to work!


$iscript = "Gmail";

$baseurl = dirname($_SERVER['PHP_SELF']);
$basedir = dirname(__FILE__);
if(eregi("/", $basedir))
{
  $slash = "/";
}
else
{
  $slash = "\\";
}

$scriptspath = $basedir . $slash . "scripts" . $slash;

$available_scripts = array("Gmail");
$scripts = array();

$separator = ' <font color="#999999">|</font> ';
$selects = $separator;

foreach ($available_scripts as $scriptname)
{
  $scripts[$scriptname]['filename'] = $scriptspath . strtolower($scriptname). ".php";
  $scripts[$scriptname]['logofile'] = $baseurl . "/images/" . strtolower($scriptname). ".gif";
  $scripts[$scriptname]['formname'] = $scriptname;
  $querystr['spimport'] = $scriptname;
  $importurl = '<a href="' .  make_url($querystr) . "\">$scriptname</a>";
  $selects .= $importurl . $separator;
}

#print_r($scripts);

if ($_GET['spimport'] && $_GET['spimport'] != "")
{
  $iscript = $_GET['spimport'];
}

$querystr['spimport'] = $iscript;
$formaction = make_url($querystr);
$formname = $scripts[$iscript]['formname'];
$logofile = $scripts[$iscript]['logofile'];

$formdisclaimer = "*Your username and password will not be stored on this server.";

function make_url($getarr)
{
  $serverget = explode("&", $_SERVER['QUERY_STRING']);
  
  foreach ($serverget as $value)
  {
    $valuearr = explode("=", $value);
    if(!$getarr[$valuearr[0]])
      $getarr[$valuearr[0]]=$valuearr[1];
  }
  
  $query = "";
  foreach ($getarr as $key => $value)
  {
    if(trim($key)!="")
      $query .= "$key=$value&";
  }

  $query = trim($query, "&");
 
  $url = $_SERVER['SCRIPT_NAME'] . "?$query";

  return $url;
}
?>
