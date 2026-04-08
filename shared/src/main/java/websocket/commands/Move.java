package websocket.commands;

import chess.ChessPiece;
import chess.ChessPosition;

public record Move (ChessPosition startPosition, ChessPosition endPosition, ChessPiece.PieceType promotionType) {
}