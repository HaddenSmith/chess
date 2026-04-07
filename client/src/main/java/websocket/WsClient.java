package websocket;

import jakarta.websocket.*;
import java.io.IOException;
import java.net.URI;

public class WsClient extends Endpoint {

    private final Session session;

    public WsClient(int port) throws Exception {
        URI uri = new URI("ws://localhost:" + port + "/ws");

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);

        this.session.addMessageHandler(String.class, message -> {
            System.out.println("Received: " + message);
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