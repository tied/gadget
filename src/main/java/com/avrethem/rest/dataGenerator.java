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
import com.avrethem.utils.IssueMap;
import com.avrethem.utils.UtilPair;

import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/*
 A resource of message.
 */
@Path("/message")
public class dataGenerator {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(dataGenerator.class);
    private static final String LONG_DATE_FORMAT = "yyyy-MM-dd HH:mm";
    private static final String SHORT_DATE_FORMAT = "yyyy-MM-dd";

    private String filterId = "null";
    private String keyStatus = "null";
    private SimpleDateFormat ldf = null;
    private SimpleDateFormat sdf = null;
    private Date firstDate = null;

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
                                                @QueryParam("statusByName") String searchStatusString)
    {
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        ldf = new SimpleDateFormat(LONG_DATE_FORMAT );
        sdf = new SimpleDateFormat(SHORT_DATE_FORMAT);
        keyStatus = searchStatusString;

        try {
            filterId = filterIdString.split("filter-")[1];
            firstDate = ldf.parse(firstDateString);

            // Build Accumulative map, each entry counts
            // How many issues were opened on date, How many issues were closed on date
            //  TreeMap< String date, UtilPair< int open, int closed>>
            IssueMap m = new IssueMap();

            String jqlString = searchServlet.getQueryStringbyFilter(user, filterId);
            jqlString += " AND (createdDate > \"" + ldf.format(firstDate) +"\" OR (status CHANGED AFTER \"" + ldf.format(firstDate) + "\"))";

            List<Issue> issues = searchServlet.getIssuesByQueryString(user, jqlString);

            // Insert values for each day
            for (Issue issue : issues) {
                insertToMap(issue, m);
            }


            // Insert startValue
            UtilPair startVal = getIssueCountBeforeDate(user, firstDate);

            String key = sdf.format(firstDate);
            UtilPair value = m.containsKey(key) ? m.get(key) : new UtilPair();
            m.put(sdf.format(firstDate), value.add(startVal));

            return buildJSON(m, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.serverError().build();
    }


    public UtilPair getIssueCountBeforeDate(ApplicationUser user, Date date)
    {
        // Set start value of firstDate, because of imported projects showing wrong results
        int openBefore = 0;
        int closedBefore = 0;
        try {
            String jqlString = searchServlet.getQueryStringbyFilter(user, filterId);
            jqlString += " AND createdDate <= \"" + ldf.format(date) + "\" AND STATUS WAS \"" + keyStatus + "\" ON \"" + ldf.format(date) + "\"";
            List<Issue> issues_closed = searchServlet.getIssuesByQueryString(user, jqlString);

            closedBefore = issues_closed.size();

            jqlString = searchServlet.getQueryStringbyFilter(user, filterId);
            jqlString += " AND createdDate <= \"" + ldf.format(date) + "\" AND STATUS WAS NOT \"" + keyStatus + "\" ON \"" + ldf.format(date) + "\"";
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


    public void insertToMap(Issue issue, IssueMap m) throws Exception
    {
            Date createdDate = ldf.parse(issue.getCreated().toString().substring(0, 16));

            System.out.println("----------------------------------------------------------------------------------------------------------");
            System.out.println("Found new issue created\t" + ldf.format(createdDate) + "\t\t" + "-------- " + issue.getKey() + " --------");
            //System.out.println("ResolutionDate \t\t" + ((issue.getResolutionDate() == null) ? "No resolutionDate" : issue.getResolutionDate().toString().substring(0, 16)) );
            //System.out.println("Resolution \t\t" + ((issue.getResolution() == null) ? "No resolution" : issue.getResolution().toString().substring(0, 16)) );
            //System.out.println("Resolution ID \t\t" + ((issue.getResolutionId() == null) ? "No resolutionID" : issue.getResolutionId()) );
            System.out.println("FirstDate is :\t\t" + ldf.format(firstDate));
            System.out.println("Summary : " + ((issue.getSummary() == null) ? "No-summary" : issue.getSummary()) );

            ChangeHistoryManager changeHistoryManager = ComponentAccessor.getChangeHistoryManager();
            List<ChangeItemBean> changeItemBeans = changeHistoryManager.getChangeItemsForField(issue, IssueFieldConstants.STATUS);



            if ( createdDate.after(firstDate) ) {
                // Created AFTER firstDate

                m.incrementOpen(sdf.format(createdDate));

                // NO-HISTORY
                if (changeItemBeans.isEmpty()) {
                    System.out.println("--- No-history-issue" + "\t " + ldf.format(createdDate) + "\tIn Status : " + issue.getStatus().getName());
                    if (keyStatus.equals(issue.getStatus().getName())) {
                        m.incrementClosed(sdf.format(createdDate));
                    }
                }
                // HAS HISTORY
                else {
                    //System.out.println("------ New Bean history\t" + changeItemBeans.get(0).getCreated().toString().substring(0, 16) + "   ------------\t" + changeItemBeans.get(0).getFromString() + "\t -> \t" + changeItemBeans.get(0).getToString() + " ---");
                    if ( changeItemBeans.get(0).getFromString().equals(keyStatus) ) {
                        m.incrementClosed(changeItemBeans.get(0).getCreated().toString().substring(0, 10));
                    }

                    for (ChangeItemBean c : changeItemBeans) {
                        Date beanDate = ldf.parse(c.getCreated().toString().substring(0, 16));
                        insertFromHistoryBeans(c, m, beanDate);
                    }


                }
            }







            else {
                // Crated BEFORE firstDate

                for (ChangeItemBean c : changeItemBeans) {
                    Date beanDate = ldf.parse(c.getCreated().toString().substring(0, 16));

                    if (beanDate.after(firstDate)) {
                        insertFromHistoryBeans(c, m, beanDate);
                    }
                }

            }









    }


    /*
    *
    *
     */
    public void insertFromHistoryBeans(ChangeItemBean c, IssueMap m, Date beanDate) throws ParseException
    {
        System.out.println("Transition happend: " + "\t " + ldf.format(beanDate) + " \t\t" + c.getFromString() + "\t ->\t" + c.getToString());
            if (c.getToString().equals(keyStatus) ) {
                m.incrementClosed(sdf.format(beanDate));
            }
            if ( c.getFromString().equals(keyStatus)  ) {
                m.decrementClosed(sdf.format(beanDate));
            }
    }

    public Response buildJSON(TreeMap<String, UtilPair> m, boolean accumulative ) throws JSONException
    {
        int totOpen = 0;
        int totClosed = 0;

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
    }

}