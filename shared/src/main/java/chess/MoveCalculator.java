package chess;

import java.util.ArrayList;
import java.util.Collection;

public interface MoveCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition);

}

class KingMoves implements MoveCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> possibleMoves = new ArrayList<ChessMove>(7);
        OffSet offSet = new OffSet();
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 7; j++) {
                ChessPosition endPosition = offSet.applyOffSet(myPosition, offSet.kingOffSets[i]);
                if(endPosition.getRow() != -1 && endPosition.getColumn() != -1) {
                    ChessMove validMove = new ChessMove(endPosition, myPosition, null);
                    int validationCode = validMove.checkPosition(board.getBoard());
                    if(validationCode == 1) {
                        possibleMoves.add(validMove);
                    } else {
                        if(validationCode == 2) {
                            possibleMoves.add(validMove);
                        }
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return possibleMoves;
    }
}

class QueenMoves implements MoveCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> possibleMoves = new ArrayList<ChessMove>(7);
        OffSet offSet = new OffSet();
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 7; j++) {
                ChessPosition endPosition = offSet.applyOffSet(myPosition, offSet.queenOffSets[i]);
                if(endPosition.getRow() != -1 && endPosition.getColumn() != -1) {
                    ChessMove validMove = new ChessMove(endPosition, myPosition, null);
                    int validationCode = validMove.checkPosition(board.getBoard());
                    if(validationCode == 1) {
                        possibleMoves.add(validMove);
                    } else {
                        if(validationCode == 2) {
                            possibleMoves.add(validMove);
                        }
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return possibleMoves;
    }
}

class KnightMoves implements MoveCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> possibleMoves = new ArrayList<ChessMove>(8);
        OffSet offSet = new OffSet();
        for (int i = 0; i < 8; i++) {
            ChessPosition endPosition = offSet.applyOffSet(myPosition, offSet.knightOffSets[i]);
            if (endPosition.getRow() != -1 && endPosition.getColumn() != -1) {
                ChessMove validMove = new ChessMove(endPosition, myPosition, null);
                int validationCode = validMove.checkPosition(board.getBoard());
                if(validationCode == 1) {
                    possibleMoves.add(validMove);
                } else {
                    if (validationCode == 2) {
                        possibleMoves.add(validMove);
                    }
                    break;
                }
            } else {
                break;
            }
        }
        return possibleMoves;
    }
}

class RookMoves implements MoveCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> possibleMoves = new ArrayList<ChessMove>(7);
        OffSet offSet = new OffSet();
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 7; j++) {
                ChessPosition endPosition = offSet.applyOffSet(myPosition, offSet.rookOffSets[i]);
                if(endPosition.getRow() != -1 && endPosition.getColumn() != -1) {
                    ChessMove validMove = new ChessMove(endPosition, myPosition, null);
                    int validationCode = validMove.checkPosition(board.getBoard());
                    if(validationCode == 1) {
                        possibleMoves.add(validMove);
                    } else {
                        if(validationCode == 2) {
                            possibleMoves.add(validMove);
                        }
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return possibleMoves;
    }
}

class BishopMoves implements MoveCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> possibleMoves = new ArrayList<ChessMove>(7);
        OffSet offSet = new OffSet();
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 7; j++) {
                ChessPosition endPostition = offSet.applyOffSet(myPosition, offSet.bishopOffSets[i]);
                if(endPostition.getRow() != -1 && endPostition.getColumn() != -1) {
                    ChessMove validMove = new ChessMove(endPostition, myPosition, null);
                    int validationCode = validMove.checkPosition(board.getBoard());
                    if (validationCode == 1) {
                        possibleMoves.add(validMove);
                    } else {
                        if (validationCode == 2) {
                            possibleMoves.add(validMove);
                        }
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        
        return possibleMoves;
    }

}

class PawnMoves implements MoveCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

    }
}


class OffSet {
    int[][] bishopOffSets = {{1, -1}, {-1, -1}, {-1, 1}, {1, 1}};
    int[][] knightOffSets = {{2, -1}, { 1, -2}, { -1, -2}, {-2, -1}, {-2, 1}, {-1, 2}, {1, 2}, {2, 1}};
    int[][] rookOffSets = {{0, -1}, {-1, 0}, {0, 1}, {1, 0}};
    int[][] queenOffSets = {{1, -1}, {-1, -1}, {-1, 1}, {1, 1}, {0, -1}, {-1, 0}, {0, 1}, {1, 0}};
    int[][] kingOffSets = {{0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}};

    public ChessPosition applyOffSet(ChessPosition start, int[] changes) {
        int endRow = -1;
        int endCol = -1;

        if(0 <= start.getRow() && 7 >= start.getRow() && start.getColumn() >= 0 && start.getColumn() <= 7 ) {
            endRow = start.getRow() + changes[0];
            endCol = start.getColumn() +  changes[1];
        }

        return new ChessPosition(endRow, endCol);
    }
}