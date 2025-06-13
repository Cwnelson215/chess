package server.websocket;

import chess.ChessMove;
import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {
    private final ConcurrentHashMap<Integer, ConnectionManager> games = new ConcurrentHashMap<>();
    String[] columns = {" a ", " b ", " c ", " d ", " e ", " f ", " g ", " h "};

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> connect(command.getGameID(), session, command.getUserName(), command.getPlayerColor());
            case LEAVE -> leave(command.getGameID(), command.getUserName());
            case RESIGN -> resign(command.getGameID(), command.getUserName());
            case MAKE_MOVE -> move(command.getGameID(), command.getUserName(), command.getMove());
        }
    }

    public void connect(int gameID, Session session, String userName, String playerColor) throws IOException {
        checkForGame(gameID);
        var game = games.get(gameID);
        game.add(userName, session);
        var message = String.format("%s has joined the game as %s", userName, playerColor);
        NotificationMessage notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                message, String.valueOf(gameID), "JOIN");
        game.broadcast(userName, notification);
    }

    public void leave(int gameID, String userName) throws IOException {
        checkForGame(gameID);
        var game = games.get(gameID);
        game.remove(userName);
        var message = String.format("%s has left the game", userName);
        NotificationMessage notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                message, String.valueOf(gameID), "LEAVE");
        game.broadcast(userName, notification);
    }

    public void resign(int gameID, String userName) throws IOException {
        checkForGame(gameID);
        var game = games.get(gameID);
        game.remove(userName);
        var message = String.format("%s has resigned", userName);
        NotificationMessage notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                message, String.valueOf(gameID), "RESIGN");
        game.broadcast(userName, notification);
    }

    public void move(int gameID, String userName, ChessMove move) throws IOException {
        var startRow = move.getStartPosition().getRow();
        var startColumn = move.getStartPosition().getColumn();
        var endRow = move.getEndPosition().getRow();
        var endCol = move.getEndPosition().getColumn();

        checkForGame(gameID);
        var game = games.get(gameID);
        var message = String.format("%s has moved%s\b%s to%s\b%s", userName, columns[startColumn - 1], startRow,
                columns[endCol - 1], endRow);
        NotificationMessage notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                message, String.valueOf(gameID), "MOVE");
        game.broadcast(userName, notification);
    }

    public void checkForGame(int gameID) {
        if(!games.containsKey(gameID)) {
            addGame(gameID);
        }
    }

    public void addGame(int gameID) {
        var game = new ConnectionManager();
        games.put(gameID, game);
    }
}
