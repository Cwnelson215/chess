package dataaccess;

import model.GameData;

import java.util.Map;

public interface GamesDAO {
    GameData getGame(int gameID);
    GameData createGame(String gameName);
    Map<Integer, GameData> listGames();
    void updateGame(GameData game, int gameID);
    void clear();
    boolean isEmpty();
}
