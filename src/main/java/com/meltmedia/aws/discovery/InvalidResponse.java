package com.meltmedia.aws.discovery;

import org.apache.http.HttpResponse;

/**
 * Thrown when an unusable response is encountered while inspecting an instance.
 * 
 * @author Christian Trimble
 */
public class InvalidResponse extends InspectionException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected HttpResponse response;
	
	public InvalidResponse(HttpResponse response) {
		super();
		this.response = response;
	}

	public InvalidResponse(String message, HttpResponse response, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.response = response;
	}

	public InvalidResponse(String message, HttpResponse response, Throwable cause) {
		super(message, cause);
		this.response = response;
	}

	public InvalidResponse(String message, HttpResponse response) {
		super(message);
		this.response = response;
	}

	public InvalidResponse(HttpResponse response, Throwable cause) {
		super(cause);
		this.response = response;
	}
	
	public HttpResponse getResponse() {
		return this.response;
	}
  
}
