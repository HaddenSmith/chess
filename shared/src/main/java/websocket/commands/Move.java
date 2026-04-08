package websocket.commands;

import chess.ChessPosition;

public record Move (ChessPosition startPosition, ChessPosition endPosition) {
}