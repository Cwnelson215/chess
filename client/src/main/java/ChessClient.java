import model.UserData;

import java.util.Arrays;
import java.util.Locale;

public class ChessClient {
    private final ServerFacade server;
    private final String serverUrl;
    private State state = State.LOGGEDOUT;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> login(params);

                default -> help();
            };
        } catch (ResponseException e) {
            return e.getMessage();
        }
    }

    public String register(String... params) throws ResponseException {
        if(params.length >= 1) {
            state = State.LOGGEDIN;
            var result = server.register(new UserData(params[0], params[1], params[2]));
            return String.format("Logged in as %s", result.toString());
        }
        throw new ResponseException(400, "Expected: <username> <password> <email>");
    }

    public String login(String...params) throws ResponseException {
        if(params.length >= 1) {
            state = State.LOGGEDIN;
            var result = server.login(new UserData(params[0], params[1], params[2]));
            return String.format("You logged in as %s", result.getUsername());
        }
        throw new ResponseException(400, "Expected: <username> <password>");
    }

    public String help() {
        if(state == State.LOGGEDOUT) {
            return """
                    - help
                    - quit
                    - login <USERNAME> <PASSWORD>
                    - register <USERNAME> <PASSWORD> <EMAIL>
                    """;
        }
        return """
                - help
                - create <NAME>
                - logout
                - list
                - join <ID> [WHITE|BLACK]
                - observe <ID>
                """;
    }
}
