package model;

import chess.ChessGame;

import java.util.Random;

public class GameData {
    private String gameID;
    private String whiteUsername;
    private String blackUsername;
    private String gameName;
    private ChessGame game;

    public GameData(String gameName) {
        this.gameName = gameName;
        this.game = new ChessGame();
        this.gameID = generateGameID();
    }

    public String generateGameID() {
        StringBuilder gameID = new StringBuilder();
        Random random = new Random();
        for(int i = 0; i < 16; i++) {
            gameID.append(random.nextInt(10));
        }
        return gameID.toString();
    }

    public void assignPlayerColor(String username) {
        if(whiteUsername == null) {
            whiteUsername = username;
        } else {
            blackUsername = username;
        }
    }

    public String getGameID() {
        return gameID;
    }
}
