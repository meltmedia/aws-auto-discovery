package com.meltmedia.aws.discovery;

public class InstanceDetails {
	protected String instanceId;
	protected String availabilityZone;
	protected String endpoint;

	public String getInstanceId() {
		return instanceId;
	}

	void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getAvailabilityZone() {
		return availabilityZone;
	}

	void setAvailabilityZone(String availabilityZone) {
		this.availabilityZone = availabilityZone;
	}

	public String getEndpoint() {
		return endpoint;
	}

	void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
}