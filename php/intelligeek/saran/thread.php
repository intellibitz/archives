<?php
  require_once('init.php');

  /* make sure the person is logged in. */
  if(!isset($_SESSION['Chat_UserID']))
    exit;

  $currtime = date("YmdHis",time());

  /* maintains this user's state as active. */
  mysql_query("UPDATE Chat_Users SET LastUpdate = '" . $currtime . "'
                WHERE UserID = " . $_SESSION['Chat_UserID']);

  /* grab any messages posted since the last time we checked.
  Notice we say >= and <. This is to guarantee that we don't miss any
  messages that are posted at the same instant this query is
  executed.*/
  $sql = "SELECT Message,UserName
          FROM Chat_Messages
          INNER JOIN " . "jenChat_Users
            ON Chat_Messages.UserID = Chat_Users.UserID
          WHERE Posted >= '" . $_SESSION['Chat_Prevtime'] . "'
            AND Posted < '" . $currtime . "'
          ORDER BY Posted";
  $res = mysql_query($sql);
?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en-US" xml:lang="en-US">
  <head></head>
  <body>
    <?
      if(mysql_num_rows($res)){
        echo '<div id="contents">';
        while($row = mysql_fetch_array($res)){
          echo '<div><strong>' .
                htmlspecialchars($row['UserName']) . ': </strong>' .
                htmlspecialchars($row['Message']) . '</div>';
        }
        echo '</div>';
      }
      $_SESSION['Chat_Prevtime'] = $currtime;
    ?>
    <script type="text/javascript"><!--
      if(parent.insertMessages && document.getElementById("contents"))
        parent.insertMessages(document.getElementById("contents").innerHTML);

      //setTimeout("getMessages()", 10000); //poll server again in one second

      function getMessages(){
        document.location.reload();
      }
      //-->
    </script>
  </body>
</html>
