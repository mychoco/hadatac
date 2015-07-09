package controllers;

import http.GetSparqlQuery;

import java.io.IOException;
import java.util.TreeMap;

import models.SparqlQuery;
import models.SparqlQueryResults;
//import models.TreeQuery;
import models.TreeQueryResults;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.hierarchy_browser;
import views.html.error_page;


public class Unit extends Controller {
	
	// for /metadata HTTP GET requests
    public static Result index() {
        SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
	    String[] tabsToQuery = {"Units", "UnitsH"}; 

    	TreeMap<String, SparqlQueryResults> query_results_list = new TreeMap<String, SparqlQueryResults>();
        TreeMap<String, String> hierarchy_results_list = new TreeMap<String, String>();
        for (String tabName : tabsToQuery){
            String query_json = null;
            if (tabName.endsWith("H")) {
                System.out.println("Unit.java is requesting: " + tabName);
                try {
                    query_json = query_submit.executeQuery(tabName);
                } catch (IllegalStateException | IOException e1) {
                    return notFound(error_page.render(e1.toString()));
                    //e1.printStackTrace();
                }
                TreeQueryResults query_results = new TreeQueryResults(query_json, false);
                hierarchy_results_list.put(tabName, query_results.getQueryResult().replace("\n", " "));
            } else {
                try {
                    query_json = query_submit.executeQuery(tabName);
                } catch (IllegalStateException | IOException e1) {
                    return notFound(error_page.render(e1.toString()));
                    //e1.printStackTrace();
                }
                //System.out.println(query_json);
                SparqlQueryResults query_results = new SparqlQueryResults(query_json, tabName);
                query_results_list.put(tabName, query_results);
            }// /else
        }// /for tabName
        System.out.println("Unit index() was called!");
        //String tree_query_result = tq.getQueryResult().replace("\n", " ");
        return ok(hierarchy_browser.render(query_results_list, hierarchy_results_list, "UnitsH"));
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex() {
        SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
	    String[] tabsToQuery = {"Units", "UnitsH"}; 

    	TreeMap<String, SparqlQueryResults> query_results_list = new TreeMap<String, SparqlQueryResults>();
        TreeMap<String, String> hierarchy_results_list = new TreeMap<String, String>();
        for (String tabName : tabsToQuery){
            String query_json = null;
            if (tabName.endsWith("H")) {
                System.out.println("Unit.java is requesting: " + tabName);
                try {
                    query_json = query_submit.executeQuery(tabName);
                } catch (IllegalStateException | IOException e1) {
                    return notFound(error_page.render(e1.toString()));
                    //e1.printStackTrace();
                }
                TreeQueryResults query_results = new TreeQueryResults(query_json, false);
                hierarchy_results_list.put(tabName, query_results.getQueryResult().replace("\n", " "));
            } else {
                try {
                    query_json = query_submit.executeQuery(tabName);
                } catch (IllegalStateException | IOException e1) {
                    return notFound(error_page.render(e1.toString()));
                    //e1.printStackTrace();
                }
                //System.out.println(query_json);
                SparqlQueryResults query_results = new SparqlQueryResults(query_json, tabName);
                query_results_list.put(tabName, query_results);
            }// /else
        }// /for tabName
        System.out.println("Unit postIndex() was called!");
        //String tree_query_result = tq.getQueryResult().replace("\n", " ");
        return ok(hierarchy_browser.render(query_results_list, hierarchy_results_list, "All Documents"));
    }// /postIndex()

}