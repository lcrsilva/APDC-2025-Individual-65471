package pt.unl.fct.di.apdc.firstwebapp.util;

public class RemoveAccountRequest {
    public AuthToken authToken;
    public String userIDOrEmail;

    // Default constructor
    public RemoveAccountRequest() {}

    // Constructor to initialize values
    public RemoveAccountRequest(AuthToken authToken, String userIDOrEmail) {
        this.authToken = authToken;
        this.userIDOrEmail = userIDOrEmail;
    }
}