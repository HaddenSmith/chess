package websocket.messages;

import chess.ChessGame;
import chess.ChessMove;

import java.util.Collection;
import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 * <p>
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ServerMessage {
    ServerMessageType serverMessageType;
    private ChessGame game;
    private String message;
    private String errorMessage;
    private String color;
    private Object data;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION,
        LEGAL_MOVES
    }

    public ServerMessage(ServerMessageType type, ChessGame game, String color) {
        this.serverMessageType = type;
        this.game = game;
        this.color = color;
    }

    public ServerMessage(ServerMessageType type, String message) {
        this.serverMessageType = type;
        if(type == ServerMessageType.ERROR) {
            this.errorMessage = message;
        } else {
            this.message = message;
        }
    }

    public ServerMessage(ServerMessageType type, Object data) {
        this.serverMessageType = type;
        this.data = data;
    }

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    public ChessGame getGame() {
        return game;
    }

    public String getMessage() {
        return message;
    }

    public String getColor() {
        return color;
    }

    public Object getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerMessage that)) {
            return false;
        }
        return getServerMessageType() == that.getServerMessageType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType());
    }
}
