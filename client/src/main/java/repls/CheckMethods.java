package repls;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import model.GameData;
import serverfacade.ResponseException;
import serverfacade.ServerFacade;

public class CheckMethods {
    public void params(String s, String...params) throws ResponseException {
        if(params.length > 0) {
            throw new ResponseException(400, String.format("No inputs required for %s command", s));
        }
    }

    public void state(State expectedState, State actualState) throws ResponseException {
        if(expectedState != actualState) {
            throw new  ResponseException(400, String.format("Error: must be %s to perform that action", expectedState.toString()));
        }
    }

    public void pieceColor(ChessPosition position, ChessGame currentGame, String playerColor) throws ResponseException {
        ChessPiece piece = currentGame.getBoard().getPiece(position);
        if(piece != null) {
            if(piece.getTeamColor().equals(ChessGame.TeamColor.WHITE)) {
                if(!playerColor.equals("white")) {
                    throw new ResponseException(400, "Selected piece is not your color");
                }
            } else {
                if(!playerColor.equals("black")) {
                    throw new ResponseException(400, "Selected piece is not your color");
                }
            }
        } else {
            throw new ResponseException(400, "No piece in found selected square");
        }
    }

    public void gameStatus(boolean gameOver) throws ResponseException {
        if(gameOver) {
            throw new ResponseException(400, "Game is over, no more moves may be made");
        }
    }

    public void colorAvailable(ServerFacade server, String gameID, String playerColor, String authToken) throws ResponseException {
        var list = server.listGames(authToken);
        GameData game = list.get(Integer.parseInt(gameID) - 1);
        if(playerColor.equals("white")) {
            if(game.getWhiteUsername() != null) {
                throw new ResponseException(400, "Selected color is already taken");
            }
        } else if(game.getBlackUsername() != null) {
            throw new ResponseException(400, "Selected color is already taken");
        }
    }
}
