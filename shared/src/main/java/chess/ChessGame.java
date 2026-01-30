package chess;

import java.util.Collection;
import java.util.List;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor currentTeamTurn = TeamColor.WHITE; //White starts
    private ChessBoard board = new ChessBoard();

    public ChessGame() {

    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTeamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTeamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) return null; //no piece there
        if (piece.getTeamColor() != currentTeamTurn) return List.of(); //not my turn, so return empty list
        //if your in check, the only valid moves is the moves that get you out of check (move king, capture piece that is threatening you, block the check)

        throw new RuntimeException("Not implemented");
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented"); //if move is contained in the list i get from valid moves, then move, else throw exeption
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        for (int row = 8; row >= 1; row--) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition targetPosition = new ChessPosition(row, col);
                ChessPiece targetPiece = board.getPiece(targetPosition);
                if (targetPiece != null && targetPiece.getTeamColor() == teamColor && targetPiece.getPieceType() == ChessPiece.PieceType.KING) {
                    PieceMovesCalculator calculator = new PieceMovesCalculator(board, targetPosition);
                    return calculator.calculateIsInCheck();
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented"); //if your turn, and no valid moves, and in check, then checkmate
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        board.resetBoard();
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public String toString() {
        StringBuilder boardString = new StringBuilder("   1 2 3 4 5 6 7 8\n");
        for (int row = 8; row >= 1; row--) {
            boardString.append(row).append("|");
            for (int col = 1; col <= 8; col++) {
                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                boardString.append(piece == null ? " " : getPieceChar(piece));
                boardString.append("|");
            }
            boardString.append("\n");
        }
        boardString.setLength(boardString.length() - 1); //get rid of extra "|"
        return boardString.toString();
    }

    private char getPieceChar(ChessPiece piece) {
        char pieceChar = '?';
        if (piece.getPieceType() == ChessPiece.PieceType.KING) pieceChar = 'k';
        else if (piece.getPieceType() == ChessPiece.PieceType.QUEEN) pieceChar = 'q';
        else if (piece.getPieceType() == ChessPiece.PieceType.ROOK) pieceChar = 'r';
        else if (piece.getPieceType() == ChessPiece.PieceType.KNIGHT) pieceChar = 'n';
        else if (piece.getPieceType() == ChessPiece.PieceType.BISHOP) pieceChar = 'b';
        else if (piece.getPieceType() == ChessPiece.PieceType.PAWN) pieceChar = 'p';
        return piece.getTeamColor() == TeamColor.WHITE ? Character.toUpperCase(pieceChar) : pieceChar;
    }
}
