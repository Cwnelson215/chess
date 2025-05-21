package dataaccess;

import model.UserData;
import java.util.HashMap;
import java.util.Map;

public class UserDOA {
    private final Map<String, UserData> users = new HashMap<>(1);

    public UserData getUser(String username) {
        return users.get(username);
    }

    public void createUser(UserData newUser) {
        users.put(newUser.getUsername(), newUser);
    }


}
