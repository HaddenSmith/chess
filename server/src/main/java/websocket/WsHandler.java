package websocket;

import com.google.gson.Gson;
import dataaccess.GameDAO;
import io.javalin.websocket.*;

import model.GameData;
import service.ClearService;
import service.GameService;
import service.UserService;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

                    String color = null;

                    if (username.equals(gameData.whiteUsername())) {
                        color = "white";
                    } else if (username.equals(gameData.blackUsername())) {
                        color = "black";
                    }

                    gameSessions.putIfAbsent(gameID, new HashMap<>());
                    gameSessions.get(gameID).put(username, ctx);

                    ServerMessage load = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData.game(), color);

                    ctx.send(GSON.toJson(load));

                    // Notify others
                    notifyOthers(String.format("%s joined the game as %s", username, color), gameID, username);
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

                    gameService.resignGame(gameID, username);

                    notifyOthers(String.format("%s has resigned. Game over.", username), gameID, username);

                    GameData gameData = gameService.getGame(gameID);

                    ServerMessage load = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData.game(), null);

                    Map<String, WsContext> sessions = gameSessions.get(gameID);
                    if (sessions != null) {
                        for (WsContext sessionCtx : sessions.values()) {
                            sessionCtx.send(GSON.toJson(load));
                        }
                    }
                }
            }
        } catch (Exception e) {
            ctx.send("Error processing message");
        }
    }

    public void onClose(WsCloseContext ctx) {
        System.out.println("Closed");

//        for (Set<WsContext> sessions : gameSessions.values()) {
//            sessions.remove(ctx);
//        }
    }

    private void notifyOthers(String message, int gameID, String username) {
        ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        for (Map.Entry<String, WsContext> entry : gameSessions.get(gameID).entrySet()) {
            if (!entry.getKey().equals(username)) {
                entry.getValue().send(GSON.toJson(notification));
            }
        }
    }
}