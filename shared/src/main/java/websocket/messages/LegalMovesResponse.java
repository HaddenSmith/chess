package websocket.messages;

import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;

public record LegalMovesResponse(ChessPosition position, Collection<ChessMove> moves) {
}
