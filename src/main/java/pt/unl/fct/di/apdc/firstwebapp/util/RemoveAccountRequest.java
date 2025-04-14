package pt.unl.fct.di.apdc.firstwebapp.util;

public class RemoveAccountRequest {
    public AuthToken authToken;
    public String userIDOrEmail;


    public RemoveAccountRequest() {}


    public RemoveAccountRequest(AuthToken authToken, String userIDOrEmail) {
        this.authToken = authToken;
        this.userIDOrEmail = userIDOrEmail;
    }
}