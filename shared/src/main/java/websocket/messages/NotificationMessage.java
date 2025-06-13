package websocket.messages;

public class NotificationMessage extends ServerMessage {
    private final String message;
    private String gameID;
    private String type;

    public NotificationMessage(ServerMessageType messageType, String message, String gameID, String type) {
        super(messageType);
        this.type = type;
        this.message = message;
        this.gameID = gameID;
    }

    public String toString() {
        return message;
    }

    public String getGameID() {
        return gameID;
    }

    public String getType() {
        return type;
    }
}
