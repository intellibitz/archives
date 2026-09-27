	var message=-1;
	var online=-1;
	var firsttime = false;
	var sound=1;
	var ibg = 0;
	var name = null;
	var sendmsg = false;
	var recmsg = false;
	var deltarget=0;
	var showcount=0;
	var lastmsg = null;
	var lastsendmsg = null;
	var music = null;
	var nudge=0;
	var Wink=1;
	var regular = new RegExp("[wink]+[0-9]{1,2}");
	var numRand = 0;
	var hint=new Array(5)
	

	function createXMLHttpRequest() {
     		try { return new ActiveXObject("Msxml2.XMLHTTP");    } catch(e) {}
     		try { return new ActiveXObject("Microsoft.XMLHTTP"); } catch(e) {}
    		try { return new XMLHttpRequest();                   } catch(e) {}
     		alert("XMLHttpRequest not supported");
     		return null;
		}
		
		function hintRand(){
		numRand = Math.floor(Math.random()*26)%5;

     hint[0]="<strong><font size=\"1\" face=\"MS Sans Serif, Tahoma, sans-serif\" color=\"#FFFFFF\">�ѧ���մ��Դ������ҡ�ء��������͡ѹ�Ф�Ѻ �÷ӵ���������������ѧⴹẹ��� </font></strong><font face=\"MS Sans Serif, Tahoma, sans-serif\"><a href=\"#\" onClick=\"History()\">[�ѹ�֡���ʹ���]</a></font>";
     hint[1]="<strong><font size=\"1\" face=\"MS Sans Serif, Tahoma, sans-serif\" color=\"#FFFFFF\">�ҡ��ͧ���ʹ���Ẻ��ǹ��� ��顴�����ʹ�ҹ����������͡�ٴ���Ẻ��ǵ�͵�� </font></strong><font face=\"MS Sans Serif, Tahoma, sans-serif\"><a href=\"#\" onClick=\"History()\">[�ѹ�֡���ʹ���]</a></font>";
     hint[2]="<strong><font size=\"1\" face=\"MS Sans Serif, Tahoma, sans-serif\" color=\"#FFFFFF\">�ٻẺ�����Ш�᪷��͡�þٴ���Ẻ����� GroupMode �����͡������ٴ�ҹ��� </font></strong><font face=\"MS Sans Serif, Tahoma, sans-serif\"><a href=\"#\" onClick=\"History()\">[�ѹ�֡���ʹ���]</a></font>";   
     hint[3]="<strong><font size=\"1\" face=\"MS Sans Serif, Tahoma, sans-serif\" color=\"#FFFFFF\">�ٴ��¡ѹ�����Ҩҷ�����Ҿ�յ�͵���ͧ ��Ф�����´��¹Ф�Ѻ ��Ҩ������Ե÷��յ�͡ѹ </font></strong><font face=\"MS Sans Serif, Tahoma, sans-serif\"><a href=\"#\" onClick=\"History()\">[�ѹ�֡���ʹ���]</a></font>";
     hint[4]="<strong><font size=\"1\" face=\"MS Sans Serif, Tahoma, sans-serif\" color=\"#FFFFFF\">�ҡ���ѭ��� � ���͵�ͧ���ʤ�Ի��᪷����ö�Դ��������� service@thaimess.com </font></strong><font face=\"MS Sans Serif, Tahoma, sans-serif\"><a href=\"#\" onClick=\"History()\">[�ѹ�֡���ʹ���]</a></font>";
	 
	 document.getElementById("messhint").innerHTML =  hint[numRand];
	 
	 
	 setTimeout("hintRand()", 10000);

		}
		
	function SetSendTo(sendto) {
	
	var pForm = document.forms[0];
    var pBody = getRequestBody(pForm);
	
	if(document.forms[0].group.value =="" || confirm("Please Confirm To Deactivate Group " + document.forms[0].group.value + " ?")==true)
	{
	document.forms[0].group.value = "";
	
			if(sendto!="all")
			{
				if(sendto == document.forms[0].memno.value)
				{
				document.getElementById("status").innerHTML = "<strong>Status :</strong> Cannot Talk To Yourself";
				}
				else
				{
				
					SendToReq = createXMLHttpRequest();
            		SendToReq.open("GET","./Data.php?action=userinfo&no=" + sendto, true);
					SendToReq.setRequestHeader( "If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT" );
            		SendToReq.onreadystatechange = function () {
                	if (SendToReq.readyState == 4) {
                    	if (SendToReq.status == 200) {
								var  data = SendToReq.responseText.split("|#|");
								

								if(data[0]=="no" || data[0]==null)
								{
								document.getElementById("status").innerHTML = "<strong>Status :</strong> This Person Is Offline";
								}

								
								else
								{
								document.forms[0].sendto.value = data[0];
								document.forms[0].sendtoip.value = data[1];
								var _sendtoshow = "<strong>Message To</strong><br><img src=\"./images/" + data[2] + ".gif\"> " + document.forms[0].sendto.value + " ( <u><a target=\"_parent\" onClick=\"SetSendTo('all')\" onMouseOver=\"this.style.cursor='hand';this.style.color='#000000'\" onMouseOut=\"this.style.color='#154062'\" title=\"Click Here To Back To Public Mode\">Cancel</a></u> )";
								document.getElementById("sendtoshow").innerHTML = $.find(_sendtoshow);
								// document.getElementById("sendtoshow").innerHTML = "<strong>Message To</strong><br><img src=\"./images/" + data[2] + ".gif\"> " + document.forms[0].sendto.value + " ( <u><a target=\"_parent\" onClick=\"SetSendTo('all')\" onMouseOver=\"this.style.cursor='hand';this.style.color='#000000'\" onMouseOut=\"this.style.color='#154062'\" title=\"Click Here To Back To Public Mode\">Cancel</a></u> )";
								document.getElementById("status").innerHTML = "<strong>Status :</strong> Choose Whisper Mode Already ( <a target=\"_parent\" onClick=\"SetSendTo('all')\" onMouseOver=\"this.style.cursor='hand';this.style.color='#000000'\" onMouseOut=\"this.style.color='#154062'\" title=\"Click Here To Back To Public Mode\">Click Here To Send To EveryBody</a> )";
								document.forms[0].data.focus();
								}
							

							}
                    	}
                	}         
            		SendToReq.send(null);
				}
			}
			else
			{
			document.forms[0].sendto.value = "";
			document.getElementById("sendtoshow").innerHTML = "<strong>Message To</strong><br>All";
			document.getElementById("status").innerHTML = "<strong>Status :</strong> Open Public Mode Already";
document.forms[0].data.focus();
			}
			
		}
}
        
        function MessageRequest(SendType) {
			recmsg = true;
			MessageReq = createXMLHttpRequest();
            MessageReq.open("GET","./lib/message.php?messno=" + message, true);
			MessageReq.setRequestHeader( "If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT" );
            MessageReq.onreadystatechange = function () {
                if (MessageReq.readyState == 4) {
                    if (MessageReq.status == 200) {
					
					if(MessageReq.responseText!="")
					{
						var ret = MessageReq.responseText;
						var dataset = ret.split("|@|");
						
					for(var k=0;k<dataset.length;k++)
					{	
						var data = dataset[k].split("|#|");
						
			if(data[7]!=message)
			{
					if(lastmsg!=data[1] || lastsendmsg!=data[0])
					{
					lastmsg = data[1];
					lastsendmsg = data[0];
					
						if(data[0]==name || data[6]==name || (data[6]=="" && data[9]==document.forms[0].group.value) || (data[6]=="" && data[9]==""))
						{
						var output ="";
						
						
							if(MessageReq.responseText=="")
							{
							document.getElementById("status").innerHTML = "Status : Your Message Is Empty";
							}
							else
							{
							
									if(data[1]=="shakescreen")
									{ 
									output+="&nbsp;<font size=\"1\" face=\"MS Sans Serif, Tahoma, sans-serif\"><font color=\"#000000\"><i>" + data[0] + " Has Nudge Your Screen</i></font></font><br>";
									}
									else if(data[1]=="takein")
									{
									output+="&nbsp;<font size=\"1\" face=\"MS Sans Serif, Tahoma, sans-serif\"><font color=\"#000000\"><i>" + data[0] + " Has Join In This Room</i></font></font><br>";
									}
									else if(data[1].match(regular))
									{
									output+="&nbsp;<font size=\"1\" face=\"MS Sans Serif, Tahoma, sans-serif\"><font color=\"#000000\"><i>" + data[0] + " Has Use Winks </i></font></font><br>";
									}
									else
									{
							output += "&nbsp;<font size=\"1\" face=\"MS Sans Serif, Tahoma, sans-serif\"><font color=\"#000000\"> &nbsp;[" + data[5] + "]\t</font><font color=\"" + data[4] + "\"><img src=\"images/" + data[8] +".gif\"> <strong>" + data[0];
							
							if(data[6]!=""){output += " -> " + data[6];}
							if(data[9]!=""){output += " [G]-> " + data[9];}
							output += "</strong>  : ";
							if(data[2]==1){ output += "<strong>";}
							if(data[3]==1){ output += "<u>";}
							
							
							output += data[1]
							
							if(data[3]==1){ output += "</u>";}
							if(data[2]==1){ output += "</strong>";}
							
							output += "</font></font><br>";
									}
							
							
							showcount++;
								if(showcount>=35)
								{					
						 		deltarget = document.getElementById("mess").innerHTML.indexOf("<BR>");
								document.getElementById("mess").innerHTML = document.getElementById("mess").innerHTML.substring(deltarget+4) + output;
								}
								else
								{
								document.getElementById("mess").innerHTML += output;
								}
								
								
								if(data[1]=="shakescreen")
								{
									if(nudge==1)
									{
									shakeScreen(1);
									}
								document.getElementById("plsnd").innerHTML = "�" + playSnd("nudge.wma");
								}
								else  if(data[1]=="takein")
								{
								document.getElementById("plsnd").innerHTML = "�" + playSnd("takein.wma");
								}
									else if(data[1].match(regular))
									{
									var wink = data[1].split("wink");
									
									showWink(wink[1]);
									}
								else
								{
								document.getElementById("plsnd").innerHTML = "�" + playSnd("type.wav");
								}
									
							document.getElementById("mess").scrollTop = document.getElementById("mess").scrollHeight;

							}
						}
						message	 = data[7];
						
						if(document.getElementById("status").innerHTML!="<strong>Status :</strong> Sending Message")
						{
						document.getElementById("status").innerHTML = "<strong>Status :</strong> Message Updated.";
						}
					}
					}
					}
					
					}
						
                    } 
                }            
            };
           MessageReq.send(null); 
		   
		   if(SendType==1){setTimeout("MessageRequest(1)", 3000);}      
		   recmsg = false;
        }

        function OnlineRequest() {
		   OnlineReq = createXMLHttpRequest();
            OnlineReq.open("GET","./data/online.dat", true);
			OnlineReq.setRequestHeader( "If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT" );
            OnlineReq.onreadystatechange = function () {
                if (OnlineReq.readyState == 4) {
                    if (OnlineReq.status == 200) {
                        if(OnlineReq.responseText!=online && OnlineReq.responseText!=null){
						online = OnlineReq.responseText;
						GetOnline();
						}
                    }
                }            
            };
            OnlineReq.send(null);       
			
			setTimeout("OnlineRequest()", 5000);   
        }
		
		function OnlineCheck() {
		   OCheck = createXMLHttpRequest();
            OCheck.open("GET","Data.php?action=delonline", true);
			OCheck.setRequestHeader( "If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT" );
            OCheck.send(null);
			
			setTimeout("OnlineCheck()", 120000);   
        }
		
		      function GetOnline() {
			  var head = "";
			var onlineList = "";
			var status = "";
			var onlineNum = 0;
	
		    GetOnlineReq = createXMLHttpRequest();       
            GetOnlineReq.open("GET","Data.php?action=online", true);
			GetOnlineReq.setRequestHeader( "If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT" );


            GetOnlineReq.onreadystatechange = function () {
                if (GetOnlineReq.readyState == 4) {
                    if (GetOnlineReq.status == 200) {
			
						var onlineAllData = GetOnlineReq.responseText.split('|@|');
						
						for(var i=0;i<onlineAllData.length;i++)
						{
							var onlineData = onlineAllData[i].split('|#|');
							
							

							if(onlineData[2])
							{
								
								onlineList += "<tr><td width=\"25\"><img src=\"images/avatar/" + onlineData[1] + ".gif\"></td><td height=\"35\" width=\"105\"><strong><font color=\"#000000\" size=\"2\"><a target=\"_parent\" onClick=\"SetSendTo('" + onlineData[3] + "')\" onMouseOver=\"this.style.cursor='hand';this.style.color='#FF0000'\" onMouseOut=\"this.style.color='#000000'\" title=\"Open Whisper Mode With "+ onlineData[0] +"\">" + onlineData[0] + "</a></strong><br></font><font color=\"#FFFFFF\">" + onlineData[2] + " ..</font></td></tr>";
								onlineNum++;
							}
							
						}
						
							head="<table  width=130><tr><td colspan=\"2\"><font size=\"2\"><strong><u>Friends List</u></strong> ( " + onlineNum + " )</font></td></tr>"
							
							document.getElementById("useronline").innerHTML = head + onlineList + "</table>";
                    } else {
                        document.getElementById("useronline").innerHTML = GetOnlineReq.statusText;
                    }
                }            
            };
            GetOnlineReq.send(null);       
        }
		
		function SetStatus(no) {
		  			
		var mystatus = "online";
		var checkpass ="";
		var personal		="";
		var pForm = document.forms[0];
		var pBody = getRequestBody(pForm);   
			
		mystatus = document.forms[0].status.value;
		document.forms[0].statushide.value= mystatus;
		
		if(mystatus=="DJ")
		{
		checkpass = prompt("��س�������ʷ�����Դ�ŧ");
		}
		
		if(mystatus=="Personal")
		{
		document.forms[0].statushide.value = prompt("Insert Your Personal Status");
		}
		
		pForm = document.forms[0];
		pBody = getRequestBody(pForm);
		
		if((mystatus!="DJ") || (mystatus=="DJ" && checkpass=="password"))
		{
			 SetStatusReq = createXMLHttpRequest(); 
            SetStatusReq.open("post","Data.php?action=setstatus", true);
			SetStatusReq.setRequestHeader( "Content-Type", "application/x-www-form-urlencoded");
			

            SetStatusReq.onreadystatechange = function () {
                if (SetStatusReq.readyState == 4) {
                    if (SetStatusReq.status == 200) {
                        GetOnline();
                    }
                }            
            };
            SetStatusReq.send(pBody);       
			}
			else
			{
			alert("�������١��ͧ �������������繴�������ѧ� !!!");
			}
			
        }
		
		
		
		function UpdateOnline() {     

	UonlineReq = createXMLHttpRequest();
    var pForm = document.forms[0];
    var pBody = getRequestBody(pForm);   
	UonlineReq.open("post", "Data.php?action=updateonline", true);
	UonlineReq.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    UonlineReq.send(pBody); 
				UonlineReq.onreadystatechange = function () 
				{
					if (UonlineReq.readyState == 4) 
					{
						if (UonlineReq.status == 200) 
						{
							if(UonlineReq.responseText!=1)
							{
							document.getElementById("status").innerHTML = "<strong>Status :</strong> Cannot Update Online Data , System Will Redo In Next 20 Seconds.";
							}
							else
							{
							document.getElementById("status").innerHTML = "<strong>Status :</strong> Update Online Data Successfully";
							}
						}
					}
				}
	setTimeout("UpdateOnline()", 20000);  
			
}

function Nudge() {
	var pForm = document.forms[0];   
	
	pForm.data.value="shakescreen";
	SendData();
	
}		

function insertWink(winkno) {
	var pForm = document.forms[0];   
	
	pForm.data.value="wink"+winkno;
	SendData();
	select_wink.style.visibility="hidden";
	
}		

function SendData() {     
		
		if(sendmsg==false)
		{
			sendmsg = true;
            var pForm = document.forms[0];
            var pBody = getRequestBody(pForm);    
			document.getElementById("status").innerHTML = "<strong>Status :</strong> Sending Your Message";
		   SendDat = createXMLHttpRequest();  
			
			if(pForm.data.value=="")
			{
				document.getElementById("status").innerHTML = "<strong>Status :</strong> Your Message Is Empty";
			}  
			else
			{
            SendDat.open("post", "Data.php?action=write", true);
            SendDat.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
            SendDat.send(pBody); 
			
				SendDat.onreadystatechange = function () {
	                if (SendDat.readyState == 4) {
	                    if (SendDat.status == 200) {
	                    
			                    if(SendDat.responseText==-1)
			                    {
			                    document.getElementById("status").innerHTML = "<strong>Status :</strong> Cannot Send Message";
			                    }
			                    else
			                    {
									if(SendDat.responseText > 0)
									{
										if(recmsg==false){MessageRequest(0);}
										document.getElementById("status").innerHTML = "<strong>Status :</strong> Your Message Sent";
										pForm.data.value = "";
										document.forms[0].data.focus();
									}
									else
									{
										document.getElementById("status").innerHTML = "<strong>Status :</strong> Group Data Is Incorrect";
										document.forms[0].data.focus();
									}
								}
						}
					}
				}
			}
			
			sendmsg = false;
		}
		else
		{
			document.getElementById("status").innerHTML = "<strong>Status :</strong> Please Wait While Sending Message";
		}

			
}

		function SoundState() {     
			if(sound==0){
				sound=1;
				document.getElementById("soundch").innerHTML="Sound Off";
				document.getElementById("status").innerHTML = "<strong>Status :</strong> Activated Sound System";
			}
			else if(sound==1){
				sound=0;
				document.getElementById("soundch").innerHTML="Sound On";
				document.getElementById("status").innerHTML = "<strong>Status :</strong> Deactivated Sound System";
			}	
		}
		
				function NudgeState() {     
			if(nudge==0){
				nudge=1;
				document.getElementById("nudgech").innerHTML="Nudge Off";
				document.getElementById("status").innerHTML = "<strong>Status :</strong> Activated Nudge System";
			}
			else if(nudge==1){
				nudge=0;
				document.getElementById("nudgech").innerHTML="Nudge On";
				document.getElementById("status").innerHTML = "<strong>Status :</strong> Deactivate Nudge System";
			}	
		}
		
		function WinkState() {     
			if(Wink==0){
				Wink=1;
				document.getElementById("winkch").innerHTML="Winks Off";
				document.getElementById("status").innerHTML = "<strong>Status :</strong> Activated Winks System";
			}
			else if(Wink==1){
				Wink=0;
				document.getElementById("winkch").innerHTML="Winks On";
				document.getElementById("status").innerHTML = "<strong>Status :</strong> Deactivated Winks System";
			}	
		}

        
        function getRequestBody(pForm) {
            var nParams = new Array();
            
            for (var i=0 ; i < pForm.elements.length; i++) {
                var pParam = encodeURIComponent(pForm.elements[i].name);
                pParam += "=";
                pParam += encodeURIComponent(pForm.elements[i].value);
                nParams.push(pParam);
            } 
            
            return nParams.join("&");        
        }
        
        function saveResult(pMessage) {
            var divStatus = document.getElementById("mess");
            divStatus.innerHTML = pMessage;            
        }
		
		function SetBold() {
			var pForm = document.forms[0];        
			if(pForm.boldval.value==1)
			{
			pForm.boldval.value=0;
			pForm.data.style.fontWeight = 'normal';
			document.all.b.style.backgroundColor = '#FFFFFF';
			document.getElementById("status").innerHTML = "<strong>Status :</strong> Set To Unbold Character";
			}
			else
			{
			pForm.boldval.value=1;
			//pForm.data.style.fontWeight = 'bold';
			document.all.b.style.backgroundColor = '#B1B1B1';
			document.getElementById("status").innerHTML = "<strong>Status :</strong> Set To Bold Character";
			}
        }
		
				function SetUnderline() {
			var pForm = document.forms[0];        
			if(pForm.underlineval.value==1)
			{
			pForm.underlineval.value=0;
			pForm.data.style.textDecoration = 'none';
			document.all.u.style.backgroundColor = '#FFFFFF';
			document.getElementById("status").innerHTML = "<strong>Status :</strong> Set To Not Underline Character";
			}
			else
			{
			pForm.underlineval.value=1;
			pForm.data.style.textDecoration = 'underline';
			document.all.u.style.backgroundColor = '#B1B1B1';
			document.getElementById("status").innerHTML = "<strong>Status :</strong> Set To Underline Character";
			}
        }
		
		function playSnd(wav) {
     var MSIE=navigator.userAgent.indexOf("MSIE");
     var NETS=navigator.userAgent.indexOf("Netscape");
     var OPER=navigator.userAgent.indexOf("Opera");
	 
	 if(firsttime ==false){firsttime=true;return "";}
	if(sound ==0){return "";}
	
     if((MSIE>-1) || (OPER>-1)) {
          return "<BGSOUND SRC="+wav+" AUTOSTART=TRUE HIDDEN=true VOLUME=100 LOOP=FALSE>";
     } else {
          return "<EMBED SRC="+wav+" AUTOSTART=TRUE HIDDEN=true VOLUME=100 LOOP=FALSE>";
     }
}

	function History(){
	window.open("history.php#bottom",'','scrollbars=yes,width=610,height=300');
	document.getElementById("status").innerHTML = "<strong>Status :</strong> Show Last 1 Hour History";
	}
	
	function About(){
	document.getElementById("status").innerHTML = "<strong>Status :</strong> Show About Page";
	window.open("about.php",'','scrollbars=no,width=300,height=200');
	}
	
	function InsertEmo(emo){
	var data=document.getElementById("data");
	data.focus();
	data.value = data.value + " " + "[emo=" + emo + "] ";
	select_emo.style.visibility="hidden";
	document.getElementById("status").innerHTML = "<strong>Status :</strong> Insert Emoticons";
	}
	
	function MusicRequest() {
	
		   MusicReq = createXMLHttpRequest();
            MusicReq.open("GET","./now.php", true);
			MusicReq.setRequestHeader( "If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT" );
            MusicReq.onreadystatechange = function () {
                if (MusicReq.readyState == 4) {
                    if (MusicReq.status == 200) {
                        if(MusicReq.responseText!=music){

						music = MusicReq.responseText;
						
						if(music==""){document.getElementById("songshow").innerHTML = "Radio Is Off";}
						else
						{
						document.getElementById("songshow").innerHTML = MusicReq.responseText;
						}
						
						}
                    } 
                }            
            };
            MusicReq.send(null);       
			
			setTimeout("MusicRequest()", 1000);   
        }
        
        function _CloseOnEsc() {
  if (event.keyCode == 27) { window.close(); return; }
}

function Init() {                                                       // run on page load
  document.body.onkeypress = _CloseOnEsc;

  color = window.dialogArguments;
  color = ValidateColor(color) || '000000';
  View(color);                                                          // set default color
}

function View(color) {                  // preview color
  document.all.ColorPreview.style.backgroundColor = '#' + color;
  document.all.ColorHex.value = '#' + color;
}

function LogOut(){

	if(confirm("Confirm To Logout")==true)
	{
		alert("You Are Logged Out!");
		window.location="index.php"; 
	}
}

function Set(string) {                   // select color
  color = ValidateColor(string);
  if (color == null) { alert("Invalid color code: " + string); }        // invalid color
  else {                                                                // valid color
    View(color);                          // show selected color
	document.message.color.value=color;
	select_color.style.visibility="hidden";
	document.message.data.style.color = '#' + color;
	document.all.colors.style.backgroundColor = '#' + color;
	document.getElementById("status").innerHTML = "<strong>Status :</strong> Color Changed.";
  }
}

function ValidateColor(string) {                // return valid color code
  string = string || '';
  string = string + "";
  string = string.toUpperCase();
  chars = '0123456789ABCDEF';
  out   = '';

  for (i=0; i<string.length; i++) {             // remove invalid color chars
    schar = string.charAt(i);
    if (chars.indexOf(schar) != -1) { out += schar; }
  }
    
  if (out.length != 6) { return null; }            // check length
  return out;
} 

	function show_color(){
		if(select_color.style.visibility=="visible")
			{
			select_color.style.visibility="hidden";
			}
		else
			{
			ShowColorReq = createXMLHttpRequest();       
            ShowColorReq.open("GET","./Data.php?action=color", true);
			ShowColorReq.setRequestHeader( "If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT" );
            ShowColorReq.onreadystatechange = function () {
                if (ShowColorReq.readyState == 4) {
                    if (ShowColorReq.status == 200) {
                        document.getElementById("select_color").innerHTML = ShowColorReq.responseText;
                    } else {
                        document.getElementById("select_color").innerHTML = ShowColorReq.statusText;
                    }
                }            
            };
            ShowColorReq.send(null);      
			select_color.style.visibility="visible";
			}
	}
	
		function show_emo(){
		if(select_emo.style.visibility=="visible")
			{
			select_emo.style.visibility="hidden";
			}
		else
			{
			ShowEmoReq = createXMLHttpRequest();       
            ShowEmoReq.open("GET","./Data.php?action=emotable", true);
			ShowEmoReq.setRequestHeader( "If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT" );
            ShowEmoReq.onreadystatechange = function () {
                if (ShowEmoReq.readyState == 4) {
                    if (ShowEmoReq.status == 200) {
                        document.getElementById("select_emo").innerHTML = ShowEmoReq.responseText;
                    } else {
                        document.getElementById("select_emo").innerHTML = ShowEmoReq.statusText;
                    }
                }            
            };
            ShowEmoReq.send(null);      
			select_emo.style.visibility="visible";
			}
	}
	
			function GroupMode(){
		if(groupchat.style.visibility=="visible")
			{
			groupchat.style.visibility="hidden";
			document.getElementById("status").innerHTML = "<strong>Status :</strong> Close GroupMode Window";
			}
		else
			{
			document.getElementById("status").innerHTML = "<strong>Status :</strong> Open GroupMode Window";
			GroupReq = createXMLHttpRequest();       
            GroupReq.open("GET","./Data.php?action=groupchat", true);
			GroupReq.setRequestHeader( "If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT" );
            GroupReq.onreadystatechange = function () {
                if (GroupReq.readyState == 4) {
                    if (GroupReq.status == 200) {
                        document.getElementById("groupchat").innerHTML = GroupReq.responseText;
                    } else {
                        document.getElementById("groupchat").innerHTML = GroupReq.statusText;
                    }
                }            
            };
            GroupReq.send(null);      
			groupchat.style.visibility="visible";
			}
	}
	
	function CreateGroup(){
	var groupname = "";
	var grouppass = "";
	
	groupchat.style.visibility="hidden";
	if(document.forms[0].group.value =="" || confirm("Please Confirm To Deactivate Your Group")==true)
	{
	document.forms[0].group.value = "";
	groupname = prompt("Input Your Groupname ( Use only a-zA-Z Or 0-9 )");
	grouppass = prompt("Input Your Password( Use only a-zA-Z Or 0-9 )");

			GroupCreateReq = createXMLHttpRequest();       
            GroupCreateReq.open("GET","./Data.php?action=creategroup&groupname=" + groupname + "&grouppass=" + grouppass, true);
			GroupCreateReq.setRequestHeader( "If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT" );
            GroupCreateReq.onreadystatechange = function () {
                if (GroupCreateReq.readyState == 4) {
                    if (GroupCreateReq.status == 200) {
                        if(GroupCreateReq.responseText == 1)
						{
						document.getElementById("status").innerHTML = "<strong>Status :</strong> Your Group Is Already Created.";
						alert("Group Created! Your Can Join This Group Now");
						}
						else
						{
						alert("Group Is Not Create : Please Use Only a-z Or  0-9 At most 10 Character Or You 're not Input Groupname Or Password");
						}
                    }
                }            
            };
            GroupCreateReq.send(null);      
	}

	}
	
	function SelectGroup(){
	
	var groupname = document.forms[0].logingroupname.value;
	var grouppass = document.forms[0].logingrouppass.value;
	var pForm = document.forms[0];
	var data = "";
    var pBody = getRequestBody(pForm);    
	document.getElementById("status").innerHTML = "<strong>Status :</strong> Selecting Group";
	

	SelGroup = createXMLHttpRequest();  
	
	if(document.forms[0].sendto.value =="" || confirm("Confirm To Deactivate  Whisper Mode" + document.forms[0].sendto.value)==true)
	{
		document.forms[0].sendto.value ="";
		if(document.forms[0].group.value =="" || confirm("Please Confirm To Deactivate Your Group")==true)
		{

				SelGroup.open("post", "Data.php?action=checkgroup", true);
	            SelGroup.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	            SelGroup.send(pBody); 
				
				SelGroup.onreadystatechange = function () {
	                if (SelGroup.readyState == 4) {
	                    if (SelGroup.status == 200) {

					                    if(SelGroup.responseText==0)
					                    {
					                    document.getElementById("status").innerHTML = "<strong>Status :</strong> Cannot Join This Group";
										alert("Password Incorrect : Cannot Join This Group");
										groupchat.style.visibility="hidden";
					                    }
					                    else
					                    {
												document.getElementById("status").innerHTML = "<strong>Status :</strong> Group Joined";
												data = SelGroup.responseText.split("|#|");
												document.forms[0].group.value = data[0];
												document.forms[0].sendto.value = "";
												alert("You 're In Group : " + data[0] + " Already");

												var _sendtoshow ="<strong>Message To</strong><br><img src=\"./images/buttons/groupchat.gif\"> " + document.forms[0].group.value + " ( <u><a target=\"_parent\" onClick=\"SetSendTo('all')\" onMouseOver=\"this.style.cursor='hand';this.style.color='#000000'\" onMouseOut=\"this.style.color='#154062'\" title=\"Click Here To Back To Public Mode\">Cancel</a></u> )";
												document.getElementById("sendtoshow").innerHTML = $.find(_sendtoshow);
												// document.getElementById("sendtoshow").innerHTML = "<strong>Message To</strong><br><img src=\"./images/buttons/groupchat.gif\"> " + document.forms[0].group.value + " ( <u><a target=\"_parent\" onClick=\"SetSendTo('all')\" onMouseOver=\"this.style.cursor='hand';this.style.color='#000000'\" onMouseOut=\"this.style.color='#154062'\" title=\"Click Here To Back To Public Mode\">Cancel</a></u> )";

												groupchat.style.visibility="hidden";
												
										}
										
									
						}
					}
				}
						
		}
	}
	

	}
	
	function GroupList(){
	
	groupchat.style.visibility="hidden";
	
			document.getElementById("status").innerHTML = "<strong>Status :</strong> Open GroupMode Window";
			GroupListReq = createXMLHttpRequest();       
            GroupListReq.open("GET","./Data.php?action=grouplist", true);
			GroupListReq.setRequestHeader( "If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT" );
            GroupListReq.onreadystatechange = function () {
                if (GroupListReq.readyState == 4) {
                    if (GroupListReq.status == 200) {
					document.getElementById("groupchat").innerHTML = GroupListReq.responseText;
					groupchat.style.visibility="visible";
                    }
                }            
            };
            GroupListReq.send(null);      

	}
	
	
	function wink_select(){
		if(select_wink.style.visibility=="visible")
			{
			select_wink.style.visibility="hidden";
			}
		else
			{
			ShowWinkReq = createXMLHttpRequest();       
            ShowWinkReq.open("GET","./Data.php?action=wink", true);
			ShowWinkReq.setRequestHeader( "If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT" );
            ShowWinkReq.onreadystatechange = function () {
                if (ShowWinkReq.readyState == 4) {
                    if (ShowWinkReq.status == 200) {
                        document.getElementById("select_wink").innerHTML = ShowWinkReq.responseText;
                    } else {
                        document.getElementById("select_wink").innerHTML = ShowWinkReq.statusText;
                    }
                }            
            };
            ShowWinkReq.send(null);      
			select_wink.style.visibility="visible";
			}
	}

function shakeScreen(n)
{ 
  var i,j; 
  if(top.moveBy)
  { 
    for(i=10; i>0; i--)
      for(j=n; j>0; j--)
	  {
        top.moveBy(0,i); 
        top.moveBy(i,0); 
        top.moveBy(0,-i); 
        top.moveBy(-i,0); 
      } 
  } 
} 

function showWink(wink) {

if(Wink==1)
{
  document.getElementById("wink_show").innerHTML = "<object classid=\"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000\" codebase=\"http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,29,0\" width=\"500\" height=\"500\"><param name=\"LOOP\" value=\"false\"><param name=\"movie\" value=\"winks/" + wink + ".swf\"><PARAM NAME=\"WMode\" VALUE=\"Transparent\"><param name=\"quality\" value=\"high\"><embed src=\"winks/" + wink + ".swf\" quality=\"high\" pluginspage=\"http://www.macromedia.com/go/getflashplayer\" type=\"application/x-shockwave-flash\" width=\"500\" height=\"500\" wmode=\"transparent\"/></embed></object>";
  wink_show.style.visibility="visible";
  setTimeout('document.getElementById("wink_show").innerHTML ="";wink_show.style.visibility="hidden"',8000);
  }
  
}

    function Initial() {
	var pForm = document.forms[0];    
	name = document.forms[0].nname.value;
	document.message.color.value="#000000";
	document.all.colors.style.backgroundColor = '#FFFFFF';
	MessageRequest(1);
	OnlineRequest();
	UpdateOnline();
	MusicRequest();
	setTimeout('OnlineCheck()',10000);
	}
	
