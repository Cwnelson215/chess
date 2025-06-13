package serverfacade.websocket;

import chess.ChessMove;
import com.google.gson.Gson;
import serverfacade.ResponseException;
import websocket.commands.UserGameCommand;
import websocket.messages.NotificationMessage;

import javax.websocket.*;
import java.io.IOException;
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
                    NotificationMessage notification = new Gson().fromJson(message, NotificationMessage.class);
                    notificationHandler.notify(notification);
                }
            });
        } catch(Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void joinGame(String authToken, int gameID, String userName, String playerColor) throws IOException {
        var command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID,
                userName, playerColor, null);
        this.session.getBasicRemote().sendText(new Gson().toJson(command));
    }

    public void leaveGame(String authToken, int gameID, String userName) throws IOException {
        var command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken,gameID,
                userName, null, null);
        this.session.getBasicRemote().sendText(new Gson().toJson(command));
    }

    public void resignGame(String authToken, int gameID, String userName) throws IOException {
        var command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID,
                userName, null, null);
        this.session.getBasicRemote().sendText(new Gson().toJson(command));
    }

    public void makeMove(String authToken, int gameID, String userName, ChessMove move) throws IOException {
        var command = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID,
                userName, null, move);
        this.session.getBasicRemote().sendText(new Gson().toJson(command));
    }

}
