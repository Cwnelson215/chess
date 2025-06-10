package server.websocket;

import chess.ChessMove;
import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import javax.swing.*;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {
    private final ConcurrentHashMap<Integer, ConnectionManager> games = new ConcurrentHashMap<>();

    @OnWebSocketMessage
    public void onMessage(Session session, String message, String userName, String playerColor, ChessMove move) throws IOException {
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> connect(command.getGameID(), session, userName, playerColor);
            case LEAVE -> leave(command.getGameID(), userName);
            case RESIGN -> resign(command.getGameID(), userName);
            case MAKE_MOVE -> move(command.getGameID(), userName, move);
        }
    }

    public void connect(int gameID, Session session, String userName, String playerColor) throws IOException {
        if(games.contains(gameID)) {
            var game = games.get(gameID);
            game.add(userName, session);
            var message = String.format("%s has joined the game as %s", userName, playerColor);
            NotificationMessage notification = new Gson().fromJson(message, NotificationMessage.class);
            game.broadcast(userName, notification);
        }
    }

    public void leave(int gameID, String userName) throws IOException {
        var game = games.get(gameID);
        game.remove(userName);
        var message = String.format("%s has left the game", userName);
        NotificationMessage notification = new Gson().fromJson(message, NotificationMessage.class);
        game.broadcast(userName, notification);
    }

    public void resign(int gameID, String userName) throws IOException {
        var game = games.get(gameID);
        game.remove(userName);
        var message = String.format("%s has resigned", userName);
        NotificationMessage notification = new Gson().fromJson(message, NotificationMessage.class);
        game.broadcast(userName, notification);

    }

    public void move(int gameID, String userName, ChessMove move) throws IOException {
        var startRow = move.getStartPosition().getRow();
        var startColumn = move.getStartPosition().getColumn();
        var endRow = move.getEndPosition().rowConverter();
        var endCol = move.getEndPosition().getColumn();

        var game = games.get(gameID);
        var message = String.format("%s has moved %s%s to %s%s", userName, startRow, startColumn, endRow, endCol);
        NotificationMessage notification = new Gson().fromJson(message, NotificationMessage.class);
        game.broadcast(userName, notification);
    }

    public void addGame(int gameID) {
        var game = new ConnectionManager();
        games.put(gameID, game);
    }
}
