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
        this.my_x = myPosition.getColumn();
        this.my_y = myPosition.getRow();
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

    private boolean isInBounds (int x, int y) {
        return (x > 0 && x <= 8) && (y > 0 && y <= 8);
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
        int [][] offsets = {{1, 2}, {1, -2}, {-1, 2}, {-1, -2}, {2, 1}, {2, -1}, {-2, 1}, {-2, -1}};
        return generateStepMoves(offsets);
    }

    private Collection<ChessMove> PawnMovesCalculator() {
        List<ChessMove> possibleMoves = new ArrayList<>();
        int [] whitePawnMoves = {1, 2, 2, 8}; //First Number is regular move, second number is double move, third is starting y, fourth is ending y
        int [] blackPawnMoves = {-1, -2, 7, 1};
        int [] direction = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? whitePawnMoves : blackPawnMoves;

        //Move Forward
        ChessPosition targetPosition = new ChessPosition(my_y + direction[0], my_x);
        ChessPiece targetPiece = board.getPiece(targetPosition);
        if (targetPiece == null) possibleMoves.add(new ChessMove(myPosition, targetPosition, null));

        //Move Forward Twice
        if (my_y == direction[2] && targetPiece == null) {
            targetPosition = new ChessPosition(my_y + direction[1], my_x);
            targetPiece = board.getPiece(targetPosition);
            if (targetPiece == null) possibleMoves.add(new ChessMove(myPosition, targetPosition, null));
        }

        //Kill Diagonal
        if (my_x != 8) { //right
            targetPosition = new ChessPosition(my_y + direction[0], my_x + 1);
            targetPiece = board.getPiece(targetPosition);
            if (targetPiece != null && targetPiece.getTeamColor() != piece.getTeamColor()) { //I can move up one space
                possibleMoves.add(new ChessMove(myPosition, targetPosition, null));
            }
        }
        if (my_x != 1) { //left
            targetPosition = new ChessPosition(my_y + direction[0],my_x - 1);
            targetPiece = board.getPiece(targetPosition);
            if (targetPiece != null && targetPiece.getTeamColor() != piece.getTeamColor()) { //I can move up one space
                possibleMoves.add(new ChessMove(myPosition, targetPosition, null));
            }
        }

        //If I can promote
        List<ChessMove> finalizedPossibleMoves = new ArrayList<>();
        for (ChessMove move : possibleMoves) {
            if (move.getEndPosition().getRow() == direction[3]) {
                ChessPosition startPosition = move.getStartPosition();
                ChessPosition endPosition = move.getEndPosition();
                finalizedPossibleMoves.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.QUEEN));
                finalizedPossibleMoves.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.BISHOP));
                finalizedPossibleMoves.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.KNIGHT));
                finalizedPossibleMoves.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.ROOK));
            } else {
                finalizedPossibleMoves.add(move);
            }
        }

        return finalizedPossibleMoves;
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
            int y = direction[0];
            int x = direction[1];

            while (isInBounds(my_y + y, my_x + x)) {
                ChessPosition targetPosition = new ChessPosition(my_y + y, my_x + x);
                ChessPiece targetPiece = board.getPiece(targetPosition);

                if (targetPiece == null) {
                    possibleMoves.add(new ChessMove(myPosition, targetPosition, null));
                    if (y != 0) y = y < 0 ? y - 1 : y + 1;
                    if (x != 0) x = x < 0 ? x - 1 : x + 1;
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
            int y = offset[0];
            int x = offset[1];

            if (isInBounds(my_y + y, my_x + x)) {
                ChessPosition targetPosition = new ChessPosition(my_y + y, my_x + x);
                ChessPiece targetPiece = board.getPiece(targetPosition);
                if ((targetPiece == null) || (targetPiece.getTeamColor() != piece.getTeamColor())) {
                    possibleMoves.add(new ChessMove(myPosition, targetPosition, null));
                }
            }
        }
        return possibleMoves;
    }
}