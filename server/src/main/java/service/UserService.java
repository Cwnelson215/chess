package service;
import dataaccess.AuthDOA;
import dataaccess.UserDOA;
import model.AuthData;
import model.UserData;

public class UserService {
    UserDOA userDatabase = new UserDOA();
    AuthDOA authDatabase = new AuthDOA();

    public RegisterResult register(RegisterRequest registerRequest) {
        String username = registerRequest.username();
        UserData user = userDatabase.getUser(username);
        if(user != null) {
            return null;
        }
        UserData newUser = new UserData(username, registerRequest.password(),
                registerRequest.email());
        AuthData userAuth = new AuthData(username);
        userDatabase.createUser(newUser);
        authDatabase.createAuth(userAuth);
        return new RegisterResult(username, userAuth.getAuthToken());
    }
}
