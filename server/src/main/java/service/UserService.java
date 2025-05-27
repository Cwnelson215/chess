package service;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class UserService {
    UserDAO userDatabase = new MemoryUserDAO();
    AuthDAO authDatabase = new MemoryAuthDAO();
    GamesDAO gamesDatabase = new MemoryGamesDAO();

    public void clearDataBase() throws HTTPException, DataAccessException {
        userDatabase.clear();
        authDatabase.clear();
        gamesDatabase.clear();
        if(!userDatabase.isEmpty() || !authDatabase.isEmpty() || !gamesDatabase.isEmpty()) {
            throw new HTTPException(500, "Failed to clear databases");
        }
    }

    public RegisterResult register(RegisterRequest registerRequest) throws HTTPException, DataAccessException {
        String username = registerRequest.username();
        if(username == null || registerRequest.password() == null || registerRequest.email() == null) {
            throw new HTTPException(400, "Bad Request");
        }
        UserData user = userDatabase.getUser(username);
        if(user != null) {
            throw new HTTPException(403, "Error: username already taken");
        }
        UserData newUser = new UserData(username, registerRequest.password(),
                registerRequest.email());
        AuthData userAuth = new AuthData(username);
        userDatabase.createUser(newUser);
        authDatabase.createAuth(userAuth);
        return new RegisterResult(username, userAuth.getAuthToken());
    }

    public LoginResult login(LoginRequest loginRequest) throws HTTPException, DataAccessException {
        String username = loginRequest.username();
        String password = loginRequest.password();
        var user = userDatabase.getUser(username, password);
        if(username == null || password == null) {
            throw new HTTPException(400, "Bad Request");
        }
        if(user == null) {
            throw new HTTPException(401, "Unauthorized");
        }
        AuthData newAuth = new AuthData(username);
        authDatabase.createAuth(newAuth);
        AuthData auth = authDatabase.getAuth(newAuth.getAuthToken());
        return new LoginResult(username, auth.getAuthToken());
    }

    public Object logout(String authToken) throws HTTPException {
        checkAuthorization(authToken);
        authDatabase.deleteAuth(authToken);
        return null;
    }

    public ListGamesResponse listGames(String authToken) throws HTTPException {
        checkAuthorization(authToken);
        return new ListGamesResponse(gamesDatabase.listGames());
    }

    public CreateResponse create(String gameName, String authToken) throws HTTPException {
        if(gameName == null) {
            throw new HTTPException(400, "Bad Request");
        }
        checkAuthorization(authToken);
        GameData newGame = gamesDatabase.createGame(gameName);
        return new CreateResponse(Integer.parseInt(newGame.getGameID()));
    }

    public void join(JoinRequest joinRequest, String authToken) throws HTTPException {
        checkAuthorization(authToken);
        String username = authDatabase.getUsername(authToken);
        GameData game = gamesDatabase.getGame(String.valueOf(joinRequest.gameID()));
        if(game == null) {
            throw new HTTPException(400, "Bad Request");
        }
        if(!Objects.equals(joinRequest.playerColor(), "WHITE") &&
                !Objects.equals(joinRequest.playerColor(), "BLACK")) {
            throw new HTTPException(400, "Bad Request");
        }
        if(game.getColorUsername(joinRequest.playerColor()) != null) {
            throw new HTTPException(403, "already taken");
        }
        game.assignPlayerColor(username, joinRequest.playerColor());
        gamesDatabase.updateGame(game, String.valueOf(joinRequest.gameID()));
    }

    public void checkAuthorization(String authToken) throws HTTPException {
        var auth = authDatabase.getAuth(authToken);
        if (auth == null) {
            throw new HTTPException(401, "Unauthorized");
        }
    }
}
