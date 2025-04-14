package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pt.unl.fct.di.apdc.firstwebapp.util.*;


import java.util.*;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class UserResource {

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
    private final Gson g = new Gson();

    @POST
    @Path("/changerole")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeUserRole(ChangeRoleData data) {

        if (data == null || data.authToken == null || data.targetUsername == null || data.newRole == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing fields.").build();
        }

        Key requesterKey = userKeyFactory.newKey(data.authToken.username);
        Entity requester = datastore.get(requesterKey);
        if (requester == null || !requester.getString("user_role").equals(data.authToken.role)) {
            return Response.status(Response.Status.FORBIDDEN).entity("Invalid token or user role mismatch.").build();
        }

        String requesterRole = data.authToken.role;
        Key targetKey = userKeyFactory.newKey(data.targetUsername);
        Entity targetUser = datastore.get(targetKey);
        if (targetUser == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Target user not found.").build();
        }

        String targetRole = targetUser.getString("user_role");
        String newRole = data.newRole;
        boolean isAllowed = false;

        switch (requesterRole) {
            case "ADMIN":
                isAllowed = true;
                break;
            case "BACKOFFICE":
                if ((targetRole.equals("ENDUSER") || targetRole.equals("PARTNER")) &&
                        (newRole.equals("ENDUSER") || newRole.equals("PARTNER"))) {
                    isAllowed = true;
                }
                break;
            case "ENDUSER":
                return Response.status(Response.Status.FORBIDDEN).entity("ENDUSERs cannot change roles.").build();
        }

        if (!isAllowed) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("You don't have permission to change this user's role.").build();
        }

        Entity updatedUser = Entity.newBuilder(targetUser)
                .set("user_role", newRole)
                .build();
        datastore.update(updatedUser);

        return Response.ok("Role updated successfully!").build();
    }

    @POST
    @Path("/changeaccountstate")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeAccountState(ChangeAccountStateData data) {
        if (data == null || data.authToken == null || data.targetUsername == null || data.newState == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing fields.").build();
        }

        Key requesterKey = userKeyFactory.newKey(data.authToken.username);
        Entity requester = datastore.get(requesterKey);
        if (requester == null || !requester.getString("user_role").equals(data.authToken.role)) {
            return Response.status(Response.Status.FORBIDDEN).entity("Invalid token or user role mismatch.").build();
        }

        String requesterRole = data.authToken.role;
        Key targetKey = userKeyFactory.newKey(data.targetUsername);
        Entity targetUser = datastore.get(targetKey);
        if (targetUser == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Target user not found.").build();
        }

        String newState = data.newState;
        boolean isAllowed = false;

        switch (requesterRole) {
            case "ADMIN":
                isAllowed = true;
                break;
            case "BACKOFFICE":
                if (newState.equals("ACTIVATED") || newState.equals("DEACTIVATED")) {
                    isAllowed = true;
                }
                break;
            case "ENDUSER":
                return Response.status(Response.Status.FORBIDDEN).entity("ENDUSERs cannot change account state.").build();
        }

        if (!isAllowed) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("You don't have permission to change this account's state.").build();
        }

        Entity updatedUser = Entity.newBuilder(targetUser)
                .set("user_state", newState)
                .build();
        datastore.update(updatedUser);

        return Response.ok("Account state updated successfully!").build();
    }
    // ✨ NEW: Get available roles for the current user

    @POST
    @Path("/availableRoles")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getAvailableRoles(AuthToken token) {
        Key tokenKey = datastore.newKeyFactory().setKind("AuthToken").newKey(token.tokenID);
        Entity tokenEntity = datastore.get(tokenKey);

        if (tokenEntity == null) {
            return Response.status(Response.Status.FORBIDDEN).entity("Session expired or token is invalid.").build();
        }
        if (token == null || token.username == null || token.role == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing token information.").build();
        }

        Key userKey = userKeyFactory.newKey(token.username);
        Entity user = datastore.get(userKey);
        if (user == null || !user.getString("user_role").equals(token.role)) {
            return Response.status(Response.Status.FORBIDDEN).entity("Invalid token or role mismatch.").build();
        }

        List<String> roles = new ArrayList<>();
        switch (token.role) {
            case "ADMIN":
                roles = Arrays.asList("ADMIN", "BACKOFFICE", "ENDUSER", "PARTNER");
                break;
            case "BACKOFFICE":
                roles = Arrays.asList("ENDUSER", "PARTNER");
                break;
            case "ENDUSER":
                // ENDUSER can't change roles, return empty list
                break;
        }

        return Response.ok(g.toJson(roles)).build();
    }

    // ✨ NEW: Get available account states for the current user
    @POST
    @Path("/availableStates")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getAvailableAccountStates(AuthToken token) {
        Key tokenKey = datastore.newKeyFactory().setKind("AuthToken").newKey(token.tokenID);
        Entity tokenEntity = datastore.get(tokenKey);

        if (tokenEntity == null) {
            return Response.status(Response.Status.FORBIDDEN).entity("Session expired or token is invalid.").build();
        }
        if (token == null || token.username == null || token.role == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing token information.").build();
        }

        Key userKey = userKeyFactory.newKey(token.username);
        Entity user = datastore.get(userKey);
        if (user == null || !user.getString("user_role").equals(token.role)) {
            return Response.status(Response.Status.FORBIDDEN).entity("Invalid token or role mismatch.").build();
        }

        List<String> states = new ArrayList<>();
        switch (token.role) {
            case "ADMIN":
                states = Arrays.asList("ACTIVATED", "DEACTIVATED");
                break;
            case "BACKOFFICE":
                states = Arrays.asList("ACTIVATED", "DEACTIVATED");
                break;
            case "ENDUSER":
                // ENDUSER can't change state
                break;
        }

        return Response.ok(g.toJson(states)).build();
    }
    @POST
    @Path("/removeAccount")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeUserAccount(RemoveAccountRequest data) {
        if (data == null || data.authToken == null || data.userIDOrEmail == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing fields.").build();
        }

        // Get the requester (the one trying to delete)
        Key requesterKey = userKeyFactory.newKey(data.authToken.username);
        Entity requester = datastore.get(requesterKey);
        if (requester == null || !requester.getString("user_role").equals(data.authToken.role)) {
            return Response.status(Response.Status.FORBIDDEN).entity("Invalid token or user role mismatch.").build();
        }

        String requesterRole = data.authToken.role;
        Entity targetUser = null;

        // First try to find the user by key (username)
        try {
            Key targetKey = userKeyFactory.newKey(data.userIDOrEmail);
            targetUser = datastore.get(targetKey);
        } catch (Exception e) {
            // Handle exceptions just in case
        }

        // If not found by key, try email
        if (targetUser == null) {
            Query<Entity> queryByEmail = Query.newEntityQueryBuilder()
                    .setKind("User")
                    .setFilter(StructuredQuery.PropertyFilter.eq("user_email", data.userIDOrEmail))
                    .build();

            QueryResults<Entity> results = datastore.run(queryByEmail);
            if (results.hasNext()) {
                targetUser = results.next();
            }
        }

        if (targetUser == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Target user not found.").build();
        }

        // Role of the target
        String targetRole = targetUser.getString("user_role");

        // Permission logic
        boolean isAllowed = false;
        switch (requesterRole) {
            case "ADMIN":
                isAllowed = true;
                break;
            case "BACKOFFICE":
                if (targetRole.equals("ENDUSER") || targetRole.equals("PARTNER")) {
                    isAllowed = true;
                }
                break;
            case "ENDUSER":
                return Response.status(Response.Status.FORBIDDEN).entity("ENDUSERs cannot remove accounts.").build();
        }

        if (!isAllowed) {
            return Response.status(Response.Status.FORBIDDEN).entity("You don't have permission to remove this account.").build();
        }

        // Delete the user
        datastore.delete(targetUser.getKey());
        return Response.ok("Account removed successfully!").build();
    }
    @POST
    @Path("/list")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response listUsers(AuthToken token) {
        Key tokenKey = datastore.newKeyFactory().setKind("AuthToken").newKey(token.tokenID);
        Entity tokenEntity = datastore.get(tokenKey);

        if (tokenEntity == null) {
            return Response.status(Response.Status.FORBIDDEN).entity("Session expired or token is invalid.").build();
        }
        if (token == null || token.username == null || token.role == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing token information.").build();
        }

        // Fetch the requester (the one making the request)
        Key requesterKey = userKeyFactory.newKey(token.username);
        Entity requester = datastore.get(requesterKey);
        if (requester == null || !requester.getString("user_role").equals(token.role)) {
            return Response.status(Response.Status.FORBIDDEN).entity("Invalid token or role mismatch.").build();
        }

        String requesterRole = token.role;

        // Only allow admins and back office roles to list users
        if (!requesterRole.equals("ADMIN") && !requesterRole.equals("BACKOFFICE") && !requesterRole.equals("ENDUSER")) {
            return Response.status(Response.Status.FORBIDDEN).entity("You don't have permission to list users.").build();
        }

        // Query to fetch all users (you could add pagination here for large datasets)
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("User")
                .build();
        QueryResults<Entity> users = datastore.run(query);

        List<Map<String, Object>> userList = new ArrayList<>();
        while (users.hasNext()) {
            Entity user = users.next();
            Map<String, Object> userData = new HashMap<>();

            // Add user data fields
            userData.put("username", user.getKey().getName());
            userData.put("role", user.getString("user_role"));
            userData.put("email", user.getString("user_email"));
            userData.put("name", user.getString("user_name"));
            userData.put("phone", user.getString("user_phone"));
            userData.put("profile", user.getString("user_profile"));
            userData.put("state", user.getString("user_state"));



            // Add logic for ENDUSER listing
            if (requesterRole.equals("ENDUSER")) {
                // Check if the user is an ENDUSER and has a PUBLIC profile and ACTIVO state
                if (user.getString("user_role").equals("ENDUSER") &&
                        user.getString("user_profile").equals("PUBLIC") &&
                        user.getString("user_state").equals("ACTIVO")) {
                    userList.add(userData);
                }
            } else if (requesterRole.equals("BACKOFFICE") && user.getString("user_role").equals("ENDUSER")) {
                // BACKOFFICE can see all ENDUSERs
                userList.add(userData);
            } else if (requesterRole.equals("ADMIN")) {
                // ADMIN can see all users regardless of profile or state
                userList.add(userData);
            }
        }

        // Return the list of users
        return Response.ok(g.toJson(userList)).build();
    }

    @POST
    @Path("/changeAttributes")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeAccountAttributes(ChangeAccountAttributesData data) {
        if (data == null || data.authToken == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing fields.").build();
        }

        // Validate requester
        Key requesterKey = userKeyFactory.newKey(data.authToken.username);
        Entity requester = datastore.get(requesterKey);
        if (requester == null || !requester.getString("user_role").equals(data.authToken.role)) {
            return Response.status(Response.Status.FORBIDDEN).entity("Invalid token or user role mismatch.").build();
        }

        String requesterRole = data.authToken.role;
        String targetUsername = data.targetUsername != null ? data.targetUsername : data.authToken.username;

        // Get target user
        Key targetKey = userKeyFactory.newKey(targetUsername);
        Entity targetUser = datastore.get(targetKey);
        if (targetUser == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Target user not found.").build();
        }

        // Check if account is activated
        if (!targetUser.getString("user_state").equals("ACTIVATED")) {
            return Response.status(Response.Status.FORBIDDEN).entity("Account must be activated to modify attributes.").build();
        }

        // Check permissions based on role
        boolean isAllowed = false;
        String targetRole = targetUser.getString("user_role");

        switch (requesterRole) {
            case "ADMIN":
                // ADMIN can modify any attribute of any user
                isAllowed = true;
                break;
            case "BACKOFFICE":
                // BACKOFFICE can modify ENDUSER and PARTNER accounts (except username and email)
                if ((targetRole.equals("ENDUSER") || targetRole.equals("PARTNER"))) {
                    isAllowed = true;
                    if (data.email != null) {
                        return Response.status(Response.Status.FORBIDDEN)
                                .entity("BACKOFFICE cannot change email.").build();
                    }
                }
                break;
            case "ENDUSER":
                // ENDUSER can only modify their own account (except username, email, name, role and state)
                if (targetUsername.equals(data.authToken.username)) {
                    isAllowed = true;
                    if (data.email != null) {
                        return Response.status(Response.Status.FORBIDDEN)
                                .entity("ENDUSER cannot change email.").build();
                    }
                    if (data.name != null && !data.name.equals(targetUser.getString("user_name"))) {
                        return Response.status(Response.Status.FORBIDDEN)
                                .entity("ENDUSER cannot change name.").build();
                    }
                }
                break;
        }

        if (!isAllowed) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("You don't have permission to modify this account.").build();
        }

        // Build the updated entity
        Entity.Builder updatedUserBuilder = Entity.newBuilder(targetUser);

        // Only allow name change if not ENDUSER or if ADMIN changing another user
        if (data.name != null && !data.name.isEmpty() &&
                (requesterRole.equals("ADMIN") ||
                        (requesterRole.equals("BACKOFFICE") && !targetUsername.equals(data.authToken.username)))) {
            updatedUserBuilder.set("user_name", data.name);
        }

        // Only ADMIN can change email
        if (data.email != null && !data.email.isEmpty() && requesterRole.equals("ADMIN")) {
            updatedUserBuilder.set("user_email", data.email);
        }

        // All roles can change phone and profile for allowed accounts
        if (data.phone != null && !data.phone.isEmpty()) {
            updatedUserBuilder.set("user_phone", data.phone);
        }

        if (data.profile != null && !data.profile.isEmpty()) {
            updatedUserBuilder.set("user_profile", data.profile);
        }

        // Save changes
        datastore.update(updatedUserBuilder.build());

        return Response.ok("Account attributes updated successfully!").build();
    }

    @POST
    @Path("/changePassword")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changePassword(ChangePasswordData data) {
        if (data == null || data.authToken == null || data.currentPassword == null ||
                data.newPassword == null || data.confirmationPassword == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing fields.").build();
        }

        // Validate that new password and confirmation match
        if (!data.newPassword.equals(data.confirmationPassword)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("New password and confirmation do not match.").build();
        }

        // Validate requester
        Key userKey = userKeyFactory.newKey(data.authToken.username);
        Entity user = datastore.get(userKey);
        if (user == null || !user.getString("user_role").equals(data.authToken.role)) {
            return Response.status(Response.Status.FORBIDDEN).entity("Invalid token or user role mismatch.").build();
        }

        // Verify current password
        String storedPassword = user.getString("user_pwd");
        if (!storedPassword.equals(data.currentPassword)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Current password is incorrect.").build();
        }

        // Validate new password (you might want to add more complexity checks)
        if (data.newPassword.length() < 6) {
            return Response.status(Response.Status.BAD_REQUEST).entity("New password must be at least 6 characters long.").build();
        }

        // Update the password
        Entity updatedUser = Entity.newBuilder(user)
                .set("user_pwd", data.newPassword)
                .build();
        datastore.update(updatedUser);

        return Response.ok("Password changed successfully!").build();
    }

    @POST
    @Path("/logout")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response logout(LogoutData data) {
        if (data == null || data.authToken == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing token.").build();
        }

        // Verify the token exists in the datastore
        Key tokenKey = datastore.newKeyFactory()
                .setKind("AuthToken")
                .newKey(data.authToken.tokenID);

        Entity tokenEntity = datastore.get(tokenKey);
        if (tokenEntity == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Token not found.").build();
        }

        // Verify the token belongs to the user
        if (!tokenEntity.getString("username").equals(data.authToken.username)) {
            return Response.status(Response.Status.FORBIDDEN).entity("Token doesn't belong to user.").build();
        }

        // Delete the token from datastore to revoke it
        datastore.delete(tokenKey);

        return Response.ok("Logout successful."+ tokenKey.getName()).build();
    }
}