var ws = new WebSocket("ws://127.0.0.1:8100/");
var P_NUM = 3;
var R_NUM = 2;

//Messaggio di prova per codifica json
var text = '{"name":"John","birth":"1986-12-14","city":"New York"}';

ws.onopen = function() {
  alert("Opened!");
  ws.send("Hello Server");
};

ws.onmessage = function (evt) {
  alert("Message: " + evt.data);
};

ws.onclose = function() {
  printstatus();
  //alert("Closed!");
};

ws.onerror = function(err) {
  alert("Error: " + err);
};

function printstatus(){
  var el = document.getElementById("status");
  el.innerHTML = "Status";
};

function searchPatient(){
  var obj = {name:"John",age:30,city:"New York"};
  var myJSON = JSON.stringify(obj);
  document.getElementById("demo").innerHTML = myJSON;

  var msg = new Object();
  msg.name = "Prova";
  msg.size = "12";
  msg.year = "45";
  var msgJSON = JSON.stringify(msg);
  document.getElementById("demo").innerHTML += msgJSON;
  console.log(JSON.parse(msgJSON));
  //ws.send();
};

function searchRoom(){
  var obj = JSON.parse(text);
  obj.birth = new Date(obj.birth);

  document.getElementById("demo").innerHTML = text + obj.name + ", " + obj.birth;
  //ws.send();
};

function showPatient(){
  var dimColumn = 12/P_NUM;
  var classDimColumn = "col-md-" + dimColumn;
  var el = document.getElementById("Patient");
  for (i = 0; i < P_NUM; i++) {
    var div = document.createElement("div");
    div.setAttribute("class", classDimColumn);
    var h3 = document.createElement("h3");
    h3.innerHTML = "Patient n. " + i;
    div.appendChild(h3);
    var p = document.createElement("p");
    p.setAttribute("class", "lead");
    p.innerHTML = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
    div.appendChild(p);
    el.appendChild(div);
  }
};

function showRoom(){
  var dimColumn = 12/R_NUM;
  var classDimColumn = "col-md-" + dimColumn;
  var el = document.getElementById("Room");
  for (i = 0; i < R_NUM; i++) {
    var div = document.createElement("div");
    div.setAttribute("class", classDimColumn);
    var h3 = document.createElement("h3");
    h3.innerHTML = "Room n. " + i;
    div.appendChild(h3);
    var p = document.createElement("p");
    p.setAttribute("class", "lead");
    p.innerHTML = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
    div.appendChild(p);
    el.appendChild(div);
  }
};

function show(){
  showPatient();
  showRoom();
}
