package dataaccess;

import com.google.gson.Gson;
import model.GameData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class MySqlGameDAO implements GamesDAO{
    SqlUpdate updater = new SqlUpdate();

    public MySqlGameDAO() throws DataAccessException {
        String[] createStatements = {
                """
                CREATE TABLE IF NOT EXISTS games (
                    `gameID` varchar(256) NOT NULL,
                    `whiteUsername` varchar(256) DEFAULT NULL,
                    `blackUsername` varchar(256) DEFAULT NULL,
                    `gameName` varchar(256) NOT NULL,
                    `json` TEXT DEFAULT NULL,
                    PRIMARY KEY(`gameID`)
                )
                """
        };
        updater.configureDatabase(createStatements);
    }

    public GameData getGame(String gameID) throws DataAccessException {
        if(gameID == null) {
            throw new DataAccessException("null input");
        }
        try(var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT json FROM games WHERE gameID=?";
            try(var ps = conn.prepareStatement(statement)) {
                ps.setString(1, gameID);
                try(var rs = ps.executeQuery()) {
                    if(rs.next()) {
                        return readGame(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("unable to read data: %s", e.getMessage()));
        }
        return null;
    }


    public GameData createGame(String gameName) throws DataAccessException {
        if(gameName == null) {
            throw new DataAccessException("null input");
        }
        GameData gameData = new GameData(gameName);
        var statement = "INSERT INTO games (gameID, gameName, json) VALUES (?, ?, ?)";
        var json = new Gson().toJson(gameData);
        updater.executeUpdate(statement, gameData.getGameID(), gameData.getGameName(), json);
        return gameData;
    }

    public void updateGame(GameData game, String gameID)  throws DataAccessException {
        if(game == null || gameID == null) {
            throw new DataAccessException("one or more inputs is null");
        }
        var statement = "UPDATE games SET whiteUsername=?, blackUsername=?, json=? WHERE gameID=?";
        var json = new Gson().toJson(game);
        updater.executeUpdate(statement, game.getWhiteUsername(), game.getBlackUsername(), json, gameID);
    }

    public ArrayList<GameData> listGames() throws DataAccessException {
        ArrayList<GameData> games = new ArrayList<>(1);
        try(var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT json FROM games";
            try(var ps = conn.prepareStatement(statement)) {
                try(var rs = ps.executeQuery()) {
                    while(rs.next()) {
                        games.add(readGame(rs));
                    }
                    return games;
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("unable to read data: %s", e.getMessage()));
        }
    }

    public void clear() throws DataAccessException {
        try(var conn = DatabaseManager.getConnection()) {
            var statement = "DELETE FROM games";
            try(var ps = conn.prepareStatement(statement)) {
                ps.executeUpdate();
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("unable to read data: %s", e.getMessage()));
        }
    }

    public boolean isEmpty() throws DataAccessException {
        try(var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT COUNT(*) FROM games";
            try(var ps = conn.prepareStatement(statement)) {
                try(var rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("unable to read data: %s", e.getMessage()));
        }
    }

    private GameData readGame(ResultSet rs) throws SQLException {
        var json = rs.getString("json");
        return new Gson().fromJson(json, GameData.class);
    }
}
