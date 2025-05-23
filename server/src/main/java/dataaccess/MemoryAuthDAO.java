package dataaccess;

import model.AuthData;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class MemoryAuthDAO implements AuthDAO {
    private final Map<String, AuthData> auths = new HashMap<>(1);

    public AuthData getAuth(String authToken){
        return auths.get(authToken);
    }

    public AuthData getAuthToken(String username) {
        AtomicReference<AuthData> data = new AtomicReference<>();
        auths.forEach((key, value) -> {
           if(Objects.equals(value.getUsername(), username)) {
               data.set(value);
           }
        });
        return data.get();
    }

    public String getUsername(String authToken) {return auths.get(authToken).getUsername();}

    public void clear() {
        auths.clear();
    }

    public void deleteAuth(String authToken) {
        auths.remove(authToken);
    }

    public void createAuth(AuthData newAuth) {
        auths.put(newAuth.getAuthToken(), newAuth);
    }

    public boolean isEmpty() {
        return auths.isEmpty();
    }
}
