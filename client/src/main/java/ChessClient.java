import model.GameData;
import model.UserData;

import java.util.Arrays;

public class ChessClient {
    private final ServerFacade server;
    private final String serverUrl;
    private String authToken = null;
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
                case "logout" -> logout();
                case "create" -> create(params);
                case "join" -> join(params);
                case "quit" -> "Goodbye! \uD83D\uDE0A";
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
            authToken = result.getAuthToken();
            return String.format("Logged in as %s", result.getUsername());
        }
        throw new ResponseException(400, "Expected: <username> <password> <email>");
    }

    public String login(String...params) throws ResponseException {
        if(params.length >= 1) {
            state = State.LOGGEDIN;
            var result = server.login(params[0], params[1]);
            authToken = result.getAuthToken();
            return String.format("You logged in as %s", result.getUsername());
        }
        throw new ResponseException(400, "Expected: <username> <password>");
    }

    public String logout() throws ResponseException {
        checkState();
        state = State.LOGGEDOUT;
        server.logout(authToken);
        return "Successfully logged out! Have a nice day!";
    }

    public String create(String...params) throws ResponseException {
        if(params.length > 1) {
            throw new ResponseException(400, "Too many arguments given for create command; only a name is required");
        }
        checkState();
        server.createGame(params[0], authToken);
        return "Game created! Don't forget to join!";
    }

    public String join(String...params) throws ResponseException {
        if(params.length == 2) {
            checkState();
            server.joinGame(params[0], Integer.parseInt(params[1]), authToken);
            return "You've joined a game!";
        }
        throw new ResponseException(400, "two arguments expected, playerColor and gameID");
    }

    public GameData[] list(String...params) throws ResponseException {
        checkState();
        return server.listGames(authToken);
    }

    public String help() {
        if(state == State.LOGGEDOUT) {
            return """
                    - help
                    - login <USERNAME> <PASSWORD>
                    - register <USERNAME> <PASSWORD> <EMAIL>
                    - quit
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

    public String getState() {
        return state.toString();
    }

    public void checkState() throws ResponseException {
        if(state != State.LOGGEDIN) {
            throw new ResponseException(400, "must be logged in to perform this action");
        }
    }
}
