package websocket;

import chess.ChessGame;
import client.ui.ChessBoardPrinter;
import com.google.gson.Gson;
import jakarta.websocket.*;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.net.URI;

public class WsClient extends Endpoint {

    private final Session session;

    public WsClient(int port) throws Exception {
        URI uri = new URI("ws://localhost:" + port + "/ws");

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);

        this.session.addMessageHandler(String.class, message -> {
            //System.out.println("Raw: " + message);

            ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);

            if (serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME) {
                ChessGame game = serverMessage.getGame();
                String color = serverMessage.getColor();

                ChessBoardPrinter printer = new ChessBoardPrinter(game.getBoard(), color);

                System.out.println(printer.buildBoardString());
            }
            if (serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.NOTIFICATION) {
                System.out.println("Notification: " + serverMessage.getMessage());
            }
        });
    }

    public void send(String msg) throws IOException {
        session.getBasicRemote().sendText(msg);
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        // You can leave this empty for now
    }
}