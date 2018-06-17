package com.avrethem.rest;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * A resource of message.
 */
@Path("/message")
public class dataGenerator {

    @GET
    @AnonymousAllowed
    @Produces({MediaType.APPLICATION_JSON)
    public Response getMessage()
    {
       

        return Response.ok(new dataGeneratorModel("Hello World")).build();
    }
}