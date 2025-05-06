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
        Collection<ChessMove> possibleMoves = new ArrayList<ChessMove>(4);
        OffSet offSet = new OffSet();
        ChessPiece.PieceType promotionPiece = null;
        int diagonals = checkDiagonals(board, myPosition);
        ChessPosition diagonalLeft = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 1);
        ChessPosition diagoanlRight = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 1);
        if(board.getBoard()[myPosition.getRow()][myPosition.getColumn()].checkIfMoved()) {
            ChessPosition endPosition = new ChessPosition(myPosition.getRow() - 1 , myPosition.getColumn());
            if(endPosition.getRow() == 0) {
                promotionPiece = ChessPiece.PieceType.QUEEN;
            }
            ChessMove move = new ChessMove(endPosition, myPosition, promotionPiece);
            int validationCode = move.checkPosition(board.getBoard());
            if(validationCode == 1) {
                possibleMoves.add(move);
            }

            if(diagonals == 1) {
                ChessMove DL = new ChessMove(diagonalLeft, myPosition, null);
                possibleMoves.add(DL);
            } else if(diagonals == 2) {
                ChessMove DR = new ChessMove(diagoanlRight, myPosition, null);
                possibleMoves.add(DR);
            } else if(diagonals == 3) {
                ChessMove move1 = new ChessMove(diagonalLeft, myPosition, null);
                ChessMove move2 = new ChessMove(diagoanlRight, myPosition, null);
                possibleMoves.add(move1);
                possibleMoves.add(move2);
            }
        } else {
            for(int i = 0; i < 2; i++) {
                ChessPosition endPosition = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn());
                ChessMove move = new ChessMove(endPosition, myPosition, null);
                int validationCode = move.checkPosition(board.getBoard());
                if(validationCode == 1) {
                    possibleMoves.add(move);
                } else {
                    break;
                }
            }
            if(diagonals == 1) {
                ChessMove move = new ChessMove(diagonalLeft, myPosition, null);
                possibleMoves.add(move);
            } else if(diagonals == 2) {
                ChessMove move = new ChessMove(diagoanlRight, myPosition, null);
                possibleMoves.add(move);
            } else if(diagonals == 3) {
                ChessMove move1 = new ChessMove(diagonalLeft, myPosition, null);
                ChessMove move2 = new ChessMove(diagoanlRight, myPosition, null);
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
        ChessPosition[] diagonals = {
                new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() -1),
                new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 1)
        };

        for(int i = 0; i < 2; i++) {
            if(gameBoard[diagonals[i].getRow()][diagonals[i].getColumn()] != null) {
                if(gameBoard[diagonals[i].getRow()][diagonals[i].getColumn()].getTeamColor() != gameBoard[myPosition.getRow()][myPosition.getColumn()].getTeamColor()) {
                    if (i == 0) {
                        enemyToLeft = true;
                    } else {
                        enemyToRight = true;
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

        if(0 <= start.getRow() && 7 >= start.getRow() && start.getColumn() >= 0 && start.getColumn() <= 7 ) {
            endRow = start.getRow() + changes[0];
            endCol = start.getColumn() +  changes[1];
        }

        return new ChessPosition(endRow, endCol);
    }
}