import chess.*;

import java.util.Objects;

import static ui.EscapeSequences.SET_TEXT_COLOR_BLUE;
import static ui.EscapeSequences.SET_TEXT_UNDERLINE;

public class Main {
    public static void main(String[] args) {
        var serverUrl = "http://localhost:8080";
        var client = new ChessClient(serverUrl);
        System.out.println(SET_TEXT_UNDERLINE + SET_TEXT_COLOR_BLUE + "Welcome to the Chess Client! Type one of the following to start! \uD83D\uDE04");
        new Repl(client).run();
//        if(Objects.equals(client.getState(), "LOGGEDOUT")) {
//            System.out.println(SET_TEXT_UNDERLINE + SET_TEXT_COLOR_BLUE + "Welcome to the Chess Client! Type one of the following to start! \uD83D\uDE04");
//            new PreLoginRepl(client).run();
//        } else if(Objects.equals(client.getState(), "LOGGEDIN")) {
//            new PostLoginRepl(client).run();
//        } else {
//
//        }
    }
}