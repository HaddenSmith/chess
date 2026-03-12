package dataaccess;

import org.junit.jupiter.api.BeforeEach;

public class DAOTestBase {

    public SQLUserDAO userDAO;
    public SQLAuthDAO authDAO;
    public SQLGameDAO gameDAO;

    @BeforeEach
    void setUp() throws Exception {
        new MySqlDataAccess();

        userDAO = new SQLUserDAO();
        authDAO = new SQLAuthDAO();
        gameDAO = new SQLGameDAO();

        authDAO.clear();
        gameDAO.clear();
        userDAO.clear();
    }
}