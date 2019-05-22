<html>

<body style="margin: 35px">
<form>

    <input id="messageField" type="text">
    <input onclick="sendMsg('ONMESSAGE','b00c5a7b1e2fed67e4d7eb9978ea83a3',msgField.value,'3a1e31bc48479b0272af727cc9d91f21');" value="send" type="button">
</form>

<div id="msg-box" style="width:500px; height: 400px; background: #eee; overflow:auto;"></div>


<script>
    var webSocket = new WebSocket("ws://localhost:8080/wschat");
    var msgField = document.getElementById("messageField");
    var divMsg = document.getElementById("msg-box");

    function sendMsg(context, token, message, toToken) {
        var msgToSend = "{\"context\":\""+context+"\",\"token\":\""+token+"\",\"message\":\""+message+"\"}";msgField.value
        webSocket.send(msgToSend);
        divMsg.innerHTML += "<div style='color:red'>Client> " + msgToSend +
            "</div>"
        msgField.value = "";
    }

    webSocket.onmessage = function(message) {
        divMsg.innerHTML += "Server> : " + message.data;

    }

    webSocket.onopen = function() {
        console.log("connection opened");
        sendMsg('ONCONNECTION','b00c5a7b1e2fed67e4d7eb9978ea83a3','','');
    };

    webSocket.onclose = function() {
        console.log("connection closed");
    };

    webSocket.onerror = function wserror(message) {
        console.log("error: " + message);
    }


</script>
</body>
</html>
