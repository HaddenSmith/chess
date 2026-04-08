package client.ui;

import chess.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ChessBoardPrinter {
    private final ChessBoard board;
    private final boolean isWhiteView;
    ChessPosition highlightedPiece = null;
    ArrayList<ChessMove> possibleMoves = null;

    public ChessBoardPrinter(ChessBoard board, String teamColor) {
        this.board = board;

        // If it's not black it's either a white player or just an observer that sees as if he was the white player
        this.isWhiteView = (teamColor == null) || !teamColor.equalsIgnoreCase("black");
    }

    public ChessBoardPrinter(ChessBoard board, String teamColor, Collection<ChessMove> possibleMoves, ChessPosition highlightedPiece) {
        this(board, teamColor);
        this.possibleMoves = new ArrayList<>(possibleMoves);
        this.highlightedPiece = highlightedPiece;
    }

    public String buildBoardString() {
        StringBuilder boardString = new StringBuilder();

        int rowStart = isWhiteView ? 8 : 1;
        int rowEnd = isWhiteView ? 1 : 8;
        int rowStep = isWhiteView ? -1 : 1;

        int colStart = isWhiteView ? 1 : 8;
        int colEnd = isWhiteView ? 8 : 1;
        int colStep = isWhiteView ? 1 : -1;

        boardString.append(buildColLabels()); // Build top column layer

        for(int row = rowStart; row != rowEnd + rowStep; row += rowStep) {
            boardString.append(buildRowLabel(row));

            for(int col = colStart; col != colEnd + colStep; col += colStep) {
                boardString.append(getSquareColor(row, col));

                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                if(piece != null) { // If there is a piece in that square
                    boardString.append(buildPieceSymbol(piece));
                } else {
                    boardString.append(EscapeSequences.EMPTY);
                }

                boardString.append(EscapeSequences.RESET_BG_COLOR);
            }

            boardString.append(buildRowLabel(row)).append("\n");
        }

        boardString.append(buildColLabels()); // Build bottom column layer

        return boardString.toString();
    }

    private String buildColLabels() {
        List<String> colLabels = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h");
        StringBuilder out = new StringBuilder();

        out.append(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
        out.append(EscapeSequences.SET_TEXT_COLOR_BLACK);

        out.append(EscapeSequences.EMPTY);

        for(int col = 0; col < 8; col++) {
            String colLabel = colLabels.get(isWhiteView ? col : (7 - col));
            out.append("\u2003").append(colLabel).append(" ");
        }

        out.append(EscapeSequences.EMPTY);

        out.append(EscapeSequences.RESET_BG_COLOR);
        out.append(EscapeSequences.RESET_TEXT_COLOR);
        out.append("\n");

        return out.toString();
    }

    private String buildRowLabel(int num) {
        return EscapeSequences.SET_BG_COLOR_LIGHT_GREY +
                EscapeSequences.SET_TEXT_COLOR_BLACK +
                "\u2003" + num + " " +
                EscapeSequences.RESET_BG_COLOR +
                EscapeSequences.RESET_TEXT_COLOR;
    }

    private String buildPieceSymbol(ChessPiece piece) {
        StringBuilder out = new StringBuilder();

        if(piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            out.append(EscapeSequences.SET_TEXT_COLOR_RED);
            if (piece.getPieceType() == ChessPiece.PieceType.KING) { out.append(EscapeSequences.WHITE_KING); }
            else if (piece.getPieceType() == ChessPiece.PieceType.QUEEN) { out.append(EscapeSequences.WHITE_QUEEN); }
            else if (piece.getPieceType() == ChessPiece.PieceType.BISHOP) { out.append(EscapeSequences.WHITE_BISHOP); }
            else if (piece.getPieceType() == ChessPiece.PieceType.KNIGHT) { out.append(EscapeSequences.WHITE_KNIGHT); }
            else if (piece.getPieceType() == ChessPiece.PieceType.ROOK) { out.append(EscapeSequences.WHITE_ROOK); }
            else if (piece.getPieceType() == ChessPiece.PieceType.PAWN) { out.append(EscapeSequences.WHITE_PAWN); }
            out.append(EscapeSequences.RESET_TEXT_COLOR);
        } else if(piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
            out.append(EscapeSequences.SET_TEXT_COLOR_BLUE);
            if (piece.getPieceType() == ChessPiece.PieceType.KING) { out.append(EscapeSequences.BLACK_KING); }
            else if (piece.getPieceType() == ChessPiece.PieceType.QUEEN) { out.append(EscapeSequences.BLACK_QUEEN); }
            else if (piece.getPieceType() == ChessPiece.PieceType.BISHOP) { out.append(EscapeSequences.BLACK_BISHOP); }
            else if (piece.getPieceType() == ChessPiece.PieceType.KNIGHT) { out.append(EscapeSequences.BLACK_KNIGHT); }
            else if (piece.getPieceType() == ChessPiece.PieceType.ROOK) { out.append(EscapeSequences.BLACK_ROOK); }
            else if (piece.getPieceType() == ChessPiece.PieceType.PAWN) { out.append(EscapeSequences.BLACK_PAWN); }
            out.append(EscapeSequences.RESET_TEXT_COLOR);
        }

        return out.toString();
    }

    private String getSquareColor(int row, int col) {
        if (highlightedPiece != null) {
            if (highlightedPiece.getRow() == row && highlightedPiece.getColumn() == col) {
                return EscapeSequences.SET_BG_COLOR_YELLOW;
            }

            if (isPossibleMove(row, col)) {
                return getMoveHighlightColor(row, col);
            }
        }

        return getDefaultSquareColor(row, col);
    }

    private boolean isPossibleMove(int row, int col) {
        if (possibleMoves == null) { return false; }

        for (ChessMove move : possibleMoves) {
            ChessPosition pos = move.getEndPosition();
            if (pos.getRow() == row && pos.getColumn() == col) {
                return true;
            }
        }

        return false;
    }

    private String getMoveHighlightColor(int row, int col) {
        if ((row + col) % 2 == 0) {
            return EscapeSequences.SET_BG_COLOR_DARK_GREEN;
        } else {
            return EscapeSequences.SET_BG_COLOR_GREEN;
        }
    }

    private String getDefaultSquareColor(int row, int col) {
        if ((row + col) % 2 == 0) {
            return EscapeSequences.SET_BG_COLOR_BLACK;
        } else {
            return EscapeSequences.SET_BG_COLOR_WHITE;
        }
    }
}
