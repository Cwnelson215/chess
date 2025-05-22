package dataaccess;

import model.AuthData;

public interface AuthDAO {
    AuthData getAuth(String authToken);
    AuthData getAuthToken(String username);
    void clear();
    void deleteAuth(String authToken);
    void createAuth(AuthData newAuth);
    String getUsername(String authToken);
}
