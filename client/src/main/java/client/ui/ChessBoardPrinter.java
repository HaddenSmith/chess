package client.ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;

import java.util.Arrays;
import java.util.List;

public class ChessBoardPrinter {
    private final ChessBoard board;
    boolean isWhiteView;

    public ChessBoardPrinter(ChessBoard board, String teamColor) {
        this.board = board;

        // If it's not black it's either a white player or just an observer that sees as if he was the white player
        this.isWhiteView = !teamColor.equalsIgnoreCase("black");
    }

    public String printBoard() {
        StringBuilder out = new StringBuilder();

        /*
        //ROW
        int rowStart = whiteView ? 8 : 1;
        int rowEnd = whiteView ? 1 : 8;
        int rowStep = whiteView ? -1 : 1;

        //COL
        int colStart = whiteView ? 1 : 8;
        int colEnd = whiteView ? 8 : 1;
        int colStep = whiteView ? 1 : -1;
         */

        out.append(buildColLabels());

        // choose row/col direction based on whiteView

        // loop rows
        // print row number
        // loop cols
        // get piece from chessBoard
        // convert piece to symbol
        // append square
        // print row number

        // print bottom column labels

        return out.toString();
    }

    private String buildColLabels() {
        List<String> colLabels = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h");
        StringBuilder out = new StringBuilder();

        out.append(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
        out.append(EscapeSequences.SET_TEXT_COLOR_BLACK);

        out.append(EscapeSequences.EMPTY);

        for(int col = 0; col < 8; col++) {
            String colLabel = colLabels.get(isWhiteView ? col : (7 - col));
            out.append(" ").append(colLabel).append(" ");
        }

        out.append(EscapeSequences.EMPTY);

        out.append(EscapeSequences.RESET_BG_COLOR);
        out.append(EscapeSequences.RESET_TEXT_COLOR);
        out.append("\n");

        return out.toString();
    }

    private String getPieceSymbol(ChessPiece piece) {
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            if (piece.getPieceType() == ChessPiece.PieceType.KING) { return EscapeSequences.WHITE_KING; }
            if (piece.getPieceType() == ChessPiece.PieceType.QUEEN) { return EscapeSequences.WHITE_QUEEN; }
            if (piece.getPieceType() == ChessPiece.PieceType.BISHOP) { return EscapeSequences.WHITE_BISHOP; }
            if (piece.getPieceType() == ChessPiece.PieceType.KNIGHT) { return EscapeSequences.WHITE_KNIGHT; }
            if (piece.getPieceType() == ChessPiece.PieceType.ROOK) { return EscapeSequences.WHITE_ROOK; }
            if (piece.getPieceType() == ChessPiece.PieceType.PAWN) { return EscapeSequences.WHITE_PAWN; }
        } else if (piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
            if (piece.getPieceType() == ChessPiece.PieceType.KING) { return EscapeSequences.BLACK_KING; }
            if (piece.getPieceType() == ChessPiece.PieceType.QUEEN) { return EscapeSequences.BLACK_QUEEN; }
            if (piece.getPieceType() == ChessPiece.PieceType.BISHOP) { return EscapeSequences.BLACK_BISHOP; }
            if (piece.getPieceType() == ChessPiece.PieceType.KNIGHT) { return EscapeSequences.BLACK_KNIGHT; }
            if (piece.getPieceType() == ChessPiece.PieceType.ROOK) { return EscapeSequences.BLACK_ROOK; }
            if (piece.getPieceType() == ChessPiece.PieceType.PAWN) { return EscapeSequences.BLACK_PAWN; }
        }
        return "";
    }

    private String getSquareColor(int row, int col) {
        if ((row + col) % 2 == 0) {
            return EscapeSequences.SET_BG_COLOR_WHITE;
        } else {
            return  EscapeSequences.SET_BG_COLOR_BLACK;
        }
    }
}
