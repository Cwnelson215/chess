package repls;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import model.AuthData;
import model.GameData;
import model.UserData;
import serverfacade.ResponseException;
import serverfacade.ServerFacade;
import serverfacade.websocket.NotificationHandler;
import serverfacade.websocket.WebSocketFacade;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class ChessClient {
    private final ServerFacade server;
    private final String serverUrl;
    NotificationHandler notificationHandler;
    private WebSocketFacade ws;
    private String authToken = null;
    private String userName = null;
    private String gameID = null;
    private String playerColor = null;
    private ChessBoard currentGameBoard = null;
    private State state = State.LOGGEDOUT;
    String[] columns = {" a ", " b ", " c ", " d ", " e ", " f ", " g ", " h "};
    String[] rows = {" 1 ", " 2 ", " 3 ", " 4 ", " 5 ", " 6 ", " 7 ", " 8 "};

    public ChessClient(String serverUrl, NotificationHandler notificationHandler) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
        this.notificationHandler = notificationHandler;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> login(params);
                case "logout" -> logout(params);
                case "create" -> create(params);
                case "join" -> join(params);
                case "list" -> list(params);
                case "observe" -> observe(params);
                case "quit" -> "Goodbye! \uD83D\uDE0A";
                case "leave" -> exit(params);
                case "resign" -> resignGame();
                default -> help();
            };
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public String register(String... params) throws ResponseException {
        if(params.length == 3) {
            try {
                var result = server.register(new UserData(params[0], params[1], params[2]));
                authToken = result.getAuthToken();
                userName = result.getUsername();
                state = State.LOGGEDIN;
                return String.format("Logged in as %s", result.getUsername());
            } catch(ResponseException e) {
                return String.format("Error: username %s is already taken", params[0]);
            }
        }
        throw new ResponseException(400, "Error: Expected <username> <password> <email> as inputs");
    }

    public String login(String...params) throws ResponseException {
        if(params.length == 2) {
            AuthData result = null;
            try {
                result = server.login(params[0], params[1]);
            } catch(ResponseException e) {
                return "Error: username or password incorrect";
            }
            authToken = result.getAuthToken();
            userName = result.getUsername();
            state = State.LOGGEDIN;
            return String.format("You logged in as %s", result.getUsername());
        }
        throw new ResponseException(400, "Error: Expected <username> <password> as inputs");
    }

    public String logout(String...params) throws ResponseException {
        checkParams("logout", params);
        checkState(State.LOGGEDIN);
        server.logout(authToken);
        authToken = null;
        userName = null;
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
            if(!isInt(params[1])) {
                throw new ResponseException(400, "Game id should contain only integers");
            }
            if(params[1].length() != 1) {
                throw new ResponseException(400, "Game id should be exactly 1 number long");
            }
            String id = getGameId(Integer.parseInt(params[1]));
            checkState(State.LOGGEDIN);
            try {
                ws = new WebSocketFacade(serverUrl, notificationHandler);
                if(params[0].equals("white")) {
                    server.joinGame("WHITE", Integer.parseInt(id), authToken);
                    ws.joinGame(authToken, Integer.parseInt(id), userName, "WHITE");
                } else if(params[0].equals("black")) {
                    server.joinGame("BLACK", Integer.parseInt(id), authToken);
                    ws.joinGame(authToken, Integer.parseInt(id), userName, "BLACK");
                } else {
                    throw new ResponseException(400, "incorrect color input try again");
                }
            } catch(ResponseException e) {
                return "Your requested color is already taken";
            } catch(IOException e) {
                return "Something went wrong";
            }
            state = State.INGAME;
            StringBuilder sb = new StringBuilder(String.format("You've joined as the %s team!\n", params[0]));
            gameID = id;
            playerColor = params[0];
            return drawBoard(sb, params[0]);
        }
        throw new ResponseException(400, "two arguments expected, playerColor and gameID");
    }

    public String list(String...params) throws ResponseException {
        checkParams("list", params);
        checkState(State.LOGGEDIN);
        var list = server.listGames(authToken);
        if(list.isEmpty()) {
            return "No games currently being played";
        }
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for(GameData game : list) {
            sb.append(i).append(". ");
            sb.append(game.getGameName());
            if(game.getWhiteUsername() != null) {
                sb.append(String.format(" %s is joined as White", game.getWhiteUsername()));
            }
            if(game.getBlackUsername() != null) {
                sb.append(String.format(" %s is joined as Black", game.getBlackUsername()));
            }
            sb.append("\n");
            i++;
        }
        return sb.toString();
    }

    public String observe(String...params) throws ResponseException {
        if(params.length == 1) {
            checkState(State.LOGGEDIN);
            if(!isInt(params[0])) {
                throw new ResponseException(400, "Game id should contain only integers");
            }
            if(params[0].length() != 1) {
                throw new ResponseException(400, "Game id should be exactly 1 number long");
            }
            String id = getGameId(Integer.parseInt(params[0]));
            try {
                server.joinGame("observer", Integer.parseInt(id), authToken);
            } catch(ResponseException e) {
                return "Something went wrong";
            }
            state = State.INGAME;
            StringBuilder sb = new StringBuilder("Observing game\n");
            return boardBuilder(sb, "observer");
        }
        throw new ResponseException(400, "only the game ID is needed");
    }

    public String exit(String...params) throws ResponseException, IOException {
        checkParams("exit", params);
        checkState(State.INGAME);
        state = State.LOGGEDIN;
        ws =  new WebSocketFacade(serverUrl, notificationHandler);
        ws.leaveGame(authToken, Integer.parseInt(gameID), userName);
        gameID = null;
        return "Game exited";
    }

    public String resignGame(String...params) throws ResponseException, IOException {
        checkParams("resign", params);
        checkState(State.INGAME);
        System.out.println("Would you like to resign the match?");
        System.out.printf("[%s]>>> ", state.toString());
        Scanner scanner = new Scanner(System.in);
        var input =  scanner.nextLine();
        if(input.equals("yes")) {
            state = State.LOGGEDIN;
            ws =  new WebSocketFacade(serverUrl, notificationHandler);
            ws.resignGame(authToken, Integer.parseInt(gameID), userName);
            gameID = null;
            return "You're game has been resigned";
        } else {
            return "Carry on";
        }
    }

    public String help() {
        if(state == State.LOGGEDOUT) {
            return """
                    - help
                    - login <USERNAME> <PASSWORD>
                    - register <USERNAME> <PASSWORD> <EMAIL>
                    - quit
                    """;
        } else if(state == State.LOGGEDIN) {
            return """
                    - help
                    - create <NAME>
                    - logout
                    - list
                    - join [WHITE|BLACK] <ID>
                    - observe <ID>
                    """;
        }
        return """
                - help
                - redraw (Redraws game board)
                - leave
                - move
                - resign
                - check_moves
               """;
    }

    public String getState() {
        return state.toString();
    }

    public void checkState(State expectedState) throws ResponseException {
        if(expectedState != state) {
            throw new  ResponseException(400, String.format("Error: must be %s to perform that action", expectedState.toString()));
        }
    }

    public String drawBoard(StringBuilder sb, String s) throws ResponseException {
        checkState(State.INGAME);
        setGameBoard();
        String[] backgroundColors = {SET_BG_COLOR_WHITE, SET_BG_COLOR_BLACK};
        ChessPiece[][] board = currentGameBoard.getBoard();
        if(Objects.equals(s, "white") || Objects.equals(s, "observer")) {
            sb.append(SET_BG_COLOR_LIGHT_GREY).append(EMPTY).append(SET_TEXT_COLOR_BLUE);
            for(String col : columns) {
                sb.append(col);
            }
            sb.append(EMPTY).append("\n");
            for (int i = 7; i > -1; i--) {
                sb.append(rows[i]);
                for (int j = 7; j > -1; j--) {
                    var background = backgroundColors[j % 2];
                    var sequence = getEscapeSequences(board[i][j]);
                    sb.append(background).append(sequence);
                }
                sb.append(SET_BG_COLOR_LIGHT_GREY).append(rows[i]).append("\n");
            }
            sb.append(SET_BG_COLOR_LIGHT_GREY).append(EMPTY);
            for(String col : columns) {
                sb.append(col);
            }
            sb.append(EMPTY).append("\n").append(RESET_BG_COLOR);
        } else {
            sb.append(SET_BG_COLOR_LIGHT_GREY).append(EMPTY).append(SET_TEXT_COLOR_BLUE);
            for(int i = 7; i > -1; i--) {
                sb.append(columns[i]);
            }
            sb.append(EMPTY).append("\n");
            for(int i = 0; i < 8; i++) {
                sb.append(rows[i]);
                for(int j = 0; j < 8; j++) {
                    var background = backgroundColors[j % 2];
                    var sequence = getEscapeSequences(board[i][j]);
                    sb.append(background).append(sequence);
                }
                sb.append(SET_BG_COLOR_LIGHT_GREY).append(rows[i]).append("\n");
            }
            sb.append(SET_BG_COLOR_LIGHT_GREY).append(EMPTY);
            for(String col : columns) {
                sb.append(col);
            }
            sb.append(EMPTY).append("\n").append(RESET_BG_COLOR);
        }
        return sb.toString();
    }

    public String getEscapeSequences(ChessPiece piece) {
        if(piece == null) {
            return EMPTY;
        }
        var type = piece.getPieceType();
        if(piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            return switch(type) {
                case KING -> WHITE_KING;
                case QUEEN -> WHITE_QUEEN;
                case BISHOP -> WHITE_BISHOP;
                case KNIGHT -> WHITE_KNIGHT;
                case ROOK -> WHITE_ROOK;
                case PAWN -> WHITE_PAWN;
            };
        } else {
            return switch(type) {
                case KING -> BLACK_KING;
                case QUEEN -> BLACK_QUEEN;
                case BISHOP -> BLACK_BISHOP;
                case KNIGHT -> BLACK_KNIGHT;
                case ROOK -> BLACK_ROOK;
                case PAWN -> BLACK_PAWN;
            };
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
            for(int i = 7; i > -1; i--) {
                sb.append(columns[i]);
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
            for(int i = 7; i > -1; i--) {
                sb.append(columns[i]);
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

    public boolean isInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    public void setGameBoard() throws ResponseException {
        var list = server.listGames(authToken);
        for(GameData game : list) {
            if(Objects.equals(game.getGameID(), gameID)) {
                currentGameBoard = game.getGame().getBoard();
            }
        }
    }

    public void checkParams(String s, String...params) throws ResponseException{
        if(params.length > 0) {
            throw new ResponseException(400, String.format("No inputs required for %s command", s));
        }
    }

    private String getGameId(int gameNumber) throws ResponseException {
        var games = server.listGames(authToken);
        return games.get(gameNumber - 1).getGameID();
    }
}
