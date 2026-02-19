package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import java.util.UUID;

public class UserService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData register(String username, String password, String email) throws DataAccessException {
        if (userDAO.getUser(username) == null) {
            userDAO.createUser(new UserData(username, password, email));
            return login(username, password);
        } else throw new DataAccessException("User Already Exists");
    }

    public AuthData login(String username, String password) throws DataAccessException {
        UserData user = userDAO.getUser(username);
        if (user != null && (user.username().equals(username) && user.password().equals(password))) {
            String authToken = UUID.randomUUID().toString();
            AuthData userAuthData = new AuthData(authToken, username);
            authDAO.createAuth(userAuthData);
            return userAuthData;
        } else throw new DataAccessException("Invalid Login Credentials");
    }

    public void logout(String authToken) throws DataAccessException {
        authDAO.deleteAuth(authToken);
    }
}