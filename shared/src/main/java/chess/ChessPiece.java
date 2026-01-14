package chess;

import java.util.Collection;
import java.util.List;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
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
        ChessPiece piece = board.getPiece(myPosition);
        if (piece.getPieceType() == PieceType.KING) return KingMovesCalculator(board, myPosition);
        if (piece.getPieceType() == PieceType.QUEEN) return QueenMovesCalculator(board, myPosition);
        if (piece.getPieceType() == PieceType.KNIGHT) return KnightMovesCalculator(board, myPosition);
        if (piece.getPieceType() == PieceType.PAWN) return PawnMovesCalculator(board, myPosition);
        if (piece.getPieceType() == PieceType.BISHOP) return BishopMovesCalculator(board, myPosition);
        if (piece.getPieceType() == PieceType.ROOK) return RookMovesCalculator(board, myPosition);
        return null;
    }

    //I need to return a LIST of ChessMove objects, each object takes ChessMove Takes start position, end position, and promotionPiece as a parameter
    // For example return List.of(new ChessMove(new ChessPosition(5,4), new ChessPosition(1, 8), null
    private Collection<ChessMove> KingMovesCalculator(ChessBoard board, ChessPosition myPosition) {
        return null;
    }

    private Collection<ChessMove> QueenMovesCalculator(ChessBoard board, ChessPosition myPosition) {
        return null;
    }

    private Collection<ChessMove> KnightMovesCalculator(ChessBoard board, ChessPosition myPosition) {
        return null;
    }

    private Collection<ChessMove> PawnMovesCalculator(ChessBoard board, ChessPosition myPosition) {
        return null;
    }

    private Collection<ChessMove> BishopMovesCalculator(ChessBoard board, ChessPosition myPosition) {
        return null;
    }

    private Collection<ChessMove> RookMovesCalculator(ChessBoard board, ChessPosition myPosition) {
        return null;
    }
}
