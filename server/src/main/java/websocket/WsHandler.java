package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import com.google.gson.Gson;
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
                    sendNotification(String.format("%s joined the game as %s", username, role), gameID, username, false);
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
                    sendNotification(String.format("%s has left the game.", username), gameID, username, false);
                }
                case RESIGN -> {
                    String username = userService.getUsername(command.getAuthToken());
                    int gameID = command.getGameID();
                    GameData gameData = gameService.getGame(gameID);

                    String color = getColor(username, gameData);

                    if (gameData.game().isGameOver()) {
                        ctx.send(GSON.toJson(new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Game is already over")));
                        return;
                    }

                    if (color == null) {
                        ctx.send(GSON.toJson(new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Observers cannot resign")));
                        return;
                    }

                    gameService.resignGame(gameID, username);

                    sendNotification(String.format("%s has resigned. Game over.", username), gameID, username, true);
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

                    ChessPosition start = command.getMove().startPosition();
                    ChessPosition end = command.getMove().endPosition();

                    if (start == null || end == null) {
                        ctx.send(GSON.toJson(new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Invalid move input")));
                        return;
                    }

                    GameData gameData = gameService.getGame(gameID);
                    ChessGame game = gameData.game();

                    if (game.isInCheckmate(ChessGame.TeamColor.WHITE) || game.isInCheckmate(ChessGame.TeamColor.BLACK) ||
                            game.isInStalemate(ChessGame.TeamColor.WHITE) || game.isInStalemate(ChessGame.TeamColor.BLACK) ||
                                game.isGameOver()) {
                        ctx.send(GSON.toJson(new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Game is already over")));
                        return;
                    }

                    ChessGame.TeamColor turn = game.getTeamTurn();
                    ChessGame.TeamColor opponent = turn == ChessGame.TeamColor.WHITE ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;

                    String playerColorStr = getColor(username, gameData);

                    if (playerColorStr == null) {
                        ctx.send(GSON.toJson(new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Observers cannot make moves")));
                        return;
                    }

                    ChessGame.TeamColor playerColor = playerColorStr.equals("white") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;

                    if (game.getTeamTurn() != playerColor) {
                        ctx.send(GSON.toJson(new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Not your turn")));
                        return;
                    }

                    var piece = game.getBoard().getPiece(start);

                    if (piece == null || piece.getTeamColor() != playerColor) {
                        ctx.send(GSON.toJson(new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Invalid move: not your piece")));
                        return;
                    }

                    try {
                        ChessMove move = new ChessMove(start, end, null);

                        game.makeMove(move);

                        if (game.isInCheckmate(opponent)) {
                            sendNotification("Checkmate! " + opponent + " loses.", gameID, null, true);
                        } else if (game.isInStalemate(opponent)) {
                            sendNotification("Stalemate! Game is a draw.", gameID, null, true);
                        } else if (game.isInCheck(opponent)) {
                            sendNotification(opponent + " is in check!", gameID, null, true);
                        }

                        gameService.updateGame(gameID, game);

                        // Send updated board to EVERYONE (including sender)
                        ServerMessage load = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, game, null);

                        Map<String, WsContext> sessions = gameSessions.get(gameID);
                        if (sessions != null) {
                            for (WsContext sessionCtx : sessions.values()) {
                                sessionCtx.send(GSON.toJson(load));
                            }
                        }

                        String message = String.format("%s moved from %s to %s", username, toChessNotation(start), toChessNotation(end));
                        sendNotification(message, gameID, username, false);
                    } catch (InvalidMoveException e) {
                        ctx.send(GSON.toJson(new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Invalid move")));
                    }
                }
            }
        } catch (Exception e) {
            ctx.send(GSON.toJson(new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Error processing message")));
        }
    }

    public void onClose(WsCloseContext ctx) {
        System.out.println("Closed");

        try {
            Integer foundGameID = null;
            String foundUsername = null;

            for (Map.Entry<Integer, Map<String, WsContext>> gameEntry : gameSessions.entrySet()) {
                Integer gameID = gameEntry.getKey();
                Map<String, WsContext> session = gameEntry.getValue();

                for (Map.Entry<String, WsContext> sessionEntry : session.entrySet()) {
                    if (sessionEntry.getValue().equals(ctx)) {
                        foundGameID = gameID;
                        foundUsername = sessionEntry.getKey();
                        break;
                    }
                }

                if (foundGameID != null) break;
            }

            if (foundGameID == null || foundUsername == null) {
                System.out.println("No session found for closing context.");
                return;
            }

            Map<String, WsContext> session = gameSessions.get(foundGameID);

            if (session != null) {
                session.remove(foundUsername);

                if (session.isEmpty()) {
                    gameSessions.remove(foundGameID);
                }
            }

            try {
                gameService.leaveGame(foundGameID, foundUsername);
            } catch (Exception e) {
                System.out.println("Failed to remove user from game: " + e.getMessage());
            }

            if (session != null) {
                sendNotification(foundUsername + " disconnected.", foundGameID, foundUsername, false);
            }

        } catch (Exception e) {
            System.out.println("onClose error: " + e.getMessage());
        }
    }

    private void sendNotification(String message, int gameID, String username, boolean notifyAll) {
        ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        for (Map.Entry<String, WsContext> entry : gameSessions.get(gameID).entrySet()) {
            if (notifyAll || !entry.getKey().equals(username)) {
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