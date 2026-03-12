package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SQLAuthDAOTest extends DAOTestBase {

    @Test
    void createAuthPositive() throws Exception {
        AuthData auth = new AuthData("token123", "bob");

        authDAO.createAuth(auth);
        AuthData result = authDAO.getAuth("token123");

        assertNotNull(result);
        assertEquals("token123", result.authToken());
        assertEquals("bob", result.username());
    }

    @Test
    void createAuthNegativeDuplicateToken() throws Exception {
        AuthData auth = new AuthData("token123", "bob");
        authDAO.createAuth(auth);

        assertThrows(DataAccessException.class, () ->
                authDAO.createAuth(new AuthData("token123", "alice"))
        );
    }

    @Test
    void getAuthPositive() throws Exception {
        authDAO.createAuth(new AuthData("abc", "bob"));

        AuthData result = authDAO.getAuth("abc");

        assertNotNull(result);
        assertEquals("abc", result.authToken());
        assertEquals("bob", result.username());
    }

    @Test
    void getAuthNegativeMissingToken() throws Exception {
        AuthData result = authDAO.getAuth("missing");

        assertNull(result);
    }

    @Test
    void deleteAuthPositive() throws Exception {
        authDAO.createAuth(new AuthData("deadToken", "bob"));

        authDAO.deleteAuth("deadToken");

        assertNull(authDAO.getAuth("deadToken"));
    }

    @Test
    void deleteAuthNegativeMissingToken() {
        assertDoesNotThrow(() -> authDAO.deleteAuth("missing"));
    }

    @Test
    void clearPositive() throws Exception {
        authDAO.createAuth(new AuthData("a1", "bob"));
        authDAO.createAuth(new AuthData("a2", "alice"));

        authDAO.clear();

        assertNull(authDAO.getAuth("a1"));
        assertNull(authDAO.getAuth("a2"));
    }
}