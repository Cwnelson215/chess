package dataaccess;

import model.GameData;

import java.util.Map;

public interface GamesDAO {
    GameData getGame(String gameID);
    GameData createGame(String gameName);
    Map listGames();
    void updateGame(GameData game, String gameID);
    void clear();
}
