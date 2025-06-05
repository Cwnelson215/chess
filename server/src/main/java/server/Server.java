package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.GameData;
import model.UserData;
import service.*;
import spark.*;


import java.util.ArrayList;
import java.util.Map;


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
            Spark.put("/game", this::joinGame);
            Spark.delete("/db", this::clearData);
        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public Object registerUser(Request req, Response res) throws HTTPException, DataAccessException {
        try {
            RegisterRequest request = new Gson().fromJson(req.body(), RegisterRequest.class);
            var registerResult = userService.register(request);
            return new Gson().toJson(registerResult);
        } catch (HTTPException e) {
            res = e.createResponse(res);
            return res.body();
        } catch (DataAccessException e) {
            throw e;
        }
    }

    public Object loginUser(Request req, Response res) throws HTTPException, DataAccessException {
        try {
            UserData user = new Gson().fromJson(req.body(), UserData.class);
            var loginResult = userService.login(new LoginRequest(user.getUsername(), user.getPassword()));
            return new Gson().toJson(loginResult);
        } catch (HTTPException e) {
            res = e.createResponse(res);
            return res.body();
        }
    }

    public Object logoutUser(Request req, Response res) throws HTTPException, DataAccessException {
        try {
            String authToken = req.headers("authorization");
            var logoutResult = userService.logout(authToken);
            return new Gson().toJson(logoutResult);
        } catch (HTTPException e) {
            res = e.createResponse(res);
            return res.body();
        }
    }

    public Object clearData(Request req, Response res) throws DataAccessException {
        try {
            userService.clearDataBase();
            return new Gson().toJson(null);
        } catch (HTTPException e) {
            res = e.createResponse(res);
            return res.body();
        }
    }

    public Object gamesList(Request req, Response res) throws HTTPException, DataAccessException{
        try {
            String authToken = req.headers("authorization");
            var gamesList = userService.listGames(authToken);
            return new Gson().toJson(gamesList);
        } catch (HTTPException e) {
            res = e.createResponse(res);
            return res.body();
        }
    }

    public Object createGame(Request req, Response res) throws Exception {
        try {
            String authToken = req.headers("authorization");
            GameData game = new Gson().fromJson(req.body(), GameData.class);
            var createResponse = userService.create(game.getGameName(), authToken);
            return new Gson().toJson(createResponse);
        } catch (HTTPException e) {
            res = e.createResponse(res);
            return res.body();
        }
    }

    public Object joinGame(Request req, Response res) throws HTTPException, DataAccessException {
        try {
            String authToken = req.headers("authorization");
            JoinRequest joinRequest = new Gson().fromJson(req.body(), JoinRequest.class);
            userService.join(joinRequest, authToken);
            return new Gson().toJson(null);
        } catch(HTTPException e) {
            res = e.createResponse(res);
            return res.body();
        }
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
