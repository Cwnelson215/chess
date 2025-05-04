package chess;

import java.util.Collection;
import java.util.List;

public interface MoveCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition);

}

class KingMoves implements MoveCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

    }
}

class QueenMoves implements MoveCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

    }
}

class KnightMoves implements MoveCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

    }
}

class RookMoves implements MoveCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

    }
}

class BishopMoves implements MoveCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessMove[] possibleMoves = {};
        int currentRow = myPosition.getRow();
        int currentCol = myPosition.getColumn();


        return List.of(possibleMoves);
    }
}

class PawnMoves implements MoveCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

    }
}