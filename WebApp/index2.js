/*
* Le variabili seguenti servono per memorizzare il numero di pazienti e di
* stanze presenti nell'ospedale. Per ogni paziente e per ogni stanza si
* memorizza inoltre il numero di risorse massime.
*/
var P_NUM = 0;
var R_NUM = 0;
var N_RES_PAT = 5;
var N_RES_ROOM = 3;

var chart;

var fireON = new Array();

var sendp1;
var sendr1;

var activeP = 1;


/*
* I pazienti e le stanze presenti nell'ospedale vengono memorizzati
* dinamicamente in due array separati dopo aver inviato una richiesta al Web
* Server.
*/
var patArray = new Array();
var roomArray = new Array();

class Resource{

  constructor(desc, name, imgPath, type, value, unity, time) {
    this.desc = desc;
    this.name = name;
    this.imgPath = imgPath;
    this.type = type;
    this.value = value;
    this.unity = unity;
    this.time = time;
    this.history = [];
    this.ledON = 0;

  }

  setValue(value, time){
    this.value = value/10;
    this.time = time;
  }

  saveHistory(payload){
    var hist = [];
    for(var loc in payload){
      var h = parseInt(payload[loc].t.slice(0,2));
      var m = parseInt(payload[loc].t.slice(2,4));
      var s = parseInt(payload[loc].t.slice(4,6));
      var time = (h*3600 + m*60 +s)*1000;
      time = new Date(time);
      hist.push({x: time, y: parseInt(payload[loc].e)/10});
    }
    this.history = hist;
  }

  deleteHistory(){
    this.history = [];
  }
}

/*
* Si assume di conoscerle a priori le risorse per il paziente per esigenze
* di progetto
*/
class Patient {
  constructor(id) {
    this.id = id;
    this.ledA = new Resource("Led", "LedA", "images/led.png", "A", 0, null, null);
    this.temp = new Resource("Temperature", "Temp", "images/temperature.png", "S", 36, "°C", null);
    this.oxyValv = new Resource("Oxygen Valve", "OxyValv", "images/oxyValv.png", "A", 100, "%", null);
    this.oxyS = new Resource("Oxygen Pressure", "OxyS", "images/oxySens.png", "S", 100, "%", null);
    this.hrs = new Resource("Heart Rate", "HRS", "images/hr.png", "S", 80, "bpm", null);
  }
}

/*
* Si assume di conoscerle a priori le risorse per la stanza per esigenze
* di progetto
*/
class Room {
  constructor(id) {
    this.id = id;
    this.tempR = new Resource("Temperature", "TempR", "images/temperature.png", "S", 20, "°C", null);
    this.airCon = new Resource("Air Conditioner", "AirCon", "images/airconditionar.png", "A", 20, "°C", null);
    this.fireAl = new Resource("Fire Alarm", "FireAl", "images/firealarm.png", "A", 0, null, null);
  }
}

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
    case 1:{ //DONE
      for (var loc in resp.payload){
        patArray[P_NUM] = new Patient(resp.payload[loc].e);
        P_NUM++;
      }
      createTab("p");
      break;
    }
    case 2:{ //DONE
      for (var loc in resp.payload){
        roomArray[R_NUM] = new Room(resp.payload[loc].e);
        fireON[R_NUM] = 0;
        R_NUM++;
      }
      createTab("r");
      break;
    }
    case 3:{ //DONE
      var resource;
      if(resp.type == "p"){
        var i = 0;
        for(var i = 0; i < P_NUM; i++){
          if(patArray[i].id == resp.id_ent){
            if(patArray[i].hrs.name == resp.res_name){
              patArray[i].hrs.saveHistory(resp.payload);
              resource = patArray[i].hrs;
            }
            if(patArray[i].temp.name == resp.res_name){
              patArray[i].temp.saveHistory(resp.payload);
              resource = patArray[i].temp;
            }
            if(patArray[i].oxyValv.name == resp.res_name){
              patArray[i].oxyValv.saveHistory(resp.payload);
              resource = patArray[i].oxyValv;
            }
            if(patArray[i].oxyS.name == resp.res_name){
              patArray[i].oxyS.saveHistory(resp.payload);
              resource = patArray[i].oxyS;
            }
            if(patArray[i].ledA.name == resp.res_name){
              patArray[i].ledA.saveHistory(resp.payload);
              resource = patArray[i].ledA;
            }
          }
        }
      } else{
        var i = 0;
        for(var i = 0; i < R_NUM; i++){
          if(roomArray[i].id == resp.id_ent){
            if(roomArray[i].airCon.name == resp.res_name){
              roomArray[i].airCon.saveHistory(resp.payload);
              resource = roomArray[i].airCon;
            }
            if(roomArray[i].tempR.name == resp.res_name){
              roomArray[i].tempR.saveHistory(resp.payload);
              resource = roomArray[i].tempR;
            }
            if(roomArray[i].fireAl.name == resp.res_name){
              roomArray[i].fireAl.saveHistory(resp.payload);
              resource = roomArray[i].fireAl;
            }
          }
        }
      }

    graphs(resource.desc, resource.history, resp.type, resp.id_ent, resp.res_name);
    break;
    }
    case 4:{ //DONE
      if(resp.payload == "done"){
        if(resp.type == "p"){
          var i = 0;
          for(var i = 0; i < P_NUM; i++){
            if(patArray[i].id == resp.id_ent){
              if(patArray[i].hrs.name == resp.res_name)
                patArray[i].hrs.deleteHistory();

              if(patArray[i].temp.name == resp.res_name)
                patArray[i].temp.deleteHistory();

              if(patArray[i].oxyValv.name == resp.res_name)
                patArray[i].oxyValv.deleteHistory();

              if(patArray[i].oxyS.name == resp.res_name)
                patArray[i].oxyS.deleteHistory();

              if(patArray[i].ledA.name == resp.res_name)
                patArray[i].ledA.deleteHistory();

            }
          }
        } else{
          var i = 0;
          for(var i = 0; i < R_NUM; i++){
            if(roomArray[i].id == resp.id_ent){
              if(roomArray[i].airCon.name == resp.res_name)
                roomArray[i].airCon.deleteHistory();

              if(roomArray[i].tempR.name == resp.res_name)
                roomArray[i].tempR.deleteHistory();

              if(roomArray[i].fireAl.name == resp.res_name)
                roomArray[i].fireAl.deleteHistory();
            }
          }
        }
      }
      break;
    }
    case 5:{ //nothing todo
      //alert("Message: " + evt.data);
      //if(functionStep == "error1" || functionStep == "error2"){
        //error(errorCode, errorId);
      //}
      //if(functionStep == "sendSetValue"){
        //sendSetValue(resetType, resetId, resetName);
      //}
      break;
    }
    case 6:{ //nothing todo
      //alert("Message: " + evt.data);
      break;
    }
    case 7:{ //todo
      if(resp.type == "p"){
        for (var i = 0; i < P_NUM; i++) {
          if(patArray[i].id == resp.id_ent){
            var pat = patArray[i];
            var res;
            if(resp.res_name == pat.hrs.name)
              res = pat.hrs;
            if(resp.res_name == pat.oxyS.name)
              res = pat.oxyS;
            if(resp.res_name == pat.temp.name)
              res = pat.temp;
            if(resp.res_name == pat.oxyValv.name)
              res = pat.oxyValv;
            if(resp.res_name == pat.ledA.name)
              res = pat.ledA;
            updateGraphs(res, resp);
          }
        }
      } else{
        for(var i = 0; i < R_NUM; i++){
          if(roomArray[i].id == resp.id_ent){
            var room = roomArray[i];
            var res;
            if(resp.res_name == room.fireAl.name)
              res = room.fireAl;
            if(resp.res_name == room.tempR.name)
              res = room.tempR;
            if(resp.res_name == room.airCon.name)
              res = room.airCon;
            updateGraphs(res, resp);
          }
        }
      }
      break;
    }
    case 8:{ //DONE
      if(resp.type == "p"){
        clearInterval(sendp1);
        var j = 0;
        for(var i = 0; i < P_NUM; i++){
          if(patArray[i].id == resp.id_ent){
            patArray[i].hrs.setValue(resp.HRS.e, resp.HRS.t);
            patArray[i].temp.setValue(resp.Temp.e, resp.Temp.t);
            patArray[i].oxyValv.setValue(resp.OxyValv.e, resp.OxyValv.t);
            patArray[i].oxyS.setValue(resp.OxyS.e, resp.OxyS.t);
            patArray[i].ledA.setValue(resp.LedA.e, resp.LedA.t);
            if(patArray[i].ledA.value != 0)
              patArray[i].ledON = 1;
            else patArray[i].ledON = 0;
            document.getElementById("Patient").style.display="block";
            updateTab(i,resp.type);
          }
        }
      } else{
        for(var i = 0; i < R_NUM; i++){
          if(roomArray[i].id == resp.id_ent){
            roomArray[i].fireAl.setValue(resp.FireAl.e, resp.FireAl.t);
            roomArray[i].tempR.setValue(resp.TempR.e, resp.TempR.t);
            roomArray[i].airCon.setValue(resp.AirCon.e, resp.AirCon.t);
            if(roomArray[i].fireAl.value != 0)
              fireON[i] = 1;
            else
              fireON[i] = 0;
            document.getElementById("Room").style.display="block";
            updateTab(i, resp.type);
          }
        }
      }

      break;
    }
  }
};

ws.onclose = function() {
  var el = document.getElementById("title");
  el.innerHTML = "Disconnected";
};

ws.onerror = function(err) {
  alert("Error: webServer Disconnected");
};

/*
* Funzione invocata con l'evento "onclick" sul bottone "Automatic Mode"
* La funzione prevede l'invio al WebServer di un messaggio per richiedere
* l'attivazione della modalità automatica per il paziente o per la stanza
*/
function sendAutomaticMode(type, id){
  var auto = "{'id':9, 'type':'"+ type +"', 'id_ent':" + id + ", 'res_name':'all'}";
  ws.send(auto);
  setTimeout(function(){
  ;
  }, 100);
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
    clearInterval(sendr1);
    clearInterval(sendp1);
    document.getElementById("Patient").style.display = "block";
    document.getElementById("Room").style.display = "none";
    var last_value = "{'id':8, 'type':'p', 'id_ent':" +patArray[0].id + ", 'res_name':'all'}";
    ws.send(last_value);
      /*for(var i = 0; i < P_NUM; i++){
        var last_value = "{'id':8, 'type':'p', 'id_ent':" + patArray[i].id + ", 'res_name':'all'}";
        ws.send(last_value);
        //setTimeout(function(){
          //ws.send(last_value);
          //;
        //}, 1000);
      }*/
  } else{
    clearInterval(sendp1);
    clearInterval(sendr1);
    document.getElementById("Patient").style.display = "none";
    document.getElementById("Room").style.display = "block";
    var last_value = "{'id':8, 'type':'r', 'id_ent':" + roomArray[0].id + ", 'res_name':'all'}";
    ws.send(last_value);
    //setTimeout(function() {
      //ws.send(last_value);
      //;
    //}, 1000);
      /*for(var i = 0; i < R_NUM; i++){
        var last_value = "{'id':8, 'type':'r', 'id_ent':" + roomArray[i].id + ", 'res_name':'all'}";
        ws.send(last_value);
        //setTimeout(function() {
          //ws.send(last_value);
          //;
        //}, 1000);
      }*/
  }
};

/*
* Funzione invocata con l'evento "onclick" sul bottone "Display Graph"
* La funzione prevede l'invio al WebServer di un messaggio per richiedere
* l'invio dello storico dei valori disponibile per la risorsa selezionata
*/
function sendHistoryRequest(type, id, name){
    var allValue = "{'id':3, 'type':'" + type.toLowerCase() + "', 'id_ent':" + id + ", 'res_name':'" + name + "'}";
    ws.send(allValue);
    setTimeout(function() {
      ;
    }, 100);
};

/*
* Funzione invocata cdurante la visualizzazione del grafico.
* Invia al webServer un pacchetto per richiedere l'ultimo valore in modo da
* aggiornare il grafico quando è stato ricevuto un nuovo dato
*/
function sendGetLastValue(type, id, name){
    var lastValue = "{'id':7, 'type':'" + type.toLowerCase() + "', 'id_ent':" + id + ", 'res_name':'" + name + "'}";
    ws.send(lastValue);
    setTimeout(function() {
      ;
    }, 200);
};

/*
* Funzione invocata con l'evento "onclick" sul bottone "Delete History"
* La funzione prevede l'invio al WebServer di un messaggio per richiedere
* la cancellazione dello storico dei valori disponibile per la risorsa selezionata
*/
function sendDeleteHistory(type, id, name){
    var deleteHistory = "{'id':4, 'type':'" + type.toLowerCase() + "', 'id_ent':" + id + ", 'res_name':'" + name + "'}";
    ws.send(deleteHistory);
    setTimeout(function() {
      ;
    }, 200);
};

/*
* Funzione invocata con l'evento "onclick" sul bottone "Set/Reset"
* La funzione prevede l'invio al WebServer di un messaggio per richiedere
* il settaggio del valore inviato nel messaggio dei valori
*/
function sendSetValue(type, id, name){
    if(name == "AirCon"){
      var value = document.getElementById("NewValueAirCon"+id).value*10;
      var setValue = "{'id':5, 'type':'" + type.toLowerCase() + "', 'id_ent':" + id + ", 'res_name':'" + name + "', 'value':" + value + "}";
      ws.send(setValue);
      setTimeout(function() {
        setValue = "{'id':5, 'type':'" + type.toLowerCase() + "', 'id_ent':" + id + ", 'res_name':'TempR', 'value':" + value + "}";
        ws.send(setValue);
      }, 100);
    }

    if(name == "OxyValv"){
      var value = document.getElementById("NewValueOxyValv"+id).value*10;
      var setValue = "{'id':5, 'type':'" + type.toLowerCase() + "', 'id_ent':" + id + ", 'res_name':'" + name + "', 'value':" + value + "}";
      ws.send(setValue);
      setTimeout(function() {
        setValue = "{'id':5, 'type':'" + type.toLowerCase() + "', 'id_ent':" + id + ", 'res_name':'OxyS', 'value':" + value + "}";
        ws.send(setValue);
      }, 100);
    }

    if(name == "FireAl"){
      var setValue = "{'id':5, 'type':'" + type.toLowerCase() + "', 'id_ent':" + id + ", 'res_name':'" + name + "', 'value':0}";
      fireON[id] = 0;
      ws.send(setValue);
      setTimeout(function() {
        ;
      }, 100);
    }

    if(name == "LedA"){
      var setValue = "{'id':5, 'type':'" + type.toLowerCase() + "', 'id_ent':" + id + ", 'res_name':'Temp', 'value':360}";
      ws.send(setValue);
      setTimeout(function(){
        setValue = "{'id':5, 'type':'" + type.toLowerCase() + "', 'id_ent':" + id + ", 'res_name':'HRS', 'value':800}";
        ws.send(setValue);
        setTimeout(function(){
          setValue = "{'id':5, 'type':'" + type.toLowerCase() + "', 'id_ent':" + id + ", 'res_name':'" + name + "', 'value':0}";
          ws.send(setValue);
        }, 100);
      }, 100);
    }
};

function error(error){
  if(error == 1){
      var setValue = "{'id':5, 'type':'p', 'id_ent':" + activeP + ", 'res_name':'Temp', 'value':420}";
      ws.send(setValue);
      setTimeout(function(){
        setValue = "{'id':9, 'type':'p', 'id_ent':" + activeP + ", 'res_name':'all'}";
        ws.send(setValue);
      }, 100);
  } else if(error == 2){
        var setValue = "{'id':5, 'type':'p', 'id_ent':" + activeP + ", 'res_name':'HRS', 'value':1180}";
        ws.send(setValue);
        setTimeout(function(){
          setValue = "{'id':9, 'type':'p', 'id_ent':" + activeP + ", 'res_name':'all'}";
          ws.send(setValue);
        }, 100);
  }
}

function containsONEs(array){
  var found = 0;
  for(var i = 0; i < R_NUM; i++)
    if(fireON[i] == 1)
      found++;
  if(found > 0)
    return found;
  else
    return 0;
}

/*
* Funzione invocata dopo aver ricevuto un messaggio di riposta al messaggio con
* id = 8. Serve per aggiornare i valori visualizzati per ogni risorsa sia nel
* caso dei pazienti sia nel caso delle stanze
*/
function updateTab(index, type){
  if(type == "p"){
    clearInterval(sendr1);
    clearInterval(sendp1);
    sendp1 = setInterval(function(){
      document.getElementById("h3"+patArray[index].hrs.name+patArray[index].id).innerHTML = patArray[index].hrs.value + " " + patArray[index].hrs.unity;
      document.getElementById("h3"+patArray[index].oxyS.name+patArray[index].id).innerHTML = patArray[index].oxyS.value + " " + patArray[index].oxyS.unity;
      document.getElementById("h3"+patArray[index].temp.name+patArray[index].id).innerHTML = patArray[index].temp.value + " " + patArray[index].temp.unity;
      document.getElementById("h3"+patArray[index].oxyValv.name+patArray[index].id).innerHTML = patArray[index].oxyValv.value + " " + patArray[index].oxyValv.unity;
      if(patArray[index].ledON == 1){
        document.getElementById("h3"+patArray[index].ledA.name+patArray[index].id).innerHTML = "On";
        document.getElementById("h3"+patArray[index].ledA.name+patArray[index].id).style.color = "#ff0000";
      }
      else{
        document.getElementById("h3"+patArray[index].ledA.name+patArray[index].id).innerHTML = "Off";
        document.getElementById("h3"+patArray[index].ledA.name+patArray[index].id).style.color = "#000000";
      }

      var last_value = "{'id':8, 'type':'"+type+"', 'id_ent':" + patArray[index].id + ", 'res_name':'all'}";
      ws.send(last_value);
    }, (500));
    //clearInterval(sendp1);
  } else {
    clearInterval(sendp1);
    clearInterval(sendr1);
    sendr1 = setInterval(function(){
      document.getElementById("h3"+roomArray[index].tempR.name+roomArray[index].id).innerHTML = roomArray[index].tempR.value + " " + roomArray[index].tempR.unity;
      document.getElementById("h3"+roomArray[index].airCon.name+roomArray[index].id).innerHTML = roomArray[index].airCon.value + " " + roomArray[index].airCon.unity;
      //document.getElementById("h3"+roomArray[index].fireAl.name+roomArray[index].id).innerHTML = roomArray[index].fireAl.value;
      if(containsONEs(fireON) == R_NUM)
        document.getElementById("h3"+roomArray[index].fireAl.name+roomArray[index].id).style.color = "#ff0000";
      else
        document.getElementById("h3"+roomArray[index].fireAl.name+roomArray[index].id).style.color = "#000000";

      if(roomArray[index].fireAl.value == 0)
        document.getElementById("h3"+roomArray[index].fireAl.name+roomArray[index].id).innerHTML = "Off";
      else
        document.getElementById("h3"+roomArray[index].fireAl.name+roomArray[index].id).innerHTML = "On";

      var last_value = "{'id':8, 'type':'"+type+"', 'id_ent':" + roomArray[index].id + ", 'res_name':'all'}";
      ws.send(last_value);
    }, (500));
  }
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
  h3.setAttribute("id", "h3"+resource.name+id);
  if(resource.unity != null)
    h3.innerHTML = resource.value + " " + resource.unity;
  else
    h3.innerHTML = resource.value;

  var p1 = document.createElement("p");

  var button = document.createElement("button");
  button.setAttribute("type", "button");
  button.setAttribute("class", "btn btn-primary");
  button.setAttribute("data-toggle", "modal");
  button.setAttribute("data-target", "#myModal");
  button.onclick = function(){sendHistoryRequest(type, id, resource.name);};

  button.innerHTML = "Draw Graph";

  var a1 = document.createElement("a");
  a1.setAttribute("class", "btn btn-default");
  a1.setAttribute("role", "button");
  a1.innerHTML = "Del. History";
  a1.onclick = function(){sendDeleteHistory(type, id, resource.name);};

  p1.appendChild(button);
  p1.appendChild(a1);

  if(resource.name == "FireAl" || resource.name == "LedA"){
    var a2 = document.createElement("a");
    a2.setAttribute("class", "btn btn-default");
    a2.setAttribute("role", "button");
    a2.innerHTML = "Reset";
    a2.onclick = function(){sendSetValue(type, id, resource.name);};
    p1.appendChild(a2);
  }

  caption.appendChild(h2);
  caption.appendChild(h3);
  caption.appendChild(p1);

  if(resource.name == "AirCon" || resource.name == "OxyValv"){
    var dform = document.createElement("div");
    dform.setAttribute("class", "form-group");
    var label = document.createElement("label");
    label.setAttribute("for", "NewValue"+resource.name+id);
    label.innerHTML = "Insert the new value";

    var input = document.createElement("input");
    input.setAttribute("class", "form-control");
    input.setAttribute("id", "NewValue"+resource.name+id);
    input.setAttribute("placeholder", "Insert a value");

    var p = document.createElement("p");
    dform.appendChild(label);
    dform.appendChild(input);
    p.appendChild(dform);

    var button = document.createElement("button");
    button.setAttribute("type", "button");
    button.setAttribute("class", "btn btn-primary");
    button.onclick = function(){sendSetValue(type, id, resource.name);};

    button.innerHTML = "Set New Value";
    p.appendChild(button);
    caption.appendChild(p);
  }

  thumbnail.appendChild(img);
  thumbnail.appendChild(caption);
  column.appendChild(thumbnail);
  row.appendChild(column);

  el.appendChild(row);

};


function createIconSimulation(type, id, el, row){
  var column = document.createElement("div");
  column.setAttribute("class", "col-sm-6 col-md-4");

  var thumbnail = document.createElement("div");
  thumbnail.setAttribute("class", "thumbnail");

  var caption = document.createElement("div");
  caption.setAttribute("class", "caption");

  var h = document.createElement("h3");
  h.innerHTML = "Working Mode";

  var p0 = document.createElement("p");

  button = document.createElement("button");
  button.setAttribute("type", "button");
  button.setAttribute("class", "btn btn-primary");
  button.onclick = function(){sendAutomaticMode(type.toLowerCase(), id);};
  button.innerHTML = "Automatic mode";
  p0.appendChild(button);
  caption.appendChild(h);
  caption.appendChild(p0);

  var h3 = document.createElement("h3");
  h3.innerHTML = "Simulation Error";

  var p = document.createElement("p");

  button = document.createElement("button");
  button.setAttribute("type", "button");
  button.setAttribute("class", "btn btn-primary");
  button.setAttribute("onclick", "error(1)");
  //button.onclick = function(){error(1, id);};

  button.innerHTML = "Generate Error 1";
  p.appendChild(button);

  var p1 = document.createElement("p");

  button = document.createElement("button");
  button.setAttribute("type", "button");
  button.setAttribute("class", "btn btn-primary");
  button.setAttribute("onclick", "error(2)");
  //button.onclick = function(){error(2, id);};

  button.innerHTML = "Generate Error 2";
  p1.appendChild(button);

  caption.appendChild(h3);
  caption.appendChild(p);
  caption.appendChild(p1);
  thumbnail.appendChild(caption);
  column.appendChild(thumbnail);
  row.appendChild(column);
  el.appendChild(row);
}

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

    createIconSimulation(type, id, el, row);

  } else {
    stringId = "room";
    var el = document.getElementById(stringId + id);
    var row = document.createElement("div");
    row.setAttribute("class", "row");
    createResourceIcon(stringId, index, id, roomArray[index].tempR, type, el, row);
    createResourceIcon(stringId, index, id, roomArray[index].airCon, type, el, row);
    createResourceIcon(stringId, index, id, roomArray[index].fireAl, type, el, row);

    var button = document.createElement("button");
    button.setAttribute("type", "button");
    button.setAttribute("class", "btn btn-primary");
    button.onclick = function(){sendAutomaticMode(type.toLowerCase(), id);};
    button.innerHTML = "Automatic mode";
    el.appendChild(button);
  }
};

function changePat(id){
  activeP = id;
  ws.send("{'id':8, 'type':'p', 'id_ent':" + id + ", 'res_name':'all'}");
}

function changeRoom(id){
  ws.send("{'id':8, 'type':'r', 'id_ent':" + id + ", 'res_name':'all'}");
}

/*
* Funzione che crea dinamicamente le voci del menu a forma di TAB con cui sarà
* possibile navigare tra l'elenco delle stanze o dei pazienti presenti
*/
function createTab(type){
  if(type == "p"){
    /*
    * Seleziono i div che andrò a popolare dinamicamente ogni volta che verrà
    * premuto il menu "Paziente".
    * Se esistono già li elimino dal DOM in modo che non mi si crei più di una
    * instanza per volta
    */
    document.getElementById("Room").style.display="none";
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
        a.setAttribute("onclick", "changePat("+id+")");
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
  } else{
    /*
    * Seleziono i div che andrò a popolare dinamicamente ogni volta che verrà
    * premuto il menu "Room".
    * Se esistono già li elimino dal DOM in modo che non mi si crei più di una
    * instanza per volta
    */
    document.getElementById("Patient").style.display="none";
    document.getElementById("Room").style.display="block";
    var el = document.getElementById("Room");
    var u = document.getElementById("tabRoom");
      if(u)
        el.removeChild(u);
    var di = document.getElementById("conRoom");
      if(di)
        el.removeChild(di);

    /*
    * Se il numero di stanze è uguale a 0 allora stampo semplicemente un testo
    * che mi dice che non ci sono stanze. Altrimenti popolo i div selezionati
    * sopra con una struttura a TAB che prevede una voce per ogni Stanza di cui
    * si è ricevuto l'id dal WebServer.
    * Dopo aver creato la struttura a TAB per ogni stanza viene invocata la
    * funzione "buildInterface()" che andrà a popolare il div relativo alla
    * singola stanza.
    */
    if(R_NUM == 0){
      var div = document.createElement("div");
      div.setAttribute("class", "col-md-4");
      var h3 = document.createElement("h3");
      h3.innerHTML = "Any Room";
      div.appendChild(h3);
      el.appendChild(div);
    } else {

      var ul = document.createElement("ul");
      ul.setAttribute("class", "nav nav-tabs");
      ul.setAttribute("role", "tablist");
      ul.setAttribute("id", "tabRoom");

      var div = document.createElement("div");
      div.setAttribute("class", "tab-content");
      div.setAttribute("id", "conRoom");

      for(var i = 0; i < R_NUM; i++){
        var li = document.createElement("li");
        li.setAttribute("role", "presentation");

        var d = document.createElement("div");
        d.setAttribute("role", "tabpanel");
        d.setAttribute("class", "tab-pane");
        var id = roomArray[i].id;
        d.setAttribute("id", "room" + id);

        var a = document.createElement("a");
        a.setAttribute("class", "tab");
        a.setAttribute("href", "#room" + id);
        a.setAttribute("aria-controls", "room" + id);
        a.setAttribute("role", "tab");
        a.setAttribute("data-toggle", "tab");
        a.setAttribute("onclick", "changeRoom("+id+")");
        a.innerHTML = "Room " + id;


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

      for(var i = 0; i < R_NUM; i++)
        buildInterface(i, roomArray[i].id, "R");
    }
  }
};

function updateGraphs(res, resp){
  //alert("new value");
  var h = parseInt(resp.payload.t.slice(0,2));
  var m = parseInt(resp.payload.t.slice(2,4));
  var s = parseInt(resp.payload.t.slice(4,6));
  var time = (h*3600 + m*60 +s)*1000;

  res.history.push({x: time, y: (parseInt(resp.payload.e)/10)});

  chart.render();
  //graphs(res.desc, res.history, res.type, res.id_ent, res.res_name);

  setTimeout(function(){
    sendGetLastValue(resp.type, resp.id_ent, res.name);
  }, 300);

}

/*
* Funzione per stampare il grafico all'interno dell'elemento Modal di bootstrap
* prende 3 argomenti in ingresso:
*   - l'indice della stanza o del paziente
*   - l'indice della risorsa di cui si vuole il grafico
*   - "P" o "R" per discriminare tra paziente e stanza
*/
function graphs(desc, value, type, id_ent, res_name){
  var el = document.getElementById("modal-body");
  var div = document.createElement("div");

  document.getElementById("myModalLabel").innerHTML = desc;
  div.setAttribute("id", "chartContainer1");
  el.appendChild(div);

  chart = new CanvasJS.Chart("chartContainer1", {
    theme: "theme2",
    animationEnabled: true,
    backgroundColor: null,
    axisX: {
      title: "Time",
      titleFontColor: "black",
      titleFontSize: 20,
      titleFontFamily: "tahoma",
      titleFontStyle: "italic",
      labelFontStyle: "italic",
      labelAutoFit: true,
      labelFontFamily: "tahoma",
      labelFontColor: "black",
      labelFontSize: 10,
      xValueType: "dateTime",
      interval: 30,
      intervalType: "minute",
      lineColor: "black",
      lineThickness: 3,
      tickLength: 5,
      tickColor: "black"

    },
    axisY: {
      title: "Value",
      titleFontColor: "black",
      titleFontSize: 20,
      titleFontFamily: "tahoma",
      titleFontStyle: "italic",
      labelFontStyle: "italic",
      labelAutoFit: true,
      labelFontFamily: "tahoma",
      labelFontColor: "black",
      labelFontSize: 10,
      lineColor: "black",
      lineThickness: 3,
      tickLength: 5,
      tickColor: "black",
      gridThickness: 1,
      gridDashType: "dash",
      interval: 10,
      includeZero: false
    },
    data: [{
      type: "spline",
      cursor: "defualt",
      color: "green",
      lineColor: "green",
      lineThickness: 2,
      dataPoints: value

    }]
  });

  chart.render();

  setTimeout(function(){
    sendGetLastValue(type, id_ent, res_name);
  }, 300);
};

/*
* Gestisce le azioni che vengono effettuate al caricamento della pagina.
* Si occupa di gestire le richieste per scoprire:
*   - quanti e chi sono i pazienti
*   - quante e quali sono le stanze
*/
function findPatientsRooms(){

  setTimeout(function(){
    var WhatPatients = "{'id':1}";
    ws.send(WhatPatients);
  }, 100);

  setTimeout(function(){
    var WhatRooms = "{'id':2}";
    ws.send(WhatRooms);
  }, 100);

  setTimeout(function(){
    document.getElementById("Patient").style.display="none";
    document.getElementById("Room").style.display="none";
  }, 150);
};
