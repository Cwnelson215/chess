package dataaccess;

import model.GameData;

import java.util.ArrayList;
import java.util.Map;

public interface GamesDAO {
    GameData getGame(String gameID) throws DataAccessException;
    GameData createGame(String gameName) throws DataAccessException;
    ArrayList<GameData> listGames()  throws DataAccessException;
    void updateGame(GameData game, String gameID)  throws DataAccessException;
    void clear()  throws DataAccessException;
    boolean isEmpty()  throws DataAccessException;
}
