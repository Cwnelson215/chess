package dataaccess;

import model.UserData;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MemoryUserDAO implements UserDAO {
    private final Map<String, UserData> users = new HashMap<>(1);

    public UserData getUser(String username) {
        return users.get(username);
    }

    public UserData getUser(String username, String password) {
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

    public boolean isEmpty() {
        return users.isEmpty();
    }

}
