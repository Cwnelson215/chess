package server.websocket;

import chess.ChessMove;
import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;
import websocket.messages.NotificationMessage;

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
        NotificationMessage notification = new Gson().fromJson(message, NotificationMessage.class);
        game.broadcast(userName, notification);
    }

    public void leave(int gameID, String userName) throws IOException {
        checkForGame(gameID);
        var game = games.get(gameID);
        game.remove(userName);
        var message = String.format("%s has left the game", userName);
        NotificationMessage notification = new Gson().fromJson(message, NotificationMessage.class);
        game.broadcast(userName, notification);
    }

    public void resign(int gameID, String userName) throws IOException {
        checkForGame(gameID);
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

        checkForGame(gameID);
        var game = games.get(gameID);
        var message = String.format("%s has moved %s%s to %s%s", userName, startRow, columns[startColumn - 1],
                endRow, columns[endCol - 1]);
        NotificationMessage notification = new Gson().fromJson(message, NotificationMessage.class);
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
