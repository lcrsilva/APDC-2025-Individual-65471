package pt.unl.fct.di.apdc.firstwebapp.util;

public class RegisterData {
	public String username;
	public String password;
	public String confirmation;
	public String email;
	public String name;
	public String phone;
	public String profile;


	public String citizenCard;
	public String role = "ENDUSER";
	public String nif;
	public String employer;
	public String job;
	public String address;
	public String employerNif;
	public String state = "DEACTIVATED";
	public String photo;

	public RegisterData() {}

	public boolean validRegistration() {
		return isNonEmpty(username) &&
				isNonEmpty(password) &&
				isNonEmpty(confirmation) &&
				isNonEmpty(email) &&
				isNonEmpty(name) &&
				isNonEmpty(phone) &&
				isNonEmpty(profile) &&
				email.matches(".+@.+\\..+") &&
				password.equals(confirmation) &&
				password.matches(".*[A-Z].*") &&
				password.matches(".*[a-z].*") &&
				password.matches(".*[0-9].*") &&
				password.matches(".*[^a-zA-Z0-9].*") &&
				password.length() >= 8;
	}

	private boolean isNonEmpty(String s) {
		return s != null && !s.isBlank();
	}
}