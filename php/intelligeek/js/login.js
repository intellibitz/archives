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

  http_request.onreadystatechange = alertContents1;
  http_request.open("POST", url, true);
  http_request.setRequestHeader(
    "Content-type",
    "application/x-www-form-urlencoded",
  );
  http_request.setRequestHeader("Content-length", parameters.length);
  http_request.setRequestHeader("Connection", "close");
  http_request.send(parameters);
}

function alertContents1() {
  if (http_request.readyState == 4) {
    if (http_request.status == 200) {
      //alert(http_request.responseText);
      result = http_request.responseText;
      if (result == "Improper Login Please Login Again!") {
        document.getElementById("loginmsg").innerHTML = result;
        document.getElementById("username").value = "";
        document.getElementById("password").value = "";
        document.getElementById("username").focus();
      } else {
        document.getElementById("faq").innerHTML = result;
      } // alert (result);
    } else {
      alert("There was a problem with the request.");
    }
  }
}

function getlog(obj) {
  var poststr1 =
    "username=" +
    encodeURI(document.getElementById("username").value) +
    "&password=" +
    encodeURI(document.getElementById("password").value);

  makePOSTRequest("../login/logindb.php", poststr1);
}
