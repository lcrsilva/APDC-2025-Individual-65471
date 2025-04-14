package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StringValue;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.OrderBy;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response.Status;
import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangeRoleData;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

    private static final String MESSAGE_INVALID_CREDENTIALS = "Incorrect username or password.";
    private static final String MESSAGE_NEXT_PARAMETER_INVALID = "Request parameter 'next' must be greater or equal to 0.";

    private static final String LOG_MESSAGE_LOGIN_ATTEMP = "Login attempt by user: ";
    private static final String LOG_MESSAGE_LOGIN_SUCCESSFUL = "Login successful by user: ";
    private static final String LOG_MESSAGE_WRONG_PASSWORD = "Wrong password for: ";
    private static final String LOG_MESSAGE_UNKNOW_USER = "Failed login attempt for username: ";

    private static final String USER_PWD = "user_pwd";
    private static final String USER_LOGIN_TIME = "user_login_time";

    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");

    private final Gson g = new Gson();

    public LoginResource() {

    }


    @POST
    @Path("/v3")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doLoginV3(LoginData data) {
        LOG.fine("Login attempt by user: " + data.username);

        Key userKey = userKeyFactory.newKey(data.username);
        Entity user = datastore.get(userKey);

        if (user == null || !user.getString("user_pwd").equals(DigestUtils.sha512Hex(data.password))) {
            LOG.warning("Invalid credentials for: " + data.username);
            return Response.status(Status.FORBIDDEN).entity("Invalid credentials.").build();
        }

        String role = user.getString("user_role");
        AuthToken token = new AuthToken(data.username, role);

        // Store the token in Datastore
        KeyFactory tokenKeyFactory = datastore.newKeyFactory().setKind("AuthToken");
        Key tokenKey = tokenKeyFactory.newKey(token.tokenID);
        Entity tokenEntity = Entity.newBuilder(tokenKey)
                .set("username", token.username)
                .set("role", token.role)
                .set("creationData", token.creationData)
                .set("expirationData", token.expirationData)
                .build();
        datastore.put(tokenEntity);

        LOG.info("Login successful for: " + data.username);
        return Response.ok(g.toJson(token)).build();
    }

    @Path("/session")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public class SessionResource {

        private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
        private static final Gson g = new Gson();

        @POST
        @Path("/logout")
        @Consumes(MediaType.APPLICATION_JSON)
        public Response logout(AuthToken token) {
            if (token == null || token.tokenID == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Missing token.").build();
            }

            // Remove the token from Datastore
            Key tokenKey = datastore.newKeyFactory().setKind("AuthToken").newKey(token.tokenID);
            datastore.delete(tokenKey);

            return Response.ok("Logout successful.").build();
        }
    }

    @POST
    @Path("/user")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLatestLogins(LoginData data) {

        Key userKey = userKeyFactory.newKey(data.username);

        Entity user = datastore.get(userKey);
        if (user != null && user.getString(USER_PWD).equals(DigestUtils.sha512Hex(data.password))) {

            // Get the date of yesterday
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            Timestamp yesterday = Timestamp.of(cal.getTime());

            Query<Entity> query = Query.newEntityQueryBuilder()
                    .setKind("UserLog")
                    .setFilter(
                            CompositeFilter.and(
                                    PropertyFilter.hasAncestor(
                                            datastore.newKeyFactory().setKind("User").newKey(data.username)),
                                    PropertyFilter.ge(USER_LOGIN_TIME, yesterday)))
                    .setOrderBy(OrderBy.desc(USER_LOGIN_TIME))
                    .setLimit(3)
                    .build();
            QueryResults<Entity> logs = datastore.run(query);

            List<Date> loginDates = new ArrayList<Date>();
            logs.forEachRemaining(userlog -> {
                loginDates.add(userlog.getTimestamp(USER_LOGIN_TIME).toDate());
            });


            return Response.ok(g.toJson(loginDates)).build();
        }
        return Response.status(Status.FORBIDDEN).entity(MESSAGE_INVALID_CREDENTIALS)
                .build();
    }

    @POST
    @Path("/user/pagination")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLatestLogins(@QueryParam("next") String nextParam, LoginData data) {

        int next;

        // Checking for valid request parameter values
        try {
            next = Integer.parseInt(nextParam);
            if (next < 0)
                return Response.status(Status.BAD_REQUEST).entity(MESSAGE_NEXT_PARAMETER_INVALID).build();
        } catch (NumberFormatException e) {
            return Response.status(Status.BAD_REQUEST).entity(MESSAGE_NEXT_PARAMETER_INVALID).build();
        }

        Key userKey = userKeyFactory.newKey(data.username);

        Entity user = datastore.get(userKey);
        if (user != null && user.getString(USER_PWD).equals(DigestUtils.sha512Hex(data.password))) {

            // Get the date of yesterday
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            Timestamp yesterday = Timestamp.of(cal.getTime());

            Query<Entity> query = Query.newEntityQueryBuilder()
                    .setKind("UserLog")
                    .setFilter(
                            CompositeFilter.and(
                                    PropertyFilter.hasAncestor(
                                            datastore.newKeyFactory().setKind("User").newKey(data.username)),
                                    PropertyFilter.ge(USER_LOGIN_TIME, yesterday)))
                    .setOrderBy(OrderBy.desc(USER_LOGIN_TIME))
                    .setLimit(3)
                    .setOffset(next)
                    .build();
            QueryResults<Entity> logs = datastore.run(query);

            List<Date> loginDates = new ArrayList<Date>();
            logs.forEachRemaining(userlog -> {
                loginDates.add(userlog.getTimestamp(USER_LOGIN_TIME).toDate());
            });

            return Response.ok(g.toJson(loginDates)).build();
        }
        return Response.status(Status.FORBIDDEN).entity(MESSAGE_INVALID_CREDENTIALS)
                .build();
    }


}
