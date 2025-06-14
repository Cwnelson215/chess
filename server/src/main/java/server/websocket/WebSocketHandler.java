package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.LeaveRequest;
import service.UpdateRequest;
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
    private boolean gameOver = false;
    private final UserService userService;

    public WebSocketHandler(UserService userService) {
        this.userService = userService;
    }

    private final ConcurrentHashMap<Integer, ConnectionManager> games = new ConcurrentHashMap<>();
    String[] columns = {" a ", " b ", " c ", " d ", " e ", " f ", " g ", " h "};

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException, DataAccessException {
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> connect(command.getGameID(), session, command.getAuthToken());
            case LEAVE -> leave(command.getGameID(), command.getAuthToken(), session);
            case RESIGN -> resign(command.getGameID(), command.getAuthToken(), session);
            case MAKE_MOVE -> move(command.getGameID(), command.getMove(), command.getAuthToken(), session);
        }
    }

    public void connect(int gameID, Session session,String authToken) throws IOException, DataAccessException {
        checkForGame(gameID);
        var game = games.get(gameID);
        if(gameOver) {
            gameOver = false;
        }
        String userName = "";
        try {
            userName = userService.getUserName(authToken);
            game.add(userName, session);
            var playerColor = userService.getPlayerColor(userName, String.valueOf(gameID));
            var message = String.format("%s has joined the game as an observer", userName);
            if(!playerColor.equals("observer")) {
                message = String.format("%s has joined the game as the %s player", userName, playerColor);
            }
            NotificationMessage notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    message, String.valueOf(gameID), "LEAVE");
            LoadMessage loadMessage = new LoadMessage(ServerMessage.ServerMessageType.LOAD_GAME,
                    new Gson().toJson(userService.getGame(gameID), GameData.class));
            game.broadcast(userName, notification);
            game.notify(userName, loadMessage);
        } catch(Exception e) {
            sendError(e, session);
        }
    }

    public void leave(int gameID, String authToken, Session session) throws IOException {
        try {
            String userName = userService.getUserName(authToken);
            String playerColor = userService.getPlayerColor(userName, String.valueOf(gameID));
            var game = games.get(gameID);
            game.remove(userName);
            var message = String.format("%s has left the game", userName);
            NotificationMessage notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    message, String.valueOf(gameID), "LEAVE");
            game.broadcast(userName, notification);
            userService.leave(new LeaveRequest(playerColor, gameID));
        } catch (Exception e) {
            sendError(e, session);
        }
    }

    public void resign(int gameID, String authToken, Session session) throws IOException {
        try {
            String userName = userService.getUserName(authToken);
            String playerColor = userService.getPlayerColor(userName, String.valueOf(gameID));
            if(playerColor.equals("observer")) {
                throw new IOException("Observers may not resign the game");
            }
            var game = games.get(gameID);
            if(game.checkResign()) {
                throw new IOException("Games has already been resigned");
            }
            var message = String.format("%s has resigned", userName);
            NotificationMessage notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    message, String.valueOf(gameID), "RESIGN");
            game.broadcast(null, notification);
            game.resign();
            gameOver = true;
        } catch (Exception e) {
            sendError(e, session);
        }
    }

    public void move(int gameID, ChessMove move, String authToken, Session session) throws IOException, DataAccessException {
        var startRow = move.getStartPosition().getRow();
        var startColumn = move.getStartPosition().getColumn();
        var endRow = move.getEndPosition().getRow();
        var endCol = move.getEndPosition().getColumn();
        checkForGame(gameID);
        var game = games.get(gameID);
        GameData gameState = userService.getGame(gameID);
        try {
            checkGameState(null, gameState);
            String userName = userService.getUserName(authToken);
            String playerColor = userService.getPlayerColor(userName, String.valueOf(gameID));
            checkTurn(gameState, playerColor);
            gameState.makeMove(move);
            String message;
            if (playerColor.equals("BLACK")) {
                message = String.format("%s has moved%s\b%s to%s\b%s", userName, columns[startColumn - 1], startRow,
                        columns[endCol - 1], endRow);
                message = checkGameState(message, gameState);
            } else {
                message = String.format("%s has moved%s\b%s to%s\b%s", userName, columns[8 - startColumn], startRow,
                        columns[8 - endCol], endRow);
                message = checkGameState(message, gameState);
            }
            NotificationMessage notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    message, String.valueOf(gameID), "MOVE");
            LoadMessage load = new LoadMessage(ServerMessage.ServerMessageType.LOAD_GAME,
                    new Gson().toJson(gameState));
            game.broadcast(null, load);
            game.broadcast(userName, notification);
            userService.updateGame(new UpdateRequest(gameState, String.valueOf(gameID)));
        } catch (Exception e) {
            sendError(e, session);
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

    public String checkGameState(String message, GameData gameState) throws InvalidMoveException {
        if(gameOver) {
            throw new InvalidMoveException("Game over, no more moves may be made");
        }
        var game = gameState.getGame();
        if(game.isInCheckmate(ChessGame.TeamColor.WHITE)) {
            gameOver = true;
            return message + String.format(". %s is in checkmate!\nGame Over!", gameState.getWhiteUsername());
        } else if(game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
            gameOver = true;
            return message + String.format(". %s is in checkmate!\nGame Over!", gameState.getBlackUsername());
        }
        if(game.isInStalemate(ChessGame.TeamColor.BLACK) || game.isInStalemate(ChessGame.TeamColor.WHITE)) {
            gameOver = true;
            return message + "Stalemate!\nGame Over!";
        }
        if(game.isInCheck(ChessGame.TeamColor.WHITE)) {
            return message + String.format(". %s is in check!", gameState.getWhiteUsername());
        } else if(game.isInCheck(ChessGame.TeamColor.BLACK)) {
            return message + String.format(". %s is in check!", gameState.getBlackUsername());
        }
        return message;
    }

    public void checkTurn(GameData game, String playerColor) throws InvalidMoveException {
        if(playerColor.equals("WHITE")) {
            if(!game.getGame().getTeamTurn().equals(ChessGame.TeamColor.WHITE)) {
                throw new InvalidMoveException("It's not your turn");
            }
        } else if(playerColor.equals("BLACK")) {
            if(!game.getGame().getTeamTurn().equals(ChessGame.TeamColor.BLACK)) {
                throw new InvalidMoveException("It's not your turn");
            }
        } else {
            throw new InvalidMoveException("Observers cannot make moves");
        }
    }

    public void sendError(Exception msg, Session session) throws IOException {
        var error = new ErrorMessage(ServerMessage.ServerMessageType.ERROR,
                "Error: " + msg.getMessage());
        session.getRemote().sendString(new Gson().toJson(error));
    }
}
