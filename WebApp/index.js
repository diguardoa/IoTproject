var ws = new WebSocket("ws://127.0.0.1:8100/");
var P_NUM = 0;
var paz_array = new Array();
var R_NUM = 0;
var room_array = new Array();

var ready_to_show = 2;

//Messaggio di prova per codifica json
var text = '{"name":"John","birth":"1986-12-14","city":"New York"}';

var all_value = "{'id':3, 'type':'p', 'id_ent':1, 'res_name':'OxyS'}";
var set_value = "{'id':5, 'type':'p', 'id_ent':1, 'res_name':'OxyS', 'value':800}";

ws.onopen = function() {
  //alert("Opened!");
  //ws.send("Hello Server");

};

ws.onmessage = function (evt) {
  //alert("Message: " + evt.data);
  var resp = JSON.parse(evt.data);
  switch (resp.id) {
    case 1:
      for (var loc in resp.payload) {
        paz_array[P_NUM] = resp.payload[loc].e;
        P_NUM++;
        //alert("P " + resp.payload[loc].e);
      }
      ready_to_show--;
      break;
   
    case 2:
      for (var loc in resp.payload) {
        room_array[R_NUM] = resp.payload[loc].e;
        R_NUM++;
        //alert("R " + resp.payload[loc].e);
      }
      ready_to_show--;
      break;
    case 3:
      alert("Message: " + evt.data);
      break;


    case 8:
      alert("Message: " + evt.data);
      break;
  }
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
  var last_value = "{'id':8, 'type':'p', 'id_ent':1, 'res_name':'all'}";
  ws.send(last_value);
  /*
  HRS
  LedA
  OxyValv
  Temp
  OxyS
  */
  //ws.send();
};

function searchRoom(){
  for (var i=0; i < R_NUM; i++)   {
    var last_value = "{'id':8, 'type':'r', 'id_ent':" + room_array[i] + ", 'res_name':'all'}";
    ws.send(last_value);
  }
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
  
  
  setTimeout(function() {
  var WhatPatients = "{'id':1}";
  ws.send(WhatPatients);
  var WhatRooms = "{'id':2}";
  ws.send(WhatRooms);

  ws.send(set_value);
  }, 200);

  setTimeout(function() {
    showPatient();
    showRoom();
  }, 500);

  setTimeout(function() {
    ws.send(all_value);
  }, 1000);
}
