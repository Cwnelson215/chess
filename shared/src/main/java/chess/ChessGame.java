package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor currentTeam = TeamColor.WHITE;
    private ChessBoard gameBoard = new ChessBoard();



    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTeam;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.currentTeam = team;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return currentTeam == chessGame.currentTeam && Objects.equals(gameBoard, chessGame.gameBoard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentTeam, gameBoard);
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        Collection<ChessMove> possibleMoves = gameBoard.getPiece(startPosition).pieceMoves(gameBoard, startPosition);
        ChessPiece selectedPiece = gameBoard.getPiece(startPosition);

        if(isInCheck(selectedPiece.getTeamColor())) {
            ChessPosition kingPosition = findKing(selectedPiece.getTeamColor());
            possibleMoves = checkMoves(gameBoard.getPiece(kingPosition).pieceMoves(gameBoard, kingPosition), gameBoard.getPiece(kingPosition).getTeamColor());
        }

        return possibleMoves;
    }

    public void checkIfValidMove(Collection<ChessMove> moves, ChessMove move) throws InvalidMoveException {
        if(moves.contains(move)) {
            return;
        } else {
            throw new InvalidMoveException("Invalid Move");
        }
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if(gameBoard.getPiece(move.getStartPosition()) != null) {
            int endRow = move.getEndPosition().getRow() - 1;
            int endCol = move.getEndPosition().getColumn() - 1;
            int validationCode = move.checkPosition(gameBoard);

            if(isInCheck(gameBoard.getPiece(move.getStartPosition()).getTeamColor())) {
                throw new InvalidMoveException("You're in check");
            }

            Collection<ChessMove> possibleMoves = validMoves(move.getStartPosition());

            //Checks if it's the right teams turn
            checkTurn(gameBoard.getPiece(move.getStartPosition()));
            //Checks if this is a valid move for the piece to make
            checkIfValidMove(possibleMoves, move);

            ChessPiece.PieceType promotionPiece = move.getPromotionPiece();
            TeamColor teamColor = gameBoard.getPiece(move.getStartPosition()).getTeamColor();

            if (promotionPiece == null) {
                if (validationCode != 3) {
                    if (gameBoard.getPiece(move.getEndPosition()) == null) {
                        gameBoard.addPiece(move.getEndPosition(), gameBoard.getPiece(move.getStartPosition()));
                    } else {
                        gameBoard.getBoard()[endRow][endCol] = null;
                        gameBoard.addPiece(move.getEndPosition(), gameBoard.getPiece(move.getStartPosition()));
                    }
                } else {
                    throw new InvalidMoveException("Cannot Capture Same Team Color");
                }
            } else {
                if (gameBoard.getPiece(move.getEndPosition()) == null) {
                    gameBoard.addPiece(move.getEndPosition(), new ChessPiece(teamColor, promotionPiece));
                } else {
                    gameBoard.getBoard()[endRow][endCol] = null;
                    gameBoard.addPiece(move.getEndPosition(), new ChessPiece(teamColor, promotionPiece));
                }
            }

            if (gameBoard.getPiece(move.getStartPosition()).getTeamColor() == TeamColor.WHITE) {
                setTeamTurn(TeamColor.BLACK);
            } else {
                setTeamTurn(TeamColor.WHITE);
            }
            gameBoard.getBoard()[move.getStartPosition().getRow() - 1][move.getStartPosition().getColumn() - 1] = null;
        } else {
            throw new InvalidMoveException("No piece at starting position");
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKing(teamColor);
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                if(gameBoard.getBoard()[i][j] != null) {
                    if(gameBoard.getBoard()[i][j].getTeamColor() != teamColor && gameBoard.getBoard()[i][j].getPieceType() != ChessPiece.PieceType.KING) {
                        ChessPosition piecePosition = new ChessPosition(i+1, j+1);
                        Collection<ChessMove> possibleMoves = gameBoard.getPiece(piecePosition).pieceMoves(gameBoard, piecePosition);
                        ChessMove check = new ChessMove(new ChessPosition(i+1, j+1), kingPosition, null);
                        if(possibleMoves.contains(check)) {
                            gameBoard.getPiece(kingPosition).setCheck(true);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        ChessPosition kingPosition = findKing(teamColor);
        Collection<ChessMove> kingMoves = gameBoard.getPiece(kingPosition).pieceMoves(gameBoard, kingPosition);
        kingMoves = checkMoves(kingMoves, gameBoard.getPiece(kingPosition).getTeamColor());
        return kingMoves.isEmpty();
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.gameBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.gameBoard;
    }

    public ChessPosition findKing(TeamColor kingColor) {
        ChessPosition king = null;
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                if(gameBoard.getBoard()[i][j] != null) {
                    if (gameBoard.getBoard()[i][j].getPieceType() == ChessPiece.PieceType.KING) {
                        if (gameBoard.getBoard()[i][j].getTeamColor() == kingColor) {
                            king = new ChessPosition(i + 1, j + 1);
                        }
                    }
                }
            }
        }
        return king;
    }

    public void checkTurn(ChessPiece piece) throws InvalidMoveException {
        if(piece.getTeamColor() != getTeamTurn()) {
            throw new InvalidMoveException("IT'S NOT YOUR TURN!");
        }
        return;
    }

    public Collection<ChessMove> checkMoves(Collection<ChessMove> moves, TeamColor teamColor) {
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                ChessPiece piece = gameBoard.getBoard()[i][j];
                if(piece != null) {
                    if(piece.getTeamColor() != teamColor) {
                        Collection<ChessMove> pieceMoves = gameBoard.getPiece(new ChessPosition(i + 1, j + 1)).pieceMoves(gameBoard, new ChessPosition(i + 1, j + 1));
                        for(ChessMove move : moves) {
                            for(ChessMove pieceMove : pieceMoves) {
                                if(pieceMove.getEndPosition().equals(move.getEndPosition())) {
                                    moves.remove(move);
                                }
                            }
                        }
                    }
                }
            }
        }

        return moves;
    }



}
