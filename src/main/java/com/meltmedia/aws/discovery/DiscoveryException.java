package com.meltmedia.aws.discovery;

/**
 * The top level type for discovery exceptions.
 * 
 * @author Christian Trimble
 */
public class DiscoveryException extends RuntimeException {
	public DiscoveryException() {
		super();
	}

	public DiscoveryException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DiscoveryException(String message, Throwable cause) {
		super(message, cause);
	}

	public DiscoveryException(String message) {
		super(message);
	}

	public DiscoveryException(Throwable cause) {
		super(cause);
	}

	private static final long serialVersionUID = 1L;

}
