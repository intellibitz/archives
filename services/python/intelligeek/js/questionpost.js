var http_request = false;
function makePOSTRequest(url, parameters) {
  http_request = false;
  if (window.XMLHttpRequest) {
    // Mozilla, Safari,...
    http_request = new XMLHttpRequest();
    if (http_request.overrideMimeType) {
      // set type accordingly to anticipated content type
      //http_request.overrideMimeType('text/xml');
      http_request.overrideMimeType("text/html");
    }
  } else if (window.ActiveXObject) {
    // IE
    try {
      http_request = new ActiveXObject("Msxml2.XMLHTTP");
    } catch (e) {
      try {
        http_request = new ActiveXObject("Microsoft.XMLHTTP");
      } catch (e) {}
    }
  }
  if (!http_request) {
    alert("Cannot create XMLHTTP instance");
    return false;
  }

  http_request.onreadystatechange = alertContents;
  http_request.open("POST", url, true);
  http_request.setRequestHeader(
    "Content-type",
    "application/x-www-form-urlencoded",
  );
  http_request.setRequestHeader("Content-length", parameters.length);
  http_request.setRequestHeader("Connection", "close");
  http_request.send(parameters);
}

function alertContents() {
  if (http_request.readyState == 4) {
    if (http_request.status == 200) {
      //alert(http_request.responseText);
      result = http_request.responseText;
      //alert (result);
      document.getElementById("faq").innerHTML = result;
      //document.getElementById("question").style.visibility='hidden';
      //document.getElementById("email").style.visibility='hidden';
      //document.getElementById("faq").style.visibility='hidden';
    } else {
      alert("There was a problem with the request.");
    }
  }
}

function get(obj) {
  var poststr =
    "question=" +
    encodeURI(document.getElementById("question").value) +
    "&email=" +
    encodeURI(document.getElementById("email").value);
  makePOSTRequest("../askquestion/askquestiondb.py", poststr);
}
function ansget(obj) {
  var poststr =
    "question=" +
    encodeURI(document.getElementById("question").value) +
    "&answer=" +
    encodeURI(document.getElementById("answer").value) +
    "&email=" +
    encodeURI(document.getElementById("email").value);
  makePOSTRequest("../askquestion/answerdb.py", poststr);
}
function getreg(obj) {
  var poststr =
    "username1=" +
    encodeURI(document.getElementById("username1").value) +
    "&password1=" +
    encodeURI(document.getElementById("password1").value) +
    "&email=" +
    encodeURI(document.getElementById("email").value) +
    "&quizmail=" +
    encodeURI(document.getElementById("quizmail").value) +
    "&mobile=" +
    encodeURI(document.getElementById("mobile").value) +
    "&quizsms=" +
    encodeURI(document.getElementById("quizsms").value) +
    "&gender=" +
    encodeURI(document.getElementById("gender").value) +
    "&dob=" +
    encodeURI(document.getElementById("demo1").value);
  // var quizmail=document.getElementById("quizmail").value ;
  makePOSTRequest("../register/registermsg.py", poststr);
  document.getElementById("faq").value = "";
  document.getElementById("username1").value = "";
  document.getElementById("password1").value = "";
  document.getElementById("email").value = "";
  document.getElementById("quizmail").value = "";
  document.getElementById("mobile").value = "";
  document.getElementById("quizsms").value = "";
  document.getElementById("gender").value = "";
  document.getElementById("demo1").value = "";

  // alert (poststr);
  // alert (quizmail);
}

function getupload(obj) {
  var poststr =
    "userfile=" + encodeURI(document.getElementById("userfile").value);
  // "&upload=" + encodeURI( document.getElementById("upload").value );
  //alert (poststr);

  makePOSTRequest("../login/uploaddb.py", poststr);
}
function getforgotpswd(obj) {
  var poststr =
    "emailfp=" + encodeURI(document.getElementById("emailfp").value);
  // "&upload=" + encodeURI( document.getElementById("upload").value );
  //alert (poststr);

  makePOSTRequest("../login/forgotpswddb.py", poststr);
  document.getElementById("email").value = "";
}
function getchangepswd(obj) {
  var poststr =
    "oldpwsd=" +
    encodeURI(document.getElementById("oldpwsd").value) +
    "&newpwsd=" +
    encodeURI(document.getElementById("newpwsd").value);
  //alert (poststr);

  makePOSTRequest("../login/changepswddb.py", poststr);
  document.getElementById("oldpwsd").value = "";
  document.getElementById("newpwsd").value = "";
}
function getonlinename(obj) {
  var poststr =
    "username=" + encodeURI(document.getElementById("username").value);

  //alert (poststr);

  makePOSTRequest("../include/who_is_online.py", poststr);
  //document.getElementById('oldpwsd').value="";
  //document.getElementById('newpwsd').value="";
}
