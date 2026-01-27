package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

public class PieceMovesCalculator {
    private final ChessBoard board;
    private final ChessPosition myPosition;
    private final ChessPiece myPiece;
    private final int my_y;
    private final int my_x;

    public PieceMovesCalculator(ChessBoard board, ChessPosition myPosition) {
        this.board = board;
        this.myPosition = myPosition;
        this.myPiece = board.getPiece(myPosition);
        this.my_y = myPosition.getRow();
        this.my_x = myPosition.getColumn();
    }

    public Collection<ChessMove> calculateMoves() {
        if (myPiece.getPieceType() == ChessPiece.PieceType.KING) return KingMovesCalculator();
        if (myPiece.getPieceType() == ChessPiece.PieceType.QUEEN) return QueenMovesCalculator();
        if (myPiece.getPieceType() == ChessPiece.PieceType.ROOK) return RookMovesCalculator();
        if (myPiece.getPieceType() == ChessPiece.PieceType.KNIGHT) return KnightMovesCalculator();
        if (myPiece.getPieceType() == ChessPiece.PieceType.BISHOP) return BishopMovesCalculator();
        if (myPiece.getPieceType() == ChessPiece.PieceType.PAWN) return PawnMovesCalculator();
        return null;
    }

    private boolean isInBounds (int y, int x) {
        return (y >= 1 && y <= 8) && (x >= 1 && x <= 8);
    }

    private Collection<ChessMove> KingMovesCalculator() {
        int [][] offsets = {{1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}};
        return generateStepMoves(offsets);
    }

    private Collection<ChessMove> QueenMovesCalculator() {
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, -1}, {-1, 1}, {1, -1}};
        return generateSlidingMoves(directions);
    }

    private Collection<ChessMove> RookMovesCalculator() {
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        return generateSlidingMoves(directions);
    }

    private Collection<ChessMove> KnightMovesCalculator() {
        int [][] offsets = {{1, 2}, {1, -2}, {-1, 2}, {-1, -2}, {2, 1}, {2, -1}, {-2, 1}, {-2, -1}};
        return generateStepMoves(offsets);
    }

    private Collection<ChessMove> BishopMovesCalculator() {
        int[][] directions = {{1, 1}, {-1, -1}, {-1, 1}, {1, -1}};
        return generateSlidingMoves(directions);
    }

    private Collection<ChessMove> PawnMovesCalculator() {
        List<ChessMove> possibleMoves = new ArrayList<>();
        int [] whitePawnMoves = {1, 2, 2, 8}; //1st num is move, 2nd num is double move, 3rd is starting row, 4th is ending row
        int [] blackPawnMoves = {-1, -2, 7, 1};
        int [] direction = myPiece.getTeamColor() == ChessGame.TeamColor.WHITE ? whitePawnMoves : blackPawnMoves;

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

        //Capture Diagonal
        if (my_x != 8) { //Capture diagonally right
            targetPosition = new ChessPosition(my_y + direction[0], my_x + 1);
            targetPiece = board.getPiece(targetPosition);
            if (targetPiece != null && targetPiece.getTeamColor() != myPiece.getTeamColor()) { //I can move up one space
                possibleMoves.add(new ChessMove(myPosition, targetPosition, null));
            }
        }
        if (my_x != 1) { //Capture diagonally left
            targetPosition = new ChessPosition(my_y + direction[0],my_x - 1);
            targetPiece = board.getPiece(targetPosition);
            if (targetPiece != null && targetPiece.getTeamColor() != myPiece.getTeamColor()) { //I can move up one space
                possibleMoves.add(new ChessMove(myPosition, targetPosition, null));
            }
        }

        //Promote
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
                    if (targetPiece.getTeamColor() != myPiece.getTeamColor()) {
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
                if ((targetPiece == null) || (targetPiece.getTeamColor() != myPiece.getTeamColor())) {
                    possibleMoves.add(new ChessMove(myPosition, targetPosition, null));
                }
            }
        }
        return possibleMoves;
    }
}