import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;
    private static UserData user = new UserData("Carter", "password", "email@email.com");

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(String.format("http://localhost:%d", port));
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clear() throws DataAccessException {
        server.clearData(null, null);
    }

    @Test
    public void sampleTest() {
        assertTrue(true);
    }

    @Test
    public void registerSuccess() throws ResponseException {
        var auth = facade.register(user);
        assertNotNull(auth);
        assertTrue(auth.getAuthToken().length() > 10);
    }

    @Test
    public void registerFailure() throws ResponseException {
        assertThrows(ResponseException.class, () -> facade.register(new UserData(null, null, null)));
    }

    @Test
    public void logoutSuccess() throws ResponseException {
        var auth = facade.register(user);
        var result = facade.logout(auth.getAuthToken());
        assertNull(result);
    }

    @Test
    public void logoutFailure() throws ResponseException {
        facade.register(user);
        assertThrows(ResponseException.class, () -> facade.logout("incorrect-auth"));
    }

    @Test
    public void loginSuccess() throws ResponseException {
        var auth = facade.register(user);
        facade.logout(auth.getAuthToken());
        var newAuth = facade.login(user.getUsername(), user.getPassword());
        assertNotEquals(auth.getAuthToken(), newAuth.getAuthToken());
    }

    @Test
    public void loginFailure() throws ResponseException {
        var auth = facade.register(user);
        facade.logout(auth.getAuthToken());
        assertThrows(ResponseException.class, () -> facade.login(user.getUsername(), "wrong"));
    }

}
