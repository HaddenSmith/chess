package client.ui;

import com.google.gson.Gson;
import result.*;
import request.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final String serverURL;

    public ServerFacade(int port) {
        this.serverURL = "http://localhost:" + port;
    }

    public UserResult register(String username, String password, String email) throws Exception {
        RegisterRequest request = new RegisterRequest(username, password, email);
        return makeRequest("POST", "/user", request, null, UserResult.class);
    }

    public UserResult login(String username, String password) throws Exception {
        LoginRequest request = new LoginRequest(username, password);
        return makeRequest("POST", "/session", request, null, UserResult.class);
    }

    public void logout(String authToken) throws Exception {
        makeRequest("DELETE", "/session", null, authToken, null);
    }

    public ListGamesResult listGames(String authToken) throws Exception {
        return makeRequest("GET", "/game", null, authToken, ListGamesResult.class);
    }

    public CreateGameResult createGame(String authToken, String gameName) throws Exception {
        CreateGameRequest request = new CreateGameRequest(gameName);
        return makeRequest("POST", "/game", request, authToken, CreateGameResult.class);
    }

    public void joinGame(String authToken, String playerColor, int gameID) throws Exception {
        JoinGameRequest request = new JoinGameRequest(playerColor, gameID);
        makeRequest("PUT", "/game", request, authToken, null);
    }

    private <T> T makeRequest(String method, String path, Object requestBody, String authToken, Class<T> responseClass) throws Exception {
        String url = serverURL + path;

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(new URI(url));

        if (authToken != null) {
            requestBuilder.header("authorization", authToken);
        }

        if (requestBody != null) {
            String jsonBody = gson.toJson(requestBody);
            requestBuilder.header("Content-Type", "application/json");
            requestBuilder.method(method, HttpRequest.BodyPublishers.ofString(jsonBody));
        } else {
            requestBuilder.method(method, HttpRequest.BodyPublishers.noBody());
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            if (responseClass == null) {
                return null;
            }
            return gson.fromJson(response.body(), responseClass);
        } else {
            ErrorResponse error = gson.fromJson(response.body(), ErrorResponse.class);
            if (error != null && error.message() != null) {
                throw new Exception(error.message());
            } else {
                throw new Exception("Error: unknown error");
            }
        }
    }
}
