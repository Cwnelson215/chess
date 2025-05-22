package dataaccess;

import model.UserData;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserDOA {
    private final Map<String, UserData> users = new HashMap<>(1);

    public UserData getUser(String username) {
        return users.get(username);
    }

    public Object getUser(String username, String password) {
        UserData user = users.get(username);
        if(user == null) {
            return null;
        }
        if(Objects.equals(user.getPassword(), password)) {
            return user;
        }
        return null;
    }

    public void createUser(UserData newUser) {
        users.put(newUser.getUsername(), newUser);
    }

    public void clear() {
        users.clear();
    }


}
