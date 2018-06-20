package com.avrethem.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.issue.search.SearchService.IssueSearchParameters;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.query.order.SortOrder;

import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.component.ComponentAccessor;

public class searchServlet extends HttpServlet{
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(searchServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ApplicationUser loggedInUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();

        resp.setContentType("text/html");
        PrintWriter writer = resp.getWriter();

        writer.println("This is how a servlet module works... <br><br>");
        writer.println("<br>1<br><br>");
        writer.println("<br>2<br>");
        writer.println("<br>3<br><br>");
        writer.println("<br>4<br><br>");
        writer.println("<br>5");
    }


    static public List<Issue> getIssuesInQuery(ApplicationUser user, String jqlQuery ) throws SearchException {
        SearchService searchService = ComponentAccessor.getComponent(SearchService.class);
        SearchService.ParseResult parseResult = searchService.parseQuery(user, jqlQuery);

        if (parseResult.isValid()) {
            Query query = parseResult.getQuery();
            SearchResults results = searchService.search(user, query, PagerFilter.getUnlimitedFilter());
            return results.getIssues();
        } else {
            log.error("[issues-metric]@searchServlet::getIssueInQuery] Error parsing query:" + jqlQuery);
            return Collections.emptyList();
        }
    }

    static public List<Issue> getIssuesInFilter(ApplicationUser user, String filterId ) throws SearchException {
        SearchService searchService = ComponentAccessor.getComponent(SearchService.class);

        SearchRequestManager srm = ComponentAccessor.getComponentOfType(SearchRequestManager.class);
        SearchRequest filter = srm.getSearchRequestById(user, Long.valueOf(filterId));
        // filterName == filter.getQuery().getQueryString();

        String jqlQuery = filter.getQuery().getQueryString();
        System.out.println("[issues-metric]@searchServlet::getIssuesInFilterBackInTime(...)] Do query: '" + jqlQuery + "'");
            Query query = filter.getQuery();
            SearchResults results = searchService.search(user, query, PagerFilter.getUnlimitedFilter());
            return results.getIssues();
    }

    static public List<Issue> getIssuesInFilterBackInTime(ApplicationUser user, String filterId, String backInTime ) throws SearchException {
        SearchRequestManager srm = ComponentAccessor.getComponentOfType(SearchRequestManager.class);
        SearchRequest filter = srm.getSearchRequestById(user, Long.valueOf(filterId));
        String jqlQuery = filter.getQuery().getQueryString();
        jqlQuery += " AND createdDate >= startOfDay(-" + backInTime + ")";
        log.info("[issues-metric]@searchServlet::getIssuesInFilterBackInTime(...)] Do query: '" + jqlQuery + "'");
        return getIssuesInQuery(user, jqlQuery);
    }

    static public List<Issue> getIssuesByFilterAndByStatusBackInTime(ApplicationUser user, String filterId, String  status, String backInTime ) throws SearchException {
        SearchRequestManager srm = ComponentAccessor.getComponentOfType(SearchRequestManager.class);
        SearchRequest filter = srm.getSearchRequestById(user, Long.valueOf(filterId));
        String jqlQuery = filter.getQuery().getQueryString();
        jqlQuery += " AND status was " + status + " ON endOfDay(-" + backInTime + ")";
        log.info("[issues-metric]@searchServlet::getIssuesInFilterBackInTime(...)] Do query: '" + jqlQuery + "'");
        return getIssuesInQuery(user, jqlQuery);
    }

}