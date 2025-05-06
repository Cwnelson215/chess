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
            ChessPosition carrier = myPosition;
            for(int j = 0; j < 7; j++) {
                ChessPosition endPosition = offSet.applyOffSet(carrier, offSet.bishopOffSets[i]);
                carrier = endPosition;
                if(endPosition.getRow() != -1 && endPosition.getColumn() != -1) {
                    ChessMove validMove = new ChessMove(myPosition, endPosition, null);
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
            ChessPosition carrier = myPosition;
            for(int j = 0; j < 7; j++) {
                ChessPosition endPosition = offSet.applyOffSet(carrier, offSet.bishopOffSets[i]);
                carrier = endPosition;
                if(endPosition.getRow() != -1 && endPosition.getColumn() != -1) {
                    ChessMove validMove = new ChessMove(myPosition, endPosition, null);
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
        ChessPosition carrier = myPosition;
        for(int i = 0; i < 7; i++) {
            ChessPosition endPosition = offSet.applyOffSet(carrier, offSet.bishopOffSets[i]);
            carrier = endPosition;
            if (endPosition.getRow() != -1 && endPosition.getColumn() != -1) {
                ChessMove validMove = new ChessMove(myPosition, endPosition, null);
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
            ChessPosition carrier = myPosition;
            for(int j = 0; j < 7; j++) {
                ChessPosition endPosition = offSet.applyOffSet(carrier, offSet.bishopOffSets[i]);
                carrier = endPosition;
                if(endPosition.getRow() != -1 && endPosition.getColumn() != -1) {
                    ChessMove validMove = new ChessMove(myPosition, endPosition, null);
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
            ChessPosition carrier = myPosition;
            for(int j = 0; j < 7; j++) {
                ChessPosition endPosition = offSet.applyOffSet(carrier, offSet.bishopOffSets[i]);
                carrier = endPosition;
                if(endPosition.getRow() != -1 && endPosition.getColumn() != -1) {
                    ChessMove validMove = new ChessMove(myPosition, endPosition, null);
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
        Collection<ChessMove> possibleMoves = new ArrayList<ChessMove>(4);
        OffSet offSet = new OffSet();
        ChessPiece.PieceType promotionPiece = null;
        int diagonals = checkDiagonals(board, myPosition);
        ChessPosition diagonalLeft = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 1);
        ChessPosition diagoanlRight = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 1);
        if(board.getPiece(myPosition).checkIfMoved()) {
            ChessPosition endPosition = new ChessPosition(myPosition.getRow() - 1 , myPosition.getColumn());
            if(endPosition.getRow() == 0) {
                promotionPiece = ChessPiece.PieceType.QUEEN;
            }
            ChessMove move = new ChessMove(myPosition, endPosition, null);
            int validationCode = move.checkPosition(board.getBoard());
            if(validationCode == 1) {
                possibleMoves.add(move);
            }

            if(diagonals == 1) {
                ChessMove DL = new ChessMove(myPosition, diagonalLeft, null);
                possibleMoves.add(DL);
            } else if(diagonals == 2) {
                ChessMove DR = new ChessMove(myPosition,diagoanlRight,null);
                possibleMoves.add(DR);
            } else if(diagonals == 3) {
                ChessMove move1 = new ChessMove(myPosition, diagonalLeft,null);
                ChessMove move2 = new ChessMove(myPosition, diagoanlRight,null);
                possibleMoves.add(move1);
                possibleMoves.add(move2);
            }
        } else {
            for(int i = 0; i < 2; i++) {
                ChessPosition endPosition = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn());
                ChessMove move = new ChessMove(myPosition, endPosition, null);
                int validationCode = move.checkPosition(board.getBoard());
                if(validationCode == 1) {
                    possibleMoves.add(move);
                } else {
                    break;
                }
            }
            if(diagonals == 1) {
                ChessMove move = new ChessMove(myPosition, diagonalLeft, null);
                possibleMoves.add(move);
            } else if(diagonals == 2) {
                ChessMove move = new ChessMove(myPosition, diagoanlRight,null);
                possibleMoves.add(move);
            } else if(diagonals == 3) {
                ChessMove move1 = new ChessMove(myPosition, diagonalLeft, null);
                ChessMove move2 = new ChessMove(myPosition, diagoanlRight,null);
                possibleMoves.add(move1);
                possibleMoves.add(move2);
            }
        }

        return possibleMoves;
    }

    public int checkDiagonals(ChessBoard board, ChessPosition myPosition) {
        boolean enemyToLeft = false;
        boolean enemyToRight = false;
        ChessPiece[][] gameBoard = board.getBoard();
        ChessPosition leftDiag = null;
        ChessPosition rightDiag = null;

        if(myPosition.getColumn() != 1) {
            leftDiag = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 1);
        }
        if(myPosition.getColumn() != 8) {
            rightDiag = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 1);
        }
        ChessPosition[] diagonals = {leftDiag, rightDiag};

        for(int i = 0; i < 2; i++) {
            if(diagonals[i] != null) {
                if(gameBoard[diagonals[i].getRow() - 1][diagonals[i].getColumn() - 1] != null) {
                    if(board.getPiece(myPosition).getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                        if (i == 0) {
                            enemyToLeft = true;
                        } else {
                            enemyToRight = true;
                        }
                    }
                }
            }
        }

        if(enemyToLeft) {
            if(enemyToRight) {
                return 3;
            } else {
                return 1;
            }
        } else if(enemyToRight) {
            return 2;
        }

        return 0;
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

        if(1 <= start.getRow() + changes[0] && 8 >= start.getRow() + changes[0] && start.getColumn() + changes[1] >= 1 && start.getColumn() + changes[1] <= 8 ) {
            endRow = start.getRow() + changes[0];
            endCol = start.getColumn() + changes[1];
        }

        return new ChessPosition(endRow, endCol);
    }
}