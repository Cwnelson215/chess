package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DataAccessTests {
    UserDAO userDatabase;
    AuthDAO authDatabase;
    GamesDAO gamesDatabase;

    private final String username = "Carter";
    private final String password = "1234";
    private final String email = "123@456.com";
    UserData user = new UserData(username, password, email);
    AuthData auth = new AuthData(username);

    {
        try {
            userDatabase = new MySqlUserDAO();
            authDatabase = new MySqlAuthDAO();
            gamesDatabase = new MySqlGameDAO();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

    }
    @BeforeEach
    @Test
    public void clearSuccess() throws DataAccessException  {
        assertDoesNotThrow(() -> userDatabase.clear());
        assertDoesNotThrow(() -> authDatabase.clear());
        assertDoesNotThrow(() -> gamesDatabase.clear());
    }

    @Test
    public void createSuccess() {
        assertDoesNotThrow(() -> userDatabase.createUser(new UserData(username, password, email)));
        assertDoesNotThrow(() -> authDatabase.createAuth(auth));
        assertDoesNotThrow(() -> gamesDatabase.createGame("newGame"));
    }

    @Test
    public void createFailure() {
        assertThrows(DataAccessException.class, () -> userDatabase.createUser(new UserData(null, password, email)));
        assertThrows(DataAccessException.class, () -> authDatabase.createAuth(null));
        assertThrows(DataAccessException.class, () -> gamesDatabase.createGame(null));
    }

    @Test
    public void getSuccess() throws DataAccessException {
        userDatabase.createUser(user);
        var userResult = userDatabase.getUser(username);
        assertNotNull(userResult);
        assertEquals(username, userResult.getUsername());
        assertDoesNotThrow(() -> userDatabase.getUser(username, password));

        authDatabase.createAuth(auth);
        var authResult = authDatabase.getAuth(auth.getAuthToken());
        assertNotNull(authResult);
        assertEquals(auth.getAuthToken(), authResult.getAuthToken());

        var game = gamesDatabase.createGame("newGame");
        var gameResult = gamesDatabase.getGame(game.getGameID());
        assertNotNull(gameResult);
        assertEquals(game.getGameName(), gameResult.getGameName());
    }

    @Test
    public void getFailure() throws DataAccessException {
        userDatabase.createUser(user);
        assertThrows(DataAccessException.class, () -> userDatabase.getUser(null));
        assertThrows(DataAccessException.class, () -> userDatabase.getUser(username, null));
        assertThrows(DataAccessException.class, () -> userDatabase.getUser(null, password));
        assertThrows(DataAccessException.class, () -> userDatabase.getUser(null, null));

        authDatabase.createAuth(auth);
        assertThrows(DataAccessException.class, () -> authDatabase.getAuth(null));

        var game = gamesDatabase.createGame("newGame");
        var gameResult = gamesDatabase.getGame(game.getGameID());
        assertThrows(DataAccessException.class, () -> gamesDatabase.getGame(null));
    }

    @Test
    public void authGetUsernameSuccess() throws DataAccessException {
        authDatabase.createAuth(auth);
        String name = authDatabase.getUsername(auth.getAuthToken());
        assertNotNull(name);
    }

    @Test
    public void authGetUsernameFailure() throws DataAccessException {
        authDatabase.createAuth(auth);
        String name = authDatabase.getUsername(null);
        assertNull(name);
    }

    @Test
    public void deleteAuthSuccess() throws DataAccessException {
        authDatabase.createAuth(auth);
        assertDoesNotThrow(() -> authDatabase.deleteAuth(auth.getAuthToken()));
    }

    @Test
    public void deleteAuthFailure() throws DataAccessException {
        authDatabase.createAuth(auth);
        assertThrows(DataAccessException.class, () -> authDatabase.deleteAuth(null));
    }

    @Test
    public void isEmpty() {
        assertDoesNotThrow(() -> userDatabase.isEmpty());
        assertDoesNotThrow(() -> authDatabase.isEmpty());
        assertDoesNotThrow(() -> gamesDatabase.isEmpty());
    }

    @Test
    public void updateGameSuccess() throws DataAccessException {
        var game = gamesDatabase.createGame("newGame");
        game.assignPlayerColor("Carter", "WHITE");
        assertDoesNotThrow(() -> gamesDatabase.updateGame(game, game.getGameID()));
    }

    @Test
    public void updateGameFailure() throws DataAccessException {
        var game = gamesDatabase.createGame("newGame");
        game.assignPlayerColor("Carter", "WHITE");
        assertThrows(DataAccessException.class, () -> gamesDatabase.updateGame(null, game.getGameID()));
        assertThrows(DataAccessException.class, () -> gamesDatabase.updateGame(game, null));
    }


}
