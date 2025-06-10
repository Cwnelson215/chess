package websocket.messages;

public class LoadMessage extends ServerMessage {
    private final String message;


    public LoadMessage(ServerMessageType type, String message) {
        super(type);
        this.message = message;
    }

    public String toString() {
        return message;
    }
}
