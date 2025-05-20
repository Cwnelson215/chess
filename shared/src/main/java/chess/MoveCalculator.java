package chess;

import java.util.ArrayList;
import java.util.Collection;

public interface MoveCalculator {
    Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition);
}

class KingMoves implements MoveCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece currentPiece = board.getPiece(myPosition);
        Collection<ChessMove> possibleMoves = new ArrayList<>(7);
        OffSet offSet = new OffSet();
        for(int i = 0; i < 8; i++) {
            ChessPosition endPosition = offSet.applyOffSet(myPosition, offSet.kingOffSets[i]);
            currentPiece.addMoves(board, myPosition, endPosition, possibleMoves);
        }
        return possibleMoves;
    }

}

class QueenMoves implements MoveCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece currentPiece = board.getPiece(myPosition);
        Collection<ChessMove> possibleMoves = new ArrayList<>(7);
        OffSet offSet = new OffSet();
        int validationCode;
        for (int i = 0; i < 8; i++) {
            ChessPosition carrier = myPosition;
            for(int j = 0; j < 7; j++) {
                ChessPosition endPosition = offSet.applyOffSet(carrier, offSet.queenOffSets[i]);
                carrier = endPosition;
                validationCode = currentPiece.addMoves(board, myPosition, endPosition, possibleMoves);
                if(validationCode != 1) {
                    break;
                }
            }
        }
        return possibleMoves;
    }
}

class BishopMoves implements MoveCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece currentPiece = board.getPiece(myPosition);
        Collection<ChessMove> possibleMoves = new ArrayList<>(7);
        OffSet offSet = new OffSet();
        int validationCode;
        for (int i = 0; i < 4; i++) {
            ChessPosition carrier = myPosition;
            for(int j = 0; j < 7; j++) {
                ChessPosition endPosition = offSet.applyOffSet(carrier, offSet.bishopOffSets[i]);
                carrier = endPosition;
                validationCode = currentPiece.addMoves(board, myPosition, endPosition, possibleMoves);
                if(validationCode != 1) {
                    break;
                }
            }
        }

        return possibleMoves;
    }
}

class KnightMoves implements MoveCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece currentPiece = board.getPiece(myPosition);
        Collection<ChessMove> possibleMoves = new ArrayList<>(8);
        OffSet offSet = new OffSet();
        for(int i = 0; i < 8; i++) {
            ChessPosition endPosition = offSet.applyOffSet(myPosition, offSet.knightOffSets[i]);
            currentPiece.addMoves(board, myPosition, endPosition, possibleMoves);
        }
        return possibleMoves;
    }


}

class RookMoves implements MoveCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece currentPiece = board.getPiece(myPosition);
        Collection<ChessMove> possibleMoves = new ArrayList<>(7);
        OffSet offSet = new OffSet();
        int validationCode;
        for (int i = 0; i < 4; i++) {
            ChessPosition carrier = myPosition;
            for(int j = 0; j < 7; j++) {
                ChessPosition endPosition = offSet.applyOffSet(carrier, offSet.rookOffSets[i]);
                carrier = endPosition;
                validationCode = currentPiece.addMoves(board, myPosition, endPosition, possibleMoves);
                if(validationCode != 1) {
                    break;
                }
            }
        }
        return possibleMoves;
    }
}

class PawnMoves implements MoveCalculator {
    Collection<ChessMove> possibleMoves = new ArrayList<>(4);
    ChessPiece.PieceType[] promotionPieces = {
            ChessPiece.PieceType.QUEEN,
            ChessPiece.PieceType.ROOK,
            ChessPiece.PieceType.KNIGHT,
            ChessPiece.PieceType.BISHOP
    };
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
            addDiagonals(board, diagonals, myPosition, diagonalLeft, diagonalRight, true);
        } else {
            doubleMove(board, myPosition, row, col);
            addDiagonals(board, diagonals, myPosition, diagonalLeft, diagonalRight, false);
        }
    }

    public void moveBlack(ChessBoard board, ChessPosition myPosition) {
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        int diagonals = checkDiagonals(board, myPosition, ChessGame.TeamColor.BLACK);
        ChessPosition diagonalLeft = new ChessPosition(row - 1, col - 1);
        ChessPosition diagonalRight = new ChessPosition(row - 1, col + 1);
        if(board.getPiece(myPosition).checkIfMoved(myPosition)) {
            ChessPosition endPosition = new ChessPosition(row - 1, col);
            if(myPosition.getRow() == 2) {
                promotionLoop(board, myPosition, endPosition, 0);
            } else {
                ChessMove move = new ChessMove(myPosition, endPosition, null);
                int validationCode = move.checkPosition(board);
                if(validationCode == 1) {
                    possibleMoves.add(move);
                }
            }
            addDiagonals(board, diagonals, myPosition, diagonalLeft, diagonalRight, true);
        } else {
            doubleMove(board, myPosition, row, col);
            addDiagonals(board, diagonals, myPosition, diagonalLeft, diagonalRight, false);
        }
    }

    public void doubleMove(ChessBoard board, ChessPosition myPosition, int row, int column) {
        ChessPosition endPosition;
        int carrierRow = row - 1;
        for(int i = 0; i < 2; i++) {
            if(board.getPiece(myPosition).getTeamColor() != ChessGame.TeamColor.WHITE) {
                endPosition = new ChessPosition(carrierRow, column);
                carrierRow -= 1;
            } else {
                carrierRow += 1;
                endPosition = new ChessPosition(carrierRow + 1, column);
            }
            ChessMove move = new ChessMove(myPosition, endPosition, null);
            int validationCode = move.checkPosition(board);
            if(validationCode == 1) {
                possibleMoves.add(move);
            } else {
                break;
            }
        }
    }

    public int checkDiagonals(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor teamColor) {
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        boolean enemyToLeft = false;
        boolean enemyToRight = false;
        ChessPiece[][] gameBoard = board.getBoard();
        ChessPosition[] diagonals = getChessPositions(teamColor, col, row);

        for(int i = 0; i < 2; i++) {
            if(diagonals[i] == null) {continue;}
            if(gameBoard[diagonals[i].getRow() - 1][diagonals[i].getColumn() - 1] == null) {continue;}
            if(board.getPiece(myPosition).getTeamColor() == board.getPiece(diagonals[i]).getTeamColor()) {continue;}
            if (i == 0) {
                enemyToLeft = true;
            } else {
                enemyToRight = true;
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

    private static ChessPosition[] getChessPositions(ChessGame.TeamColor teamColor, int col, int row) {
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
        return new ChessPosition[]{leftDiag, rightDiag};
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

    public void addDiagonals(ChessBoard board, int diagonals, ChessPosition myPosition,
                             ChessPosition diagonalLeft, ChessPosition diagonalRight, boolean loopNeeded) {
        if(diagonals == 1) {
            loopNeeded(board, diagonals, myPosition, diagonalLeft, loopNeeded);
        } else if(diagonals == 2) {
            loopNeeded(board, diagonals, myPosition, diagonalRight, loopNeeded);
        } else if(diagonals == 3) {
            loopNeeded(board, diagonals, myPosition, diagonalLeft, loopNeeded);
            loopNeeded(board, diagonals, myPosition, diagonalRight, loopNeeded);
        }
    }

    private void loopNeeded(ChessBoard board, int diagonals, ChessPosition myPosition, ChessPosition diagonal, boolean loopNeeded) {
        if(loopNeeded) {
            if(myPosition.getRow() == 2 || myPosition.getRow() == 7) {
                promotionLoop(board, myPosition, diagonal, diagonals);
            } else {
                ChessMove move = new ChessMove(myPosition, diagonal, null);
                possibleMoves.add(move);
            }
        } else {
            ChessMove move = new ChessMove(myPosition, diagonal, null);
            possibleMoves.add(move);
        }
    }

}


class OffSet {
    int[][] bishopOffSets = {{1, -1}, {-1, -1}, {-1, 1}, {1, 1}};
    int[][] rookOffSets = {{0, -1}, {-1, 0}, {0, 1}, {1, 0}};
    int[][] knightOffSets = {
            {2, -1},
            { 1, -2},
            { -1, -2},
            {-2, -1},
            {-2, 1},
            {-1, 2},
            {1, 2},
            {2, 1}
    };
    int[][] queenOffSets = {
            {0, -1},
            {-1, -1},
            {-1, 0},
            {-1, 1},
            {0, 1},
            {1, 1},
            {1, 0},
            {1, -1}
    };
    int[][] kingOffSets = {
            {0, -1},
            {-1, -1},
            {-1, 0},
            {-1, 1},
            {0, 1},
            {1, 1},
            {1, 0},
            {1, -1}
    };

    public ChessPosition applyOffSet(ChessPosition start, int[] changes) {
        int endRow = -1;
        int endCol = -1;

        if(1 <= start.getRow() + changes[0] && 8 >= start.getRow() + changes[0]
                && start.getColumn() + changes[1] >= 1 && start.getColumn() + changes[1] <= 8 ) {
            endRow = start.getRow() + changes[0];
            endCol = start.getColumn() + changes[1];
        }

        return new ChessPosition(endRow, endCol);
    }
}