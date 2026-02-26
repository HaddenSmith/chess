package server;

import com.google.gson.Gson;
import io.javalin.Javalin;
import dataaccess.*;
import model.AuthData;
import service.*;

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
    }

    private void clearEndPoint() {
        Gson gson = new Gson();

        javalin.delete("/db", ctx -> {
            try {
                clearService.clear();
                ctx.status(200);

                ctx.result(gson.toJson(new Object())); // returns {}
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
                ctx.result(gson.toJson(result));
                ctx.status(200);
            } catch (DataAccessException e) {
                ctx.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
                if (e.getMessage().equals("User Already Exists")) ctx.status(403);
                //if (e.getMessage().equals("")) ctx.status(400);
                //if (e.getMessage().equals("")) ctx.status(500);
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
                ctx.result(gson.toJson(result));
                ctx.status(200);
            } catch (DataAccessException e) {
                ctx.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
                if (e.getMessage().equals("User Already Exists")) ctx.status(403);
                //if (e.getMessage().equals("")) ctx.status(400);
                //if (e.getMessage().equals("")) ctx.status(500);
            }
        });
    }

    private void logoutEndPoint() {
        Gson gson = new Gson();

        javalin.delete("/session", ctx -> {
           try {
               String authToken = ctx.header("authorization");
               userService.logout(authToken);
               ctx.result(gson.toJson(new Object())); //Returns {}
               ctx.status(200);
           } catch (DataAccessException e) {
               ctx.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
               if (e.getMessage().equals("User Already Exists")) ctx.status(403);
               //if (e.getMessage().equals("")) ctx.status(400);
               //if (e.getMessage().equals("")) ctx.status(500);
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