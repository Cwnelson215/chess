package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import javax.swing.*;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {
    private final ConcurrentHashMap<Integer, ConnectionManager> games = new ConcurrentHashMap<>();

    @OnWebSocketMessage
    public void onMessage(Session session, String message, String userName) throws IOException {
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> connect(command.getGameID(), session, userName);
        }

    }

    public void connect(int gameID, Session session, String userName) throws IOException {
        if(games.contains(gameID)) {
            var game = games.get(gameID);
            var message = String.format("%s has joined the game", userName);
            game.broadcast(userName, message);
        }
    }


    public void addGame(int gameID) {
        var game = new ConnectionManager();
        games.put(gameID, game);
    }
}
