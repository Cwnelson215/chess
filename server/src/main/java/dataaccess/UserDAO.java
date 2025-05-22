package dataaccess;

import model.UserData;

public interface UserDAO {
    UserData getUser(String username);
    UserData getUser(String username, String password);
    void createUser(UserData newUser);
    void clear();
}
