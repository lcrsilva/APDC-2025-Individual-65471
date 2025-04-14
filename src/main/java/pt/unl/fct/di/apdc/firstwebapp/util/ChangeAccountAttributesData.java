package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangeAccountAttributesData {
    public AuthToken authToken;
    public String targetUsername;
    public String name;
    public String email;
    public String phone;
    public String profile;

    public ChangeAccountAttributesData() {}

    public ChangeAccountAttributesData(AuthToken authToken, String targetUsername, String name,
                                       String email, String phone, String profile) {
        this.authToken = authToken;
        this.targetUsername = targetUsername;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.profile = profile;
    }
}