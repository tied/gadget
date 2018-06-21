package com.avrethem.rest;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;

import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.status.Status;
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
import java.util.Date;

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
    @Path("/getDatesetByStatusAndByDate")
    public Response getDatesetByStatusAndByDate(@QueryParam("filterId") String filterIdString,
                                                @QueryParam("timePeriod") String timePeriodString,
                                                @QueryParam("firstDate") String firstDateString,
                                                @QueryParam("statusByName") String statusString)
    {
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        //firstDateString += " 23:59"; // To be sure in searches, also search by minutes
        // Build map
        TreeMap<String, UtilPair> m = new TreeMap<String, UtilPair>();

        try {
            filterIdString = filterIdString.split("filter-")[1];
            //log.info("[SYSTEM] filterId: " + filterIdString);

            // Make Query
            String jqlString = searchServlet.getQueryStringbyFilter(user, filterIdString);
            jqlString += " AND (createdDate >= endOfDay" + firstDateString + "\" OR (status CHANGED AFTER \"" + firstDateString + "\" OR status CHANGED ON \"" + firstDateString + "\"))";

            List<Issue> issues = searchServlet.getIssuesByQueryString(user, jqlString);

            synchronized (issues) {
                for (Issue issue : issues) {

                    String issueDate = issue.getCreated().toString().substring(0, 16);
                    System.out.println("----------------------------------------------------------------------------------------------------------");
                    System.out.println("Found new issue created\t " + issueDate + "\t\t" + issue.getKey() + "------------------");
                    System.out.println("ResolutionDate \t\t" + ((issue.getResolutionDate() == null) ? "No resolutionDate" : issue.getResolutionDate().toString().substring(0, 16)) );
                    System.out.println("Resolution \t\t" + ((issue.getResolution() == null) ? "No resolution" : issue.getResolution().toString().substring(0, 16)) );
                    System.out.println("Resolution ID \t\t" + ((issue.getResolutionId() == null) ? "No resolutionID" : issue.getResolutionId()) );
                    System.out.println("Summary : " + ((issue.getSummary() == null) ? "No-summary" : issue.getSummary()) );
                    System.out.println("FirstDate : \t " + firstDateString );

                    if ( issueDate.compareTo(firstDateString) >= 0 ) {
                        System.out.println("## Add open issue \t[" + issueDate + "]");
                        UtilPair pair = m.containsKey(issueDate) ? m.get(issueDate) : new UtilPair();
                        m.put(issueDate.substring(0, 10), pair.add(1, 0));

                    }

                    ChangeHistoryManager changeHistoryManager = ComponentAccessor.getChangeHistoryManager();
                    List<ChangeItemBean> changeItemBeans = changeHistoryManager.getChangeItemsForField(issue, IssueFieldConstants.STATUS);

                    if (changeItemBeans.isEmpty() && issue.getStatus().getName().equals(statusString) )
                    {
                        System.out.println("--- No-history-issue:" + "\t " + issue.getCreated().toString().substring(0, 16));
                        System.out.println(">>> "+ statusString +" isssue \t\t[" + issue.getCreated().toString().substring(0, 16) + "]\t " + issue.getStatus().getName());

                        UtilPair pair = m.containsKey(issueDate) ? m.get(issueDate) : new UtilPair();
                        m.put(issue.getCreated().toString().substring(0, 10), pair.add(0, 1));
                   }
                    synchronized (m) {
                            for (ChangeItemBean c : changeItemBeans) {
                                synchronized (c) {

                                    issueDate = c.getCreated().toString().substring(0, 16);

                                    //System.out.println("----------- New Bean transistion" + "\t\t\t\t" + c.getFromString() + "\t -> \t" + c.getToString());
                                    if ( issueDate.compareTo(firstDateString) >= 0 ) {
                                        String beanDate = c.getCreated().toString().substring(0,16);
                                        System.out.println("Transition happend: " + "\t " + c.getCreated().toString().substring(0, 16) + " \t\t" + c.getFromString() + "\t ->\t" + c.getToString());

                                        UtilPair pair = m.containsKey(issueDate) ? m.get(issueDate) : new UtilPair();
                                        String resolutionDate = ((issue.getResolutionDate() == null) ? "0000-00-00" : issue.getResolutionDate().toString().substring(0, 16));

                                        if (c.getToString().equals(statusString) && resolutionDate.compareTo(firstDateString) > 0) {
                                            m.put(beanDate.substring(0,10), pair.add(0, 1));
                                            System.out.println(">>> "+ statusString +" isssue \t[" + beanDate + "]\t\t@ ");
                                        }
                                        if (c.getFromString().equals(statusString) && resolutionDate.compareTo(firstDateString) > 0) {
                                            m.put(beanDate.substring(0, 10), pair.sub(0, 1));
                                            System.out.println("<<< Re-"+statusString+" isssue\t[" + beanDate + "]\t\t@ ");
                                        }
                                    } else {
                                        //System.out.println("--- Other transistion:" + "\t " + issueDate + " \t\t" + c.getFromString() + "\t ->\t" + c.getToString());
                                    }
                                }

                            }
                        //}

                    }
                }
                }
        } catch (SearchException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Set start value of firstDate, because of imported projects showing wrong results
        int openBefore = 0;
        int closedBefore = 0;
        try {
            String jqlString = searchServlet.getQueryStringbyFilter(user, filterIdString);
            jqlString += " AND createdDate <  \"" + firstDateString + "\" AND status = \"" + statusString + "\"";
            List<Issue> issues_closed = searchServlet.getIssuesByQueryString(user, jqlString);

            closedBefore = issues_closed.size();

            jqlString = searchServlet.getQueryStringbyFilter(user, filterIdString);
            jqlString += " AND createdDate < \"" + firstDateString + "\" AND status != \"" + statusString + "\"";
            List<Issue> issues_open = searchServlet.getIssuesByQueryString(user, jqlString);

            openBefore = issues_open.size() + closedBefore;


        }catch (SearchException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Open: " + openBefore);
        System.out.println("Closed: " + closedBefore);

       UtilPair pair = m.containsKey(firstDateString) ? m.get(firstDateString) : new UtilPair();
       m.put(firstDateString.substring(0, 10), pair.add(openBefore, closedBefore)); // Insert into first date in map


        // Build Json
        int totOpen = 0;
        int totClosed = 0;
        try {
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();

                for(Map.Entry<String, UtilPair> entry : m.entrySet()) {
                    JSONObject jsonItem = new JSONObject();

                    totOpen += entry.getValue().open;
                    totClosed += entry.getValue().closed;

                    jsonItem.put("issuedate", entry.getKey());
                    jsonItem.put("opened", totOpen);
                    jsonItem.put("closed", totClosed);

                    jsonArray.put(jsonItem);

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