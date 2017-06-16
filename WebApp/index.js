/*
* Le variabili seguenti servono per memorizzare il numero di pazienti e di
* stanze presenti nell'ospedale. Per ogni paziente e per ogni stanza si
* memorizza inoltre il numero di risorse massime.
*/
var P_NUM = 0;
var R_NUM = 0;
var N_RES_PAT = 5;
var N_RES_ROOM = 3;

/*
* I pazienti e le stanze presenti nell'ospedale vengono memorizzati
* dinamicamente in due array separati dopo aver inviato una richiesta al Web
* Server.
*/
var patArray = new Array();
var roomArray = new Array();

var value_graphs = [{ x: new Date(2012, 00, 1), y: 450 },
      { x: new Date(2012, 01, 1), y: 414 },
      { x: new Date(2012, 02, 1), y: 520 },
      { x: new Date(2012, 03, 1), y: 460 },
      { x: new Date(2012, 04, 1), y: 450 },
      { x: new Date(2012, 05, 1), y: 500 },
      { x: new Date(2012, 06, 1), y: 480 },
      { x: new Date(2012, 07, 1), y: 480 },
      { x: new Date(2012, 08, 1), y: 410 },
      { x: new Date(2012, 09, 1), y: 500 },
      { x: new Date(2012, 10, 1), y: 480 },
      { x: new Date(2012, 11, 1), y: 510 }];

class Resource {
  constructor(desc, imgPath, type, value, unity, time) {
    this.desc = desc;
    this.imgPath = imgPath;
    this.type = type;
    this.value = value;
    this.unity = unity;
    this.time = time;
    this.history = 0;
  }

  setValue(value, time){
    this.value = value;
    this.time = time;
  }

  saveHistory(history){
    this.history = history;
  }
}

/*
* Si assume di conoscerle a priori le risorse per il paziente per esigenze
* di progetto
*/
class Patient {
  constructor(id) {
    this.id = id;
    this.ledA = new Resource("Led", "images/led.png", "A", null, null, null);
    this.temp = new Resource("Temperature", "images/temperature.png", "S", null, "°C", null);
    this.oxyValv = new Resource("Oxygen Valve", "images/oxyValv.png", "A", null, null, null);
    this.oxyS = new Resource("Oxygen Pressure", "images/oxySens.png", "S", 90, "%", null);
    this.hrs = new Resource("Heart Rate", "images/hr.png", "S", 60, "hpm", null);
  }
}

/*
* Si assume di conoscerle a priori le risorse per la stanza per esigenze
* di progetto
*/
class Room {
  constructor(id) {
    this.id = id;
    this.tempR = new Resource("Temperature", "images/temperature.png", "S", null, "°C", null);
    this.airCon = new Resource("Air Conditioner", "images/hr.png", "A", null, null, null);
    this.fireAl = new Resource("Fire Alarm", "images/oxySens.png", "A", null, null, null);
  }
}

var all_value = "{'id':3, 'type':'p', 'id_ent':1, 'res_name':'OxyS'}";
var set_value = "{'id':5, 'type':'p', 'id_ent':1, 'res_name':'OxyS', 'value':800}";

/*
* La comunicazione tra webApp e WebServer è gestita tramite una WebSocket
* questa viene implementata con i quattro metodi seguenti:
*   - onopen
*   - onclose
*   - onerror
*   - onmessage
* Le prime tre gestiscono le azioni rispettivamente all'apertura, chiusura e al
* verificarsi di errori.
* La onmessage è quella che si occupa di decoficiare il messaggio ricevuto e
* invocare quindi le funzioni desiderate.
* L'invio di messaggi al server avviane tramite la funzione "send()"
*/
var ws = new WebSocket("ws://127.0.0.1:8100/");

ws.onopen = function() {
  var el = document.getElementById("title");
  el.innerHTML += " Connected";
};

ws.onmessage = function (evt) {
  var resp = JSON.parse(evt.data);
  switch (resp.id) {
    case 1:{
      for (var loc in resp.payload){
        patArray[P_NUM] = new Patient(resp.payload[loc].e);
        P_NUM++;
      }
      //alert("Pats: " + P_NUM);
      break;
    }
    case 2:{
      for (var loc in resp.payload){
        roomArray[R_NUM] = new Room(resp.payload[loc].e);
        R_NUM++;
      }
      //alert("Rooms: " + R_NUM);
      break;
    }
    case 3:{ //todo
      alert("Message: " + evt.data);
      break;
    }
    case 4:{ //todo
      alert("Message: " + evt.data);
      break;
    }
    case 5:{ //todo
      alert("Message: " + evt.data);
      break;
    }
    case 6:{ //todo
      alert("Message: " + evt.data);
      break;
    }
    case 7:{ //todo
      alert("Message: " + evt.data);
      break;
    }
    case 8:{
      if(resp.type == "p"){
        var i = 0;
        while(patArray[i].id != resp.id_ent)
          i++;
        patArray[i].hrs.setValue(resp.HRS.e, resp.HRS.t);
        patArray[i].temp.setValue(resp.Temp.e, resp.Temp.t);
        patArray[i].oxyValv.setValue(resp.OxyValv.e, resp.OxyValv.t);
        patArray[i].oxyS.setValue(resp.OxyS.e, resp.OxyS.t);
        patArray[i].ledA.setValue(resp.LedA.e, resp.LedA.t);
      } else{
        var i = 0;
        while(roomArray[i].id != resp.id_ent)
          i++;
        roomArray[i].fireAl.setValue(resp.FireAl.e, resp.FireAl.t);
        roomArray[i].tempR.setValue(resp.TempR.e, resp.TempR.t);
        roomArray[i].airCon.setValue(resp.AirCon.e, resp.AirCon.t);
      }
      createTab();
      break;
    }
  }
};

ws.onclose = function() {
  var el = document.getElementById("title");
  el.innerHTML += " Disconnected";
};

ws.onerror = function(err) {
  alert("Error: " + err);
};

/*
* Funzione invocata con l'evento "onclick" sulla voce del menu "Paziente"
* La funzione prevede l'invio al WebServer di un messaggio per richiedere
* l'invio dell'ultimo valore disponibile per ogni sensore e attuatore in modo
* da poter mostrare tramite l'intefaccia utente lo stato attuale del paziente
* selezionato.
*/
function sendCreateTab(type){
  if(type == "P"){
    setTimeout(function() {
      for(var i = 0; i < P_NUM; i++){
        var last_value = "{'id':8, 'type':'p', 'id_ent':" + patArray[i].id + ", 'res_name':'all'}";
        ws.send(last_value);
      }
    }, 50);
  } else{
    setTimeout(function() {
      for(var i = 0; i < R_NUM; i++){
        var last_value = "{'id':8, 'type':'r', 'id_ent':" + roomArray[i].id + ", 'res_name':'all'}";
        ws.send(last_value);
      }
    }, 50);
  }
};

function prova() {
  alert("prova");
};

/*
* Funzione che crea dinamicamente le icone per le risorse. In particolare
* crea un incona contenente il nome della risorsa, una immagine, l'ultimo valore
* acquisito, e due bottoni, uno per visualizzare il grafico e l'altro per
* eliminare lo storico del sensore dall'archivio
*/
function createResourceIcon(stringId, index, id, resource, type, el, row) {

  var column = document.createElement("div");
  column.setAttribute("class", "col-sm-6 col-md-4");

  var thumbnail = document.createElement("div");
  thumbnail.setAttribute("class", "thumbnail");

  var img = document.createElement("img");
  img.setAttribute("src", resource.imgPath);
  img.setAttribute("alt", "");

  var caption = document.createElement("div");
  caption.setAttribute("class", "caption");

  var h2 = document.createElement("h2");
  h2.innerHTML = resource.desc;

  var h3 = document.createElement("h3");
  if(resource.type == "S")
    h3.innerHTML = resource.value + " " + resource.unity;
  else
    h3.innerHTML = resource.value;

  var p1 = document.createElement("p");

  var button = document.createElement("button");
  button.setAttribute("type", "button");
  button.setAttribute("class", "btn btn-primary");
  button.setAttribute("data-toggle", "modal");
  button.setAttribute("data-target", "#myModal");
  //var fun = "graphs(" + resource.desc + ", " + resource.history + ");";
  //alert(fun);
  if(type == "P")
    //button.setAttribute('onclick', "graphs(" + resource.desc + ", " + resource.history + ")");
    button.onclick = function(){graphs(resource.desc, resource.history);};

  button.innerHTML = "Display Graph";

  var a1 = document.createElement("a");
  a1.setAttribute("href", "#");
  a1.setAttribute("class", "btn btn-default");
  a1.setAttribute("role", "button");
  a1.innerHTML = "Delete History";

  p1.appendChild(button);
  p1.appendChild(a1);
  caption.appendChild(h2);
  caption.appendChild(h3);
  caption.appendChild(p1);
  thumbnail.appendChild(img);
  thumbnail.appendChild(caption);
  column.appendChild(thumbnail);
  row.appendChild(column);

  el.appendChild(row);
};

/*
* Funzione utilizzata per generare dinamicamente il contenuto da mostrare
* relativo al paziente selezionato. In particolare andrà a mostrare una serie
* di caselle (una per ogni sensore e attuatore) con la possibilità:
*   - consultare lo stato corrente della risorsa;
*   - graficare l'andamento dello storico
*   - eliminare lo storico
* In ingresso riceve tre parametri:
*   - l'indice del paziente o della stanza all'interno dell'opportuno array
*   - in numero identificativo (il nome) del paziente
*   - "P" o "R" per discriminare se si tratta di un paziente o di una stanza
*/
function buildInterface(index, id, type){

  var stringId;

  if(type == "P"){
    stringId = "patient";
    var el = document.getElementById(stringId + id);
    var row = document.createElement("div");
    row.setAttribute("class", "row");
    createResourceIcon(stringId, index, id, patArray[index].hrs, type, el, row);
    createResourceIcon(stringId, index, id, patArray[index].oxyS, type, el, row);
    createResourceIcon(stringId, index, id, patArray[index].temp, type, el, row);
    createResourceIcon(stringId, index, id, patArray[index].ledA, type, el, row);
    createResourceIcon(stringId, index, id, patArray[index].oxyValv, type, el, row);
  } else {
    stringId = "room";
    var el = document.getElementById(stringId + id);
    var row = document.createElement("div");
    row.setAttribute("class", "row");
    createResourceIcon(stringId, index, id, roomArray[index].tempR, type, el, row);
    createResourceIcon(stringId, index, id, roomArray[index].airCon, type, el, row);
    createResourceIcon(stringId, index, id, roomArray[index].fireAl, type, el, row);
  }
};

function createTab(){
  /*
  * Seleziono i div che andrò a popolare dinamicamente ogni volta che verrà
  * premuto il menu "Paziente".
  * Se esistono già li elimino dal DOM in modo che non mi si crei più di una
  * instanza per volta
  */
  document.getElementById("Patient").style.display="block";
  var el = document.getElementById("Patient");
  var u = document.getElementById("tabPat");
    if(u)
      el.removeChild(u);
  var di = document.getElementById("conPat");
    if(di)
      el.removeChild(di);

  /*
  * Se il numero di pazienti è uguale a 0 allora stampo semplicemente un testo
  * che mi dice che non ci sono pazienti. Altrimenti popolo i div selezionati
  * sopra con una struttura a TAB che prevede una voce per ogni Paziente di cui
  * si è ricevuto l'id dal WebServer.
  * Dopo aver creato la struttura a TAB per ogni paziente viene invocata la
  * funzione "buildInterface()" che andrà a popolare il div relativo al
  * singolo paziente.
  */
  if(P_NUM == 0){
    var div = document.createElement("div");
    div.setAttribute("class", "col-md-4");
    var h3 = document.createElement("h3");
    h3.innerHTML = "Any Patient";
    div.appendChild(h3);
    el.appendChild(div);
  } else {

    var ul = document.createElement("ul");
    ul.setAttribute("class", "nav nav-tabs");
    ul.setAttribute("role", "tablist");
    ul.setAttribute("id", "tabPat");

    var div = document.createElement("div");
    div.setAttribute("class", "tab-content");
    div.setAttribute("id", "conPat");

    for(var i = 0; i < P_NUM; i++){
      var li = document.createElement("li");
      li.setAttribute("role", "presentation");

      var d = document.createElement("div");
      d.setAttribute("role", "tabpanel");
      d.setAttribute("class", "tab-pane");
      var id = patArray[i].id;
      d.setAttribute("id", "patient" + id);

      var a = document.createElement("a");
      a.setAttribute("class", "tab");
      a.setAttribute("href", "#patient" + id);
      a.setAttribute("aria-controls", "patient" + id);
      a.setAttribute("role", "tab");
      a.setAttribute("data-toggle", "tab");
      a.innerHTML = "Patient " + id;

      if(i == 0){
        li.setAttribute("class", "active");
        d.setAttribute("class", "tab-pane active");
      }

      li.appendChild(a);
      ul.appendChild(li);
      div.appendChild(d);

    }
    el.appendChild(ul);
    el.appendChild(div);

    for(var i = 0; i < P_NUM; i++)
      buildInterface(i, patArray[i].id, "P");
  }
};

/*
* Funzione per stampare il grafico all'interno dell'elemento Modal di bootstrap
* prende 3 argomenti in ingresso:
*   - l'indice della stanza o del paziente
*   - l'indice della risorsa di cui si vuole il grafico
*   - "P" o "R" per discriminare tra paziente e stanza
*/
function graphs(desc, value){
  var el = document.getElementById("modal-body");
  var div = document.createElement("div");

  document.getElementById("myModalLabel").innerHTML = desc;
  div.setAttribute("id", "chartContainer1");
  el.appendChild(div);

  var chart = new CanvasJS.Chart("chartContainer1", {
    theme: "theme2",
    animationEnabled: true,
    axisX: {
      valueFormatString: "MMM",
      interval: 1,
      intervalType: "month"

    },
    axisY: {
      includeZero: false

    },
    data: [{
      type: "line",
      //lineThickness: 3,
      dataPoints: value_graphs

    }]
  });

  chart.render();
};

/*
* Gestisce le azioni che vengono effettuate al caricamento della pagina.
* Si occupa di gestire le richieste per scoprire:
*   - quanti e chi sono i pazienti
*   - quante e quali sono le stanze
*/
function findPatientsRooms(){
  document.getElementById("Patient").style.display="none";
  document.getElementById("Room").style.display="none";

  setTimeout(function() {
    var WhatPatients = "{'id':1}";
    ws.send(WhatPatients);
    var WhatRooms = "{'id':2}";
    ws.send(WhatRooms);
  //ws.send(set_value);
  }, 200);

  //setTimeout(function() {
    //ws.send(all_value);
  //}, 1000);
};
