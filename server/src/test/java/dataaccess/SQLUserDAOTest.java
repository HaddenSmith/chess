package dataaccess;

import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

public class SQLUserDAOTest extends DAOTestBase {

    @Test
    void createUserPositive() throws Exception {
        UserData user = new UserData("bob", "pw123", "bob@email.com");
        userDAO.createUser(user);

        UserData result = userDAO.getUser("bob");

        assertNotNull(result);
        assertEquals("bob", result.username());
        assertEquals("bob@email.com", result.email());
        assertNotEquals("pw123", result.password());
        assertTrue(BCrypt.checkpw("pw123", result.password()));
    }

    @Test
    void createUserNegativeDuplicate() throws Exception {
        UserData user = new UserData("bob", "pw123", "bob@email.com");
        userDAO.createUser(user);

        assertThrows(DataAccessException.class, () ->
                userDAO.createUser(new UserData("bob", "otherpw", "other@email.com"))
        );
    }

    @Test
    void getUserPositive() throws Exception {
        userDAO.createUser(new UserData("alice", "pw", "alice@email.com"));

        UserData result = userDAO.getUser("alice");

        assertNotNull(result);
        assertEquals("alice", result.username());
        assertEquals("alice@email.com", result.email());
    }

    @Test
    void getUserNegativeMissingUser() throws Exception {
        UserData result = userDAO.getUser("missing");

        assertNull(result);
    }

    @Test
    void clearPositive() throws Exception {
        userDAO.createUser(new UserData("bob", "pw", "bob@email.com"));

        userDAO.clear();

        assertNull(userDAO.getUser("bob"));
    }
}