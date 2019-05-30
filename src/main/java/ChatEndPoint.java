


import java.io.IOException;

import com.google.gson.Gson;
import jdk.nashorn.internal.runtime.SharedPropertyMap;
import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.WsOutbound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ChatEndPoint extends WebSocketServlet {

    private static final Logger log = LoggerFactory.getLogger(ChatEndPoint.class);

    @Override
    protected boolean verifyOrigin(String origin) {
        log.trace("Origin: {}", origin);
        return true;
    }

    @Override
    protected StreamInbound createWebSocketInbound(String subProtocol, HttpServletRequest request) {
        WebSocketConnection webSocketConnection = new WebSocketConnection();
        return webSocketConnection;
    }

    public static class WebSocketConnection extends MessageInbound {


        @Override
        protected void onOpen(WsOutbound wsOutbound) {
//            this.wsOutboundStore = wsOutbound;

        }

        @Override
        protected void onClose(int status) {
            //remove outbound from shared pool
            if(SharableSocketPool.poolObjects!=null && SharableSocketPool.poolObjects.size()>0)
            {
                SharableSocketPool.poolObjects.remove(this.getWsOutbound());
            }
        }


        @Override
        protected void onBinaryMessage(ByteBuffer byteBuffer) throws IOException {
            throw new UnsupportedOperationException("No se soportan mensajes binarios");
        }

        @Override
        protected void onTextMessage(CharBuffer charBuffer) throws IOException {
            try {
                final String data = charBuffer.toString();
                Gson gson = new Gson();
                WSEndpointRequest wsEndpointRequest = gson.fromJson(data, WSEndpointRequest.class);
                if(wsEndpointRequest!=null)
                {
                    if(wsEndpointRequest.context!=null && !wsEndpointRequest.context.isEmpty())
                    {
                        if(wsEndpointRequest.context.equals("ONCONNECTION"))
                        {
                            //get the token now
                            if(wsEndpointRequest.token!=null && !wsEndpointRequest.token.isEmpty())
                            {

                                PoolObject poolObject = new PoolObject();
                                poolObject.wsOutbound = this.getWsOutbound();
                                poolObject.token = wsEndpointRequest.token;
                                SharableSocketPool.poolObjects.add(poolObject);

                                //get user by token
                                DatabaseConnection.User user = new  DatabaseConnection().getUserByToken(wsEndpointRequest.token);
                                if(user!=null && user.userId>0)
                                {
                                    if(SharableSocketPool.poolObjects!=null && SharableSocketPool.poolObjects.size()>0)
                                    {
                                        for(int i=0,j=SharableSocketPool.poolObjects.size();i<j;i++)
                                        {
                                            if(SharableSocketPool.poolObjects.get(i).token.equals(user.token)) continue;
                                            //send if possible
                                            SharableSocketPool.poolObjects.get(i).wsOutbound
                                                    .writeTextMessage(CharBuffer.wrap("{\"status\":1,\"message\":\""+user.userName+" joined!\"" +
                                                            ",\"user\":"+user.toJson()+",\"returnContext\":\"MEMBJOIN\"}"));
                                        }
                                    }
                                }
                                else{
                                    throw new Exception("Unknown user");
                                }

                            }
                            else{
                                throw new Exception("Unknown token");
                            }
                        }
                        else if(wsEndpointRequest.context.equals("ONMESSAGE")){
                            //send to all
                            //get the token now
                            if(wsEndpointRequest.token!=null && !wsEndpointRequest.token.isEmpty())
                            {
                                //get user by token
                                DatabaseConnection.User user = new  DatabaseConnection().getUserByToken(wsEndpointRequest.token);
                                if(user!=null && user.userId>0)
                                {
                                    if(wsEndpointRequest.message!=null && !wsEndpointRequest.message.isEmpty())
                                    {
                                        if(wsEndpointRequest.toToken==null || wsEndpointRequest.toToken.isEmpty())
                                        {
                                            throw new Exception("No receiver identity");
                                        }
                                        DatabaseConnection.User userTo = new  DatabaseConnection().getUserByToken(wsEndpointRequest.toToken);
                                        if(userTo==null || userTo.userId<=0)
                                        {
                                            throw new Exception("No such receiver found");
                                        }
                                        System.out.println(user.userName+"-->"+ userTo.userName);
                                        if(SharableSocketPool.poolObjects!=null && SharableSocketPool.poolObjects.size()>0)
                                        {
                                            for(int i=0,j=SharableSocketPool.poolObjects.size();i<j;i++)
                                            {
                                                PoolObject poolObject = SharableSocketPool.poolObjects.get(i);
                                                if(poolObject.token.equals(userTo.token)){
                                                    SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd HH:mm z");
                                                    Date date = new Date(System.currentTimeMillis());
                                                    //send if possible
                                                    CharBuffer out = CharBuffer
                                                            .wrap("{\"status\":1,\"message\":\""+
                                                                    wsEndpointRequest.message+"\",\"user\":"+
                                                                    user.toJson()+",\"date\":\""+formatter.format(date)
                                                                    +"\",\"returnContext\":\"MESSAGERCVD\"}");
                                                    System.out.println(out.toString());
                                                    poolObject.wsOutbound.writeTextMessage(out);
                                                }

                                            }
                                        }else{
                                            throw new Exception("No users online");
                                        }
                                    }else{
                                        throw new Exception("No message");
                                    }

                                }
                                else{
                                    throw new Exception("Unknown user");
                                }
                            }
                            else{
                                throw new Exception("Unknown context");
                            }
                        }
                        else if(wsEndpointRequest.context.equals("ONBURSTSEND")){
                            //send to all
                            //get the token now
                            if(wsEndpointRequest.token!=null && !wsEndpointRequest.token.isEmpty())
                            {
                                //get user by token
                                DatabaseConnection.User user = new  DatabaseConnection().getUserByToken(wsEndpointRequest.token);
                                if(user!=null && user.userId>0)
                                {
                                    if(wsEndpointRequest.message!=null && !wsEndpointRequest.message.isEmpty())
                                    {
                                        if(wsEndpointRequest.channel==null || wsEndpointRequest.channel.isEmpty())
                                        {
                                            throw new Exception("No channel specified");
                                        }
                                        if(SharableSocketPool.poolObjects!=null && SharableSocketPool.poolObjects.size()>0)
                                        {
                                            for(int i=0,j=SharableSocketPool.poolObjects.size();i<j;i++)
                                            {

                                                //burst send to everyone
                                                PoolObject poolObject = SharableSocketPool.poolObjects.get(i);
                                                SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd HH:mm z");
                                                Date date = new Date(System.currentTimeMillis());
                                                //send if possible
                                                poolObject.wsOutbound.writeTextMessage(CharBuffer
                                                        .wrap("{\"status\":1,\"message\":\""+
                                                                wsEndpointRequest.message+"\",\"user\":"+user.toJson()+",\"date\":\""+formatter.format(date)+"\",\"returnContext\":\"BURSTRCVD\"}"));
                                            }
                                        }else{
                                            throw new Exception("No users online");
                                        }
                                    }else{
                                        throw new Exception("No message");
                                    }

                                }
                                else{
                                    throw new Exception("Unknown user");
                                }
                            }
                            else{
                                throw new Exception("Unknown context");
                            }
                        }
                        else
                        {
                            throw new Exception("Unknown context");
                        }
                    }
                    else{
                        throw new Exception("No context found");
                    }
                }
                else{
                    throw new Exception("Fatal Error : 10");
                }
            }catch (Exception e)
            {
                System.out.println(e);
                getWsOutbound().writeTextMessage(CharBuffer.wrap("{\"status\":0,\"message\":\""+e.getLocalizedMessage()+"\"}"));
            }

//            getWsOutbound().writeTextMessage(CharBuffer.wrap("Hola " + user + " desde WebSocket"));
        }
    }

    class WSEndpointRequest{
        public  String context = "";
        public  String token = "";
        public String message = "";
        public String toToken = "";
        public String channel = "";
    }
}
