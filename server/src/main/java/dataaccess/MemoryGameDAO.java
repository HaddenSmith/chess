package dataaccess;

import model.GameData;
import java.util.Collection;
import java.util.HashMap;

public class MemoryGameDAO implements GameDAO {

    private final HashMap<Integer, GameData> games = new HashMap<>();
    private int gameIDCounter = 1;

    @Override
    public GameData createGame(GameData game) {
        game = new GameData(gameIDCounter++, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
        games.put(game.gameID(), game);
        return game;
    }

    @Override
    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    @Override
    public Collection<GameData> listGames() {
        return games.values();
    }

    @Override
    public void updateGame(GameData newData) {
        games.put(newData.gameID(), newData);
    }

    @Override
    public void clear() throws DataAccessException {
        games.clear();
    }
}
