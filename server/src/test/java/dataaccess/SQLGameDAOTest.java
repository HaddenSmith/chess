package dataaccess;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class SQLGameDAOTest extends DAOTestBase {

    @Test
    void createGamePositive() throws Exception {
        GameData created = gameDAO.createGame(
                new GameData(0, null, null, "game1", new ChessGame())
        );

        assertNotNull(created);
        assertTrue(created.gameID() > 0);
        assertEquals("game1", created.gameName());

        GameData fetched = gameDAO.getGame(created.gameID());
        assertNotNull(fetched);
        assertEquals("game1", fetched.gameName());
    }

    @Test
    void createGameNegativeNullGameName() {
        assertThrows(DataAccessException.class, () ->
                gameDAO.createGame(new GameData(0, null, null, null, new ChessGame()))
        );
    }

    @Test
    void getGamePositive() throws Exception {
        GameData created = gameDAO.createGame(
                new GameData(0, null, null, "game1", new ChessGame())
        );

        GameData fetched = gameDAO.getGame(created.gameID());

        assertNotNull(fetched);
        assertEquals(created.gameID(), fetched.gameID());
        assertEquals("game1", fetched.gameName());
    }

    @Test
    void getGameNegativeMissingGame() throws Exception {
        GameData fetched = gameDAO.getGame(999999);

        assertNull(fetched);
    }

    @Test
    void listGamesPositive() throws Exception {
        gameDAO.createGame(new GameData(0, null, null, "g1", new ChessGame()));
        gameDAO.createGame(new GameData(0, null, null, "g2", new ChessGame()));

        Collection<GameData> games = gameDAO.listGames();

        assertNotNull(games);
        assertEquals(2, games.size());
    }

    @Test
    void listGamesNegativeEmptyList() throws Exception {
        Collection<GameData> games = gameDAO.listGames();

        assertNotNull(games);
        assertTrue(games.isEmpty());
    }

    @Test
    void updateGamePositivePersistsBoardAndPlayers() throws Exception {
        GameData created = gameDAO.createGame(
                new GameData(0, null, null, "game1", new ChessGame())
        );

        ChessGame movedGame = new ChessGame();
        movedGame.makeMove(new ChessMove(
                new ChessPosition(2, 5),
                new ChessPosition(4, 5),
                null
        ));

        GameData updated = new GameData(
                created.gameID(),
                "whitePlayer",
                "blackPlayer",
                "updatedGame",
                movedGame
        );

        gameDAO.updateGame(updated);

        GameData fetched = gameDAO.getGame(created.gameID());

        assertNotNull(fetched);
        assertEquals("whitePlayer", fetched.whiteUsername());
        assertEquals("blackPlayer", fetched.blackUsername());
        assertEquals("updatedGame", fetched.gameName());

        ChessPiece movedPiece = fetched.game().getBoard().getPiece(new ChessPosition(4, 5));
        ChessPiece originalSquare = fetched.game().getBoard().getPiece(new ChessPosition(2, 5));

        assertNotNull(movedPiece);
        assertEquals(ChessGame.TeamColor.WHITE, movedPiece.getTeamColor());
        assertEquals(ChessPiece.PieceType.PAWN, movedPiece.getPieceType());
        assertNull(originalSquare);
    }

    @Test
    void updateGameNegativeMissingGame() {
        ChessGame game = new ChessGame();
        GameData missing = new GameData(999999, "w", "b", "missing", game);

        assertThrows(DataAccessException.class, () ->
                gameDAO.updateGame(missing)
        );
    }

    @Test
    void clearPositive() throws Exception {
        gameDAO.createGame(new GameData(0, null, null, "g1", new ChessGame()));
        gameDAO.createGame(new GameData(0, null, null, "g2", new ChessGame()));

        gameDAO.clear();

        assertTrue(gameDAO.listGames().isEmpty());
    }
}