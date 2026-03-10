package dataaccess;

import model.AuthData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLAuthDAO implements AuthDAO {

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        String statement = "INSERT INTO auth (authToken, username) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {

            ps.setString(1, auth.authToken());
            ps.setString(2, auth.username());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Error: already taken");
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String statement = "SELECT * FROM auth WHERE authToken = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {

            ps.setString(1, authToken);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String authToken1 = rs.getString("authToken");
                String username = rs.getString("username");

                return new AuthData(authToken1, username);
            }

            return null;

        } catch (SQLException e) {
            throw new DataAccessException("Error: unable get AuthData");
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        String statement = "DELETE FROM auth WHERE authToken = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {

            ps.setString(1, authToken);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Error: unable delete Auth");
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String statement = "TRUNCATE auth";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error: unable to clear");
        }
    }
}
