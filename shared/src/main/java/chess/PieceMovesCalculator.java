package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

public class PieceMovesCalculator {
    private final ChessBoard board;
    private final ChessPosition myPosition;
    private final ChessPiece myPiece;
    private final int myRow;
    private final int myCol;

    public PieceMovesCalculator(ChessBoard board, ChessPosition myPosition) {
        this.board = board;
        this.myPosition = myPosition;
        this.myPiece = board.getPiece(myPosition);
        this.myRow = myPosition.getRow();
        this.myCol = myPosition.getColumn();
    }

    public Collection<ChessMove> calculateMoves() {
        if (myPiece.getPieceType() == ChessPiece.PieceType.KING) { return calculateKingMoves(); }
        if (myPiece.getPieceType() == ChessPiece.PieceType.QUEEN) { return calculateQueenMoves(); }
        if (myPiece.getPieceType() == ChessPiece.PieceType.ROOK) { return calculateRookMoves(); }
        if (myPiece.getPieceType() == ChessPiece.PieceType.KNIGHT) { return calculateKnightMoves(); }
        if (myPiece.getPieceType() == ChessPiece.PieceType.BISHOP) { return calculateBishopMoves(); }
        if (myPiece.getPieceType() == ChessPiece.PieceType.PAWN) { return calculatePawnMoves(); }
        return List.of(); //returns empty list
    }

    private boolean isInBounds (int y, int x) {
        return (y >= 1 && y <= 8) && (x >= 1 && x <= 8);
    }

    private Collection<ChessMove> calculateKingMoves() {
        int [][] offsets = {{1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}};
        return generateStepMoves(offsets);
    }

    private Collection<ChessMove> calculateQueenMoves() {
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, -1}, {-1, 1}, {1, -1}};
        return generateSlidingMoves(directions);
    }

    private Collection<ChessMove> calculateRookMoves() {
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        return generateSlidingMoves(directions);
    }

    private Collection<ChessMove> calculateKnightMoves() {
        int [][] offsets = {{1, 2}, {1, -2}, {-1, 2}, {-1, -2}, {2, 1}, {2, -1}, {-2, 1}, {-2, -1}};
        return generateStepMoves(offsets);
    }

    private Collection<ChessMove> calculateBishopMoves() {
        int[][] directions = {{1, 1}, {-1, -1}, {-1, 1}, {1, -1}};
        return generateSlidingMoves(directions);
    }

    private Collection<ChessMove> calculatePawnMoves() {
        List<ChessMove> possibleMoves = new ArrayList<>();
        boolean isWhite = myPiece.getTeamColor() == ChessGame.TeamColor.WHITE;
        int forward = isWhite ? 1 : -1;
        int doubleForward = isWhite ? 2 : -2;
        int startRow = isWhite ? 2 : 7;
        int promotionRow = isWhite ? 8 : 1;

        //Move Forward
        ChessPosition targetPosition = new ChessPosition(myRow + forward, myCol);
        ChessPiece targetPiece = board.getPiece(targetPosition);
        if (targetPiece == null) {
            possibleMoves.add(new ChessMove(myPosition, targetPosition, null));
        }

        //Move Forward Twice
        if (myRow == startRow && targetPiece == null) {
            targetPosition = new ChessPosition(myRow + doubleForward, myCol);
            targetPiece = board.getPiece(targetPosition);
            if (targetPiece == null) {
                possibleMoves.add(new ChessMove(myPosition, targetPosition, null));
            }
        }

        //Capture Diagonal
        if (myCol != 8) { //Capture diagonally right
            targetPosition = new ChessPosition(myRow + forward, myCol + 1);
            targetPiece = board.getPiece(targetPosition);
            if (targetPiece != null && targetPiece.getTeamColor() != myPiece.getTeamColor()) { //I can move up one space
                possibleMoves.add(new ChessMove(myPosition, targetPosition, null));
            }
        }
        if (myCol != 1) { //Capture diagonally left
            targetPosition = new ChessPosition(myRow + forward,myCol - 1);
            targetPiece = board.getPiece(targetPosition);
            if (targetPiece != null && targetPiece.getTeamColor() != myPiece.getTeamColor()) { //I can move up one space
                possibleMoves.add(new ChessMove(myPosition, targetPosition, null));
            }
        }

        //Promote
        List<ChessMove> finalizedPossibleMoves = new ArrayList<>();
        for (ChessMove move : possibleMoves) {
            if (move.getEndPosition().getRow() == promotionRow) {
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

            while (isInBounds(myRow + y, myCol + x)) {
                ChessPosition targetPosition = new ChessPosition(myRow + y, myCol + x);
                ChessPiece targetPiece = board.getPiece(targetPosition);

                if (targetPiece == null) {
                    possibleMoves.add(new ChessMove(myPosition, targetPosition, null));
                    if (y != 0) { y = y < 0 ? y - 1 : y + 1; }
                    if (x != 0) { x = x < 0 ? x - 1 : x + 1; }
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

            if (isInBounds(myRow + y, myCol + x)) {
                ChessPosition targetPosition = new ChessPosition(myRow + y, myCol + x);
                ChessPiece targetPiece = board.getPiece(targetPosition);
                if ((targetPiece == null) || (targetPiece.getTeamColor() != myPiece.getTeamColor())) {
                    possibleMoves.add(new ChessMove(myPosition, targetPosition, null));
                }
            }
        }
        return possibleMoves;
    }

    public boolean calculateIsInCheck() {
        Collection<ChessMove> vision = calculateQueenMoves();
        vision.addAll(calculateKnightMoves());

        for (ChessMove move : vision) {
            ChessPiece targetPiece = board.getPiece(move.getEndPosition());
            if (targetPiece != null && targetPiece.getTeamColor() != myPiece.getTeamColor()) {
                 PieceMovesCalculator calculator = new PieceMovesCalculator(board, move.getEndPosition());
                 for (ChessMove possibleMove : calculator.calculateMoves()) {
                     if (possibleMove.getEndPosition().equals(myPosition)) { return true; }
                 }
            }
        }
        return false;
    }
}