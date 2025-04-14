package pt.unl.fct.di.apdc.firstwebapp.util;

import java.util.UUID;

public class AuthToken {

	public static final long EXPIRATION_TIME = 1000 * 60 * 60 * 2; // 2 hours

	public String username;
	public String tokenID;
	public long creationData;
	public long expirationData;
	public String role;
	public String userIDOrEmail;  // New field to identify the target user for account removal

	// Default constructor
	public AuthToken() {
	}



	// Constructor for creating AuthToken with role and username, and optional userIDOrEmail for account removal
	public AuthToken(String username, String role) {
		this.username = username;
		this.tokenID = UUID.randomUUID().toString();
		this.creationData = System.currentTimeMillis();
		this.expirationData = this.creationData + EXPIRATION_TIME;
		this.role = role;
	}

	// New constructor to allow passing userIDOrEmail for account removal
	public AuthToken(String username, String role, String userIDOrEmail) {
		this.username = username;
		this.tokenID = UUID.randomUUID().toString();
		this.creationData = System.currentTimeMillis();
		this.expirationData = this.creationData + EXPIRATION_TIME;
		this.role = role;
		this.userIDOrEmail = userIDOrEmail; // Set the target user ID or email for account removal
	}

	// Getters and Setters (if needed, though not essential for this logic)

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getTokenID() {
		return tokenID;
	}

	public void setTokenID(String tokenID) {
		this.tokenID = tokenID;
	}

	public long getCreationData() {
		return creationData;
	}

	public void setCreationData(long creationData) {
		this.creationData = creationData;
	}

	public long getExpirationData() {
		return expirationData;
	}

	public void setExpirationData(long expirationData) {
		this.expirationData = expirationData;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getUserIDOrEmail() {
		return userIDOrEmail;
	}

	public void setUserIDOrEmail(String userIDOrEmail) {
		this.userIDOrEmail = userIDOrEmail;
	}
}