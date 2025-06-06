import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Arrays;
import java.util.Objects;

import static ui.EscapeSequences.*;

public class ChessClient {
    private final ServerFacade server;
    private final String serverUrl;
    private String authToken = null;
    private State state = State.LOGGEDOUT;
    String[] columns = {" a ", " b ", " c ", " d ", " e ", " f ", " g ", " h "};
    String[] rows = {" 1 ", " 2 ", " 3 ", " 4 ", " 5 ", " 6 ", " 7 ", " 8 "};

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
                case "list" -> list();
                case "observe" -> observe(params);
                case "quit" -> "Goodbye! \uD83D\uDE0A";
                case "exit" -> exit();
                default -> help();
            };
        } catch (ResponseException e) {
            return e.getMessage();
        }
    }

    public String register(String... params) throws ResponseException {
        if(params.length >= 1) {
            var result = server.register(new UserData(params[0], params[1], params[2]));
            authToken = result.getAuthToken();
            state = State.LOGGEDIN;
            return String.format("Logged in as %s", result.getUsername());
        }
        throw new ResponseException(400, "Expected: <username> <password> <email>");
    }

    public String login(String...params) throws ResponseException {
        if(params.length >= 1) {
            AuthData result = null;
            try {
                result = server.login(params[0], params[1]);
            } catch(ResponseException e) {
                return "Error: username or password incorrect";
            }
            authToken = result.getAuthToken();
            state = State.LOGGEDIN;
            return String.format("You logged in as %s", result.getUsername());
        }
        throw new ResponseException(400, "Expected: <username> <password>");
    }

    public String logout() throws ResponseException {
        checkState(State.LOGGEDIN);
        server.logout(authToken);
        state = State.LOGGEDOUT;
        return "Successfully logged out! Have a nice day!";
    }

    public String create(String...params) throws ResponseException {
        if(params.length > 1) {
            throw new ResponseException(400, "Too many arguments given for create command; only a name is required");
        }
        checkState(State.LOGGEDIN);
        server.createGame(params[0], authToken);
        return "Game created! Don't forget to join!";
    }

    public String join(String...params) throws ResponseException {
        if(params.length == 2) {
            checkState(State.LOGGEDIN);
            try {
                if(params[0].equals("white")) {
                    server.joinGame("WHITE", Integer.parseInt(params[1]), authToken);
                } else if(params[0].equals("black")) {
                    server.joinGame("BLACK", Integer.parseInt(params[1]), authToken);
                } else {
                    throw new ResponseException(400, "incorrect color input");
                }
            } catch(ResponseException e) {
                return "Something went wrong! Try again!";
            }
            state = State.INGAME;
            StringBuilder sb = new StringBuilder(String.format("You've joined as the %s team!\n", params[0]));
            return boardBuilder(sb, params[0]);
        }
        throw new ResponseException(400, "two arguments expected, playerColor and gameID");
    }

    public String list() throws ResponseException {
        checkState(State.LOGGEDIN);
        var list = server.listGames(authToken);
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for(GameData game : list) {
            sb.append(i).append(". ");
            sb.append("Name:");
            sb.append(game.getGameName());
            sb.append("   ID:");
            sb.append(game.getGameID());
            sb.append("\n");
            i++;
        }
        return sb.toString();
    }

    public String observe(String...params) throws ResponseException {
        if(params.length == 1) {
            checkState(State.LOGGEDIN);
            try {
                server.joinGame("observer", Integer.parseInt(params[0]), authToken);
            } catch(ResponseException e) {
                return "Something went wrong";
            }
            state = State.INGAME;
            StringBuilder sb = new StringBuilder("Observing game\n");
            return boardBuilder(sb, "observer");
        }
        throw new ResponseException(400, "only the game ID is needed");
    }

    public String exit() throws ResponseException {
        checkState(State.INGAME);
        state = State.LOGGEDIN;
        return "Game exited";
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
                - join [WHITE|BLACK] <ID>
                - observe <ID>
                """;
    }

    public String getState() {
        return state.toString();
    }

    public void checkState(State s) throws ResponseException {
        if(s == State.LOGGEDIN) {
            if(s != State.LOGGEDIN) {
                throw new ResponseException(400, "must be logged in to perform this action");
            }
        } else {
            if(s != State.INGAME) {
                throw new ResponseException(400, "must be in game to perform this action");
            }
        }
    }

    public String boardBuilder(StringBuilder sb, String s) {
        if(Objects.equals(s, "white") || Objects.equals(s, "observer")) {
            sb.append(SET_BG_COLOR_LIGHT_GREY).append(EMPTY).append(SET_TEXT_COLOR_BLUE);
            for(String col : columns) {
                sb.append(col);
            }
            sb.append(EMPTY).append("\n");
            for(int i = 7; i > -1; i--) {
                sb.append(rows[i]);
                if(i == 7) {
                    sb.append(SET_BG_COLOR_WHITE + BLACK_ROOK);
                    sb.append(SET_BG_COLOR_BLACK + BLACK_KNIGHT);
                    sb.append(SET_BG_COLOR_WHITE + BLACK_BISHOP);
                    sb.append(SET_BG_COLOR_BLACK + BLACK_QUEEN);
                    sb.append(SET_BG_COLOR_WHITE + BLACK_KING);
                    sb.append(SET_BG_COLOR_BLACK + BLACK_BISHOP);
                    sb.append(SET_BG_COLOR_WHITE + BLACK_KNIGHT);
                    sb.append(SET_BG_COLOR_BLACK + BLACK_ROOK);
                    sb.append(SET_BG_COLOR_LIGHT_GREY).append(rows[i]).append("\n");
                } else if(i == 6) {
                    for(int j = 0; j < 4; j++) {
                        sb.append(SET_BG_COLOR_BLACK + BLACK_PAWN).append(SET_BG_COLOR_WHITE + BLACK_PAWN);
                    }
                    sb.append(SET_BG_COLOR_LIGHT_GREY).append(rows[i]).append("\n");
                } else if(i == 1) {
                    for(int j = 0; j < 4; j++) {
                        sb.append(SET_BG_COLOR_WHITE + WHITE_PAWN).append(SET_BG_COLOR_BLACK + WHITE_PAWN);
                    }
                    sb.append(SET_BG_COLOR_LIGHT_GREY).append(rows[i]).append("\n");
                } else if(i == 0) {
                    sb.append(SET_BG_COLOR_BLACK + WHITE_ROOK);
                    sb.append(SET_BG_COLOR_WHITE + WHITE_KNIGHT);
                    sb.append(SET_BG_COLOR_BLACK + WHITE_BISHOP);
                    sb.append(SET_BG_COLOR_WHITE + WHITE_QUEEN);
                    sb.append(SET_BG_COLOR_BLACK + WHITE_KING);
                    sb.append(SET_BG_COLOR_WHITE + WHITE_BISHOP);
                    sb.append(SET_BG_COLOR_BLACK + WHITE_KNIGHT);
                    sb.append(SET_BG_COLOR_WHITE + WHITE_ROOK);
                    sb.append(SET_BG_COLOR_LIGHT_GREY).append(rows[i]).append("\n");
                } else {
                    fillEmptyWhite(sb, i);
                    sb.append(SET_BG_COLOR_LIGHT_GREY).append(rows[i]).append("\n");
                }
            }
            sb.append(SET_BG_COLOR_LIGHT_GREY).append(EMPTY);
            for(String col : columns) {
                sb.append(col);
            }
            sb.append(EMPTY).append("\n").append(RESET_BG_COLOR);
        } else {
            sb.append(SET_BG_COLOR_LIGHT_GREY).append(EMPTY).append(SET_TEXT_COLOR_BLUE);
            for(String col : columns) {
                sb.append(col);
            }
            sb.append(EMPTY).append("\n");
            for(int i = 0; i < 8; i++) {
                sb.append(rows[i]);
                if(i == 0) {
                    sb.append(SET_BG_COLOR_WHITE + WHITE_ROOK);
                    sb.append(SET_BG_COLOR_BLACK + WHITE_KNIGHT);
                    sb.append(SET_BG_COLOR_WHITE + WHITE_BISHOP);
                    sb.append(SET_BG_COLOR_BLACK + WHITE_KING);
                    sb.append(SET_BG_COLOR_WHITE + WHITE_QUEEN);
                    sb.append(SET_BG_COLOR_BLACK + WHITE_BISHOP);
                    sb.append(SET_BG_COLOR_WHITE + WHITE_KNIGHT);
                    sb.append(SET_BG_COLOR_BLACK + WHITE_ROOK);
                    sb.append(SET_BG_COLOR_LIGHT_GREY).append(rows[i]).append("\n");
                } else if(i == 1) {
                    for(int j = 0; j < 4; j++) {
                        sb.append(SET_BG_COLOR_BLACK + WHITE_PAWN).append(SET_BG_COLOR_WHITE + WHITE_PAWN);
                    }
                    sb.append(SET_BG_COLOR_LIGHT_GREY).append(rows[i]).append("\n");
                } else if(i == 6) {
                    for(int j = 0; j < 4; j++) {
                        sb.append(SET_BG_COLOR_WHITE + BLACK_PAWN).append(SET_BG_COLOR_BLACK + BLACK_PAWN);
                    }
                    sb.append(SET_BG_COLOR_LIGHT_GREY).append(rows[i]).append("\n");
                } else if(i == 7) {
                    sb.append(SET_BG_COLOR_BLACK + BLACK_ROOK);
                    sb.append(SET_BG_COLOR_WHITE + BLACK_KNIGHT);
                    sb.append(SET_BG_COLOR_BLACK + BLACK_BISHOP);
                    sb.append(SET_BG_COLOR_WHITE + BLACK_KING);
                    sb.append(SET_BG_COLOR_BLACK + BLACK_QUEEN);
                    sb.append(SET_BG_COLOR_WHITE + BLACK_BISHOP);
                    sb.append(SET_BG_COLOR_BLACK + BLACK_KNIGHT);
                    sb.append(SET_BG_COLOR_WHITE + BLACK_ROOK);
                    sb.append(SET_BG_COLOR_LIGHT_GREY).append(rows[i]).append("\n");
                } else {
                    fillEmptyBlack(sb, i);
                    sb.append(SET_BG_COLOR_LIGHT_GREY).append(rows[i]).append("\n");
                }
            }
            sb.append(SET_BG_COLOR_LIGHT_GREY).append(EMPTY);
            for(String col : columns) {
                sb.append(col);
            }
            sb.append(EMPTY).append("\n").append(RESET_BG_COLOR);
        }
        return sb.toString();
    }

    public void fillEmptyWhite(StringBuilder sb, int i) {
        if(i == 5 || i == 3) {
            for(int j = 0; j < 4; j++) {
                sb.append(SET_BG_COLOR_WHITE + EMPTY);
                sb.append(SET_BG_COLOR_BLACK + EMPTY);
            }
        } else {
            for(int j = 0; j < 4; j++) {
                sb.append(SET_BG_COLOR_BLACK + EMPTY);
                sb.append(SET_BG_COLOR_WHITE + EMPTY);
            }
        }
    }
    public void fillEmptyBlack(StringBuilder sb, int i) {
        if(i == 2 || i == 4) {
            for(int j = 0; j < 4; j++) {
                sb.append(SET_BG_COLOR_WHITE + EMPTY);
                sb.append(SET_BG_COLOR_BLACK + EMPTY);
            }
        } else {
            for(int j = 0; j < 4; j++) {
                sb.append(SET_BG_COLOR_BLACK + EMPTY);
                sb.append(SET_BG_COLOR_WHITE + EMPTY);
            }
        }
    }
}
