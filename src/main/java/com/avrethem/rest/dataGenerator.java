package com.avrethem.rest;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import com.atlassian.query.Query;
import com.avrethem.servlet.searchServlet;
/**
 * A resource of message.
 */
@Path("/message")
public class dataGenerator {

    @GET
    @AnonymousAllowed
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/testmessage")
    public Response getMessage()
    {
       return Response.ok(new dataGeneratorModel("Hello World")).build();
    }

    @GET
    @AnonymousAllowed
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getDatasetFromQuery")
    public Response getDatasetFromQuery(@QueryParam("filterId") String filterIdString)
    {
        String query = "project = DEMO";
        Query conditionQuery = jqlQueryParser.parseQuery(query);
        SearchResults results = searchService.search(jiraAuthenticationContext.getUser(), query, PagerFilter.getUnlimitedFilter());

        return Response.ok(new String("Error")).build();

    }



    @GET
    @AnonymousAllowed
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getFromsearchServlet")
    public Response getFromsearchServlet(@QueryParam("filterId") String filterIdString)
    {
        try {
            ApplicationUser loggedInUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
            List<Issue> issues = searchServlet.getIssuesInQuery(loggedInUser);
            return Response.ok(issues).build();
        } catch (SearchException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return Response.ok(new String("Error")).build();

    }
}