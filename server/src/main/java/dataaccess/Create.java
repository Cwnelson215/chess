package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.HashMap;
import java.util.Map;

public class Create {

    public Map createUser(UserData newUser) throws DataAccessException {
        Map<String, String> userData = new HashMap<>(3);
        userData.put("username", newUser.getUsername());
        userData.put("password", newUser.getPassword());
        userData.put("email", newUser.getEmail());
        return userData;
    }

    public Map createAuth(AuthData newAuth) throws DataAccessException {
        Map<String, String> authData = new HashMap<>(2);
        authData.put("username", newAuth.getUsername());
        authData.put("authToken", newAuth.getAuthToken());
        return authData;
    }

}
