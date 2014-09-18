package com.meltmedia.aws.discovery;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

public class Requests {


	  /**
	   * Gets the body of the content returned from a GET request to uri.
	   * 
	   * @param client
	   *          the HttpClient instance to use for the request.
	   * @param uri
	   *          the URI to contact.
	   * @return the body of the message returned from the GET request.
	   * @throws Exception
	   *           if there is an error encounted while getting the content.
	   */
	  public static String getBody(HttpClient client, String uri) throws Exception {
	    HttpGet get = new HttpGet();
	    get.setURI(new URI(uri));
	    HttpResponse response = client.execute(get);
	    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
	      throw new Exception(String.format("Could not get url %s.", uri));
	    }
	    return EntityUtils.toString(response.getEntity());
	  }
}
