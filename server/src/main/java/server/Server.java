package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import model.UserData;
import passoff.exception.ResponseParseException;
import service.LoginRequest;
import service.RegisterRequest;
import service.UserService;
import spark.*;


public class Server {
    private final UserService userService = new UserService();

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::registerUser);
        Spark.post("/session", this::loginUser);
        Spark.delete("/session", this::logoutUser);
        Spark.get("/game", this::gamesList);
        Spark.post("/game", this::createGame);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public Object registerUser(Request req, Response res) throws ResponseParseException {
        RegisterRequest request = new Gson().fromJson(req.body(), RegisterRequest.class);
        var registerResult = userService.register(request);
        return new Gson().toJson(registerResult);
    }

    public Object loginUser(Request req, Response res) throws Exception {
        UserData user = new Gson().fromJson(req.body(), UserData.class);
        var loginResult = userService.login(new LoginRequest(user.getUsername(), user.getPassword()));
        return new Gson().toJson(loginResult);
    }

    public Object logoutUser(Request req, Response res) throws Exception {
        AuthData auth = new Gson().fromJson(req.body(), AuthData.class);
        var logoutResult = userService.logout(auth.getAuthToken());
        return new Gson().toJson(logoutResult);
    }

    public Object clearData(Request req, Response res) throws ResponseParseException {
        userService.clearDataBase();
        return null;
    }

    public Object gamesList(Request req, Response res) throws ResponseParseException, DataAccessException {
        AuthData auth = new Gson().fromJson(req.body(), AuthData.class);
        var gamesList = userService.listGames(auth.getAuthToken());
        return new Gson().toJson(gamesList);
    }

    public Object createGame(Request req, Response res) throws DataAccessException {
        String authToken = req.headers("authorization");
        GameData game = new Gson().fromJson(req.body(), GameData.class);
        var createResponse = userService.create(game.getGameName(), authToken);
        return new Gson().toJson(createResponse);
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
