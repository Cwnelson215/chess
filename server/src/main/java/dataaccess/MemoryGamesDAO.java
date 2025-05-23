package dataaccess;

import model.GameData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MemoryGamesDAO implements GamesDAO {
    private final Map<String, GameData> games = new HashMap<>(1);

    public GameData getGame(String gameID) {
        return games.get(gameID);
    }

    public GameData createGame(String gameName) {
        GameData gameData = new GameData(gameName);
        games.put(gameData.getGameID(), gameData);
        return gameData;
    }

    public void clear() {
        games.clear();
    }

    public ArrayList<GameData> listGames() {
        return makeArray(games);
    }

    public void updateGame(GameData game, String gameID) {
        games.put(gameID, game);
    }

    public boolean isEmpty() {
        return games.isEmpty();
    }

    public ArrayList<GameData> makeArray(Map<String, GameData> map) {
        ArrayList<GameData> array = new ArrayList<>(1);
        map.forEach((key, value) -> {
            array.add(value);
        });
        return array;
    }
}
