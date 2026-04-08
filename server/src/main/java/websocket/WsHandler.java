package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.websocket.*;

import model.GameData;
import service.GameService;
import service.UserService;
import websocket.commands.UserGameCommand;
import websocket.messages.LegalMovesResponse;
import websocket.messages.ServerMessage;

import java.util.*;

public class WsHandler {

    private final Gson GSON = new Gson();
    private final GameService gameService;
    private final UserService userService;
    private final Map<Integer, Map<String, WsContext>> gameSessions = new HashMap<>();

    public WsHandler(GameService gameService, UserService userService) {
        this.gameService = gameService;
        this.userService = userService;
    }

    public void onConnect(WsConnectContext ctx) {
        ctx.enableAutomaticPings();
        System.out.println("Connected");
    }

    public void onMessage(WsMessageContext ctx) {
        System.out.println("Raw message: " + ctx.message());

        try {
            UserGameCommand command = GSON.fromJson(ctx.message(), UserGameCommand.class);
            System.out.println("Parsed command: " + command.getCommandType());

            switch (command.getCommandType()) {
                case CONNECT -> {
                    int gameID = command.getGameID();
                    String username = userService.getUsername(command.getAuthToken());
                    GameData gameData = gameService.getGame(gameID);

                   String color = getColor(username, gameData);

                    gameSessions.putIfAbsent(gameID, new HashMap<>());
                    gameSessions.get(gameID).put(username, ctx);

                    ServerMessage load = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData.game(), color);

                    ctx.send(GSON.toJson(load));

                    // Notify others
                    String role = (color == null) ? "observer" : color;
                    notifyOthers(String.format("%s joined the game as %s", username, role), gameID, username);
                }
                case LEAVE -> {
                    String username = userService.getUsername(command.getAuthToken());
                    int gameID = command.getGameID();

                    gameService.leaveGame(gameID, username);

                    // Making sure that somebody has joined the session that is being called on
                    Map<String, WsContext> session = gameSessions.get(gameID);
                    if (session != null) { session.remove(username); }

                    //If it is now empty, remove it from our map
                    if (session.isEmpty()) { gameSessions.remove(gameID); }

                    // Notify others
                    notifyOthers(String.format("%s has left the game.", username), gameID, username);
                }
                case RESIGN -> {
                    String username = userService.getUsername(command.getAuthToken());
                    int gameID = command.getGameID();
                    GameData gameData = gameService.getGame(gameID);

                    gameService.resignGame(gameID, username);

                    notifyOthers(String.format("%s has resigned. Game over.", username), gameID, username);

                    ServerMessage load = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData.game(), null);

                    Map<String, WsContext> sessions = gameSessions.get(gameID);
                    if (sessions != null) {
                        for (WsContext sessionCtx : sessions.values()) {
                            sessionCtx.send(GSON.toJson(load));
                        }
                    }
                }
                case GET_LEGAL_MOVES -> {
                    ChessPosition position = command.getHighlightPiecePosition();
                    if (position == null) {
                        System.out.println("Position is null!");
                        return;
                    }
                    GameData gameData = gameService.getGame(command.getGameID());

                    Collection<ChessMove> moves = gameData.game().validMoves(position);

                    ServerMessage response = new ServerMessage(ServerMessage.ServerMessageType.LEGAL_MOVES, new LegalMovesResponse(position, moves));

                    ctx.send(GSON.toJson(response));
                }
                case MAKE_MOVE -> {
                    String username = userService.getUsername(command.getAuthToken());
                    int gameID = command.getGameID();

                    ChessPosition start = command.getStartPosition();
                    ChessPosition end = command.getEndPosition();

                    if (start == null || end == null) {
                        ctx.send(GSON.toJson(new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Invalid move input")));
                        return;
                    }

                    GameData gameData = gameService.getGame(gameID);
                    ChessGame game = gameData.game();

                    if (game.isInCheckmate(ChessGame.TeamColor.WHITE) || game.isInCheckmate(ChessGame.TeamColor.BLACK) ||
                            game.isInStalemate(ChessGame.TeamColor.WHITE) || game.isInStalemate(ChessGame.TeamColor.BLACK)) {
                        ctx.send(GSON.toJson(new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Game is already over")));
                        return;
                    }

                    ChessGame.TeamColor turn = game.getTeamTurn();
                    ChessGame.TeamColor opponent = turn == ChessGame.TeamColor.WHITE ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;

                    try {
                        ChessMove move = new ChessMove(start, end, null);

                        game.makeMove(move);

                        if (game.isInCheckmate(opponent)) {
                            notifyOthers("Checkmate! " + opponent + " loses.", gameID, null);
                        }
                        else if (game.isInStalemate(opponent)) {
                            notifyOthers("Stalemate! Game is a draw.", gameID, null);
                        }
                        else if (game.isInCheck(opponent)) {
                            notifyOthers(opponent + " is in check!", gameID, null);
                        }

                        gameService.updateGame(gameID, game);

                        String message = String.format("%s moved from %s to %s", username, toChessNotation(start), toChessNotation(end));
                        notifyOthers(message, gameID, username);

                        // Send updated board to EVERYONE (including sender)
                        ServerMessage load = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, game, null);

                        Map<String, WsContext> sessions = gameSessions.get(gameID);
                        if (sessions != null) {
                            for (WsContext sessionCtx : sessions.values()) {
                                sessionCtx.send(GSON.toJson(load));
                            }
                        }

                    } catch (Exception e) {
                        ctx.send(GSON.toJson(
                                new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Invalid move")
                        ));
                    }
                }
            }
        } catch (Exception e) {
            ctx.send("Error processing message");
        }
    }

    public void onClose(WsCloseContext ctx) throws DataAccessException {
        System.out.println("Closed");

        for (Map.Entry<Integer, Map<String, WsContext>> gameEntry : gameSessions.entrySet()) {
            Integer gameID = gameEntry.getKey();
            Map<String, WsContext> sessions = gameEntry.getValue();

            String removedUser = null;

            for (Map.Entry<String, WsContext> entry : sessions.entrySet()) {
                if (entry.getValue().equals(ctx)) {
                    removedUser = entry.getKey();
                    break;
                }
            }

            if (removedUser != null) {
                sessions.remove(removedUser);
                gameService.leaveGame(gameID, removedUser);

                notifyOthers(String.format("%s disconnected.", removedUser), gameID, removedUser);
                if (sessions.isEmpty()) { gameSessions.remove(gameID); }

                break;
            }
        }
    }

    private void notifyOthers(String message, int gameID, String username) {
        ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        for (Map.Entry<String, WsContext> entry : gameSessions.get(gameID).entrySet()) {
            if (!entry.getKey().equals(username)) {
                entry.getValue().send(GSON.toJson(notification));
            }
        }
    }

    public String getColor(String username, GameData gameData) {
        String color = null;

        if (username.equals(gameData.whiteUsername())) {
            color = "white";
        } else if (username.equals(gameData.blackUsername())) {
            color = "black";
        }
        return color;
    }

    private String toChessNotation(ChessPosition pos) {
        char col = (char) ('a' + pos.getColumn() - 1);
        int row = pos.getRow();
        return "" + col + row;
    }
}