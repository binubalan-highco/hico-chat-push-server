<html>

<body style="margin: 35px">
<form>
    <input type="text" id="usernameField" placeholder="User name" />
    <input type="button" value="get token" onclick="newUser()" /><br /><br /><br />
    <input id="messageField" type="text">
    <input type="text" id="tousertoken" placeholder="To user token" />
    <input onclick="sendMsg('ONMESSAGE',usernameField.value,msgField.value,tousertoken.value,'');" value="send" type="button"><br /><br />
    <input type="text" id="channel" placeholder="Channel name" />
    <input onclick="sendMsg('ONBURSTSEND',usernameField.value,msgField.value,'',channel.value);" value="send to channel" type="button">
</form>

<div id="msg-box" style="width:90%; height: 500px; background: #eee; overflow:auto;"></div>


<script>
    var webSocket;
    var msgField = document.getElementById("messageField");
    var divMsg = document.getElementById("msg-box");

    var usernameField = document.getElementById("usernameField");
    var tousertoken = document.getElementById("tousertoken");
    var channel = document.getElementById("channel");

    function newUser()
    {
        api("{\"context\":\"NEWUSER\",\"username\":\""+usernameField.value+"\"}",
            function(){
                if (this.readyState == 4 && this.status == 200) {
                    console.log("responseText=" + this.responseText);
                    let inputObject = JSON.parse(this.responseText);
                    if(inputObject)
                    {
                        console.log(inputObject);
                        if(inputObject.status && inputObject.status ==1 && inputObject.token)
                        {
                            usernameField.value = inputObject.token;
                            initWebSocket();
                        }else console.log("FATAL");
                    }else console.log(inputObject);

                }
            }
        )
    }

    function initWebSocket()
    {
        webSocket = new WebSocket("ws://11.100.109.22:8080/wschat");
        webSocket.onmessage = function(message) {
            let respData = JSON.parse(message.data);
            if(respData)
            {
                divMsg.innerHTML += +": " + message.data;
            }
            else{
                divMsg.innerHTML += "Response received but fata error";
            }


        }

        webSocket.onopen = function() {
            console.log("connection opened");
            sendMsg('ONCONNECTION',usernameField.value,'','','');
        };

        webSocket.onclose = function() {
            console.log("connection closed");
        };

        webSocket.onerror = function wserror(message) {
            console.log("error: " + message);
        }
    }
    function api(input,cb) {
        var xhttp = new XMLHttpRequest();
        xhttp.open("POST", "http://11.100.109.22:8080/api", true);
        xhttp.setRequestHeader("Content-type", "application/json");
        xhttp.send(input);

        xhttp.onreadystatechange = cb;
    }

    function sendMsg(context, token, message, toToken, channel) {
        var msgToSend = "{\"context\":\""+context+"\",\"token\":\""+token+
            "\",\"message\":\""+message+"\",\"toToken\":\""+toToken+"\",\"channel\":\""+channel+"\"}";
        webSocket.send(msgToSend);
        divMsg.innerHTML += "<div style='color:red'>You:  " + message +
            "</div>"
        msgField.value = "";
    }


</script>
</body>
</html>
