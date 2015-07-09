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


public class Instrument extends Controller {
	
	// for /metadata HTTP GET requests
    public static Result index() {
        SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
	    String[] tabsToQuery = {"InstrumentModels", "InstrumentModelsH"}; 

    	TreeMap<String, SparqlQueryResults> query_results_list = new TreeMap<String, SparqlQueryResults>();
        TreeMap<String, String> hierarchy_results_list = new TreeMap<String, String>();
        for (String tabName : tabsToQuery){
            String query_json = null;
            if (tabName.endsWith("H")) {
                System.out.println("Instrument.java is requesting: " + tabName);
                try {
                    query_json = query_submit.executeQuery(tabName);
                    TreeQueryResults query_results = new TreeQueryResults(query_json, false);
                    hierarchy_results_list.put(tabName, query_results.getQueryResult().replace("\n", " "));
                } catch (IllegalStateException | IOException | NullPointerException e1) {
                    return notFound(error_page.render(e1.toString(), "InstrumentModelsH"));
                    //e1.printStackTrace();
                }
            } else {
                try {
                    query_json = query_submit.executeQuery(tabName);
                    SparqlQueryResults query_results = new SparqlQueryResults(query_json, tabName);
                    query_results_list.put(tabName, query_results);
                } catch (IllegalStateException | IOException | NullPointerException e1) {
                    return notFound(error_page.render(e1.toString(), "InstrumentModelsH"));
                    //e1.printStackTrace();
                }
            }
        }// /for tabName
        System.out.println("Instrument index() was called!");
        //String tree_query_result = tq.getQueryResult().replace("\n", " ");
        return ok(hierarchy_browser.render(query_results_list, hierarchy_results_list, "InstrumentModelsH"));
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex() {        
        SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
	    String[] tabsToQuery = {"InstrumentModels", "InstrumentModelsH"}; 

    	TreeMap<String, SparqlQueryResults> query_results_list = new TreeMap<String, SparqlQueryResults>();
        TreeMap<String, String> hierarchy_results_list = new TreeMap<String, String>();
        for (String tabName : tabsToQuery){
            String query_json = null;
            if (tabName.endsWith("H")) {
                System.out.println("Instrument.java is requesting: " + tabName);
                try {
                    query_json = query_submit.executeQuery(tabName);
                    TreeQueryResults query_results = new TreeQueryResults(query_json, false);
                    hierarchy_results_list.put(tabName, query_results.getQueryResult().replace("\n", " "));
                } catch (IllegalStateException | IOException | NullPointerException e1) {
                    return notFound(error_page.render(e1.toString(), "InstrumentModelsH"));
                    //e1.printStackTrace();
                }
            } else {
                try {
                    query_json = query_submit.executeQuery(tabName);
                    SparqlQueryResults query_results = new SparqlQueryResults(query_json, tabName);
                    query_results_list.put(tabName, query_results);
                } catch (IllegalStateException | IOException | NullPointerException e1) {
                    return notFound(error_page.render(e1.toString(), "InstrumentModelsH"));
                    //e1.printStackTrace();
                }
            }// /else
        }// /for tabName
        System.out.println("Instrument postIndex() was called!");
        //String tree_query_result = tq.getQueryResult().replace("\n", " ");
        return ok(hierarchy_browser.render(query_results_list, hierarchy_results_list, "InstrumentModelsH"));
    }// /postIndex()
}