package server;

import com.google.gson.Gson;
import io.javalin.Javalin;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import service.*;

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

        registerEndpoints();

        // Register your endpoints and exception handlers here.

    }

    private void registerEndpoints() {
        clearEndPoint();
        registerEndPoint();
        loginEndPoint();
        logoutEndPoint();
        listGamesEndPoint();
        createGameEndPoint();
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
            } catch (DataAccessException e) {
                ctx.status(500);
                if (e.getMessage().equals("Error: bad request")) ctx.status(400);
                else if (e.getMessage().equals("Error: already taken")) ctx.status(403);
                ctx.result(gson.toJson(Map.of("message", e.getMessage())));
            } catch (Exception e) {
                ctx.status(500);
                ctx.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
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
            } catch (DataAccessException e) {
                ctx.status(500);
                if (e.getMessage().equals("Error: bad request")) ctx.status(400);
                else if (e.getMessage().equals("Error: unauthorized")) ctx.status(401);
                ctx.result(gson.toJson(Map.of("message", e.getMessage())));
            } catch (Exception e) {
                ctx.status(500);
                ctx.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
            }
        });
    }

    private void logoutEndPoint() {
        Gson gson = new Gson();

        javalin.delete("/session", ctx -> {
           try {
               String authToken = ctx.header("authorization");
               if (authToken == null) authToken = "";
               userService.logout(authToken);
               ctx.status(200);
               ctx.result("{}");
           } catch (DataAccessException e) {
               ctx.status(500);
               ctx.result(gson.toJson(Map.of("message", e.getMessage())));
               if (e.getMessage().equals("Error: unauthorized")) ctx.status(401);
           } catch (Exception e) {
               ctx.status(500);
               ctx.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
           }
        });
    }

    private void listGamesEndPoint() {
        Gson gson = new Gson();

        javalin.get("/game", ctx -> {
            try {
                String authToken = ctx.header("authorization");
                if (authToken == null) authToken = "";
                Collection<GameData> games = gameService.listGames(authToken);
                ctx.status(200);
            } catch (DataAccessException e) {
                ctx.status(500);
                ctx.result(gson.toJson(Map.of("message", e.getMessage())));
                if (e.getMessage().equals("Error: unauthorized")) ctx.status(401);
            } catch (Exception e) {
                ctx.status(500);
                ctx.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
            }
        });
    }

    private void createGameEndPoint() {
        Gson gson = new Gson();

        javalin.post("/game", ctx -> {
            try {
                String authToken = ctx.header("authorization");
                if (authToken == null) authToken = "";
                String gameName = gson.fromJson(ctx.body(), CreateGameRequest.class).gameName();
                GameData gameData = gameService.createGame(authToken, gameName);
                CreateGameResult result = new CreateGameResult(gameData.gameID());
                ctx.status(200);
                ctx.result(gson.toJson(result));
            } catch (DataAccessException e) {
                ctx.status(500);
                ctx.result(gson.toJson(Map.of("message", e.getMessage())));
                if (e.getMessage().equals("Error: bad request")) ctx.status(400);
                if (e.getMessage().equals("Error: unauthorized")) ctx.status(401);
            } catch (Exception e) {
                ctx.status(500);
                ctx.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
            }
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}