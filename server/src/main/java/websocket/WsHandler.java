package websocket;

import com.google.gson.Gson;
import dataaccess.GameDAO;
import io.javalin.websocket.*;

import model.GameData;
import service.GameService;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

public class WsHandler {

    private final Gson gson = new Gson();

    private final GameService gameService;

    public WsHandler(GameService gameService) {
        this.gameService = gameService;
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
                    System.out.println("User connected to game " + command.getGameID());

                    GameData gameData = gameService.getGame(command.getGameID());

                    ServerMessage response = new ServerMessage(
                            ServerMessage.ServerMessageType.LOAD_GAME,
                            gameData.game()
                    );

                    ctx.send(gson.toJson(response));
                }
            }
        } catch (Exception e) {
            ctx.send("Error processing message");
        }
    }

    public void onClose(WsCloseContext ctx) {
        System.out.println("Closed");
    }
}