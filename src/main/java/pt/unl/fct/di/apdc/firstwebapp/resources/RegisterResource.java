package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreException;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData;

@Path("/register")
public class RegisterResource {

	private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());
	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	private final Gson g = new Gson();


	public RegisterResource() {
	}    // Default constructor, nothing to do

	@POST
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response registerUserV1(LoginData data) {
		LOG.fine("Attempt to register user: " + data.username);

		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		Entity user = Entity.newBuilder(userKey)
				.set("user_pwd", DigestUtils.sha512Hex(data.password))
				.set("user_creation_time", Timestamp.now())
				.build();
		datastore.put(user);
		LOG.info("User registered " + data.username);
		return Response.ok().entity(g.toJson(true)).build();
	}

	@POST
	@Path("/v2")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response registerUserV2(RegisterData data) {
		LOG.fine("Attempt to register user: " + data.username);

		if (!data.validRegistration())
			return Response.status(Status.BAD_REQUEST).entity("Missing or wrong parameter.").build();

		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		Entity user = datastore.get(userKey);

		if (user != null)
			return Response.status(Status.BAD_REQUEST).entity("User already exists.").build();

		user = Entity.newBuilder(userKey)
				.set("user_name", data.name)
				.set("user_pwd", DigestUtils.sha512Hex(data.password))
				.set("user_email", data.email)
				.set("user_creation_time", Timestamp.now())
				.build();

		// Concurrency problem...
		// When we reach here, another client might have put() an entity with the same key...

		datastore.put(user);
		LOG.info("User registered " + data.username);


		return Response.ok().build();
	}

	@POST
	@Path("/v3")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response registerUserV3(RegisterData data) {
		LOG.fine("Attempt to register user: " + data.username);

		if (!data.validRegistration()) {
			return Response.status(Status.BAD_REQUEST).entity("Missing or wrong parameter.").build();
		}

		Transaction txn = datastore.newTransaction();
		try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
			Entity user = txn.get(userKey);

			// If the entity does not exist null is returned...
			if (user != null) {
				txn.rollback();
				return Response.status(Status.CONFLICT).entity("User already exists.").build();
			} else {
				// ... otherwise
				user = Entity.newBuilder(userKey).set("user_name", data.name)
						.set("user_pwd", DigestUtils.sha512Hex(data.password)).set("user_email", data.email)
						.set("user_creation_time", Timestamp.now()).build();
				// get() followed by put() inside a transaction is ok...
				txn.put(user);
				txn.commit();
				LOG.info("User registered " + data.username);
				return Response.ok().build();
			}
		} catch (DatastoreException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.toString()).build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}

	@POST
	@Path("/v4")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response registerUserV4(RegisterData data) {
		createRootUserIfMissing();
		LOG.fine("Attempt to register user: " + data.username);

		if (!data.validRegistration()) {
			return Response.status(Status.BAD_REQUEST).entity("Missing or wrong parameter.").build();
		}


		try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
			if (datastore.get(userKey) != null) {
				return Response.status(Status.CONFLICT).entity("Username already exists.").build();
			}

			Entity.Builder userBuilder = Entity.newBuilder(userKey)
					.set("user_name", data.name)
					.set("user_email", data.email)
					.set("user_pwd", DigestUtils.sha512Hex(data.password))
					.set("user_phone", data.phone)
					.set("user_profile", data.profile)
					.set("user_role", data.role)
					.set("user_state", data.state)
					.set("user_creation_time", Timestamp.now());

			// Optional fields
			if (data.citizenCard != null) userBuilder.set("user_cc", data.citizenCard);
			if (data.nif != null) userBuilder.set("user_nif", data.nif);
			if (data.employer != null) userBuilder.set("user_employer", data.employer);
			if (data.job != null) userBuilder.set("user_job", data.job);
			if (data.address != null) userBuilder.set("user_address", data.address);
			if (data.employerNif != null) userBuilder.set("user_employer_nif", data.employerNif);
			if (data.photo != null) userBuilder.set("user_photo", data.photo); // base64

			datastore.put(userBuilder.build());
			LOG.info("User registered successfully: " + data.username);

			return Response.ok().entity("User registered successfully!").build();
		} catch (DatastoreException e) {
			LOG.log(Level.SEVERE, e.toString());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Internal error").build();
		}
	}

	private void createRootUserIfMissing() {
		Key rootKey = datastore.newKeyFactory().setKind("User").newKey("root");

		if (datastore.get(rootKey) == null) {
			Entity rootUser = Entity.newBuilder(rootKey)
					.set("user_name", "System Administrator")
					.set("user_email", "root@system.admin")
					.set("user_pwd", DigestUtils.sha512Hex("RootAdmin123!"))
					.set("user_phone", "+351000000000")
					.set("user_profile", "private")
					.set("user_role", "ADMIN")
					.set("user_state", "ACTIVATED")
					.set("user_creation_time", Timestamp.now())
					.build();

			datastore.put(rootUser);
			LOG.info("🌱 Root user was created.");
		}
	}
}