var ws = new WebSocket("ws://127.0.0.1:8100/");

ws.onopen = function() {
    alert("Opened!");
    ws.send("{'id':6,'type':'p','id_ent':1,'res_name':'OxyS','value':800}");
};

ws.onmessage = function (evt) {
    alert("Message: " + evt.data);
};

ws.onclose = function() {
    alert("Closed!");
};

ws.onerror = function(err) {
    alert("Error: " + err);
};