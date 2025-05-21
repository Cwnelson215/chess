package server;

import com.google.gson.Gson;
import model.UserData;
import passoff.exception.ResponseParseException;
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

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public Object registerUser(Request req, Response res) throws ResponseParseException {
        UserData user = new Gson().fromJson(req.body(), UserData.class);
        var registerResult = userService.register(new RegisterRequest(user.getUsername(), user.getPassword(), user.getEmail()));
        return new Gson().toJson(registerResult);
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
