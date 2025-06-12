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
        Random random = new Random();
        return String.valueOf(random.nextInt(1000, 9999));
    }

    public void assignPlayerColor(String username, String playerColor) {
        if(Objects.equals(playerColor, "WHITE")) {
            whiteUsername = username;
        } else {
            blackUsername = username;
        }
    }

    public String getColorUsername(String color) {
        if(Objects.equals(color, "WHITE")) {
            return whiteUsername;
        } else {
            return blackUsername;
        }
    }

    public String getGameName() {return gameName;}

    public String getGameID() {
        return gameID;
    }

    public String getWhiteUsername(){return whiteUsername;}

    public String getBlackUsername(){return blackUsername;}

    public void setGame(ChessGame updatedGame) {
        this.game = updatedGame;
    }

    public ChessGame getGame() {return game;}
}
