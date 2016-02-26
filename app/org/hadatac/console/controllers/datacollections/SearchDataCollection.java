package org.hadatac.console.controllers.datacollections;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.views.html.datacollections.datacollection_browser;
import org.hadatac.data.model.AcquisitionQueryResult;
import org.hadatac.entity.pojo.Measurement;

import com.fasterxml.jackson.databind.ObjectMapper;

import play.mvc.Controller;
import play.mvc.Result;

public class SearchDataCollection extends Controller {

	public static Result index(int page, int rows, String facets) {
		ObjectMapper mapper = new ObjectMapper();
    	
    	List<String> permissions = getPermissions(session().get("user_hierarchy"));
    	
    	FacetHandler handler = null;
    	try {
    		handler = mapper.readValue(facets, FacetHandler.class);
    	} catch (Exception e) {
    		handler = new FacetHandler();
    		System.out.println("mapper.readValue: " + e.getMessage());
    	}
    	
    	AcquisitionQueryResult results = Measurement.find(page, rows, permissions, handler);
    	
    	return ok(datacollection_browser.render(results, results.toJSON(), handler.toJSON()));
	}
	
	public static Result postIndex(int page, int rows, String facets) {
		return null;
	}
	
	public static List<String> getPermissions(String permissions) {
    	List<String> result = new ArrayList<String>();
    	
    	if (permissions != null) {
	    	StringTokenizer tokens = new StringTokenizer(permissions, ",");
	    	while (tokens.hasMoreTokens()) {
	    		result.add(tokens.nextToken());
	    	}
    	}
    	
    	return result;
    }
}
