package chess;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 *  the signature of the existing methods.
 */
public class ChessMove {

    private final ChessPosition startPosition;
    private final ChessPosition endPosition;
    private final ChessPiece.PieceType promotionPiece;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     ChessPiece.PieceType promotionPiece) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.promotionPiece = promotionPiece;
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return startPosition;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return endPosition;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return promotionPiece;
    }

    public int checkPosition(ChessPiece[][] board) {
        int row = this.endPosition.getRow();
        int col = this.endPosition.getColumn();
        if(board[row][col] != null) {
            if(board[row][col].getTeamColor() != board[startPosition.getRow()][startPosition.getColumn()].getTeamColor()) {
                return 2;
            } else {
                return 3;
            }
        } else if(row == 0 || row > 7 || col == 0 || col > 7) {
            return 4;
        }
        return 1;
    }
}
