package service;
import dataaccess.AuthDOA;
import dataaccess.DataAccessException;
import dataaccess.GamesDOA;
import dataaccess.UserDOA;
import model.AuthData;
import model.GameData;
import model.UserData;

import javax.xml.crypto.Data;

public class UserService {
    UserDOA userDatabase = new UserDOA();
    AuthDOA authDatabase = new AuthDOA();
    GamesDOA gamesDatabase = new GamesDOA();

    public Object clearDataBase() {
        userDatabase.clear();
        authDatabase.clear();
        return null;
    }

    public RegisterResult register(RegisterRequest registerRequest) {
        String username = registerRequest.username();
        UserData user = userDatabase.getUser(username);
        if(user != null) {
            return null;
        }
        UserData newUser = new UserData(username, registerRequest.password(),
                registerRequest.email());
        AuthData userAuth = new AuthData(username);
        userDatabase.createUser(newUser);
        authDatabase.createAuth(userAuth);
        return new RegisterResult(username, userAuth.getAuthToken());
    }

    public LoginResult login(LoginRequest loginRequest) throws DataAccessException {
        String username = loginRequest.username();
        String password = loginRequest.password();
        var user = userDatabase.getUser(username, password);
        if(user == null) {
            throw new DataAccessException("username or password not found");
        }
        var auth = authDatabase.getAuth(username);
        if(auth == null) {
            throw new DataAccessException("Bad request");
        }
        return new LoginResult(username, auth.getAuthToken());
    }

    public Object logout(String authToken) {
        var auth = authDatabase.getAuth(authToken);
        if(auth == null) {
            return new DataAccessException("Unauthorized");
        }
        authDatabase.deleteAuth(authToken);
        return null;
    }

    public ListGamesResponse listGames(String authToken) throws DataAccessException {
        var auth = authDatabase.getAuth(authToken);
        if(auth == null) {
            throw new DataAccessException("Unauthorized");
        }
        return new ListGamesResponse(gamesDatabase.listGames());
    }

    public CreateResponse create(String gameName, String authToken) throws DataAccessException {
        var auth = authDatabase.getAuth(authToken);
        if(auth == null) {
            throw new DataAccessException("Unauthorized");
        }
        GameData newGame = gamesDatabase.creatGame(gameName);
        return new CreateResponse(newGame.getGameID());
    }
}
