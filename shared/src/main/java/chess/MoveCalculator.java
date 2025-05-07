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
            ChessPosition endPosition = offSet.applyOffSet(myPosition, offSet.kingOffSets[i]);
            if(endPosition.getRow() != -1 && endPosition.getColumn() != -1) {
                ChessMove validMove = new ChessMove(myPosition, endPosition, null);
                int validationCode = validMove.checkPosition(board);
                if(validationCode == 1) {
                    possibleMoves.add(validMove);
                } else {
                    if(validationCode == 2) {
                        possibleMoves.add(validMove);
                    }
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
                ChessPosition endPosition = offSet.applyOffSet(carrier, offSet.queenOffSets[i]);
                carrier = endPosition;
                if(endPosition.getRow() != -1 && endPosition.getColumn() != -1) {
                    ChessMove validMove = new ChessMove(myPosition, endPosition, null);
                    int validationCode = validMove.checkPosition(board);
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
        for(int i = 0; i < 8; i++) {
            ChessPosition endPosition = offSet.applyOffSet(myPosition, offSet.knightOffSets[i]);
            if (endPosition.getRow() != -1 && endPosition.getColumn() != -1) {
                ChessMove validMove = new ChessMove(myPosition, endPosition, null);
                int validationCode = validMove.checkPosition(board);
                if(validationCode == 1) {
                    possibleMoves.add(validMove);
                } else {
                    if (validationCode == 2) {
                        possibleMoves.add(validMove);
                    }
                }
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
                ChessPosition endPosition = offSet.applyOffSet(carrier, offSet.rookOffSets[i]);
                carrier = endPosition;
                if(endPosition.getRow() != -1 && endPosition.getColumn() != -1) {
                    ChessMove validMove = new ChessMove(myPosition, endPosition, null);
                    int validationCode = validMove.checkPosition(board);
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
                    int validationCode = validMove.checkPosition(board);
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
    Collection<ChessMove> possibleMoves = new ArrayList<ChessMove>(4);
    ChessPiece.PieceType[] promotionPieces = {ChessPiece.PieceType.QUEEN, ChessPiece.PieceType.ROOK, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.BISHOP};
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        if(board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.WHITE) {
            moveWhite(board, myPosition);
        } else {
            moveBlack(board, myPosition);
        }

        return possibleMoves;
    }

    public void moveWhite(ChessBoard board, ChessPosition myPosition) {
        int row = myPosition.getRow();
        int col  = myPosition.getColumn();
        OffSet offSet = new OffSet();
        int diagonals = checkDiagonals(board, myPosition, ChessGame.TeamColor.WHITE);
        ChessPosition diagonalLeft = new ChessPosition(row + 1, col - 1);
        ChessPosition diagonalRight = new ChessPosition(row + 1, col + 1);
        if(board.getPiece(myPosition).checkIfMoved(myPosition)) {
            ChessPosition endPosition = new ChessPosition(row + 1, col);
            if(myPosition.getRow() == 7) {
                promotionLoop(board, myPosition, endPosition, 0);
            } else {
                ChessMove move = new ChessMove(myPosition, endPosition, null);
                int validationCode = move.checkPosition(board);
                if(validationCode == 1) {
                    possibleMoves.add(move);
                }
            }
            if(diagonals == 1) {
                if(myPosition.getRow() == 7) {
                    promotionLoop(board, myPosition, diagonalLeft, diagonals);
                } else {
                    ChessMove DL = new ChessMove(myPosition, diagonalLeft, null);
                    possibleMoves.add(DL);
                }
            } else if(diagonals == 2) {
                if(myPosition.getRow() == 7) {
                    promotionLoop(board, myPosition, diagonalRight, diagonals);
                } else {
                    ChessMove DR = new ChessMove(myPosition, diagonalRight, null);
                    possibleMoves.add(DR);
                }
            } else if(diagonals == 3) {
                if(myPosition.getRow() == 7) {
                    promotionLoop(board, myPosition, diagonalLeft, diagonals);
                    promotionLoop(board, myPosition, diagonalRight, diagonals);
                } else{
                    ChessMove move1 = new ChessMove(myPosition, diagonalLeft, null);
                    ChessMove move2 = new ChessMove(myPosition, diagonalRight, null);
                    possibleMoves.add(move1);
                    possibleMoves.add(move2);
                }
            }
        } else {
            int carrierRow = row + 1;
            for(int i = 0; i < 2; i++) {
                ChessPosition endPosition = new ChessPosition(carrierRow, col);
                carrierRow += 1;
                ChessMove move = new ChessMove(myPosition, endPosition, null);
                int validationCode = move.checkPosition(board);
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
                ChessMove move = new ChessMove(myPosition, diagonalRight,null);
                possibleMoves.add(move);
            } else if(diagonals == 3) {
                ChessMove move1 = new ChessMove(myPosition, diagonalLeft, null);
                ChessMove move2 = new ChessMove(myPosition, diagonalRight,null);
                possibleMoves.add(move1);
                possibleMoves.add(move2);
            }
        }
    }

    public void moveBlack(ChessBoard board, ChessPosition myPosition) {
        int row = myPosition.getRow();
        int column = myPosition.getColumn();
        OffSet offSet = new OffSet();
        int diagonals = checkDiagonals(board, myPosition, ChessGame.TeamColor.BLACK);
        ChessPosition diagonalLeft = new ChessPosition(row - 1, column - 1);
        ChessPosition diagonalRight = new ChessPosition(row - 1, column + 1);
        if(board.getPiece(myPosition).checkIfMoved(myPosition)) {
            ChessPosition endPosition = new ChessPosition(row - 1, column);
            if(myPosition.getRow() == 2) {
                promotionLoop(board, myPosition, endPosition, 0);
            } else {
                ChessMove move = new ChessMove(myPosition, endPosition, null);
                int validationCode = move.checkPosition(board);
                if(validationCode == 1) {
                    possibleMoves.add(move);
                }
            }
            if(diagonals == 1) {
                if(myPosition.getRow() == 2) {
                    promotionLoop(board, myPosition, diagonalLeft, diagonals);
                } else {
                    ChessMove DL = new ChessMove(myPosition, diagonalLeft, null);
                    possibleMoves.add(DL);
                }
            } else if(diagonals == 2) {
                if(myPosition.getRow() == 2) {
                    promotionLoop(board, myPosition, diagonalRight, diagonals);
                } else {
                    ChessMove DR = new ChessMove(myPosition, diagonalRight, null);
                    possibleMoves.add(DR);
                }
            } else if(diagonals == 3) {
                if(myPosition.getRow() == 2) {
                    promotionLoop(board, myPosition, diagonalLeft, diagonals);
                    promotionLoop(board, myPosition, diagonalRight, diagonals);
                } else{
                    ChessMove move1 = new ChessMove(myPosition, diagonalLeft, null);
                    ChessMove move2 = new ChessMove(myPosition, diagonalRight, null);
                    possibleMoves.add(move1);
                    possibleMoves.add(move2);
                }
            }
        } else {
            int carrierRow = row - 1;
            for(int i = 0; i < 2; i++) {
                ChessPosition endPosition = new ChessPosition(carrierRow, column);
                carrierRow -= 1;
                ChessMove move = new ChessMove(myPosition, endPosition, null);
                int validationCode = move.checkPosition(board);
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
                ChessMove move = new ChessMove(myPosition, diagonalRight,null);
                possibleMoves.add(move);
            } else if(diagonals == 3) {
                ChessMove move1 = new ChessMove(myPosition, diagonalLeft, null);
                ChessMove move2 = new ChessMove(myPosition, diagonalRight,null);
                possibleMoves.add(move1);
                possibleMoves.add(move2);
            }
        }
    }

    public int checkDiagonals(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor teamColor) {
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        boolean enemyToLeft = false;
        boolean enemyToRight = false;
        ChessPiece[][] gameBoard = board.getBoard();
        ChessPosition leftDiag = null;
        ChessPosition rightDiag = null;

        if(col != 1) {
            if(teamColor == ChessGame.TeamColor.BLACK) {
                leftDiag = new ChessPosition(row - 1, col - 1);
            } else {
                leftDiag = new ChessPosition(row + 1, col - 1);
            }
        }
        if(col != 8) {
            if(teamColor == ChessGame.TeamColor.BLACK) {
                rightDiag = new ChessPosition(row - 1, col + 1);
            } else {
                rightDiag = new ChessPosition(row + 1, col + 1);
            }
        }
        ChessPosition[] diagonals = {leftDiag, rightDiag};

        for(int i = 0; i < 2; i++) {
            if(diagonals[i] != null) {
                if(gameBoard[diagonals[i].getRow() - 1][diagonals[i].getColumn() - 1] != null) {
                    if(board.getPiece(myPosition).getTeamColor() != board.getPiece(diagonals[i]).getTeamColor()) {
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

    public void promotionLoop(ChessBoard board, ChessPosition myPosition, ChessPosition end, int diagonals) {
        for (int i = 0; i < 4; i++) {
            ChessMove move = new ChessMove(myPosition, end, promotionPieces[i]);
            int validationCode = move.checkPosition(board);
            if (validationCode == 1 || diagonals > 0) {
                possibleMoves.add(move);
            }
        }
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