package websocket.commands;

import chess.ChessPosition;

import java.util.Objects;

/**
 * Represents a command a user can send the server over a websocket
 * <p>
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class UserGameCommand {

    private final CommandType commandType;
    private final String authToken;
    private final Integer gameID;
    private ChessPosition highlightPiecePosition;

    public UserGameCommand(CommandType commandType, String authToken, Integer gameID) {
        this.commandType = commandType;
        this.authToken = authToken;
        this.gameID = gameID;
    }

    public UserGameCommand(CommandType commandType, String authToken, Integer gameID, ChessPosition highlightPiecePosition) {
        this(commandType, authToken, gameID);
        this.highlightPiecePosition = highlightPiecePosition;
    }

    public enum CommandType {
        CONNECT,
        MAKE_MOVE,
        LEAVE,
        RESIGN,
        GET_LEGAL_MOVES
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public String getAuthToken() {
        return authToken;
    }

    public Integer getGameID() {
        return gameID;
    }

    public ChessPosition getHighlightPiecePosition() {
        return highlightPiecePosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserGameCommand that)) {
            return false;
        }
        return getCommandType() == that.getCommandType() &&
                Objects.equals(getAuthToken(), that.getAuthToken()) &&
                Objects.equals(getGameID(), that.getGameID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCommandType(), getAuthToken(), getGameID());
    }
}
