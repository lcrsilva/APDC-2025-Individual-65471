package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangeRoleData {
    public String targetUsername;
    public String newRole;
    public AuthToken authToken;

    public ChangeRoleData() {}

    public ChangeRoleData(String targetUsername, String newRole, AuthToken authToken) {
        this.targetUsername = targetUsername;
        this.newRole = newRole;
        this.authToken = authToken;
    }
}