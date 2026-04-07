package websocket;

import com.google.gson.Gson;
import io.javalin.websocket.*;

import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

public class WsHandler {

    private final Gson gson = new Gson();

    public void onConnect(WsConnectContext ctx) {
        ctx.enableAutomaticPings();
        System.out.println("Connected");
    }

    public void onMessage(WsMessageContext ctx) {
        System.out.println("Raw message: " + ctx.message());

        try {
            UserGameCommand command = gson.fromJson(ctx.message(), UserGameCommand.class);
            System.out.println("Parsed command: " + command.getCommandType());

            ServerMessage response = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);

            ctx.send(gson.toJson(response));
        } catch (Exception e) {
            ctx.send("Error processing message");
        }
    }

    public void onClose(WsCloseContext ctx) {
        System.out.println("Closed");
    }
}