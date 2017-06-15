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
  constructor(desc, imgPath, type, value, unity) {
    this.desc = desc;
    this.imgPath = imgPath;
    this.type = type;
    this.value = value;
    this.unity = unity;
  }
}

class Patient {
  constructor(id, p_res) {
    this.id = id;
    this.resources = new Array();
    for(var i = 0; i < N_RES_PAT; i++)
      this.resources[i] = new Resource(p_res[i].desc, p_res[i].imgPath,
                                p_res[i].type, p_res[i].value, p_res[i].unity);
  }
}

class Room {
  constructor(id, r_res) {
    this.id = id;
    this.resources = new Array();
    for(var i = 0; i < N_RES_ROOM; i++)
      this.resources[i] = new Resource(r_res[i].desc, r_res[i].imgPath,
                                r_res[i].type, r_res[i].value, r_res[i].unity);
  }
}

/*
* Array delle risorse che sono disponibile per ogni paziente. Si assume di
* conoscerle a priori per esigenze di progetto
*/
p_res = new Array();
p_res[0] = new Resource("Heart Rate", "images/hr.png", "S", 60, "hpm");
p_res[1] = new Resource("Oxygen Pressure", "images/oxySens.png", "S", 90, "%");
p_res[2] = new Resource("Temperature", "images/temperature.png", "S", 34, "°C");
p_res[3] = new Resource("Led", "images/led.png", "A", "Off", null);
p_res[4] = new Resource("Oxygen Valve", "images/oxyValv.png", "A", "Off", null);

/*
* Array delle risorse che sono disponibile per ogni stanza. Si assume di
* conoscerle a priori per esigenze di progetto
*/
r_res = new Array();
r_res[0] = new Resource("Air Conditioner", "images/hr.png", "A", "Off", null);
r_res[1] = new Resource("Fire Alarm", "images/oxySens.png", "A", "Off", null);
r_res[2] = new Resource("Temperature", "images/temperature.png", "S", 20, "°C");

patArray[0] = new Patient(8, p_res);
patArray[1] = new Patient(20, p_res);

roomArray[0] = new Room(4, r_res);

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
  //alert("Message: " + evt.data);
  var resp = JSON.parse(evt.data);
  switch (resp.id) {
    case 1:{
      for (var loc in resp.payload){
        patArray[P_NUM] = new Patient(resp.payload[loc].e, p_res);
        P_NUM++;
      }
      break;
    }
    case 2:{
      for (var loc in resp.payload){
        roomArray[R_NUM] = new Room(resp.payload[loc].e, r_res);
        R_NUM++;
      }
      break;
    }
    case 3:{
      alert("Message: " + evt.data);
      break;
    }
    case 4:{
      alert("Message: " + evt.data);
      break;
    }
    case 5:{
      alert("Message: " + evt.data);
      break;
    }
    case 6:{
      alert("Message: " + evt.data);
      break;
    }
    case 7:{
      alert("Message: " + evt.data);
      break;
    }
    case 8:{
      alert("Message: " + evt.data);
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

  var N, NR, stringId;
  var tempArray = new Array();

  if(type == "P"){
    tempArray = patArray;
    N = P_NUM;
    NR = N_RES_PAT;
    stringId = "patient";
  } else {
    tempArray = roomArray;
    N = R_NUM;
    NR = N_RES_ROOM;
    stringId = "room";
  }

  var el = document.getElementById(stringId + id);

  var row = document.createElement("div");
  row.setAttribute("class", "row");

  for(var i = 0; i < NR; i++){
    var column = document.createElement("div");
    column.setAttribute("class", "col-sm-6 col-md-4");

    var thumbnail = document.createElement("div");
    thumbnail.setAttribute("class", "thumbnail");

    var img = document.createElement("img");
    img.setAttribute("src", tempArray[index].resources[i].imgPath);
    img.setAttribute("alt", "");

    var caption = document.createElement("div");
    caption.setAttribute("class", "caption");

    var h2 = document.createElement("h2");
    h2.innerHTML = tempArray[index].resources[i].desc;

    var h3 = document.createElement("h3");
    if(patArray[index].resources[i].type == "S")
      h3.innerHTML = tempArray[index].resources[i].value + " " + tempArray[index].resources[i].unity;
    else
      h3.innerHTML = tempArray[index].resources[i].value;

    var p1 = document.createElement("p");

    var button = document.createElement("button");
    button.setAttribute("type", "button");
    button.setAttribute("class", "btn btn-primary");
    button.setAttribute("data-toggle", "modal");
    button.setAttribute("data-target", "#myModal");
    button.setAttribute("onclick", "graphs("+ index + ", " + i + ", " + "'P')");
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
  }
  el.appendChild(row);
};

/*
* Funzione invocata con l'evento "onclick" sulla voce del menu "Paziente"
* La funzione prevede l'invio al WebServer di un messaggio per richiedere
* l'invio dell'ultimo valore disponibile per ogni sensore e attuatore in modo
* da poter mostrare tramite l'intefaccia utente lo stato attuale del paziente
* selezionato.
*/
function createTab(type){
  var last_value = "{'id':8, 'type':'p', 'id_ent':1, 'res_name':'all'}";
  //ws.send(last_value);
  /*
  HRS
  LedA
  OxyValv
  Temp
  OxyS
  */
  //ws.send();

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
function graphs(pr_index, res_index, type){
  var tempArray = new Array();
  var el = document.getElementById("modal-body");
  var div = document.createElement("div");
  if(type == "P")
    tempArray = patArray;
  else
    tempArray = roomArray;

  document.getElementById("myModalLabel").innerHTML = tempArray[pr_index].resources[res_index].desc;
  div.setAttribute("id", "chartContainer1");
  el.appendChild(div);

  var chart = new CanvasJS.Chart("chartContainer1", {
    theme: "theme2",
    //title: {
      //text: "Earthquakes - per month"
    //},
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
      /*[
      { x: new Date(2012, 00, 1), y: 450 },
      { x: new Date(2012, 01, 1), y: 414 },
      { x: new Date(2012, 02, 1), y: 520, indexLabel: "highest", markerColor: "red", markerType: "triangle" },
      { x: new Date(2012, 03, 1), y: 460 },
      { x: new Date(2012, 04, 1), y: 450 },
      { x: new Date(2012, 05, 1), y: 500 },
      { x: new Date(2012, 06, 1), y: 480 },
      { x: new Date(2012, 07, 1), y: 480 },
      { x: new Date(2012, 08, 1), y: 410, indexLabel: "lowest", markerColor: "DarkSlateGrey", markerType: "cross" },
      { x: new Date(2012, 09, 1), y: 500 },
      { x: new Date(2012, 10, 1), y: 480 },
      { x: new Date(2012, 11, 1), y: 510 }
     ]*/
    }
    ]
  });

  chart.render();

  /*var chart = new CanvasJS.Chart("chartContainer2", {
    title: {
      text: "Share Value over a Year"
    },
    theme: "theme2",
    animationEnabled: true,
    axisX: {
      valueFormatString: "MMM"
    },
    axisY: {
      valueFormatString: "#0$"
    },
    data: [{
      type: "line",
      dataPoints: [
      { x: new Date(2012, 01, 1), y: 71, indexLabel: "gain", markerType: "triangle", markerColor: "#6B8E23", markerSize: 12 },
      { x: new Date(2012, 02, 1), y: 55, indexLabel: "loss", markerType: "cross", markerColor: "tomato", markerSize: 12 },
      { x: new Date(2012, 03, 1), y: 50, indexLabel: "loss", markerType: "cross", markerColor: "tomato", markerSize: 12 },
      { x: new Date(2012, 04, 1), y: 65, indexLabel: "gain", markerType: "triangle", markerColor: "#6B8E23", markerSize: 12 },
      { x: new Date(2012, 05, 1), y: 85, indexLabel: "gain", markerType: "triangle", markerColor: "#6B8E23", markerSize: 12 },
      { x: new Date(2012, 06, 1), y: 68, indexLabel: "loss", markerType: "cross", markerColor: "tomato", markerSize: 12 },
      { x: new Date(2012, 07, 1), y: 28, indexLabel: "loss", markerType: "cross", markerColor: "tomato", markerSize: 12 },
      { x: new Date(2012, 08, 1), y: 34, indexLabel: "gain", markerType: "triangle", markerColor: "#6B8E23", markerSize: 12 },
      { x: new Date(2012, 09, 1), y: 24, indexLabel: "loss", markerType: "cross", markerColor: "tomato", markerSize: 12 }
      ]
    }
    ]
  });

  chart.render();

  //var chart = new CanvasJS.Chart("chartContainer3", {
    title: {
      text: "Site Traffic",
      fontSize: 30
    },
    animationEnabled: true,
    axisX: {

      gridColor: "Silver",
      tickColor: "silver",
      valueFormatString: "DD/MMM"

    },
    toolTip: {
      shared: true
    },
    theme: "theme2",
    axisY: {
      gridColor: "Silver",
      tickColor: "silver"
    },
    legend: {
      verticalAlign: "bottom",
      horizontalAlign: "center"
    },
    data: [{
      type: "line",
      showInLegend: true,
      lineThickness: 2,
      name: "Visits",
      markerType: "square",
      color: "#F08080",
      dataPoints: [
      { x: new Date(2010, 0, 3), y: 650 },
      { x: new Date(2010, 0, 5), y: 700 },
      { x: new Date(2010, 0, 7), y: 710 },
      { x: new Date(2010, 0, 9), y: 658 },
      { x: new Date(2010, 0, 11), y: 734 },
      { x: new Date(2010, 0, 13), y: 963 },
      { x: new Date(2010, 0, 15), y: 847 },
      { x: new Date(2010, 0, 17), y: 853 },
      { x: new Date(2010, 0, 19), y: 869 },
      { x: new Date(2010, 0, 21), y: 943 },
      { x: new Date(2010, 0, 23), y: 970 }
      ]
    },
    {
      type: "line",
      showInLegend: true,
      name: "Unique Visits",
      color: "#20B2AA",
      lineThickness: 2,

      dataPoints: [
      { x: new Date(2010, 0, 3), y: 510 },
      { x: new Date(2010, 0, 5), y: 560 },
      { x: new Date(2010, 0, 7), y: 540 },
      { x: new Date(2010, 0, 9), y: 558 },
      { x: new Date(2010, 0, 11), y: 544 },
      { x: new Date(2010, 0, 13), y: 693 },
      { x: new Date(2010, 0, 15), y: 657 },
      { x: new Date(2010, 0, 17), y: 663 },
      { x: new Date(2010, 0, 19), y: 639 },
      { x: new Date(2010, 0, 21), y: 673 },
      { x: new Date(2010, 0, 23), y: 660 }
      ]
    }
    ],
  });

  chart.render();

  var chart = new CanvasJS.Chart("chartContainer4", {
    zoomEnabled: false,
    animationEnabled: true,
    title: {
      text: "Mobile Phone Subscriptions"
    },
    axisY2: {
      valueFormatString: "0.0 bn",

      maximum: 1.2,
      interval: .2,
      interlacedColor: "#F5F5F5",
      gridColor: "#D7D7D7",
      tickColor: "#D7D7D7"
    },
    theme: "theme2",
    toolTip: {
      shared: true
    },
    legend: {
      verticalAlign: "bottom",
      horizontalAlign: "center",
      fontSize: 15,
      fontFamily: "Lucida Sans Unicode"
    },
    data: [{
      type: "line",
      lineThickness: 3,
      axisYType: "secondary",
      showInLegend: true,
      name: "India",
      dataPoints: [
      { x: new Date(2001, 0), y: 0 },
      { x: new Date(2002, 0), y: 0.001 },
      { x: new Date(2003, 0), y: 0.01 },
      { x: new Date(2004, 0), y: 0.05 },
      { x: new Date(2005, 0), y: 0.1 },
      { x: new Date(2006, 0), y: 0.15 },
      { x: new Date(2007, 0), y: 0.22 },
      { x: new Date(2008, 0), y: 0.38 },
      { x: new Date(2009, 0), y: 0.56 },
      { x: new Date(2010, 0), y: 0.77 },
      { x: new Date(2011, 0), y: 0.91 },
      { x: new Date(2012, 0), y: 0.94 }
      ]
    },
    {
      type: "line",
      lineThickness: 3,
      showInLegend: true,
      name: "China",
      axisYType: "secondary",
      dataPoints: [
      { x: new Date(2001, 00), y: 0.18 },
      { x: new Date(2002, 00), y: 0.2 },
      { x: new Date(2003, 0), y: 0.25 },
      { x: new Date(2004, 0), y: 0.35 },
      { x: new Date(2005, 0), y: 0.42 },
      { x: new Date(2006, 0), y: 0.5 },
      { x: new Date(2007, 0), y: 0.58 },
      { x: new Date(2008, 0), y: 0.67 },
      { x: new Date(2009, 0), y: 0.78 },
      { x: new Date(2010, 0), y: 0.88 },
      { x: new Date(2011, 0), y: 0.98 },
      { x: new Date(2012, 0), y: 1.04 }
      ]
    },
    {
      type: "line",
      lineThickness: 3,
      showInLegend: true,
      name: "USA",
      axisYType: "secondary",
      dataPoints: [
      { x: new Date(2001, 00), y: 0.16 },
      { x: new Date(2002, 0), y: 0.17 },
      { x: new Date(2003, 0), y: 0.18 },
      { x: new Date(2004, 0), y: 0.19 },
      { x: new Date(2005, 0), y: 0.20 },
      { x: new Date(2006, 0), y: 0.23 },
      { x: new Date(2007, 0), y: 0.261 },
      { x: new Date(2008, 0), y: 0.289 },
      { x: new Date(2009, 0), y: 0.3 },
      { x: new Date(2010, 0), y: 0.31 },
      { x: new Date(2011, 0), y: 0.32 },
      { x: new Date(2012, 0), y: 0.33 }
      ]
    }
    ],
    legend: {
      cursor: "pointer",
      itemclick: function (e) {
        if (typeof (e.dataSeries.visible) === "undefined" || e.dataSeries.visible) {
          e.dataSeries.visible = false;
        }
        else {
          e.dataSeries.visible = true;
        }
        chart.render();
      }
    }
  });

  chart.render();
  */
};

/*
* Gestisce le azioni che vengono effettuate al caricamento della pagina.
* Si occupa di gestire le richieste per scoprire:
*   - quanti e chi sono i pazienti
*   - quante e quali sono le stanze
*/
function show(){
/*
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
*/

  document.getElementById("Patient").style.display="none";
  document.getElementById("Room").style.display="none";
};
