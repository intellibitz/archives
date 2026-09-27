//ajax request
//a - url
// id -pass the id name to return

function ajax(a, id) {
  var http = false;
  if (navigator.appName == "Microsoft Internet Explorer") {
    http = new ActiveXObject("Microsoft.XMLHTTP");
  } else {
    http = new XMLHttpRequest();
  }

  http.open("GET", a, true);
  http.onreadystatechange = function () {
    if (http.readyState == 4) {
      document.getElementById(id).innerHTML = http.responseText;
    }
  };
  http.send(null);
}
