package service;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;

public class UserService {
    UserDAO userDatabase = new MemoryUserDAO();
    AuthDAO authDatabase = new MemoryAuthDAO();
    GamesDAO gamesDatabase = new MemoryGamesDAO();

    public void clearDataBase() {
        userDatabase.clear();
        authDatabase.clear();
        gamesDatabase.clear();
    }

    public RegisterResult register(RegisterRequest registerRequest) throws Exception {
        String username = registerRequest.username();
        UserData user = userDatabase.getUser(username);
        if(user != null) {
            throw new Exception("Username already taken");
        }
        UserData newUser = new UserData(username, registerRequest.password(),
                registerRequest.email());
        AuthData userAuth = new AuthData(username);
        userDatabase.createUser(newUser);
        authDatabase.createAuth(userAuth);
        return new RegisterResult(username, userAuth.getAuthToken());
    }

    public LoginResult login(LoginRequest loginRequest) throws Exception {
        String username = loginRequest.username();
        String password = loginRequest.password();
        var user = userDatabase.getUser(username, password);
        if(user == null) {
            throw new Exception("username or password not found");
        }
        var auth = authDatabase.getAuth(username);
        if(auth == null) {
            throw new Exception("Bad request");
        }
        return new LoginResult(username, auth.getAuthToken());
    }

    public Object logout(String authToken) throws Exception {
        var auth = authDatabase.getAuth(authToken);
        if(auth == null) {
            throw new Exception("Unauthorized");
        }
        authDatabase.deleteAuth(authToken);
        return null;
    }

    public ListGamesResponse listGames(String authToken) throws Exception {
        var auth = authDatabase.getAuth(authToken);
        if(auth == null) {
            throw new Exception("Unauthorized");
        }
        return new ListGamesResponse(gamesDatabase.listGames());
    }

    public CreateResponse create(String gameName, String authToken) throws Exception {
        var auth = authDatabase.getAuth(authToken);
        if(auth == null) {
            throw new Exception("Unauthorized");
        }
        GameData newGame = gamesDatabase.createGame(gameName);
        return new CreateResponse(newGame.getGameID());
    }
}
