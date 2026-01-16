package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

public class PieceMovesCalculator {
    ChessBoard board;
    ChessPosition myPosition;
    ChessPiece piece;
    int my_x; int my_y;

    public PieceMovesCalculator(ChessBoard board, ChessPosition myPosition) {
        this.board = board;
        this.myPosition = myPosition;
        this.piece = board.getPiece(myPosition);
        this.my_x = myPosition.getRow();
        this.my_y = myPosition.getColumn();
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
    private boolean isInBounds (int x, int y) {
        return (x >= 0 && x < 8) && (y >= 0 && y < 8);
    }

    private Collection<ChessMove> KingMovesCalculator() {
        int [][] offsets = {{1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}};
        return generateStepMoves(offsets);
    }

    private Collection<ChessMove> QueenMovesCalculator() {
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, -1}, {-1, 1}, {1, -1}};
        return generateSlidingMoves(directions);
    }

    private Collection<ChessMove> KnightMovesCalculator() {
        int [][] offsets = {{2, 1}, {2, -1}, {1, -2}, {-1, 2}, {-2, -1}, {-2, 1}, {-1, 2}, {-1, -2}};
        return generateStepMoves(offsets);
    }

    private Collection<ChessMove> PawnMovesCalculator() {
        List<ChessMove> possibleMoves = new ArrayList<>();
        return possibleMoves;
    }

    private Collection<ChessMove> BishopMovesCalculator() {
        int[][] directions = {{1, 1}, {-1, -1}, {-1, 1}, {1, -1}};
        return generateSlidingMoves(directions);
    }

    private Collection<ChessMove> RookMovesCalculator() {
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        return generateSlidingMoves(directions);
    }

    private Collection<ChessMove> generateSlidingMoves(int[][] directions) {
        List<ChessMove> possibleMoves = new ArrayList<>();

        for (int[] direction : directions){
            int x = direction[0];
            int y = direction[1];

            while (isInBounds(my_x + x, my_y + y)) {
                ChessPosition targetPosition = new ChessPosition(my_x + x, my_y + y);
                ChessPiece targetPiece = board.getPiece(targetPosition);

                if (targetPiece == null) {
                    possibleMoves.add(new ChessMove(myPosition, targetPosition, null));
                    if (x != 0) x = x < 0 ? x - 1 : x + 1;
                    if (y != 0) y = y < 0 ? y - 1 : y + 1;
                } else {
                    if (targetPiece.getTeamColor() != piece.getTeamColor()) {
                        possibleMoves.add(new ChessMove(myPosition, targetPosition, null));
                    }
                    break;
                }
            }
        }
        return possibleMoves;
    }

    private Collection<ChessMove> generateStepMoves(int[][] offsets) {
        List<ChessMove> possibleMoves = new ArrayList<>();

        for (int[] offset : offsets){
            int x = offset[0];
            int y = offset[1];

            if (isInBounds(my_x + x, my_y + y)) {
                ChessPosition targetPosition = new ChessPosition(my_x + x, my_y + y);
                ChessPiece targetPiece = board.getPiece(targetPosition);
                if ((targetPiece == null) || (targetPiece.getTeamColor() != piece.getTeamColor())) {
                    possibleMoves.add(new ChessMove(myPosition, targetPosition, null));
                }
            }
        }
        return possibleMoves;
    }
}

