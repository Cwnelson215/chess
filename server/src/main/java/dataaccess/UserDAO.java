package dataaccess;

import model.UserData;

public interface UserDAO {
    UserData getUser(String username) throws DataAccessException;
    UserData getUser(String username, String password) throws DataAccessException;
    void createUser(UserData newUser) throws DataAccessException;
    void clear() throws DataAccessException;
    boolean isEmpty() throws DataAccessException;
}
