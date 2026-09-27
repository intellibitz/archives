<?php
  session_start();

  /*
   * replace the parameters used here with the appropriate information
   * for your system.
   */
  $dbhandle = mysql_connect("192.168.1.6","geek","geek");
  mysql_select_db("geek");


  /*
   * IMPORTANT: magic quotes are bad. Ideally, you should turn them off
   * in your php.ini, but if you are unable to, the code below will fix
   * the $_POST array for you.
   *
   * See http://www.php.net/manual/en/security.magicquotes.php
   *
   * If you aren't using prepared statements (mysqli, Pear:DB) or manually
   * escaping every variable that goes into a query, you are asking to get
   * pwned. For maximum portability, jenChat uses mysql_real_escape_string,
   * but prepared statements are generally the way to go.
   *
   * If you didn't understand that last paragraph (or even if you
   * did), read up on SQL Injection and why you need to worry about it.
   *
   * http://www.unixwiz.net/techtips/sql-injection.html
   *
   * OK, carry on
   */

  if(get_magic_quotes_gpc()){
    $_POST = array_map('stripslash', $_POST);
  }
  function stripslash($value){
    if(is_array($value))
      return array_map('stripslash', $value);
    else
      return stripslashes($value);
  }
?>
