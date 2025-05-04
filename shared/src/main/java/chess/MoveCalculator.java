package chess;

import java.util.Collection;

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
        Collection<ChessMove> possibleMoves = new Collection<ChessMove>;
        int currentRow = myPosition.getRow();
        int currentCol = myPosition.getColumn();
        ChessPosition startingPosition = new ChessPosition(currentRow, currentCol);
        int counter = 0;
        while(currentRow != 7 & currentCol != 0) {
            currentRow += 1;
            currentCol -=1;
            ChessPosition endingPosition = new ChessPosition(currentRow, currentCol);
            ChessMove move = new ChessMove(startingPosition, endingPosition, null);
            possibleMoves[0][counter] = move;
            counter++;
        }
        currentRow = myPosition.getRow();
        currentCol = myPosition.getColumn();

        counter = 0;
        while(currentRow != 0 & currentCol != 0) {
            currentRow -= 1;
            currentCol -=1;
            ChessPosition endingPosition = new ChessPosition(currentRow, currentCol);
            ChessMove move = new ChessMove(startingPosition, endingPosition, null);
            possibleMoves[1][counter] = move;
            counter++;
        }
        currentRow = myPosition.getRow();
        currentCol = myPosition.getColumn();

        counter = 0;
        while(currentRow != 7 & currentCol != 0) {
            currentRow -= 1;
            currentCol +=1;
            ChessPosition endingPosition = new ChessPosition(currentRow, currentCol);
            ChessMove move = new ChessMove(startingPosition, endingPosition, null);
            possibleMoves[2][counter] = move;
            counter++;
        }
        currentRow = myPosition.getRow();
        currentCol = myPosition.getColumn();

        counter = 0;
        while(currentRow != 7 & currentCol != 7) {
            currentRow += 1;
            currentCol +=1;
            ChessPosition endingPosition = new ChessPosition(currentRow, currentCol);
            ChessMove move = new ChessMove(startingPosition, endingPosition, null);
            possibleMoves[3][counter] = move;
            counter++;
        }
        
        return possibleMoves;
    }
}

class PawnMoves implements MoveCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

    }
}