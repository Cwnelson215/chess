package dataaccess;

import com.google.gson.Gson;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.ResultSet;
import java.sql.SQLException;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class MySqlUserDAO implements UserDAO{
    SqlUpdate updater = new SqlUpdate();

    public MySqlUserDAO() throws DataAccessException {
        String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS users (
                `id` int NOT NULL AUTO_INCREMENT,
                `username` varchar(256) NOT NULL,
                `password` varchar(256) NOT NULL,
                `email` varchar(256) NOT NULL,
                `json` TEXT DEFAULT NULL,
                PRIMARY KEY (`id`)
            )
            """
        };
        updater.configureDatabase(createStatements);
    }

    public UserData getUser(String username) throws DataAccessException {
        if(username == null) {
            throw new DataAccessException("input null");
        }
        try(var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, json FROM users WHERE username=?";
            try(var ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try(var rs = ps.executeQuery()) {
                    if(rs.next()) {
                        return readUser(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    public UserData getUser(String username, String password) throws DataAccessException {
        if(username == null || password == null) {
            throw new DataAccessException("one or more inputs are null");
        }
        if(verifyPassword(username, password)) {
            return getUser(username);
        }
        return null;
    }

    public void createUser(UserData newUser) throws DataAccessException {
        String password = hashPassword(newUser.getPassword());
        UserData user = new UserData(newUser.getUsername(), password, newUser.getEmail());
        var statement = "INSERT INTO users (username, password, email, json) VALUES (?, ?, ?, ?)";
        var json = new Gson().toJson(user);

        updater.executeUpdate(statement, newUser.getUsername(), password, newUser.getEmail(), json);
    }

    public void clear() throws DataAccessException {
        try(var conn = DatabaseManager.getConnection()) {
            var statement = "DELETE FROM users";
            try(var ps = conn.prepareStatement(statement)) {
                ps.executeUpdate();
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("unable to read data: %s", e.getMessage()));
        }
    }

    public boolean isEmpty() throws DataAccessException {
        try(var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT COUNT(*) FROM users";
            try(var ps = conn.prepareStatement(statement)) {
                try(var rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("unable to read data: %s", e.getMessage()));
        }
    }

    private UserData readUser(ResultSet rs) throws SQLException {
        var json = rs.getString("json");
        return new Gson().fromJson(json, UserData.class);
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    private boolean verifyPassword(String username, String password) throws DataAccessException {
        var hashedPassword = getHashedPassword(username);
        if(hashedPassword != null) {
            return BCrypt.checkpw(password, hashedPassword);
        }
        return false;
    }

    private String getHashedPassword(String username) throws DataAccessException {
        try(var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT password FROM users WHERE username=?";
            try( var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, username);
                try(var rs = preparedStatement.executeQuery()) {
                    if(rs.next()) {
                        return rs.getString("password");
                    }
                }
            }
        } catch(SQLException e) {
            throw new DataAccessException(String.format("unable to read data: %s", e.getMessage()));
        }
        return null;
    }
}
