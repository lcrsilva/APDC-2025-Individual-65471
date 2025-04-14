package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangeAccountStateData {
    public AuthToken authToken;
    public String targetUsername;
    public String newState;


    public ChangeAccountStateData() {
    }


    public ChangeAccountStateData(AuthToken authToken, String targetUsername, String newState) {
        this.authToken = authToken;
        this.targetUsername = targetUsername;
        this.newState = newState;
    }


    public AuthToken getAuthToken() {
        return authToken;
    }

    public void setAuthToken(AuthToken authToken) {
        this.authToken = authToken;
    }

    public String getTargetUsername() {
        return targetUsername;
    }

    public void setTargetUsername(String targetUsername) {
        this.targetUsername = targetUsername;
    }

    public String getNewState() {
        return newState;
    }

    public void setNewState(String newState) {
        this.newState = newState;
    }
}