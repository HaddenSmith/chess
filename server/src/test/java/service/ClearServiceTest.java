package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTest {

    private ClearService clearService;
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

        clearService = new ClearService(gameDAO, authDAO, userDAO);
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);
    }

    @Test
    void clearSuccess() throws Exception {
        // seed data
        AuthData auth = userService.register("bob", "pw", "bob@email.com");
        GameData game = gameService.createGame(auth.authToken(), "g1");

        assertNotNull(userDAO.getUser("bob"));
        assertNotNull(authDAO.getAuth(auth.authToken()));
        assertNotNull(gameDAO.getGame(game.gameID()));

        // clear
        clearService.clear();

        assertNull(userDAO.getUser("bob"));
        assertNull(authDAO.getAuth(auth.authToken()));
        assertTrue(gameDAO.listGames().isEmpty());
    }
}