package service;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Objects;

public class UserService {
    UserDAO userDatabase;
    AuthDAO authDatabase;
    GamesDAO gamesDatabase;

    {
        try {
            userDatabase = new MySqlUserDAO();
            authDatabase = new MySqlAuthDAO();
            gamesDatabase = new MySqlGameDAO();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void clearDataBase() throws HTTPException {
        try {
            userDatabase.clear();
            authDatabase.clear();
            gamesDatabase.clear();
            if (!userDatabase.isEmpty() || !authDatabase.isEmpty() || !gamesDatabase.isEmpty()) {
                throw new HTTPException(500, "Failed to clear databases");
            }
        } catch(DataAccessException e) {
            throw new HTTPException(500, e.getMessage());
        }
    }

    public RegisterResult register(RegisterRequest registerRequest) throws HTTPException, DataAccessException {
        try {
            String username = registerRequest.username();
            if (username == null || registerRequest.password() == null || registerRequest.email() == null) {
                throw new HTTPException(400, "Bad Request");
            }
            UserData user = userDatabase.getUser(username);
            if (user != null) {
                throw new HTTPException(403, "Error: username already taken");
            }
            UserData newUser = new UserData(username, registerRequest.password(),
                    registerRequest.email());
            AuthData userAuth = new AuthData(username);
            userDatabase.createUser(newUser);
            authDatabase.createAuth(userAuth);
            return new RegisterResult(username, userAuth.getAuthToken());
        } catch(DataAccessException e) {
            throw new HTTPException(500, e.getMessage());
        }
    }

    public LoginResult login(LoginRequest loginRequest) throws HTTPException, DataAccessException {
        try {
            String username = loginRequest.username();
            String password = loginRequest.password();
            var user = userDatabase.getUser(username, password);
            if (username == null || password == null) {
                throw new HTTPException(400, "Bad Request");
            }
            if (user == null) {
                throw new HTTPException(401, "Unauthorized");
            }
            AuthData newAuth = new AuthData(username);
            authDatabase.createAuth(newAuth);
            AuthData auth = authDatabase.getAuth(newAuth.getAuthToken());
            return new LoginResult(username, auth.getAuthToken());
        } catch(DataAccessException e) {
            throw new HTTPException(500, e.getMessage());
        }
    }

    public Object logout(String authToken) throws HTTPException, DataAccessException {
        try {
            checkAuthorization(authToken);
            authDatabase.deleteAuth(authToken);
            return null;
        } catch(DataAccessException e) {
            throw new HTTPException(500, e.getMessage());
        }
    }

    public ListGamesResponse listGames(String authToken) throws HTTPException, DataAccessException {
        try {
            checkAuthorization(authToken);
            return new ListGamesResponse(gamesDatabase.listGames());
        } catch(DataAccessException e) {
            throw new HTTPException(500, e.getMessage());
        }
    }

    public CreateResponse create(String gameName, String authToken) throws HTTPException, DataAccessException {
        try {
            if (gameName == null) {
                throw new HTTPException(400, "Bad Request");
            }
            checkAuthorization(authToken);
            GameData newGame = gamesDatabase.createGame(gameName);
            return new CreateResponse(Integer.parseInt(newGame.getGameID()));
        } catch(DataAccessException e) {
            throw new HTTPException(500, e.getMessage());
        }
    }

    public void join(JoinRequest joinRequest, String authToken) throws HTTPException, DataAccessException {
        if(Objects.equals(joinRequest.playerColor(), "WHITE") || Objects.equals(joinRequest.playerColor(), "BLACK")
        || Objects.equals(joinRequest.playerColor(), "observer")) {
            try {
                checkAuthorization(authToken);
                String username = authDatabase.getUsername(authToken);
                GameData game = gamesDatabase.getGame(String.valueOf(joinRequest.gameID()));
                if (game == null) {
                    throw new HTTPException(400, "Bad Request");
                }
                if (game.getColorUsername(joinRequest.playerColor()) != null && !joinRequest.playerColor().equals("observer")) {
                    throw new HTTPException(403, "already taken");
                }
                if (!joinRequest.playerColor().equals("observer")) {
                    game.assignPlayerColor(username, joinRequest.playerColor());
                }
                gamesDatabase.updateGame(game, String.valueOf(joinRequest.gameID()));
            } catch (DataAccessException e) {
                throw new HTTPException(500, e.getMessage());
            }
        } else {
            throw new HTTPException(400, "Bad Request");
        }
    }

    public void updateGame(UpdateRequest updateRequest) {
        var game = updateRequest.game();
        var gameID = updateRequest.gameID();
        try {
            gamesDatabase.updateGame(game, gameID);
        } catch(DataAccessException e) {
            throw new HTTPException(500, e.getMessage());
        }
    }

    public void checkAuthorization(String authToken) throws HTTPException, DataAccessException {
        try {
            var auth = authDatabase.getAuth(authToken);
            if (auth == null) {
                throw new HTTPException(401, "Unauthorized");
            }
        }  catch(DataAccessException e) {
            throw new HTTPException(500, e.getMessage());
        }
    }
}
