package dataaccess;

import model.AuthData;
import java.util.HashMap;
import java.util.Map;

public class MemoryAuthDAO implements AuthDAO {
    private final Map<String, AuthData> auths = new HashMap<>(1);

    public AuthData getAuth(String authToken){
        return auths.get(authToken);
    }

    public void clear() {
        auths.clear();
    }

    public void deleteAuth(String authToken) {
        auths.remove(authToken);
    }

    public void createAuth(AuthData newAuth) {
        auths.put(newAuth.getAuthToken(), newAuth);
    }
}
