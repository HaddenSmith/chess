package server;

import com.google.gson.Gson;
import io.javalin.Javalin;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import service.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class Server {

    private final Javalin javalin;
    private final UserService userService;
    private final GameService gameService;
    private final ClearService clearService;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();

        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);
        clearService = new ClearService(gameDAO, authDAO, userDAO);

        clearEndPoint();
        registerEndPoint();
        loginEndPoint();
        logoutEndPoint();
        listGamesEndPoint();
        createGameEndPoint();
        joinGameEndPoint();
    }

    private void clearEndPoint() {
        Gson gson = new Gson();

        javalin.delete("/db", ctx -> {
            try {
                clearService.clear();
                ctx.status(200);

                ctx.result("{}");
            } catch (DataAccessException e) {
                ctx.status(500);
                ctx.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
            }
        });
    }

    private void registerEndPoint() {
        Gson gson = new Gson();

        javalin.post("/user", ctx -> {
            try {
                RegisterRequest userInfo = gson.fromJson(ctx.body(), RegisterRequest.class);
                AuthData authData = userService.register(userInfo.username(), userInfo.password(), userInfo.email());
                UserResult result = new UserResult(authData.username(), authData.authToken());
                ctx.status(200);
                ctx.result(gson.toJson(result));
            } catch (Exception e) {
                ctx.status(getErrorStatusCode(e.getMessage()));
                ctx.result(gson.toJson(Map.of("message", e.getMessage())));
            }
        });
    }

    private void loginEndPoint() {
        Gson gson = new Gson();

        javalin.post("/session", ctx -> {
            try {
                LoginRequest userInfo = gson.fromJson(ctx.body(), LoginRequest.class);
                AuthData authData = userService.login(userInfo.username(), userInfo.password());
                UserResult result = new UserResult(authData.username(), authData.authToken());
                ctx.status(200);
                ctx.result(gson.toJson(result));
            } catch (Exception e) {
                ctx.status(getErrorStatusCode(e.getMessage()));
                ctx.result(gson.toJson(Map.of("message", e.getMessage())));
            }
        });
    }

    private void logoutEndPoint() {
        Gson gson = new Gson();

        javalin.delete("/session", ctx -> {
           try {
               String authToken = ctx.header("authorization");
               userService.logout(authToken);
               ctx.status(200);
               ctx.result("{}");
           } catch (Exception e) {
               ctx.status(getErrorStatusCode(e.getMessage()));
               ctx.result(gson.toJson(Map.of("message", e.getMessage())));
           }
        });
    }

    private void listGamesEndPoint() {
        Gson gson = new Gson();

        javalin.get("/game", ctx -> {
            try {
                String authToken = ctx.header("authorization");
                Collection<GameData> games = gameService.listGames(authToken);
                Collection<GameSummary> gameSummaries = new ArrayList<>();
                for (GameData data : games) {
                    gameSummaries.add(new GameSummary(data.gameID(), data.whiteUsername(), data.blackUsername(), data.gameName()));
                }
                ListGamesResult result = new ListGamesResult(gameSummaries);
                ctx.status(200);
                ctx.result(gson.toJson(result));
            } catch (Exception e) {
                ctx.status(getErrorStatusCode(e.getMessage()));
                ctx.result(gson.toJson(Map.of("message", e.getMessage())));
            }
        });
    }

    private void createGameEndPoint() {
        Gson gson = new Gson();

        javalin.post("/game", ctx -> {
            try {
                String authToken = ctx.header("authorization");
                String gameName = gson.fromJson(ctx.body(), CreateGameRequest.class).gameName();
                GameData gameData = gameService.createGame(authToken, gameName);
                CreateGameResult result = new CreateGameResult(gameData.gameID());
                ctx.status(200);
                ctx.result(gson.toJson(result));
            } catch (Exception e) {
                ctx.status(getErrorStatusCode(e.getMessage()));
                ctx.result(gson.toJson(Map.of("message", e.getMessage())));
            }
        });
    }

    private void joinGameEndPoint() {
        Gson gson = new Gson();

        javalin.put("/game", ctx -> {
            try {
                String authToken = ctx.header("authorization");
                JoinGameRequest joinRequest = gson.fromJson(ctx.body(), JoinGameRequest.class);
                gameService.joinGame(authToken, joinRequest.gameID(), joinRequest.playerColor());
                ctx.status(200);
                ctx.result("{}");
            } catch (Exception e) {
                ctx.status(getErrorStatusCode(e.getMessage()));
                ctx.result(gson.toJson(Map.of("message", e.getMessage())));
            }
        });
    }

    private int getErrorStatusCode(String errorMessage) {
        int statusCode = 500; //Defaults to a server error
        if (errorMessage.equals("Error: bad request")) { statusCode = 400; }
        if (errorMessage.equals("Error: unauthorized")) { statusCode = 401; }
        if (errorMessage.equals("Error: already taken")) { statusCode = 403; }

        return statusCode;
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}