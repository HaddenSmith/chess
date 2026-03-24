package client;

import client.ui.ServerFacade;
import org.junit.jupiter.api.*;
import result.CreateGameResult;
import result.ListGamesResult;
import result.UserResult;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearDatabase() throws Exception {
        // Use facade to clear so each test starts fresh
        facade.clear();
    }

    @Test
    void registerSuccess() throws Exception {
        UserResult result = facade.register("player1", "password", "p1@email.com");

        assertNotNull(result);
        assertEquals("player1", result.username());
        assertNotNull(result.authToken());
        assertTrue(result.authToken().length() > 10);
    }

    @Test
    void registerAlreadyTaken() throws Exception {
        facade.register("player1", "password", "p1@email.com");

        Exception ex = assertThrows(Exception.class, () ->
                facade.register("player1", "password", "p1@email.com"));

        assertTrue(ex.getMessage().contains("already taken"));
    }

    @Test
    void loginSuccess() throws Exception {
        facade.register("player1", "password", "p1@email.com");

        UserResult result = facade.login("player1", "password");

        assertNotNull(result);
        assertEquals("player1", result.username());
        assertNotNull(result.authToken());
        assertTrue(result.authToken().length() > 10);
    }

    @Test
    void loginUnauthorized() throws Exception {
        facade.register("player1", "password", "p1@email.com");

        Exception ex = assertThrows(Exception.class, () ->
                facade.login("player1", "wrongPassword"));

        assertTrue(ex.getMessage().contains("unauthorized"));
    }

    @Test
    void logoutSuccess() throws Exception {
        UserResult result = facade.register("player1", "password", "p1@email.com");

        assertDoesNotThrow(() -> facade.logout(result.authToken()));
    }

    @Test
    void logoutUnauthorized() {
        Exception ex = assertThrows(Exception.class, () ->
                facade.logout("fake-token"));

        assertTrue(ex.getMessage().contains("unauthorized"));
    }

    @Test
    void createGameSuccess() throws Exception {
        UserResult user = facade.register("player1", "password", "p1@email.com");

        CreateGameResult result = facade.createGame(user.authToken(), "My Game");

        assertNotNull(result);
        assertTrue(result.gameID() > 0);
    }

    @Test
    void createGameBadRequest() throws Exception {
        UserResult user = facade.register("player1", "password", "p1@email.com");

        Exception ex = assertThrows(Exception.class, () ->
                facade.createGame(user.authToken(), ""));

        assertTrue(ex.getMessage().contains("bad request"));
    }

    @Test
    void listGamesSuccess() throws Exception {
        UserResult user = facade.register("player1", "password", "p1@email.com");
        facade.createGame(user.authToken(), "Game One");

        ListGamesResult result = facade.listGames(user.authToken());

        assertNotNull(result);
        assertNotNull(result.games());
        assertEquals(1, result.games().size());
    }

    @Test
    void listGamesUnauthorized() {
        Exception ex = assertThrows(Exception.class, () ->
                facade.listGames("bad-token"));

        assertTrue(ex.getMessage().contains("unauthorized"));
    }

    @Test
    void joinGameSuccess() throws Exception {
        UserResult user = facade.register("player1", "password", "p1@email.com");
        CreateGameResult game = facade.createGame(user.authToken(), "Game One");

        assertDoesNotThrow(() ->
                facade.joinGame(user.authToken(), "WHITE", game.gameID()));
    }

    @Test
    void joinGameAlreadyTaken() throws Exception {
        UserResult user1 = facade.register("player1", "password", "p1@email.com");
        UserResult user2 = facade.register("player2", "password", "p2@email.com");
        CreateGameResult game = facade.createGame(user1.authToken(), "Game One");

        facade.joinGame(user1.authToken(), "WHITE", game.gameID());

        Exception ex = assertThrows(Exception.class, () ->
                facade.joinGame(user2.authToken(), "WHITE", game.gameID()));

        assertTrue(ex.getMessage().contains("already taken"));
    }
}