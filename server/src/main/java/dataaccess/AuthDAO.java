package dataaccess;

import model.AuthData;

public interface AuthDAO {
    AuthData getAuth(String authToken) throws DataAccessException;
    void clear() throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;
    void createAuth(AuthData newAuth) throws DataAccessException;
    String getUsername(String authToken) throws DataAccessException;
    boolean isEmpty() throws DataAccessException;
}
