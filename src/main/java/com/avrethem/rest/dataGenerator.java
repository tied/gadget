package com.avrethem.rest;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.query.Query;


import com.avrethem.servlet.searchServlet;
import com.avrethem.UtilPair;

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
    @Path("/getDatesetByStatusAndByDate")
    public Response getDatesetByStatusAndByDate(@QueryParam("filterId") String filterIdString,
                                                @QueryParam("timePeriod") String timePeriodString,
                                                @QueryParam("firstDate") String firstDateString,
                                                @QueryParam("statusByName") String statusString)
    {
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        firstDateString += " 00:01"; // To be sure in searches, also search by minutes
        // Build map
        TreeMap<String, UtilPair> m = new TreeMap<String, UtilPair>();

        try {
            filterIdString = filterIdString.split("filter-")[1];
            //log.info("[SYSTEM] filterId: " + filterIdString);

            // Make Query
            String jqlString = searchServlet.getQueryStringbyFilter(user, filterIdString);
            jqlString += " AND (createdDate >= startOfDay(-" + timePeriodString + ") OR (status CHANGED AFTER \"" + firstDateString.substring(0, 10) + "\" OR status CHANGED ON \"" + firstDateString.substring(0, 10) + "\"))";

            List<Issue> issues = searchServlet.getIssuesByQueryString(user, jqlString);

            synchronized (issues) {
                for (Issue issue : issues) {

                    String issueDateLongFormat = issue.getCreated().toString().substring(0, 16);
                    String issueDate = issue.getCreated().toString().substring(0, 10);

                    System.out.println("----------------------------------------------------------------------------------------------------------");
                    System.out.println("Found new issue created\t" + issueDate + "\t\t" + issue.getKey() + "------------------");
                    System.out.println("ResolutionDate \t\t" + ((issue.getResolutionDate() == null) ? "No resolutionDate" : issue.getResolutionDate().toString().substring(0, 16)) );
                    System.out.println("Resolution \t\t" + ((issue.getResolution() == null) ? "No resolution" : issue.getResolution().toString().substring(0, 16)) );
                    System.out.println("Resolution ID \t\t" + ((issue.getResolutionId() == null) ? "No resolutionID" : issue.getResolutionId()) );
                    System.out.println("FirstDate : \t\t " + firstDateString );
                    System.out.println("Summary : " + ((issue.getSummary() == null) ? "No-summary" : issue.getSummary()) );



                    if ( issueDateLongFormat.compareTo(firstDateString) >= 0 ) {
                        System.out.println("## Add open issue \t[" + issueDate + "]");
                        UtilPair pair = m.containsKey(issueDate) ? m.get(issueDate) : new UtilPair();
                        m.put(issueDate, pair.add(1, 0));

                    }

                    ChangeHistoryManager changeHistoryManager = ComponentAccessor.getChangeHistoryManager();
                    List<ChangeItemBean> changeItemBeans = changeHistoryManager.getChangeItemsForField(issue, IssueFieldConstants.STATUS);
                    /*
                    if (changeItemBeans.isEmpty() && issue.getStatus().getName().equals(statusString) )
                    {
                        System.out.println("--- No-history-issue:" + "\t " + issue.getCreated().toString().substring(0, 16));
                        System.out.println(">>> "+ statusString +" isssue \t\t[" + issue.getCreated().toString().substring(0, 16) + "]\t " + issue.getStatus().getName());

                        UtilPair pair = m.containsKey(issueDate) ? m.get(issueDate) : new UtilPair();
                        m.put(issue.getCreated().toString().substring(0, 16), pair.add(0, 1));
                   }*/
                    synchronized (m) {
                            for (ChangeItemBean c : changeItemBeans) {
                                synchronized (c) {

                                    String beanDateLongFormat = c.getCreated().toString().substring(0, 16);
                                    String beanDate = c.getCreated().toString().substring(0, 10);

                                    //System.out.println("----------- New Bean transistion" + "\t\t\t\t" + c.getFromString() + "\t -> \t" + c.getToString());
                                    if ( beanDateLongFormat.compareTo(firstDateString) >= 0 ) {
                                        System.out.println("Transition happend: " + "\t " + beanDateLongFormat + " \t\t" + c.getFromString() + "\t ->\t" + c.getToString());

                                        UtilPair pair = m.containsKey(beanDate) ? m.get(beanDate) : new UtilPair();
                                        String resolutionDate = ((issue.getResolutionDate() == null) ? "0000-00-00" : issue.getResolutionDate().toString().substring(0, 16));

                                        if ( resolutionDate.compareTo(firstDateString) > 0 ) {
                                            String resolutionId = ((issue.getResolutionId() == null) ? "1" : issue.getResolutionId());
                                            if (c.getToString().equals(statusString) && !resolutionId.equals("1")) {
                                                m.put(beanDate, pair.add(0, 1));
                                                System.out.println(">>> "+ statusString +" isssue \t[" + beanDate + "]\t\t@ ");
                                            }
                                            if ( c.getFromString().equals(statusString)  ) {
                                                m.put(beanDate, pair.sub(0, 1));
                                                System.out.println("<<< Re-"+statusString+" isssue\t[" + beanDate + "]\t\t@ ");
                                            }
                                        }
                                    }

                                }

                            }

                    }
                }
                }
        } catch (SearchException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        UtilPair startVal = getIssuesAtFirstDate(user, timePeriodString, statusString, filterIdString);

        UtilPair pair = m.containsKey(firstDateString) ? m.get(firstDateString) : new UtilPair();
        m.put(firstDateString.substring(0, 10), pair.add(startVal.open, startVal.closed)); // Insert into first date in map

        return buildAccumulativeJSON(m, true);
    }


    public UtilPair getIssuesAtFirstDate(ApplicationUser user, String timePeriodString, String statusString, String filterIdString)
    {
        // Set start value of firstDate, because of imported projects showing wrong results
        int openBefore = 0;
        int closedBefore = 0;
        try {
            String jqlString = searchServlet.getQueryStringbyFilter(user, filterIdString);
            jqlString += " AND createdDate <  startOfDay(-" + timePeriodString + ") AND status = \"" + statusString + "\"";
            List<Issue> issues_closed = searchServlet.getIssuesByQueryString(user, jqlString);

            closedBefore = issues_closed.size();

            jqlString = searchServlet.getQueryStringbyFilter(user, filterIdString);
            jqlString += " AND createdDate < startOfDay(-" + timePeriodString + ") AND status != \"" + statusString + "\"";
            List<Issue> issues_open = searchServlet.getIssuesByQueryString(user, jqlString);

            openBefore = issues_open.size() + closedBefore;


        }catch (SearchException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Open: " + openBefore);
        System.out.println("Closed: " + closedBefore);

        UtilPair ret = new UtilPair();
        ret.add(openBefore, closedBefore);
        return ret;
    }



    public Response buildAccumulativeJSON(TreeMap<String, UtilPair> m, boolean accumulative )
    {
        // Build Json
        int totOpen = 0;
        int totClosed = 0;
        try {
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();

            for(Map.Entry<String, UtilPair> entry : m.entrySet()) {
                System.out.println(entry.getKey() + " open: " + entry.getValue().open + " closed: " + entry.getValue().closed);
                JSONObject jsonItem = new JSONObject();

                if ( accumulative ) {
                    totOpen += entry.getValue().open;
                    totClosed += entry.getValue().closed;
                }
                else {
                    totOpen = entry.getValue().open;
                    totClosed = entry.getValue().closed;
                }

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