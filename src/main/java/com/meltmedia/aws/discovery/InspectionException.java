package com.meltmedia.aws.discovery;

public class InspectionException
  extends DiscoveryException
{
	private static final long serialVersionUID = 1L;

	public InspectionException() {
		super();
	}

	public InspectionException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InspectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public InspectionException(String message) {
		super(message);
	}

	public InspectionException(Throwable cause) {
		super(cause);
	}
}
