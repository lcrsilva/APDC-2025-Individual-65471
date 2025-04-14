package pt.unl.fct.di.apdc.firstwebapp.util;

public class LogoutData {
    public AuthToken authToken;

    public LogoutData() {}

    public LogoutData(AuthToken authToken) {
        this.authToken = authToken;
    }
}