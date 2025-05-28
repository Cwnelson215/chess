package dataaccess;

import com.google.gson.Gson;
import model.AuthData;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MySqlAuthDAO implements AuthDAO{

    SqlUpdate updater = new SqlUpdate();

    public MySqlAuthDAO() throws DataAccessException {
        String[] createStatements = {
                """
            CREATE TABLE IF NOT EXISTS auths (
                `username` varchar NOT NULL,
                `authToken` varchar NOT NULL,
                `json` TEXT DEFAULT NULL,
                PRIMARY KEY (`authToken`)
            )
            """
        };

        updater.configureDatabase(createStatements);
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        try(var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authToken, json FROM auths WHERE authToken=?";
            try(var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try(var rs = ps.executeQuery()) {
                    if(rs.next()) {
                        return readAuth(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("enable to read data: %s", e.getMessage()));
        }
        return null;
    }

    public String getUsername(String authToken) throws DataAccessException {
        try(var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username FROM auths WHERE authToken=?";
            try(var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try(var rs = ps.executeQuery()) {
                    if(rs.next()) {
                        return rs.getString(1);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("enable to read data: %s", e.getMessage()));
        }
        return null;
    }

    public void createAuth(AuthData newAuth) throws DataAccessException {
        var statement = "INSERT INTO auths (authToken, username, json) VALUES (?, ?, ?)";
        var json = new Gson().toJson(newAuth);
        updater.executeUpdate(statement, newAuth.getAuthToken(), newAuth.getUsername(), json);
    }

    public void deleteAuth(String authToken) throws DataAccessException {
        try(var conn = DatabaseManager.getConnection()) {
            var statement = "DELETE FROM auths WHERE authToken=?";
            try(var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                ps.executeQuery();
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("enable to read data: %s", e.getMessage()));
        }
    }

    public void clear() throws DataAccessException {
        try(var conn = DatabaseManager.getConnection()) {
            var statement = "DELETE FROM auths";
            try(var ps = conn.prepareStatement(statement)) {
                ps.executeUpdate();
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("enable to read data: %s", e.getMessage()));
        }
    }

    public boolean isEmpty() throws DataAccessException {
        try(var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT COUNT(*) FROM auths";
            try(var ps = conn.prepareStatement(statement)) {
                try(var rs = ps.executeQuery()) {
                    return rs.getInt(1) == 0;
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("enable to read data: %s", e.getMessage()));
        }
    }

    private AuthData readAuth(ResultSet rs) throws SQLException {
        var json = rs.getString("json");
        return new Gson().fromJson(json, AuthData.class);
    }
}
