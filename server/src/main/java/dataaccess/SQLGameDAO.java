package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

public class SQLGameDAO implements GameDAO {

    @Override
    public GameData createGame(GameData game) throws DataAccessException {
        String statement = "INSERT INTO game (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, game.whiteUsername());
            ps.setString(2, game.blackUsername());
            ps.setString(3, game.gameName());
            ps.setString(4, new Gson().toJson(game.game()));

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int gameID = rs.getInt(1);
                return new GameData(gameID, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
            }

            throw new DataAccessException("Error: unable to retrieve generated gameID");
        } catch (SQLException e) {
            throw new DataAccessException("Error: unable to create game");
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException{
        String statement = "SELECT * FROM game WHERE gameID = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {

            ps.setInt(1, gameID);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int gameID1 = rs.getInt("gameID");
                String whiteUsername = rs.getString("whiteUsername");
                String blackUsername = rs.getString("blackUsername");
                String gameName = rs.getString("gameName");
                ChessGame game = new Gson().fromJson(rs.getString("game"), ChessGame.class);

                return new GameData(gameID1, whiteUsername, blackUsername, gameName, game);
            }

            return null;

        } catch (SQLException e) {
            throw new DataAccessException("Error: unable get game");
        }
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        String statement = "SELECT * FROM game";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {

            ResultSet rs = ps.executeQuery();
            ArrayList<GameData> listOfGames = new ArrayList<>();

            while (rs.next()) {
                int gameID1 = rs.getInt("gameID");
                String whiteUsername = rs.getString("whiteUsername");
                String blackUsername = rs.getString("blackUsername");
                String gameName = rs.getString("gameName");
                ChessGame game = new Gson().fromJson(rs.getString("game"), ChessGame.class);

                listOfGames.add(new GameData(gameID1, whiteUsername, blackUsername, gameName, game));
            }

            return listOfGames;

        } catch (SQLException e) {
            throw new DataAccessException("Error: unable get games");
        }
    }

    @Override
    public void updateGame(GameData newData) throws DataAccessException {
        String statement = "UPDATE game SET whiteUsername = ?, blackUsername = ?, gameName = ?, game = ? WHERE gameID = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {

            ps.setString(1, newData.whiteUsername());
            ps.setString(2, newData.blackUsername());
            ps.setString(3, newData.gameName());
            ps.setString(4, new Gson().toJson(newData.game()));
            ps.setInt(5, newData.gameID());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new DataAccessException("Error: invalid gameID");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error: unable to update game");
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String statement = "TRUNCATE game";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error: unable to clear");
        }
    }
}
