package chess;

import java.util.Collection;
import java.util.List;

public class PieceMovesCalculator {
    ChessBoard board;
    ChessPosition myPosition;
    ChessPiece piece;

    public PieceMovesCalculator(ChessBoard board, ChessPosition myPosition) {
        this.board = board;
        this.myPosition = myPosition;
        this.piece = board.getPiece(myPosition);
    }

    public Collection<ChessMove> calculateMoves() {
        if (piece.getPieceType() == ChessPiece.PieceType.KING) return KingMovesCalculator();
        if (piece.getPieceType() == ChessPiece.PieceType.QUEEN) return QueenMovesCalculator();
        if (piece.getPieceType() == ChessPiece.PieceType.KNIGHT) return KnightMovesCalculator();
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) return PawnMovesCalculator();
        if (piece.getPieceType() == ChessPiece.PieceType.BISHOP) return BishopMovesCalculator();
        if (piece.getPieceType() == ChessPiece.PieceType.ROOK) return RookMovesCalculator();
        return null;
    }

    //I need to return a LIST of ChessMove objects, each object takes ChessMove Takes start position, end position, and promotionPiece as a parameter
    // For example return List.of(new ChessMove(new ChessPosition(5,4), new ChessPosition(1, 8), null
    private Collection<ChessMove> KingMovesCalculator() {
        return null;
    }

    private Collection<ChessMove> QueenMovesCalculator() {
        return null;
    }

    private Collection<ChessMove> KnightMovesCalculator() {
        return null;
    }

    private Collection<ChessMove> PawnMovesCalculator() {
        return null;
    }

    private Collection<ChessMove> BishopMovesCalculator() {
        return null;
    }

    private Collection<ChessMove> RookMovesCalculator() {
        return null;
    }
}