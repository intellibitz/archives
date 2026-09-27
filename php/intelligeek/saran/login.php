<?php
  require_once('init.php');

  if($_GET['logout']){ //they are logging out
    mysql_query("DELETE FROM Chat_Users WHERE UserID = " . $_SESSION['Chat_UserID']);
   // $_SESSION = array();
    //if(isset($_COOKIE[session_name()])){
   //   setcookie(session_name(), '', 1, '/');
    //  unset($_COOKIE[session_name()]);
   // }
    //session_destroy(); // To delete the old session file
    //header("Location: ../login/index.php");
	echo"you r logged out from the chat room";

    exit;
  }

  if(sizeof($_POST)){
    $expiretime = date("YmdHis", time() - 5);

    if($_SERVER['REQUEST_METHOD'] == 'POST'){
      if(preg_match('/^[_a-z0-9-]+$/i',$_POST['who'])){
        $result = mysql_query("SELECT UserID FROM Chat_Users WHERE UserName = '".mysql_real_escape_string($_POST['who'])."' AND LastUpdate > " . $expiretime);
        if(!mysql_fetch_array($result)){
          mysql_query("DELETE FROM Chat_Users WHERE LastUpdate <= " .$expiretime);
          mysql_query("DELETE FROM Chat_Messages WHERE Posted <= " . $expiretime);
          mysql_query("INSERT INTO Chat_Users(UserName,LastUpdate) VALUES ('".mysql_real_escape_string($_POST['who'])."'," . date("YmdHis",time()).")");
          $_SESSION['Chat_UserID'] = mysql_insert_id();
          $_SESSION['Chat_Prevtime'] = date("YmdHis",time());
          header("Location: ./chat.php");
          exit;
        }
        else
          $error = "A user with the same handle is currently in the chat room. Please try a different handle.";
      }
      else
        $error = "Handles may only contain letters, numbers, hyphens and dashes.";
    }
    else
      $error = "You must enter a handle (screen name) to enter the chat room.";
  }
?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en-US" xml:lang="en-US">
  <head>
    <title>Chat</title>
  </head>
  <body>
    <h1>Chat</h1>
    <form class="grid" method="post" action="./login.php">
      Login<br>
      <label for="who">Handle: </label><input type="text" id="who" name="who" value="<? echo htmlspecialchars($_POST['who']) ?>" />
      <input type="submit" value="Join Chat" class="submit" />
    </form>
    <p class="error">
      <? echo htmlspecialchars($error); ?>
    </p>
  </body>
</html>
