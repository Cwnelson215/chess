package dataaccess;

import model.GameData;

import java.util.ArrayList;
import java.util.Map;

public interface GamesDAO {
    GameData getGame(String gameID);
    GameData createGame(String gameName);
    ArrayList<GameData> listGames();
    void updateGame(GameData game, String gameID);
    void clear();
    boolean isEmpty();
}
