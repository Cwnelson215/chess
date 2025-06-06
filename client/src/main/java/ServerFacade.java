import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.util.ArrayList;

public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public AuthData register(UserData user) throws ResponseException {
        var path = "/user";
        return this.makeRequest("POST", path, user, AuthData.class, null);
    }

    public AuthData login(String username, String password) throws ResponseException {
        var path = "/session";
        record LoginRequest(String username, String password) {}
        try {
            return this.makeRequest("POST", path, new LoginRequest(username, password), AuthData.class, null);
        } catch(Exception e) {
            throw new ResponseException(400, e.getMessage());
        }
    }

    public Object logout(String authToken) throws ResponseException {
        var path = "/session";
        return this.makeRequest("DELETE", path, authToken, null, authToken);
    }

    public ArrayList<GameData> listGames(String authToken) throws ResponseException {
        var path = "/game";
        record ListGamesResponse(ArrayList<GameData> games) {}
        var response = this.makeRequest("GET", path, null, ListGamesResponse.class, authToken);
        return response.games;
    }

    public int createGame(String gameName, String authToken) throws ResponseException {
        var path = "/game";
        record CreateRequest(String gameName) {}
        record CreateResponse(int gameID) {}
        var result = this.makeRequest("POST", path, new CreateRequest(gameName), CreateResponse.class, authToken);
        return result.gameID;
    }

    public Object joinGame(String playerColor, int gameID, String authToken) throws ResponseException {
        var path = "/game";
        record JoinRequest(String playerColor, int gameID) {}
        var joinReq = new JoinRequest(playerColor, gameID);
        return this.makeRequest("PUT", path, joinReq, null, authToken);
    }



    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authToken) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            writeBody(request, http, authToken);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch(ResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    private static void writeBody(Object request, HttpURLConnection http, String authToken) throws IOException {
        if(authToken != null) {
            http.setRequestProperty("authorization", authToken);
        }
        if(request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try(OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if(!isSuccessful(status)) {
            try(InputStream respErr = http.getErrorStream()) {
                if(respErr != null) {
                    throw ResponseException.fromJson(respErr);
                }
            }
            throw new ResponseException(status, "other failure: " + status);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if(http.getContentLength() < 0) {
            try(InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if(responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
