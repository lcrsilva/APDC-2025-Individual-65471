package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangePasswordData {
    public AuthToken authToken;
    public String currentPassword;
    public String newPassword;
    public String confirmationPassword;

    public ChangePasswordData() {}

    public ChangePasswordData(AuthToken authToken, String currentPassword,
                              String newPassword, String confirmationPassword) {
        this.authToken = authToken;
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.confirmationPassword = confirmationPassword;
    }
}