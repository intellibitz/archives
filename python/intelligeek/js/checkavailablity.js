var request = null;
function createRequest() {
  try {
    request = new XMLHttpRequest();
    //alert('inside java');
  } catch (trymicrosoft) {
    try {
      request = new ActiveXobject("Msxml2.XMLHTTP");
    } catch (othermicrosoft) {
      try {
        request = new ActiveXobject("Microsoft.XMLHTTP");
      } catch (failed) {
        request = null;
      }
    }
  }
  if (request == null) {
    //alert("Error creating Request Object");
    document.write("error creating request");
  }
}

function updateTextbox() {
  //var url = "../php/checkavailablity.py";
  var url = "../register/checkavailablity.py";
  createRequest();

  var username = document.getElementById("username1").value;

  request.open("POST", url, true);

  //alert(url +'?user='+ escape(username));
  request.onreadystatechange = handleResponse;
  //alert("user=" +escape (usernamee));
  request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
  request.send("user=" + escape(username));
}

function handleResponse() {
  console.log("%s", request.readyState);
  if (request.readyState == 4) {
    var results = request.responseText;

    document.getElementById("errormsg").innerHTML = results;
    //document.getElementById('errorlabel').value= request.responseText;

    //document.getElementById ('response').innerHTML = request.responseText;
    //alert('hello');

    //alert('nsidehandle');

    //alert (results);

    if (results == "Username Already Exist") {
      document.getElementById("username1").value = "";
      document.getElementById("username1").focus();
    }
  }
}
