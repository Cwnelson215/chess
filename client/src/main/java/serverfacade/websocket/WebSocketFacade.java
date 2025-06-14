package serverfacade.websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import model.GameData;
import serverfacade.ResponseException;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

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
                    ServerMessage notification = new Gson().fromJson(message, ServerMessage.class);
                    if(notification.getServerMessageType().equals(ServerMessage.ServerMessageType.LOAD_GAME)) {
                        LoadMessage loadMessage = new Gson().fromJson(message, LoadMessage.class);
                        try {
                            notificationHandler.notify(loadMessage);
                        } catch (ResponseException e) {
                            throw new RuntimeException(e);
                        }
                    } else if(notification.getServerMessageType().equals(ServerMessage.ServerMessageType.ERROR)) {
                        ErrorMessage errorMessage = new Gson().fromJson(message, ErrorMessage.class);
                        try {
                            notificationHandler.notify(errorMessage);
                        } catch (ResponseException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        notification = new Gson().fromJson(message, NotificationMessage.class);
                        try {
                            notificationHandler.notify(notification);
                        } catch (ResponseException e) {
                            throw new RuntimeException(e);
                        }
                    }
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
                playerColor, null);
        this.session.getBasicRemote().sendText(new Gson().toJson(command));
    }

    public void leaveGame(String authToken, int gameID) throws IOException {
        var command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken,gameID, null, null);
        this.session.getBasicRemote().sendText(new Gson().toJson(command));
    }

    public void resignGame(String authToken, int gameID, String userName) throws IOException {
        var command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID, null, null);
        this.session.getBasicRemote().sendText(new Gson().toJson(command));
    }

    public void makeMove(String authToken, int gameID, String userName, String playerColor, ChessMove move) throws IOException {
        var command = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID, playerColor, move);
        this.session.getBasicRemote().sendText(new Gson().toJson(command));
    }

}
