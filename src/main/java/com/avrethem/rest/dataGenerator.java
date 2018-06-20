package com.avrethem.rest;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.util.ofbiz.GenericValueUtils;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.query.Query;


import com.avrethem.servlet.searchServlet;
import com.avrethem.UtilPair;

import javafx.util.Pair;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/*
 A resource of message.
 */
@Path("/message")
public class dataGenerator {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(dataGenerator.class);

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
            filterIdString = filterIdString.split("filter-")[1];
            //log.info("[SYSTEM] filterId: " + filterIdString);

            List<Issue> issues = searchServlet.getIssuesInFilter(user, filterIdString);
            //List<Issue> issues = searchServlet.getIssuesInFilterBackInTime(user, filterIdString, timePeriodString);

            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();

            try {
                for (Issue issue : issues) {
                    //log.info("[SYSTEM] Got Issue:" + issue.getKey() );
                    JSONObject jsonItem = new JSONObject();

                    jsonItem.put("created", issue.getCreated());
                    //jsonItem.put("resolutionDate", (issue.getResolutionDate() == null) ? "none" : issue.getResolutionDate());
                    //jsonItem.put("resolution", (issue.getResolution() == null) ? "none" : issue.getResolution());
                    //jsonItem.put("statusId", issue.getStatusId());
                    //jsonItem.put("status", issue.getStatusObject().getName());
                    /*
    //Possible way to extract info on Issue history
                List<ChangeHistory> changes = ComponentAccessor.getChangeHistoryManager().getChangeHistories(issue);
                ChangeHistoryManager changeHistoryManager = ComponentAccessor.getChangeHistoryManager();
                List<ChangeItemBean> changeItemBeans= changeHistoryManager.getChangeItemsForField(issue, "status");
                for ( ChangeItemBean c : changeItemBeans ) {
                    //log.info("[Issue]: " + issue.getKey() + " "+ c.getField() + ":" + c.getToString() + " " + c.getCreated() );
                    log.info("[Issue]: " + issue.getKey() + "\t " + c.getCreated() + " \t from: " + c.getFrom() + "\t to:" + c.getTo()  );
                }*/


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

        // Build map
        TreeMap<String, UtilPair> m = new TreeMap<String, UtilPair>();

        try {
            filterIdString = filterIdString.split("filter-")[1];
            //log.info("[SYSTEM] filterId: " + filterIdString);

            List<Issue> issues = searchServlet.getIssuesInFilter(user, filterIdString);
            synchronized (issues) {
                for (Issue issue : issues) {
                    String issueDate = issue.getCreated().toString().substring(0, 10);
                    UtilPair pair = m.containsKey(issueDate) ? m.get(issueDate) : new UtilPair();
                    m.put(issueDate, pair.add(1, 0));

                    ChangeHistoryManager changeHistoryManager = ComponentAccessor.getChangeHistoryManager();
                    List<ChangeItemBean> changeItemBeans = changeHistoryManager.getChangeItemsForField(issue, "status");
                    synchronized (m) {
                        for (ChangeItemBean c : changeItemBeans) {
                            System.out.println("[Issue]: " + issue.getKey() + "\t " + c.getCreated() + " \t from: " + c.getFromString() + "\t to: " + c.getToString());
                            //System.out.println( " status: " + statusString );
                            // If a issue comes from 'Closed' map[Date]-- && issue goes to 'Closed' map[Date]++
                            issueDate = c.getCreated().toString().substring(0, 10);
                            pair = m.containsKey(issueDate) ? m.get(issueDate) : new UtilPair();
                            if (c.getToString().equals(statusString))
                                m.put(issueDate, pair.add(0, 1));

                            if (c.getFromString().equals(statusString))
                                m.put(issueDate, pair.sub(0, 1));


                        }
                    }
                }
                }
        } catch (SearchException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Build Json
        try {
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();

                int totOpen = 0;
                int totClosed = 0;

                for(Map.Entry<String, UtilPair> entry : m.entrySet()) {
                    JSONObject jsonItem = new JSONObject();

                    totOpen += entry.getValue().open;
                    totClosed += entry.getValue().closed;

                    jsonItem.put("issuedate", entry.getKey());
                    jsonItem.put("opened", totOpen);
                    jsonItem.put("closed", totClosed);

                    jsonArray.put(jsonItem);

                  //  System.out.println(entry.getKey() + " Open: " + entry.getValue().open + " " + statusString + ": " + entry.getValue().closed);
                }
                jsonObject.put("issues", jsonArray);

                //If successfull
                return Response.ok(jsonObject.toString(),  MediaType.APPLICATION_JSON).build();

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // If failing
        return Response.serverError().build();
    }


    public String getJqlString(Query query) {
        return ComponentAccessor.getComponentOfType(SearchService.class).getJqlString(query);
    }

}