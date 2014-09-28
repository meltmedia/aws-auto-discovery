package com.meltmedia.aws.discovery;

import java.net.URI;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Looks up the instance details from the instance metadata found at `http://169.254.169.254/`.
 *
 * @author Christian Trimble
 */
public class InstanceInspector {
	private static final String DEFAULT_BASE_URI = "http://169.254.169.254/";
	private static final String GET_INSTANCE_ID_PATH = "./latest/meta-data/instance-id";
	private static final String GET_AVAILABILITY_ZONE_PATH = "./latest/meta-data/placement/availability-zone";
	
	public static class Builder {
		private URI baseUri = URI.create(DEFAULT_BASE_URI);
		
		public Builder withBaseUri( URI baseUri ) {
			if( baseUri != null ) {
			this.baseUri  = baseUri;
			}
			else {
				this.baseUri = URI.create(DEFAULT_BASE_URI);
			}
			return this;
		}
		
		InstanceInspector build() {
			URI instanceMetadataUri = baseUri.resolve(GET_INSTANCE_ID_PATH);
			URI availabilityZoneUri = baseUri.resolve(GET_AVAILABILITY_ZONE_PATH);
			
			return new InstanceInspector(instanceMetadataUri, availabilityZoneUri);
		}
	}

	private URI instanceMetadataUri;
	private URI availabilityZoneUri;
	
	protected InstanceInspector(URI instanceMetadataUri, URI availabilityZoneUri) {
		this.instanceMetadataUri = instanceMetadataUri;
		this.availabilityZoneUri = availabilityZoneUri;
	}

	public static Builder builder() {
		return new Builder();
	}
	
	public static InstanceInspector build() {
		return builder().build();
	}
	
	public InstanceDetails inspect() throws InspectionException {
		InstanceDetails env = new InstanceDetails();
		// get the instance id and availability zone.
		HttpClient client = null;
		try {
			client = new DefaultHttpClient();
			try {
			  env.setInstanceId(Requests.getBody(client, instanceMetadataUri));
			}
			catch( Exception e ) {
				throw new InspectionException("Could not retrieve instance id.", e);
			}
			try {
			  env.setAvailabilityZone(Requests.getBody(client, availabilityZoneUri));
			}
			catch( Exception e ) {
				throw new InspectionException("Could not retrieve availability zone.", e);
			}
		}
        finally {
			if (client != null) {
				client.getConnectionManager().shutdown();
			}
		}

		// compute the EC2 endpoint based on the availability zone.
		env.setEndpoint("ec2."
				+ env.getAvailabilityZone().replaceAll("(.*-\\d+)[^-\\d]+",
						"$1") + ".amazonaws.com");

		return env;
	}
}
