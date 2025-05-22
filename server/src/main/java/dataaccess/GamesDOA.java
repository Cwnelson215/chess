package dataaccess;

import model.GameData;

import java.util.HashMap;
import java.util.Map;

public class GamesDOA {
    private final Map<String, GameData> games = new HashMap<>(1);

    public GameData getGame(String gameID) {
        return games.get(gameID);
    }

    public GameData creatGame(String gameName) {
        GameData gameData = new GameData(gameName);
        games.put(gameData.getGameID(), gameData);
        return gameData;
    }

    public Map listGames() {
        return games;
    }
}
