<!--
	This is the HTML file for the front-end of the JSON AJAX Driven Chat application
	This code was developed by Ryan Smith of 345 Technical Services

	You may use this code in your own projects as long as this copyright is left
	in place.  All code is provided AS-IS.
	This code is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	For the rest of the code visit http://www.DynamicAJAX.com

	Copyright 2005 Ryan Smith / 345 Technical / 345 Group.
-->
<?php
session_start();
if(isset($_SESSION['username']))
{
//echo $_session['username'];
?>
<html>
	<head>
		<title>IntelliGeek Chat</title>
		<style type="text/css" media="screen">
			.chat_time {
				font-style: italic;
				font-size: 9px;
			}
		</style>
		<script language="JavaScript" type="text/javascript">
			var sendReq = getXmlHttpRequestObject();
			var receiveReq = getXmlHttpRequestObject();
			var lastMessage = 0;
			var mTimer;
			//Function for initializating the page.
			function startChat() {
				//Set the focus to the Message Box.
				document.getElementById('txt_message').focus();
				//Start Recieving Messages.
				getChatText();
			}
			//Gets the browser specific XmlHttpRequest Object
			function getXmlHttpRequestObject() {
				if (window.XMLHttpRequest) {
					return new XMLHttpRequest();
				} else if(window.ActiveXObject) {
					return new ActiveXObject("Microsoft.XMLHTTP");
				} else {
					document.getElementById('p_status').innerHTML = 'Status: Cound not create XmlHttpRequest Object.  Consider upgrading your browser.';
				}
			}

			//Gets the current messages from the server
			function getChatText() {
				if (receiveReq.readyState == 4 || receiveReq.readyState == 0) {
					receiveReq.open("GET", 'getChat.php?chat=1&last=' + lastMessage, true);
					receiveReq.onreadystatechange = handleReceiveChat;
					receiveReq.send(null);
				}
			}
			//Add a message to the chat server.
			function sendChatText() {
				if(document.getElementById('txt_message').value == '') {
					alert("You have not entered a message");
					return;
				}
				if (sendReq.readyState == 4 || sendReq.readyState == 0) {
					sendReq.open("POST", 'getChat.php?chat=1&last=' + lastMessage, true);
					sendReq.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
					sendReq.onreadystatechange = handleSendChat;
					var param = 'message=' + document.getElementById('txt_message').value;
					param += '&name=Ryan Smith';
					param += '&chat=1';
					sendReq.send(param);
					document.getElementById('txt_message').value = '';
				}
			}
			//When our message has been sent, update our page.
			function handleSendChat() {
				//Clear out the existing timer so we don't have
				//multiple timer instances running.
				clearInterval(mTimer);
				getChatText();
			}
			function handleReceiveChat() {
				if (receiveReq.readyState == 4) {
					//Get a reference to our chat container div for easy access
					var chat_div = document.getElementById('div_chat');
					//Get the AJAX response and run the JavaScript evaluation function
					//on it to turn it into a useable object.  Notice since we are passing
					//in the JSON value as a string we need to wrap it in parentheses
					var response = eval("(" + receiveReq.responseText + ")");
					for(i=0;i < response.messages.message.length; i++) {
						chat_div.innerHTML += response.messages.message[i].user;
						chat_div.innerHTML += '&nbsp;&nbsp;<font class="chat_time">' +  response.messages.message[i].time + '</font><br />';
						chat_div.innerHTML += response.messages.message[i].text + '<br />';
						chat_div.scrollTop = chat_div.scrollHeight;
						lastMessage = response.messages.message[i].id;
					}
					mTimer = setTimeout('getChatText();',2000); //Refresh our chat in 2 seconds
				}
			}
			//This functions handles when the user presses enter.  Instead of submitting the form, we
			//send a new message to the server and return false.
			function blockSubmit() {
				sendChatText();
				return false;
			}
			//This cleans out the database so we can start a new chat session.
			function resetChat() {
				if (sendReq.readyState == 4 || sendReq.readyState == 0) {
					sendReq.open("POST", 'getChat.php?chat=1&last=' + lastMessage, true);
					sendReq.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
					sendReq.onreadystatechange = handleResetChat;
					var param = 'action=reset';
					sendReq.send(param);
					document.getElementById('txt_message').value = '';
				}
			}
			//This function handles the response after the page has been refreshed.
			function handleResetChat() {
				document.getElementById('div_chat').innerHTML = '';
				getChatText();
			}
		</script>
	</head>
	<body onLoad="javascript:resetChat();">
	   <div id="chatdiv" style= "height:465px; width:460px ; border: 1px solid black;">
		<table width="460" style="background-color:#82B5EC" >
		<tr><td><h3>IntelliGeek-Chat</h2></td><td><p id="p_status">Status: <select id="status" name="status">
<option value="online">online</option>
<option value="busy">busy</option>
<option value="invisible">invisible</option>
</select></p></td></tr></table>
		<div id="div_chat" style="height: 300px; width: 460px; overflow: auto; background-color: #DDEBFB; border: 1px solid thin;">
		</div>

		<form id="frmmain" name="frmmain"  onSubmit="return blockSubmit();">

		<form onload="return resetChat();"id="frmmain" name="frmmain" onSubmit="return blockSubmit();">

		<table>
		<tr><td ><input type="text" id="txt_message" name="txt_message"style="width: 380px;height:75px;"/></td><td width="80px" height="75px"><input type="button" name="btn_send_chat" id="btn_send_chat" value="Send" style="width:75px; height:75px;" onClick="javascript:sendChatText();" /></td></tr>
		</table>
		<table width="460px" style="background-color: #DDEBFB;">
		<tr><td><input type="button" name="btn_get_chat" id="btn_get_chat" value="Refresh " onClick="javascript:getChatText();" /><input type="button" name="btn_reset_chat" id="btn_reset_chat" value="Reset" onClick="javascript:resetChat();" /></td><td></td></tr>
		</table>
			<!--<input type="text" id="txt_message" name="txt_message" style="width: 400px;height:75px;" />
			<input type="button" name="btn_send_chat" id="btn_send_chat" value="Send" style="width:75px; height:75px;" onClick="javascript:sendChatText();" />

			<input type="button" name="btn_get_chat" id="btn_get_chat" value="Refresh Chat" onClick="javascript:getChatText();" />
			<input type="button" name="btn_reset_chat" id="btn_reset_chat" value="Reset Chat" onClick="javascript:resetChat();" />-->
			<!--<table align="center"><td><input type="button" name="btn_send_chat" id="btn_send_chat" value="Send" onClick="javascript:sendChatText();" /></td></table>-->
			</div>
		</form>
	</body>

</html>
<?php
}
else
{
session_destroy();
	print " <i>Hello Guest! Your are not logged in please login to chat</i>";?>
     <!-- <a href="javascript:ajax('../login/index.php','login')">Login </a>  Here-->

	   <?php
}
?>