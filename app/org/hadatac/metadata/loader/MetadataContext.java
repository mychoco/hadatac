package org.hadatac.metadata.loader;

import java.util.Map;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RiotNotFoundException;
import org.apache.jena.shared.NotFoundException;
import org.hadatac.utils.Collections;
import org.hadatac.utils.Command;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpace;
import org.hadatac.utils.NameSpaces;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import play.Play;

public class MetadataContext implements RDFContext {

    String username = null;
    String password = null;
    String kbURL = null;   
	         // For local use:
	         //   - http://localhost:7574/solr
    boolean verbose = false;

    String processMessage = "";
    String loadFileMessage = "";
	
    public MetadataContext(String un, String pwd, String kb, boolean ver) {
        //System.out.println("Metadata management set for knowledge base at " + kb);
	    username = un;
	    password = pwd;
	    kbURL = kb;
	    verbose = ver;
    }

    public static Long playTotalTriples() {
	     MetadataContext metadata = new MetadataContext("user", 
	    		                                        "password", 
	    		                                        Play.application().configuration().getString("hadatac.solr.triplestore"), 
	    		                                        false);
      return metadata.totalTriples();
    }

    private String executeQuery(String query) throws IllegalStateException, IOException{
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //Scanner in = null;
        try {
        	HttpClient client = new DefaultHttpClient();
         	//System.out.println("Query: <" + kbURL + "/store/sparql?q=" + query + ">");
         	HttpGet request = new HttpGet(kbURL + Collections.METADATA_SPARQL + "?q=" + URLEncoder.encode(query, "UTF-8"));
        	request.setHeader("Accept", "application/sparql-results+xml");
        	HttpResponse response = client.execute(request);
            StringWriter writer = new StringWriter();
            IOUtils.copy(response.getEntity().getContent(), writer, "utf-8");
    
            return writer.toString();
            
        } finally
        {
            //in.close();
            //request.close();
        }
    }// /executeQuery()
    
    public Long totalTriples() {
    	String query = "SELECT (COUNT(*) as ?tot) WHERE { ?s ?p ?o . }";
    	try {
			String result = executeQuery(query);
		    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder builder = factory.newDocumentBuilder();
		    InputSource is = new InputSource(new StringReader(result));
		    Document doc = builder.parse(is);
		    //System.out.println(result);
		    return Long.valueOf(doc.getElementsByTagName("literal").item(0).getTextContent()).longValue();
    	} catch (Exception e) {
			//e.printStackTrace();
			return (long) -1;
		} 
    }
    
	public String clean(int mode) {
	    String message = "";
	    String straux = "";
	    //System.out.println("Is WEB? " + (mode == Feedback.WEB));
        message += Feedback.println(mode,"   Triples before [clean]: " + totalTriples());
        message += Feedback.println(mode, " ");
	    // ATTENTION: For now, it erases entirely the content of the metadata collection 
	    String query1 = "<delete><query>*:*</query></delete>";
	    String query2 = "<commit/>";
	    
	    String url1;
	    String url2;
		try {
		    url1 = Collections.getCollectionsName(Collections.METADATA_UPDATE) + "?stream.body=" + URLEncoder.encode(query1, "UTF-8");
		    url2 = Collections.getCollectionsName(Collections.METADATA_UPDATE) + "?stream.body=" + URLEncoder.encode(query2, "UTF-8");
		    //Runtime.getRuntime().exec("curl -v " + url1);
		    //Runtime.getRuntime().exec("curl -v " + url2);
		    if (verbose) {
		        message += Feedback.println(mode, url1);
		        message += Feedback.println(mode, url2);
		    }
		    String[] cmd1 = {"curl", "-v", url1};
			message += Feedback.print(mode, "    Erasing triples... ");                
		    straux = Command.exec(mode, verbose, cmd1);
		    if (mode == Feedback.WEB) {
		    	message += straux;
		    }
		    message += Feedback.println(mode, "");
			message += Feedback.print(mode, "   Committing... ");                
		    String[] cmd2 = {"curl", "-v", url2};
		    straux = Command.exec(mode, verbose, cmd2);
		    if (mode == Feedback.WEB) {
		    	message += straux;
		    }
		    message += Feedback.println(mode," ");
		    message += Feedback.println(mode," ");
			message += Feedback.print(mode,"   Triples after [clean]: " + totalTriples());                
		} catch (UnsupportedEncodingException e) {
		    System.out.println("[MetadataManagement] - ERROR encoding URLs");
		    //e.printStackTrace();
		    return message;
		}
        return message; 
	}
	
	public String getLang(String contentType) {
		if (contentType.contains("turtle")) {
			return "TTL";
		} else if (contentType.contains("rdf+xml")) {
			return "RDF/XML";
		} else {
			return "";
		}
	}
	
	/* 
	 *   contentType correspond to the mime type required for curl to process the data provided. For example, application/rdf+xml is
	 *   used to process rdf/xml content.
	 *   
	 */
	public Long loadLocalFile(int mode, String filePath, String contentType) {
		Model model = ModelFactory.createDefaultModel();
		DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(kbURL + Collections.METADATA_GRAPH);

		loadFileMessage = "";
		Long total = totalTriples();
		try {
			model.read(filePath, getLang(contentType));
			accessor.add(model);
		} catch (NotFoundException e) {
			System.out.println("NotFoundException: file " + filePath);
			System.out.println("NotFoundException: " + e.getMessage());
		} catch (RiotNotFoundException e) {
			System.out.println("RiotNotFoundException: file " + filePath);
			System.out.println("RiotNotFoundException: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("Exception: file " + filePath);
			System.out.println("Exception: " + e.getMessage());
		}
		
		//loadFileMessage = "";
		//if (verbose) {
		//	System.out.println("curl -v " + kbURL + "/store/update/bulk?commit=true -H \"Content-Type: " + contentType + "\" --data-binary @" + filePath);
		//}
		//String[] cmd = {"curl", "-v", kbURL + "/store/update/bulk?commit=true","-H", "Content-Type: " + contentType, "--data-binary", "@" + filePath};
		//loadFileMessage += executeCommand(mode, cmd);

		Long newTotal = totalTriples();
		return (newTotal - total);
	}
	
	/*
	 *  oper: "confirmed" cache ontologies from the web and load
	 *        "confrmedCache" load from cached ontologies
	 *        "cache" cache ontologies from the web
	 */
	public String loadOntologies(int mode, String oper) {
	    String message = "";
	    Long total = new Long(0);
	    if (!oper.equals("cache")) {
		    total = totalTriples();
		    message += Feedback.println(mode, "   Triples before [loadOntologies]: " + total);
		    message += Feedback.println(mode," ");
	    }
		if (!oper.equals("confirmedCache")) {
		    message += NameSpaces.getInstance().copyNameSpacesLocally(mode);
		}
		if (!oper.equals("cache")) {
		    for (Map.Entry<String, NameSpace> entry : NameSpaces.table.entrySet()) {
	    	    String abbrev = entry.getKey().toString();
	    	    String nsURL = entry.getValue().getURL();
	    	    if ((abbrev != null) && (nsURL != null) && (entry.getValue().getType() != null) && !nsURL.equals("")) {
	    		    String filePath = NameSpaces.CACHE_PATH + "copy" + "-" + abbrev.replace(":","");
	    		    message += Feedback.print(mode, "   Uploading " + filePath);
	    		    for (int i = filePath.length(); i < 50; i++) {
	    			    message += Feedback.print(mode, ".");
	    		    }
	    		    loadLocalFile(mode, filePath, entry.getValue().getType());
	    		    message += loadFileMessage;
	    		    Long newTotal = totalTriples();
	    		    message += Feedback.println(mode, "   Added " + (newTotal - total) + " triples.");
	    		
	    		    total = newTotal;
	    	    }	          
	         }
	    	 message += Feedback.println(mode," ");
		     message += Feedback.println(mode, "   Triples after [loadOntologies]: " + totalTriples());
		}
		return message;
	}
}	
	
