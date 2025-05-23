package service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    UserService service = new UserService();
    private String username = "Carter";
    private String password = "1234";
    private String email = "123@456.com";

    @Test
    public void registerSuccess() {
        var result = service.register(new RegisterRequest(username, password, email));
        assertEquals(username, result.username());
        assertNotNull(result.authToken());
    }

    @Test
    public void registerFailureBadRequest() throws HTTPException {
        assertThrows(HTTPException.class, () -> service.register(new RegisterRequest(null, password, email)));
    }

    @Test
    public void registerFailureUsernameTaken() throws HTTPException {
        service.register(new RegisterRequest(username, password, email));
        assertThrows(HTTPException.class, () -> service.register(new RegisterRequest(username, password, email)));
    }

    @Test
    public void logoutSuccess() {
        var result = service.register(new RegisterRequest(username, password, email));

        assertDoesNotThrow(() -> service.logout(result.authToken()));
    }

    @Test public void logoutFailure() throws HTTPException {
        String authToken = "auth";
        assertThrows(HTTPException.class, () -> service.logout(authToken));
    }


    @Test
    public void loginSuccess() {
        var result = service.register(new RegisterRequest(username, password, email));
        service.logout(result.authToken());
        var newResult = service.login(new LoginRequest(username, password));

        assertEquals(username, newResult.username());
        assertNotNull(newResult.authToken());
    }

    @Test
    public void loginFailureUnauthorized()  {
        var result = service.register(new RegisterRequest(username, password, email));
        service.logout(result.authToken());
        String username2 = "NotCarter";

        assertThrows(HTTPException.class, () -> service.login(new LoginRequest(username2, password)));
    }

    @Test
    public void loginFailureBadRequest()  {
        var result = service.register(new RegisterRequest(username, password, email));
        service.logout(result.authToken());
        String username2 = null;

        assertThrows(HTTPException.class, () -> service.login(new LoginRequest(username2, password)));
    }

    @Test
    public void listGamesSuccess() {
        var result = service.register(new RegisterRequest(username, password, email));
        service.create("game", result.authToken());
        assertDoesNotThrow(() -> service.listGames(result.authToken()));
    }

    @Test
    public void listGamesFailure() {
        var result = service.register(new RegisterRequest(username, password, email));
        service.create("game", result.authToken());
        assertThrows(HTTPException.class, () -> service.listGames("Bad Auth"));
    }

    @Test
    public void createSuccess() {
        var result= service.register(new RegisterRequest(username, password, email));
        var game = service.create("game", result.authToken());
        assertDoesNotThrow(() -> service.gamesDatabase.getGame(String.valueOf(game.gameID())));
    }

    @Test
    public void createFailureNullGameName() throws HTTPException {
        var result= service.register(new RegisterRequest(username, password, email));
        assertThrows(HTTPException.class, () -> service.create(null, result.authToken()));
    }

    @Test
    public void createFailureBadAuthToken() throws HTTPException {
        assertThrows(HTTPException.class, () -> service.create("new game", "notValidAuthToken"));
    }

    @Test
    public void joinSuccess() {
        var result= service.register(new RegisterRequest(username, password, email));
        var game = service.create("game", result.authToken());

        assertDoesNotThrow(() -> service.join(new JoinRequest("WHITE", game.gameID()), result.authToken()));
        assertEquals(username, service.gamesDatabase.getGame(String.valueOf(game.gameID())).getWhiteUsername());
    }

    @Test
    public void joinFailureBadGameID() {
        var result = service.register(new RegisterRequest(username, password, email));
        assertThrows(HTTPException.class, () -> service.join(new JoinRequest("WHITE", 132), result.authToken()));
    }

    @Test
    public void joinFailureBadColorTaken() {
        var result= service.register(new RegisterRequest(username, password, email));
        var game = service.create("game", result.authToken());
        service.join(new JoinRequest("WHITE", game.gameID()), result.authToken());

        var result2 = service.register(new RegisterRequest("c", "b", "d"));
        assertThrows(HTTPException.class, () -> service.join(new JoinRequest("WHITE", game.gameID()), result2.authToken()));
    }

    @Test
    public void joinFailureColorNull() {
        var result= service.register(new RegisterRequest(username, password, email));
        var game = service.create("game", result.authToken());
        assertThrows(HTTPException.class, () -> service.join(new JoinRequest(null, game.gameID()), result.authToken()));
    }

    @Test
    public void clearSuccessSingle() {
        var result= service.register(new RegisterRequest(username, password, email));
        var game = service.create("game", result.authToken());
        assertDoesNotThrow(() -> service.clearDataBase());
        assertTrue(service.authDatabase.isEmpty() && service.gamesDatabase.isEmpty() && service.userDatabase.isEmpty());
    }
}