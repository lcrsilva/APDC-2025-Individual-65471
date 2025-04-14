package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pt.unl.fct.di.apdc.firstwebapp.util.WorksheetData;

import java.util.logging.Logger;

@Path("/worksheet")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class WorksheetResource {

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final Gson g = new Gson();
    private static final Logger LOG = Logger.getLogger(WorksheetResource.class.getName());

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createWorksheet(WorksheetData data) {

        if (data == null || data.authToken == null || !data.isValid()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing or invalid data.").build();
        }


        if (!data.isAwardedValid()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing or invalid awarded fields.").build();
        }


        Key tokenKey = datastore.newKeyFactory().setKind("AuthToken").newKey(data.authToken.tokenID);
        Entity tokenEntity = datastore.get(tokenKey);

        if (tokenEntity == null || !tokenEntity.getString("username").equals(data.authToken.username)) {
            return Response.status(Response.Status.FORBIDDEN).entity("Invalid or expired token.").build();
        }


        String role = data.authToken.role;
        if (!(role.equals("ADMIN") || role.equals("BACKOFFICE"))) {
            return Response.status(Response.Status.FORBIDDEN).entity("You don't have permission to create worksheets.").build();
        }


        KeyFactory worksheetKeyFactory = datastore.newKeyFactory().setKind("Worksheet");
        Key worksheetKey = datastore.allocateId(worksheetKeyFactory.newKey());


        Entity.Builder worksheetEntityBuilder = Entity.newBuilder(worksheetKey)
                .set("reference", data.reference)
                .set("description", data.description)
                .set("propertyType", data.propertyType)
                .set("status", data.status)
                .set("createdBy", data.authToken.username)
                .set("timestamp", Timestamp.now());


        if ("AWARDED".equals(data.status)) {
            worksheetEntityBuilder
                    .set("awardDate", data.awardDate)
                    .set("startDate", data.startDate)
                    .set("endDate", data.endDate)
                    .set("partnerAccount", data.partnerAccount)
                    .set("companyName", data.companyName)
                    .set("companyNIF", data.companyNIF)
                    .set("workStatus", data.workStatus)
                    .set("observations", data.observations);
        }


        datastore.put(worksheetEntityBuilder.build());

        LOG.info("Worksheet created by " + data.authToken.username);
        return Response.ok("Worksheet created successfully!").build();
    }
}