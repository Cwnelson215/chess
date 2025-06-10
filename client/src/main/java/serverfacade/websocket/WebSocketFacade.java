package serverfacade.websocket;

import com.google.gson.Gson;
import serverfacade.ResponseException;
import websocket.messages.NotificationMessage;

import javax.websocket.*;
import java.net.URI;

public class WebSocketFacade extends Endpoint {
    Session session;
    NotificationHandler notificationHandler;

    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws ResponseException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    NotificationMessage notificationMessage = new Gson().fromJson(message, NotificationMessage.class);
                    notificationHandler.notify(notificationMessage);
                }
            });
        } catch(Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }


}
