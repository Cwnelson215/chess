package chess;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 *  the signature of the existing methods.
 */
public class ChessPiece {


    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;
    private boolean hasMoved = false;
    private Collection<ChessMove> validMoves;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return hasMoved == that.hasMoved && pieceColor == that.pieceColor && type == that.type && Objects.equals(validMoves, that.validMoves);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type, hasMoved, validMoves);
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = null;
        if(type == PieceType.KING) {
            moves = new KingMoves().pieceMoves(board, myPosition);
        } else if(type == PieceType.QUEEN) {
            moves = new QueenMoves().pieceMoves(board, myPosition);
        } else if(type == PieceType.KNIGHT) {
            moves = new KnightMoves().pieceMoves(board, myPosition);
        } else if(type == PieceType.ROOK) {
            moves = new RookMoves().pieceMoves(board, myPosition);
        } else if(type == PieceType.BISHOP) {
            moves = new BishopMoves().pieceMoves(board, myPosition);
        }  else if(type == PieceType.PAWN) {
            moves = new PawnMoves().pieceMoves(board, myPosition);
        }
        return moves;
    }

    public boolean checkIfMoved(ChessPosition myPosition) {
        if(type == PieceType.PAWN) {
            if(myPosition.getRow() != 2 && pieceColor == ChessGame.TeamColor.WHITE) {
                hasMoved = true;
            } else if(myPosition.getRow() != 7 && pieceColor == ChessGame.TeamColor.BLACK) {
                hasMoved = true;
            }
        } else{
            if(myPosition.getRow() != 1) {
                hasMoved = true;
            }
        }
        return hasMoved;
    }
}
