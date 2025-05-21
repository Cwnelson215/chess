package server;

import passoff.exception.ResponseParseException;
import spark.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class Server {

    private Collection<Map> users = new ArrayList<>(1);
    private Collection<Map> authData = new ArrayList<>(1);
    private Collection<Map> games = new ArrayList<>(1);

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

    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
