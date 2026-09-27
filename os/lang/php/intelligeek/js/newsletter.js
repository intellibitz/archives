//clear the text box content
function clear_text_box(c) {
  document.getElementById(c).value = "";
}

//news letter
function news_letter() {
  email_validation("email_for_news_letter", "message_1");
}

// email validation - i- text box div -b - message box div
function email_validation_submit(i, b) {
  var email = document.getElementById(i).value;
  //alert (email);
  if (
    /^[_a-zA-Z0-9-]+(\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\.[a-zA-Z0-9-]+)*\.(([0-9]{1,3})|([a-zA-Z]{2,3})|(aero|coop|info|museum|name))$/.test(
      email,
    )
  ) {
    // on receiving proper mail id send mail to mailing list
    var url = "../newsletter/registerpage.php?e_mail=" + email;
    newsrequest(url, b);
  } else {
    document.getElementById(b).innerHTML = "Please verify the mail address";
    document.getElementById(i).value = "";
    document.getElementById(i).focus();
  }
}

//ajax request
function newsrequest(a, id) {
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
