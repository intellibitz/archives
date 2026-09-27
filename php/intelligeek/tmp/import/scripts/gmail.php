<?php
#Copyright 2006 Svetlozar Petrov
#All Rights Reserved
#svetlozar@svetlozar.net
#http://svetlozar.net

#Script to import the names and emails from gmail contact list

#Globals Section, $location and $cookiearr should be used in any script that uses
#                                     get_contacts function
$location = "";
$cookiearr = array();

#function get_contacts, accepts as arguments $login (the username) and $password
#returns array of: array of the names and array of the emails if login successful
#otherwise returns 1 if login is invalid and 2 if username or password was not specified
function get_contacts($login, $password)
{
  #the globals will be updated/used in the read_header function
  global $location;
  global $cookiearr;
  global $ch;

  #check if username and password was given:
	if ((isset($login) && trim($login)=="") || (isset($password) && trim($password)==""))
	{
	  #return error code if they weren't
		return 2;
	}
	
	#initialize the curl session
	$ch = curl_init();
	curl_setopt($ch, CURLOPT_URL,"http://mail.google.com/mail/");
	curl_setopt($ch, CURLOPT_REFERER, "");
	curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, 0);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
	curl_setopt($ch, CURLOPT_FOLLOWLOCATION, 1);
	curl_setopt($ch, CURLOPT_HEADERFUNCTION, 'read_header');
	
	#get the html from gmail.com
  $html = curl_exec($ch);
	
	$matches = array();
	$actionarr = array();
	
	$action = "https://www.google.com/accounts/ServiceLoginAuth?service=mail";

	#parse the login form:
	#parse all the hidden elements of the form
	preg_match_all('/<input type\="hidden" name\="([^"]+)".*?value\="([^"]*)"[^>]*>/si', $html, $matches);
	$values = $matches[2];
	$params = "";
	
	$i=0;
	foreach ($matches[1] as $name)
	{
	  $params .= "$name=" . urlencode($values[$i]) . "&";
	  ++$i;
	}

  $login = urlencode($login);
  $password = urlencode($password);
  
  #submit the login form:
	curl_setopt($ch, CURLOPT_URL,$action);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER,1);
	curl_setopt($ch, CURLOPT_POST, 1);
	curl_setopt($ch, CURLOPT_POSTFIELDS, $params ."Email=$login&Passwd=$password&PersistentCookie=");

	$html = curl_exec($ch);

  #test if login was successful:
  if (!isset($cookiearr['GX']) && (!isset($cookiearr['LSID']) || $cookiearr['LSID'] == "EXPIRED"))
	{
		return 1;
	}

  #this is the new csv url:
	curl_setopt($ch, CURLOPT_URL, "http://mail.google.com/mail/contacts/data/export?exportType=ALL&groupToExport=&out=OUTLOOK_CSV");
  curl_setopt($ch, CURLOPT_RETURNTRANSFER,1);

	$html = curl_exec($ch);
	
  $csvrows = explode("\n", $html);
  array_shift($csvrows);
  
  $names = array();
  $emails = array();
  foreach ($csvrows as $row)
  {
    $values = explode(",", $row);
    if (eregi("@", $values[1]))
    {
      $names[] = ( trim($values[0])=="" ) ? $values[1] : $values [0];
      $emails[] = $values[1];
    }
  }
  

	return array($names, $emails);
}

#read_header is essential as it processes all cookies and keeps track of the current location url
#leave unchanged, include it with get_contacts
function read_header($ch, $string)
{
    global $location;
    global $cookiearr;
    global $ch;
    
    $length = strlen($string);
    if(!strncmp($string, "Location:", 9))
    {
      $location = trim(substr($string, 9, -1));
    }
    if(!strncmp($string, "Set-Cookie:", 11))
    {
      $cookiestr = trim(substr($string, 11, -1));
      $cookie = explode(';', $cookiestr);
      $cookie = explode('=', $cookie[0]);
      $cookiename = trim(array_shift($cookie)); 
      $cookiearr[$cookiename] = trim(implode('=', $cookie));
    }
    $cookie = "";
    if(trim($string) == "") 
    {
      foreach ($cookiearr as $key=>$value)
      {
        $cookie .= "$key=$value; ";
      }
      curl_setopt($ch, CURLOPT_COOKIE, $cookie);
    }

    return $length;
}

#function to trim the whitespace around names and email addresses
#used by get_contacts when parsing the csv file
function trimvals($val)
{
  return trim ($val, "\" \n");
}
?>
