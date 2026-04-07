package service;

import chess.ChessGame;
import chess.ChessPiece;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;
import java.util.Collection;

public class GameService {

    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public GameData createGame(String authToken, String gameName) throws DataAccessException {
        if (gameName == null || gameName.isEmpty()) {
            throw new DataAccessException("Error: bad request");
        }

        validateAuth(authToken);
        GameData game = new GameData(0, null, null, gameName, new ChessGame());
        return gameDAO.createGame(game);
    }

    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        validateAuth(authToken);
        return gameDAO.listGames();
    }

    public void joinGame(String authToken, int gameID, String color) throws DataAccessException {
        validateAuth(authToken);

        GameData currentGame = gameDAO.getGame(gameID);
        if (currentGame == null || color == null
           || !color.equalsIgnoreCase("white") && !color.equalsIgnoreCase("black")) {
            throw new DataAccessException("Error: bad request");
        }

        String whiteUsername = currentGame.whiteUsername();
        String blackUsername = currentGame.blackUsername();
        if (color.equalsIgnoreCase("white")) {
            if (whiteUsername == null) {
                whiteUsername = authDAO.getAuth(authToken).username();
            } else {
                throw new DataAccessException("Error: already taken");
            }
        } else {
            if (color.equalsIgnoreCase("black")) {
                if (blackUsername == null) {
                    blackUsername = authDAO.getAuth(authToken).username();
                } else {
                    throw new DataAccessException("Error: already taken");
                }
            }
        }

        gameDAO.updateGame(new GameData(gameID, whiteUsername, blackUsername, currentGame.gameName(), currentGame.game()));
    }

    private void validateAuth(String authToken) throws DataAccessException {
        if (authDAO.getAuth(authToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }
    }

    public GameData getGame(int gameID) throws DataAccessException {
        return gameDAO.getGame(gameID);
    }

    public void leaveGame(int gameID, String username) throws DataAccessException {
        GameData gameData = gameDAO.getGame(gameID);

        if (gameData == null) {
            throw new DataAccessException("Error: game not found");
        }

        String white = gameData.whiteUsername();
        String black = gameData.blackUsername();

        if (username.equals(white)) {
            white = null;
        } else if (username.equals(black)) {
            black = null;
        }

        gameDAO.updateGame(new GameData(gameID, white, black, gameData.gameName(), gameData.game()));
    }

    public void resignGame(int gameID, String username) throws DataAccessException {
        GameData gameData = gameDAO.getGame(gameID);

        if (gameData == null) { throw new DataAccessException("Error: game not found"); }

        ChessGame game = gameData.game();

        game.setGameOver(true);

        // Save updated game back to DB
        gameDAO.updateGame(new GameData(gameID, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game));
    }
}

