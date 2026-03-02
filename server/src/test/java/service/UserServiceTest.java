package service;

import dataaccess.*;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private UserService userService;
    private UserDAO userDAO;
    private AuthDAO authDAO;

    @BeforeEach
    void setUp() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        userService = new UserService(userDAO, authDAO);
    }

    @Test
    void registerSuccess() throws Exception {
        AuthData auth = userService.register("bob", "pw", "bob@email.com");

        assertNotNull(auth);
        assertEquals("bob", auth.username());
        assertNotNull(auth.authToken());
        assertNotNull(authDAO.getAuth(auth.authToken())); // token was saved
    }

    @Test
    void registerAlreadyTaken() throws Exception {
        userService.register("bob", "pw", "bob@email.com");

        DataAccessException ex = assertThrows(DataAccessException.class, () ->
                userService.register("bob", "pw2", "bob2@email.com")
        );
        assertEquals("Error: already taken", ex.getMessage());
    }

    @Test
    void loginSuccess() throws Exception {
        userService.register("alice", "pw", "alice@email.com");

        AuthData auth = userService.login("alice", "pw");

        assertEquals("alice", auth.username());
        assertNotNull(auth.authToken());
        assertNotNull(authDAO.getAuth(auth.authToken()));
    }

    @Test
    void loginUnauthorizedWrongPassword() throws Exception {
        userService.register("alice", "pw", "alice@email.com");

        DataAccessException ex = assertThrows(DataAccessException.class, () ->
                userService.login("alice", "wrong")
        );
        assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    void logoutSuccess() throws Exception {
        AuthData auth = userService.register("carl", "pw", "carl@email.com");

        assertNotNull(authDAO.getAuth(auth.authToken())); // exists

        userService.logout(auth.authToken());

        assertNull(authDAO.getAuth(auth.authToken())); // removed
    }

    @Test
    void logoutUnauthorizedEmptyToken() {
        DataAccessException ex = assertThrows(DataAccessException.class, () ->
                userService.logout("")
        );
        assertEquals("Error: unauthorized", ex.getMessage());
    }
}