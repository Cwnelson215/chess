package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    private final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public void add(String userName, Session session) {
        var connection = new Connection(userName, session);
        connections.put(userName, connection);
    }

    public void remove(String userName) {
        connections.remove(userName);
    }

    public void broadcast(String excludedUserName, ServerMessage msg) throws IOException {
        ArrayList<Connection> removeList = new ArrayList<>();
        for(var c : connections.values()) {
            if(c.session.isOpen()) {
                if(!c.userName.equals(excludedUserName)) {
                    c.send(new Gson().toJson(msg));
                }
            } else {
                removeList.add(c);
            }
        }

        for(var c : removeList) {
            connections.remove(c.userName);
        }
    }
}
