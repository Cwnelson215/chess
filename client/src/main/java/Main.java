import chess.*;

import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        var serverUrl = "http://localhost:8080";
        var client = new ChessClient(serverUrl);
        if(Objects.equals(client.getState(), "LOGGEDOUT")) {
            System.out.println("Welcome to the Chess Client! Type one of the following to start! \uD83D\uDE04");
            new PreLoginRepl(client).run();
        } else if(Objects.equals(client.getState(), "LOGGEDIN")) {

        } else {

        }
    }
}