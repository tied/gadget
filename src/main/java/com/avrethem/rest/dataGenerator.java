package com.avrethem.rest;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.query.Query;


import com.avrethem.servlet.searchServlet;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;


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
            System.out.println("[SYSTEM] filterId: " + filterIdString);
            filterIdString = filterIdString.split("filter-")[1];
            System.out.println("[SYSTEM] filterId: " + filterIdString);

            List<Issue> issues = searchServlet.getIssuesInFilter(user, filterIdString);

            //List<Issue> issues = searchServlet.getIssuesInFilterBackInTime(user, filterIdString, timePeriodString);
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();

            try {
            for (Issue issue : issues) {
                //System.out.println("[SYSTEM] Got Issue:" + issue.getKey() );
                JSONObject jsonItem = new JSONObject();
                //StatusManager statusManager;
                //Response.Status status = statusManager.getStatus(issue.getStatusId());

                    jsonItem.put("created", issue.getCreated());
                    //jsonItem.put("resolutionDate", (issue.getResolutionDate() == null) ? "none" : issue.getResolutionDate());
                    //jsonItem.put("resolution", (issue.getResolution() == null) ? "none" : issue.getResolution());
                    //jsonItem.put("statusId", issue.getStatusId());
                    jsonItem.put("status", issue.getStatusObject().getName());

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



    @GET
    @AnonymousAllowed
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getDatesetByStatusAndByDate")
    public Response getDatesetByStatusAndByDate(@QueryParam("filterId") String filterIdString,
                                                @QueryParam("timePeriod") String timePeriodString,
                                                @QueryParam("statusByName") String statusString)
    {
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();

        try {
            filterIdString = filterIdString.split("filter-")[1];


            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();

            try {
                int iterations = Integer.parseInt(timePeriodString.substring(0,(timePeriodString.length()-1)));
                String timeUnit = timePeriodString.substring((timePeriodString.length()-1), (timePeriodString.length()));
                System.out.println("[MEESSAGE] status: " + statusString);
                System.out.println("[MEESSAGE] timeUnit: " + timeUnit);
                for (int i=iterations; i >= 0; i--) {

                    JSONObject jsonItem = new JSONObject();
                    String backInTime = Integer.toString(i) + timeUnit;
                    List<Issue> issues = searchServlet.getIssuesByFilterAndByStatusBackInTime(user, filterIdString, statusString, backInTime);

                    jsonItem.put("backintime", backInTime);
                    jsonItem.put("total", Integer.toString(issues.size()) );

                    jsonArray.put(jsonItem);
                }
                jsonObject.put(("totals"), jsonArray);
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