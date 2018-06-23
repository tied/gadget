package com.avrethem.rest;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
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
import java.text.SimpleDateFormat;
import java.util.*;


/*
 A resource of message.
 */
@Path("/message")
public class dataGenerator {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(dataGenerator.class);
    private String filterId;
    private String keyStatus;
    private SimpleDateFormat longDateFormat;
    private SimpleDateFormat shortDateFormat;

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
                                                @QueryParam("firstDate") String currentDateString,
                                                @QueryParam("statusByName") String searchStatusString)
    {
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        longDateFormat  = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        shortDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date firstDate = null;
        try {
            filterId = filterIdString.split("filter-")[1];
            firstDate = longDateFormat.parse(currentDateString);
        } catch (Exception e) {
            System.out.println("Invalid filter string");
        }



        keyStatus = searchStatusString;


        // Build Accumulative map, each entry counts
        // How many issues were opened on date, How many issues were closed on date
        //  TreeMap< String date, UtilPair< int open, int closed>>
        TreeMap<String, UtilPair> m = new TreeMap<String, UtilPair>();

        try {
            String jqlString = searchServlet.getQueryStringbyFilter(user, filterId);
            jqlString += " AND (createdDate > \"" + longDateFormat.format(firstDate) +"\" OR (status CHANGED AFTER \"" + longDateFormat.format(firstDate) + "\"))";

            List<Issue> issues = searchServlet.getIssuesByQueryString(user, jqlString);
            listToMap(issues, m, firstDate);
        } catch (SearchException e) {
            e.printStackTrace();
        }

        UtilPair startVal = getIssueCountPriorToDate(user, firstDate);

        UtilPair pair = m.containsKey(shortDateFormat.format(firstDate)) ? m.get(shortDateFormat.format(firstDate)) : new UtilPair();
        m.put(shortDateFormat.format(firstDate), pair.add(startVal.open, startVal.closed));

        return buildAccumulativeJSON(m, true);
    }

    public UtilPair getIssueCountPriorToDate(ApplicationUser user, Date firstDate)
    {
        // Set start value of firstDate, because of imported projects showing wrong results
        int openBefore = 0;
        int closedBefore = 0;
        try {
            String jqlString = searchServlet.getQueryStringbyFilter(user, filterId);
            jqlString += " AND createdDate <= \"" + longDateFormat.format(firstDate) + "\" AND STATUS WAS \"" + keyStatus + "\" ON \"" + longDateFormat.format(firstDate) + "\"";
            List<Issue> issues_closed = searchServlet.getIssuesByQueryString(user, jqlString);

            closedBefore = issues_closed.size();

            jqlString = searchServlet.getQueryStringbyFilter(user, filterId);
            jqlString += " AND createdDate <= \"" + longDateFormat.format(firstDate) + "\" AND STATUS WAS NOT \"" + keyStatus + "\" ON \"" + longDateFormat.format(firstDate) + "\"";
            List<Issue> issues_open = searchServlet.getIssuesByQueryString(user, jqlString);

            openBefore = issues_open.size() + closedBefore;


        }catch (SearchException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Open: " + openBefore);
        System.out.println("Closed: " + closedBefore);

        return new UtilPair(openBefore, closedBefore);
    }

    public void listToMap(List<Issue> issues, TreeMap<String, UtilPair> m, Date firstDate)
    {
        System.out.println(" LOG: " + longDateFormat.format(firstDate) );

        for (Issue issue : issues) {
            //String issueDateLong = issue.getCreated().toString().substring(0, 16);
            //String issueDate = issueDateLong.substring(0, 10);
            Date issueDate = null;
            try {
                issueDate = longDateFormat.parse(issue.getCreated().toString().substring(0, 16));
            } catch (Exception e) {
                e.printStackTrace();
            }


            System.out.println("----------------------------------------------------------------------------------------------------------");
            System.out.println("Found new issue created\t" + longDateFormat.format(issueDate) + "\t\t" + "---------- " + issue.getKey() + " --------");
            //System.out.println("ResolutionDate \t\t" + ((issue.getResolutionDate() == null) ? "No resolutionDate" : issue.getResolutionDate().toString().substring(0, 16)) );
            //System.out.println("Resolution \t\t" + ((issue.getResolution() == null) ? "No resolution" : issue.getResolution().toString().substring(0, 16)) );
            //System.out.println("Resolution ID \t\t" + ((issue.getResolutionId() == null) ? "No resolutionID" : issue.getResolutionId()) );
            System.out.println("FirstDate is : \t\t " + longDateFormat.format(firstDate));
            System.out.println("Summary : " + ((issue.getSummary() == null) ? "No-summary" : issue.getSummary()) );


            if ( issueDate.after(firstDate) ) {
                System.out.println("## Add open issue \t[" + shortDateFormat.format(issueDate) + "]");
                UtilPair pair = m.containsKey(shortDateFormat.format(issueDate)) ? m.get(shortDateFormat.format(issueDate)) : new UtilPair();
                m.put(shortDateFormat.format(issueDate), pair.add(1, 0));
            }

            ChangeHistoryManager changeHistoryManager = ComponentAccessor.getChangeHistoryManager();
            List<ChangeItemBean> changeItemBeans = changeHistoryManager.getChangeItemsForField(issue, IssueFieldConstants.STATUS);

            if ( changeItemBeans.isEmpty() )
                System.out.println("--- No-history-issue" + "\t " + longDateFormat.format(issueDate));
                System.out.println(" In Status : \t" + issue.getStatus() + "\t\t @" + issue.getKey());

            for (ChangeItemBean c : changeItemBeans) {

                Date beanDate = null;
                try {
                    beanDate = longDateFormat.parse(c.getCreated().toString().substring(0, 16));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.println("----------- New Bean transistion" + longDateFormat.format(beanDate) + "\t\t\t\t" + c.getFromString() + "\t -> \t" + c.getToString());
                if ( beanDate.after(firstDate) ) {

                    System.out.println("Transition happend: " + "\t " + longDateFormat.format(beanDate) + " \t\t" + c.getFromString() + "\t ->\t" + c.getToString());

                    UtilPair pair = m.containsKey(shortDateFormat.format(beanDate)) ? m.get(shortDateFormat.format(beanDate)) : new UtilPair();

                    if (c.getToString().equals(keyStatus) ) {
                        m.put(shortDateFormat.format(beanDate), pair.add(0, 1));
                        System.out.println(">>> " + keyStatus + " isssue   \t[" + shortDateFormat.format(beanDate) + "]\t\t@ ");
                    }
                    if ( c.getFromString().equals(keyStatus)  ) {
                        m.put(shortDateFormat.format(beanDate), pair.sub(0, 1));
                        System.out.println("<<< Re-" + keyStatus + " isssue\t[" + shortDateFormat.format(beanDate) + "]\t\t@ ");
                    }
                }

            }

        }
    }

    public Response buildAccumulativeJSON(TreeMap<String, UtilPair> m, boolean accumulative )
    {
        int totOpen = 0;
        int totClosed = 0;
        try {
            // Build Json
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();

            for(Map.Entry<String, UtilPair> entry : m.entrySet()) {
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

            return Response.ok(jsonObject.toString(),  MediaType.APPLICATION_JSON).build();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Response.serverError().build();
    }

    public String getJqlString(Query query) {
        return ComponentAccessor.getComponentOfType(SearchService.class).getJqlString(query);
    }

}