package websocket;

import com.google.gson.Gson;
import dataaccess.GameDAO;
import io.javalin.websocket.*;

import model.GameData;
import service.GameService;
import service.UserService;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WsHandler {

    private final Gson gson = new Gson();
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
            UserGameCommand command = gson.fromJson(ctx.message(), UserGameCommand.class);
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

                    ctx.send(gson.toJson(load));

                    // Notify others
                    for (Map.Entry<String, WsContext> entry : gameSessions.get(gameID).entrySet()) {
                        if (!entry.getKey().equals(username)) {
                            String message = username + " joined the game as " + color;
                            ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
                            entry.getValue().send(gson.toJson(notification));
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
}