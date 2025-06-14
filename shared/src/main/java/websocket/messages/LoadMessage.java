package websocket.messages;

public class LoadMessage extends ServerMessage {
    private final String game;


    public LoadMessage(ServerMessageType type, String game) {
        super(type);
        this.game = game;
    }

    public String toString() {
        return game;
    }
}
