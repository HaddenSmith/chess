package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {

    private UserService userService;
    private GameService gameService;
    private UserDAO userDAO;
    private AuthDAO authDAO;
    private GameDAO gameDAO;

    @BeforeEach
    void setUp() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();

        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);
    }

    private String registerAndGetToken(String username) throws Exception {
        AuthData auth = userService.register(username, "pw", username + "@email.com");
        return auth.authToken();
    }

    @Test
    void createGameSuccess() throws Exception {
        String token = registerAndGetToken("bob");

        GameData game = gameService.createGame(token, "my game");

        assertNotNull(game);
        assertTrue(game.gameID() > 0);
        assertEquals("my game", game.gameName());
        assertNotNull(gameDAO.getGame(game.gameID()));
    }

    @Test
    void createGameBadRequestEmptyGameName() throws Exception {
        String token = registerAndGetToken("bob");

        DataAccessException ex = assertThrows(DataAccessException.class, () ->
                gameService.createGame(token, "")
        );
        assertEquals("Error: bad request", ex.getMessage());
    }

    @Test
    void listGamesSuccess() throws Exception {
        String token = registerAndGetToken("bob");

        gameService.createGame(token, "g1");
        gameService.createGame(token, "g2");

        Collection<GameData> games = gameService.listGames(token);

        assertNotNull(games);
        assertEquals(2, games.size());
    }

    @Test
    void listGamesUnauthorizedBadToken() {
        DataAccessException ex = assertThrows(DataAccessException.class, () ->
                gameService.listGames("not-a-real-token")
        );
        assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    void joinGameSuccessWhite() throws Exception {
        String token = registerAndGetToken("whitePlayer");

        GameData game = gameService.createGame(token, "join me");

        gameService.joinGame(token, game.gameID(), "WHITE");

        GameData updated = gameDAO.getGame(game.gameID());
        assertEquals("whitePlayer", updated.whiteUsername());
        assertNull(updated.blackUsername());
    }

    @Test
    void joinGameAlreadyTaken() throws Exception {
        String token1 = registerAndGetToken("p1");
        String token2 = registerAndGetToken("p2");

        GameData game = gameService.createGame(token1, "game");

        gameService.joinGame(token1, game.gameID(), "WHITE");

        DataAccessException ex = assertThrows(DataAccessException.class, () ->
                gameService.joinGame(token2, game.gameID(), "WHITE")
        );
        assertEquals("Error: already taken", ex.getMessage());
    }

    @Test
    void joinGameBadRequestInvalidColor() throws Exception {
        String token = registerAndGetToken("bob");
        GameData game = gameService.createGame(token, "game");

        DataAccessException ex = assertThrows(DataAccessException.class, () ->
                gameService.joinGame(token, game.gameID(), "GREEN")
        );
        assertEquals("Error: bad request", ex.getMessage());
    }
}