package com.avrethem.rest;

import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.atlassian.query.Query;
import com.atlassian.sal.api.component.ComponentLocator;
import com.avrethem.servlet.searchServlet;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


/*
 A resource of message.
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
    public Response getDatasetFromQuery(@QueryParam("filterId") String filterIdString,
                                        @QueryParam("timePeriod") String timePeriodString)
    {
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();

        try {
            //System.out.println("[SYSTEM] filterId: " + filterIdString);
            filterIdString = filterIdString.split("filter-")[1];
            //System.out.println("[SYSTEM] filterId: " + filterIdString);

            List<Issue> issues = searchServlet.getIssuesInFilterBackInTime(user, filterIdString, timePeriodString);
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();

            try {
            for (Issue issue : issues) {
                //System.out.println("[SYSTEM] Got Issue:" + issue.getKey() );
                JSONObject jsonItem = new JSONObject();

                    jsonItem.put("created", issue.getCreated());
                    jsonItem.put("resolutionDate", (issue.getResolutionDate() == null) ? "none" : issue.getResolutionDate());
                    jsonItem.put("resolution", (issue.getResolution() == null) ? "none" : issue.getResolution());
                    jsonItem.put("status", issue.getStatusId());

                jsonArray.put(jsonItem);
            }
            jsonObject.put("issues", jsonArray);
            return Response.ok(jsonObject.toString(),  MediaType.APPLICATION_JSON).build();

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (SearchException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return Response.serverError().build();
    }


    public String getJqlString(Query query) {
        return ComponentAccessor.getComponentOfType(SearchService.class).getJqlString(query);
    }

}