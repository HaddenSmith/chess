package dataaccess;

import model.GameData;
import java.util.Collection;
import java.util.HashMap;

public class MemoryGameDAO implements GameDAO {

    private final HashMap<Integer, GameData> games = new HashMap<>();

    @Override
    public void createGame(GameData game) {
        games.put(game.gameID(), game);
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
