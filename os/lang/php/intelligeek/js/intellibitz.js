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
////////////////////////////////////////////////////////////
var client_ans = new Array();
function collect_ans(x, y) {
  client_ans[x] = y;
  //we start the array value from one so alert start with  , dont worry
  //alert (client_ans);
}

/////////////////////////////////////////////////////////////
var total_score = null;
var total_question = null;
function check_ans(s) {
  ss = s.split("&");
  //removing the first element its empty
  client_ans.splice(0, 1);
  var counter = 0;
  for (i = 0; i < client_ans.length; i++) {
    if (ss[i] == client_ans[i]) {
      counter++;
    }
  }

  total_score += counter;
  total_question += ss.length;
}
//this sel_tag is in use by get metod query
var sel_tag = 2;
function no_of_questions(newLoc) {
  sel_tag = newLoc.options[newLoc.selectedIndex].value;
}

/*two images kept one after another, so the second image width is suntractred from first img with 
  now the are like statring from the same point
*/

function score_board() {
  var score = "<h3>Your Score </h3>";
  score += "<p>";
  score +=
    '<img src="../images/score.gif" title="' +
    total_score +
    '" width="' +
    total_score +
    'px" height="25px" alt="score"/>';
  score +=
    '<img src="../images/tab.jpg" title="' +
    total_question +
    '" width="' +
    (total_question - total_score) +
    'px" height="25px"/>&nbsp;';

  var percent = Math.round((total_score / total_question) * 100);

  score +=
    '<input type=button value ="' + percent + '%" class="field_1" />&nbsp;';
  score +=
    '<input type=button value ="' +
    total_score +
    " / " +
    total_question +
    '" class="field_1" />';
  score += "</p>";
  document.getElementById("score").innerHTML = score;
}
