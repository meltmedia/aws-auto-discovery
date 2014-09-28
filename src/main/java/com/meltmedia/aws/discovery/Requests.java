package com.meltmedia.aws.discovery;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

/**
 * Request helpers for inspecting instance metadata.
 * 
 * @author Christian Trimble
 */
public class Requests {

	  /**
	   * Gets the body of the content returned from a GET request to uri.
	   * 
	   * @param client
	   *          the HttpClient instance to use for the request.
	   * @param uri
	   *          the URI to contact.
	   * @return the body of the message returned from the GET request.
	   * @throws InspectionException
	   *           if there is an error encounted while getting the content.
	   */
	  public static String getBody(HttpClient client, URI uri) {
	    HttpResponse response = getResponse(client, uri);
	    int statusCode = response.getStatusLine().getStatusCode();
	    if (statusCode != HttpStatus.SC_OK) {
	      throw new InvalidResponse(String.format("%s return status %d", uri, statusCode), response);
	    }
	    try {
			return EntityUtils.toString(response.getEntity());
		} catch (Exception e) {
			throw new InvalidResponse(String.format("failed to parse body of %s", uri), response, e);
		}
	  }
	  
	  public static HttpResponse getResponse(HttpClient client, URI uri) {
		    try {
			    HttpGet get = new HttpGet();
			    get.setURI(uri);
		    	return client.execute(get);
		    }
		    catch( Exception e ) {
		      throw new RequestFailedException(String.format("could not get %s", uri), e);
		    }		  
	  }

}
