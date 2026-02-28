package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;
import java.util.Collection;

public class GameService {

    private final GameDAO gameDAO;
    private final AuthDAO authDAO;
    private int nextGameID = 1;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public GameData createGame(String authToken, String gameName) throws DataAccessException {
        if (authToken.isEmpty() || gameName.isEmpty()) throw new DataAccessException("Error: bad request");
        validateAuth(authToken);
        int gameID = nextGameID++;
        GameData game = new GameData(gameID, null, null, gameName, new ChessGame());
        gameDAO.createGame(game);
        return game;
    }

    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        validateAuth(authToken);
        return gameDAO.listGames();
    }

    public void joinGame(String authToken, int gameID, String color) throws DataAccessException {
        GameData currentGame = gameDAO.getGame(gameID);

        if (!color.equalsIgnoreCase("white") && !color.equalsIgnoreCase("black")
            || authToken.isEmpty() || currentGame == null) {
            throw new DataAccessException("Error: bad request");
        }

        validateAuth(authToken);

        String whiteUsername = currentGame.whiteUsername();
        String blackUsername = currentGame.blackUsername();
        if (color.equalsIgnoreCase("white")) {
            if (whiteUsername == null) whiteUsername = authDAO.getAuth(authToken).username();
            else throw new DataAccessException("Error: already taken");
        } else {
            if (color.equalsIgnoreCase("black")) {
                if (blackUsername == null) blackUsername = authDAO.getAuth(authToken).username();
                else throw new DataAccessException("Error: already taken");
            }
        }

        gameDAO.updateGame(new GameData(gameID, whiteUsername, blackUsername, currentGame.gameName(), currentGame.game()));
    }

    private void validateAuth(String authToken) throws DataAccessException {
        if (authDAO.getAuth(authToken) == null) throw new DataAccessException("Error: unauthorized");
    }
}

