package server.websocket;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public void add(String visitorName, Session session) {
        var connection = new Connection(visitorName, session);
        connections.put(visitorName, connection);
    }

    public void remove(String visitorName) {
        connections.remove(visitorName);
    }

    public void broadcast(String excludedVisitorName, String msg) throws IOException {
        ArrayList<Connection> removeList = new ArrayList<>();
        for(var c : connections.values()) {
            if(c.session.isOpen()) {
                if(c.visitorName.equals(excludedVisitorName)) {
                    c.send(msg);
                }
            } else {
                removeList.add(c);
            }
        }

        for(var c : removeList) {
            connections.remove(c.visitorName);
        }
    }
}
