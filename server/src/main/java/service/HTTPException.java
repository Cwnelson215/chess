package service;

import com.google.gson.Gson;
import spark.Response;

import java.util.Map;

public class HTTPException extends RuntimeException {
    private int status;

    public HTTPException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public Response createResponse(Response res) {
        var body = new Gson().toJson(Map.of("message",
                String.format("Error: %s", this.getMessage()), "success", false));
        res.status(this.getStatus());
        res.body(body);
        return res;
    }
}
