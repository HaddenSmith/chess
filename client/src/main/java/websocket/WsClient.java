package websocket;

import chess.ChessGame;
import chess.ChessMove;
import client.ui.ChessBoardPrinter;
import com.google.gson.Gson;
import jakarta.websocket.*;
import websocket.messages.LegalMovesResponse;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

public class WsClient extends Endpoint {

    private final Session session;
    public static ChessGame latestGame;
    public static String playerColor;

    public WsClient(int port) throws Exception {
        URI uri = new URI("ws://localhost:" + port + "/ws");

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);

        this.session.addMessageHandler(String.class, message -> {
            //System.out.println("Raw: " + message);

            ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);

            if (serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME) {
                latestGame = serverMessage.getGame();
                if (serverMessage.getColor() != null) { playerColor = serverMessage.getColor(); }

                ChessBoardPrinter printer = new ChessBoardPrinter(latestGame.getBoard(), playerColor);
                System.out.println(printer.buildBoardString());
            }

            if (serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.NOTIFICATION) {
                System.out.println("Notification: " + serverMessage.getMessage());
            }

            if (serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.LEGAL_MOVES) {
                Gson gson = new Gson();
                LegalMovesResponse response = gson.fromJson(gson.toJson(serverMessage.getData()), LegalMovesResponse.class);

                ChessBoardPrinter printer = new ChessBoardPrinter(latestGame.getBoard(), playerColor, response.moves(), response.position());

                System.out.println(printer.buildBoardString());
            }

            if (serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.ERROR) {
                System.out.println("Error: " + serverMessage.getMessage());
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