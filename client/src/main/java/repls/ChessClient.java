package repls;

import chess.*;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import serverfacade.ResponseException;
import serverfacade.ServerFacade;
import serverfacade.websocket.NotificationHandler;
import serverfacade.websocket.WebSocketFacade;
import ui.EscapeSequences;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.*;
import static ui.EscapeSequences.*;

public class ChessClient implements NotificationHandler {
    private final EscapeSequences es = new EscapeSequences();
    private final BooleanMethods booleans = new BooleanMethods();
    private final CheckMethods check = new CheckMethods();
    private final ServerFacade server;
    private final String serverUrl;
    NotificationHandler notificationHandler;
    private WebSocketFacade ws = null;
    private String authToken = null;
    private String userName = null;
    private String gameID = null;
    private String playerColor = null;
    private ChessGame currentGame = null;
    private State state = State.LOGGEDOUT;
    private boolean gameOver = false;
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
                case "leave" -> leaveGame(params);
                case "resign" -> resignGame();
                case "redraw" -> redrawBoard();
                case "check_moves" -> highlight(params);
                case "move" -> move(params);
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
            AuthData result;
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
        check.params("logout", params);
        check.state(State.LOGGEDIN, state);
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
        check.state(State.LOGGEDIN, state);
        server.createGame(params[0], authToken);
        return "Game created! Don't forget to join!";
    }

    public String join(String...params) throws ResponseException {
        if(params.length == 2) {
            if(!booleans.isInt(params[1])) {
                throw new ResponseException(400, "Game id should contain only integers");
            }
            if(params[1].length() != 1) {
                throw new ResponseException(400, "Game id should be exactly 1 number long");
            }
            ws = new WebSocketFacade(serverUrl, notificationHandler);
            String id = getGameId(Integer.parseInt(params[1]));
            check.state(State.LOGGEDIN, state);
            try {
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
                return e.getMessage();
            } catch(IOException e) {
                return "Something went wrong";
            }
            state = State.INGAME;
            gameID = id;
            playerColor = params[0];
            return "Game Joined!\n";
        }
        throw new ResponseException(400, "two arguments expected, playerColor and gameID");
    }

    public String list(String...params) throws ResponseException {
        check.params("list", params);
        check.state(State.LOGGEDIN, state);
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
            check.state(State.LOGGEDIN, state);
            if(!booleans.isInt(params[0])) {
                throw new ResponseException(400, "Game id should contain only integers");
            }
            if(params[0].length() != 1) {
                throw new ResponseException(400, "Game id should be exactly 1 number long");
            }
            if(ws == null) {
                ws = new WebSocketFacade(serverUrl, notificationHandler);
            }
            String id = getGameId(Integer.parseInt(params[0]));
            try {
                ws.joinGame(authToken, Integer.parseInt(id), userName, "observer");
            } catch(Exception e) {
                return "Something went wrong";
            }
            state = State.INGAME;
            playerColor = "observer";
            gameID = id;
            return "Game Joined!\n";
        }
        throw new ResponseException(400, "only the game ID is needed");
    }

    public String leaveGame(String...params) throws ResponseException, IOException {
        check.params("leave", params);
        check.state(State.INGAME, state);
        ws.leaveGame(authToken, Integer.parseInt(gameID));
        state = State.LOGGEDIN;
        gameID = null;
        return "Game exited";
    }

    public String resignGame(String...params) throws ResponseException, IOException {
        check.params("resign", params);
        check.state(State.INGAME, state);
        System.out.println("Would you like to resign the match?");
        System.out.printf("[%s]>>> ", state.toString());
        Scanner scanner = new Scanner(System.in);
        var input =  scanner.nextLine();
        if(input.equals("yes")) {
            ws.resignGame(authToken, Integer.parseInt(gameID), userName);
            gameOver = true;
            gameID = null;
            return "You're game has been resigned";
        } else {
            return String.format("Carry on\n%s", redrawBoard());
        }
    }

    public String redrawBoard() throws ResponseException {
        var newBoard = drawBoard(new StringBuilder(), playerColor, new ArrayList<>());
        if(checkForMate() != null) {
            return newBoard + "\n" + checkForMate();
        }
        return newBoard;
    }

    public String highlight(String...params) throws ResponseException {
        check.state(State.INGAME, state);
        ArrayList<ChessPosition> highlightedPositions;
        if(params.length != 2) {
            throw new ResponseException(400, "only 2 arguments are allowed");
        }
        if(booleans.isLetter(params[0]) && booleans.isInt(params[1])) {
            if(!booleans.checkRange(params[0]) && booleans.checkRange(params[1])) {
                throw new ResponseException(400, "row or column input out of range");
            }
            ChessPosition chosenPosition = getPosition(params[0], params[1]);
            highlightedPositions = listMoves(chosenPosition);
        } else {
            throw new ResponseException(400, "first input must be a letter and second must be a number");
        }
        return drawBoard(new StringBuilder(), playerColor, highlightedPositions);
    }

    public String move(String...params) throws ResponseException {
        check.state(State.INGAME, state);
        check.gameStatus(gameOver);
        if(params.length == 2) {
            try {
                var chosenPiece = getPosition(params[0], params[1]);
                check.pieceColor(chosenPiece, currentGame, playerColor);
                Collection<ChessMove> possibleMoves = currentGame.validMoves(chosenPiece);
                if(possibleMoves.isEmpty()) {
                    throw new ResponseException(400, "Unable to move chosen piece");
                }
                var chosenMove = confirmMove(possibleMoves);
                currentGame.makeMove(chosenMove);
                ws.makeMove(authToken, Integer.parseInt(gameID), userName, playerColor, chosenMove);
            } catch (Exception e) {
                throw new ResponseException(400, e.getMessage());
            }
        } else {
            throw new ResponseException(400, "move command must have 2 inputs");
        }
        if(checkForMate() != null) {
            return checkForMate();
        }
        return "Move Made!";
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
                - check_moves <column letter> <row number>
               """;
    }

    public String getState() {
        return state.toString();
    }

    private String drawBoard(StringBuilder sb, String s, ArrayList<ChessPosition> highlightPositions) throws ResponseException {
        check.state(State.INGAME, state);
        String[] backgroundColors = {SET_BG_COLOR_WHITE, SET_BG_COLOR_BLACK};
        String[] highlightColors = {SET_BG_COLOR_GREEN, SET_BG_COLOR_DARK_GREEN};
        ChessPiece[][] board = currentGame.getBoard().getBoard();
        String background;
        if(Objects.equals(s, "white") || Objects.equals(s, "observer")) {
            sb.append(SET_BG_COLOR_LIGHT_GREY).append(EMPTY).append(SET_TEXT_COLOR_BLUE);
            for(String col : columns) {
                sb.append(col);
            }
            sb.append(EMPTY).append("\n");
            int rowCounter = 7;
            for (int i = 1; i < 9; i++) {
                sb.append(rows[rowCounter]);
                int columnCounter = 7;
                for (int j = 1; j < 9; j++) {
                    ChessPosition position = new ChessPosition(9 - i, j);
                    if(highlightPositions.contains(position)) {
                        background = highlightColors[(i + j) % 2];
                    } else {
                        background = backgroundColors[(i + j) % 2];
                    }
                    String sequence = es.getEscapeSequences(board[rowCounter][columnCounter]);
                    sb.append(background).append(sequence);
                    columnCounter--;
                }
                sb.append(SET_BG_COLOR_LIGHT_GREY).append(rows[rowCounter]).append("\n");
                rowCounter--;
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
                    if(highlightPositions.contains(new ChessPosition(i + 1, j + 1))) {
                        background = highlightColors[(i + j) % 2];
                    } else {
                        background = backgroundColors[(i + j) % 2];
                    }
                    var sequence = es.getEscapeSequences(board[i][j]);
                    sb.append(background).append(sequence);
                }
                sb.append(SET_BG_COLOR_LIGHT_GREY).append(rows[i]).append("\n");
            }
            sb.append(SET_BG_COLOR_LIGHT_GREY).append(EMPTY);
            for(int i = 7; i > -1; i--) {
                sb.append(columns[i]);
            }
            sb.append(EMPTY).append("\n").append(RESET_BG_COLOR);
        }
        return sb.toString();
    }

    private String getGameId(int gameNumber) throws ResponseException {
        var games = server.listGames(authToken);
        return games.get(gameNumber - 1).getGameID();
    }

    private ChessPosition getPosition(String c, String r) {
        int col = convertColumn(c);
        int row = Integer.parseInt(r);
        return new ChessPosition(row, col);
    }

    private ArrayList<ChessPosition> listMoves(ChessPosition piece) {
        Collection<ChessMove> moves = currentGame.validMoves(piece);
        moves = currentGame.getMoves(moves);
        ArrayList<ChessPosition> positions = new ArrayList<>();
        for(ChessMove move : moves) {
            positions.add(move.getEndPosition());
        }
        return positions;
    }

    private int convertColumn(String col) {
        if(Objects.equals(playerColor, "black")) {
            return switch (col) {
                case "h" -> 1;
                case "g" -> 2;
                case "f" -> 3;
                case "e" -> 4;
                case "d" -> 5;
                case "c" -> 6;
                case "b" -> 7;
                case "a" -> 8;
                default -> throw new IllegalStateException("Unexpected value: " + col);
            };
        } else {
            return switch (col) {
                case "a" -> 1;
                case "b" -> 2;
                case "c" -> 3;
                case "d" -> 4;
                case "e" -> 5;
                case "f" -> 6;
                case "g" -> 7;
                case "h" -> 8;
                default -> throw new IllegalStateException("Unexpected value: " + col);
            };
        }
    }

    private ChessMove confirmMove(Collection<ChessMove> moves) {
        ArrayList<ChessMove> possibleMoves = (ArrayList<ChessMove>) moves;
        System.out.println("Please choose which move you would like to make");
        int i = 1;
        for(ChessMove move : moves) {
            String s = i + ". " +
                    columns[8 - move.getEndPosition().getColumn()] + "\b" + move.getEndPosition().getRow();
            System.out.println(s);
            i++;
        }
        System.out.print("[INGAME]>>> ");
        while(true) {
            Scanner scanner = new Scanner(System.in);
            var input = Integer.parseInt(scanner.nextLine());
            if (input > possibleMoves.size()) {
                System.out.printf(SET_TEXT_COLOR_GREEN + "%s was not an option, please try again.%n" + SET_TEXT_COLOR_BLUE, input);
                System.out.print("[INGAME]>>> ");
                continue;
            }
            return possibleMoves.get(input - 1);
        }
    }

    @Override
    public void notify(ServerMessage message) throws ResponseException {
        if(message.getServerMessageType().equals(ServerMessage.ServerMessageType.LOAD_GAME)) {
            currentGame = new Gson().fromJson(String.valueOf(message), GameData.class).getGame();
            System.out.print("\n" + redrawBoard());
            System.out.print("[INGAME]>>> ");
        } else {
            checkMessage(message);
            System.out.println(SET_TEXT_COLOR_RED + "\b".repeat(12) + message + SET_TEXT_COLOR_BLUE);
            System.out.printf("[%s]>>> ", state);
        }
    }

    private void checkMessage(ServerMessage msg) {
        String message = String.valueOf(msg);
        if(message.contains("resign")) {
            gameOver = true;
        }
    }

    private String checkForMate() {
        if(currentGame.isInCheckmate(ChessGame.TeamColor.WHITE)) {
            gameOver = true;
            return "\b".repeat(12) + "Game over, Black wins!";
        } else if(currentGame.isInCheckmate(ChessGame.TeamColor.BLACK)) {
            gameOver = true;
            return "\b".repeat(12) + "Game over, White wins!";
        }
        if(currentGame.isInStalemate(ChessGame.TeamColor.WHITE) || currentGame.isInStalemate(ChessGame.TeamColor.BLACK)) {
            gameOver = true;
            return "\b".repeat(12) + "Game Over, Stalemate!";
        }
        return null;
    }
}