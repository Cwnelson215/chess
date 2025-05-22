package model;

import chess.ChessGame;

import java.util.Objects;
import java.util.Random;

public class GameData {
    private final String gameID = generateGameID();
    private String whiteUsername;
    private String blackUsername;
    private final String gameName;
    private ChessGame game;

    public GameData(String gameName) {
        this.gameName = gameName;
        this.game = new ChessGame();
    }

    public String generateGameID() {
        StringBuilder gameID = new StringBuilder();
        Random random = new Random();
        for(int i = 0; i < 16; i++) {
            gameID.append(random.nextInt(10));
        }
        return gameID.toString();
    }

    public void assignPlayerColor(String username, String playerColor) {
        if(Objects.equals(playerColor, "WHITE")) {
            whiteUsername = username;
        } else {
            blackUsername = username;
        }
    }

    public String getGameName() {return gameName;}
    public String getGameID() {
        return gameID;
    }
}
