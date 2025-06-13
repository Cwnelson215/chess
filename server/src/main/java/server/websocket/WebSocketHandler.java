package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.JoinRequest;
import service.UserService;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {
    private UserService userService;

    public WebSocketHandler(UserService userService) {
        this.userService = userService;
    }

    private final ConcurrentHashMap<Integer, ConnectionManager> games = new ConcurrentHashMap<>();
    private boolean sendToUser = false;
    String[] columns = {" a ", " b ", " c ", " d ", " e ", " f ", " g ", " h "};

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException, DataAccessException {
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> connect(command.getGameID(), session, command.getAuthToken());
            case LEAVE -> leave(command.getGameID(), command.getUserName());
            case RESIGN -> resign(command.getGameID(), command.getUserName());
            case MAKE_MOVE -> move(command.getGameID(), command.getUserName(), command.getMove(), command.getPlayerColor(), command.getGameState());
        }
    }

    public void connect(int gameID, Session session,String authToken) throws IOException, DataAccessException {
        checkForGame(gameID);
        var game = games.get(gameID);
        try {
            var userName = userService.getUserName(authToken);
            userService.join(new JoinRequest(playerColor, gameID), authToken);
            game.add(userName, session);
            var message = String.format("%s has joined the game", userName);
            NotificationMessage notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    message, String.valueOf(gameID), "LEAVE");
            LoadMessage loadMessage = new LoadMessage(ServerMessage.ServerMessageType.LOAD_GAME,
                    new Gson().toJson(userService.getGame(gameID), GameData.class));
            game.broadcast(userName, notification);
            game.notify(userName, loadMessage);
        } catch(Exception e) {
            game.notify(userName, new ErrorMessage(ServerMessage.ServerMessageType.ERROR,
                    "Error: " + e.getMessage()));
        }
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

    public void move(int gameID, String userName, ChessMove move, String playerColor, GameData gameState) throws IOException {
        var startRow = move.getStartPosition().getRow();
        var startColumn = move.getStartPosition().getColumn();
        var endRow = move.getEndPosition().getRow();
        var endCol = move.getEndPosition().getColumn();

        checkForGame(gameID);
        var game = games.get(gameID);
        String message;
        if(playerColor.equals("BLACK")) {
            message = String.format("%s has moved%s\b%s to%s\b%s", userName, columns[startColumn - 1], startRow,
                    columns[endCol - 1], endRow);
            message = checkGameState(message, gameState);
        } else {
            message = String.format("%s has moved%s\b%s to%s\b%s", userName, columns[8 - startColumn], startRow,
                    columns[8 - endCol], endRow);
        }
        NotificationMessage notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                message, String.valueOf(gameID), "MOVE");
        if(sendToUser) {
            game.broadcast(null, notification);
        } else {
            game.broadcast(userName, notification);
        }
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

    public String checkGameState(String message, GameData gameState) {
        var game = gameState.getGame();
        if(game.isInCheckmate(ChessGame.TeamColor.WHITE)) {
            sendToUser = true;
            return message + String.format(". %s is in checkmate!\nGame Over!", gameState.getWhiteUsername());
        } else if(game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
            sendToUser = true;
            return message + String.format(". %s is in checkmate!\nGame Over!", gameState.getBlackUsername());
        }
        if(game.isInStalemate(ChessGame.TeamColor.BLACK) || game.isInStalemate(ChessGame.TeamColor.WHITE)) {
            sendToUser = true;
            return message + "Stalemate!\nGame Over!";
        }
        if(game.isInCheck(ChessGame.TeamColor.WHITE)) {
            sendToUser = true;
            return message + String.format(". %s is in check!", gameState.getWhiteUsername());
        } else if(game.isInCheck(ChessGame.TeamColor.BLACK)) {
            sendToUser = true;
            return message + String.format(". %s is in check!", gameState.getBlackUsername());
        }
        return message;
    }
}
