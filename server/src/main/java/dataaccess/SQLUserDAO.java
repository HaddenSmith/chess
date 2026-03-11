package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLUserDAO implements UserDAO {

    @Override
    public void createUser(UserData user) throws DataAccessException {
        String statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {

            String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());

            ps.setString(1, user.username());
            ps.setString(2, hashedPassword);
            ps.setString(3, user.email());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Error: already taken");
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String statement = "SELECT * FROM user WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {

            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String user = rs.getString("username");
                String password = rs.getString("password");
                String email = rs.getString("email");

                return new UserData(user, password, email);
            }

            return null;

        } catch (SQLException e) {
            throw new DataAccessException("Error: unable to read user");
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String statement = "TRUNCATE user";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error: unable to clear");
        }
    }
}