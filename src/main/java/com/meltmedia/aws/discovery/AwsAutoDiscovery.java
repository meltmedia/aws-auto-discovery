/**
 *   Copyright 2012 meltmedia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.meltmedia.aws.discovery;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.Node;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.Request;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.handlers.RequestHandler;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.transform.Unmarshaller;
import com.amazonaws.util.TimingInfo;

/**
 * A discovery component that uses the AWS EC2 API to find cluster members.
 * Membership can be determined by a general filter and/or by nodes that have
 * similar tags.
 *
 * ### Requirements
 * 
 * * The EC2 instances must be in the same region.
 * * The "ec2:Describe*" action must be accessable using either IAM
 * * credentials or an IAM instance profile.
 * * The security rules must allow TCP communication between the nodes that
 * are discovered.
 *
 * EC2 instances that match all of the supplied filters will be returned. For
 * example, if you wanted to cluster with all of the running, small instances in
 * your account, you could specify:
 * 
 * ```
 * AwsAutoDiscovery discovery = AwsAutoDiscovery.builder()
 *   .credentials("YOUR_AWS_ACCESS_KEY", "YOUR_AWS_SECRET_KEY")
 *   .withFilters(Parsers.filters("instance-state-name=running;instance-type=m1.small")
 *   .withEnvironment(EnvironmentInspector.build().inspect())
 *   .build();
 * ```
 *
 * ### IAM Instance Profiles
 * 
 * To use the instance profile associated with an EC2 instance, simply omit the
 * accessKey and secretKey attributes when building this class.
 * 
 * ```
 * AwsAutoDiscovery discovery = AwsAutoDiscovery.builder()
 *   .withTagNames(Parsers.tagNames("Type,Environment")
 *   .withEnvironment(EnvironmentInspector.build().inspect())
 *   .build();
 * ```
 *
 * ### References
 * 
 * * [EC2 Using Filtering](http://docs.amazonwebservices.com/AWSEC2/latest/UserGuide/Using_Filtering.html)
 * * [EC2 Describe Instances](http://docs.amazonwebservices.com/AWSEC2/latest/CommandLineReference/ApiReference-cmd-DescribeInstances.html)
 * * [EC2 Instance Metadata](http://docs.amazonwebservices.com/AWSEC2/latest/UserGuide/AESDG-chapter-instancedata.html)
 * 
 * @author Christian Trimble
 * @author John McEntire
 * 
 */
@SuppressWarnings("deprecation")
public class AwsAutoDiscovery implements Closeable {

	public static class Builder {
		protected AWSCredentialsProvider provider = new DefaultAWSCredentialsProviderChain();
		protected List<Filter> filters = new ArrayList<Filter>();
		protected List<String> tagNames = new ArrayList<String>();
		protected FaultListener faultListener;
		protected InstanceDetails instanceDetails;

		public Builder withCredentials(String accessKey, String secretKey) {
			this.provider = new StaticCredentialsProvider(
					new BasicAWSCredentials(accessKey, secretKey));
			return this;
		}

		public Builder withCredentialProvider(AWSCredentialsProvider provider) {
			this.provider = provider;
			return this;
		}

		public Builder withFilters(Collection<Filter> filters) {
			this.filters.addAll(filters);
			return this;
		}

		public Builder withTagNames(Collection<String> tagNames) {
			this.tagNames.addAll(tagNames);
			return this;
		}

		public Builder withFaultListener(FaultListener faultListener) {
			this.faultListener = faultListener;
			return this;
		}
		
		public Builder withInstanceDetails( InstanceDetails instanceDetails ) {
			this.instanceDetails = instanceDetails;
			return this;
		}

		public AwsAutoDiscovery build() throws DiscoveryException {
			if( instanceDetails == null ) {
				instanceDetails = InstanceInspector.build().inspect();
			}
			return new AwsAutoDiscovery(provider, instanceDetails, filters,
					tagNames, faultListener);
		}
	}

	protected AWSCredentialsProvider credentialProvider;
	private InstanceDetails instanceEnvironment;
	private Collection<Filter> filters;
	private Collection<String> tagNames;
	private FaultListener faultListener;

	public AwsAutoDiscovery(AWSCredentialsProvider credentialProvider,
			InstanceDetails instanceEnvironment, List<Filter> filters,
			List<String> tagNames, FaultListener faultListener) {
		this.credentialProvider = credentialProvider;
		this.instanceEnvironment = instanceEnvironment;
		this.filters = filters;
		this.tagNames = tagNames;
		this.faultListener = faultListener;
	}

	public static Builder builder() {
		return new Builder();
	}

	/**
	 * The EC2 client used to look up cluster members.
	 */
	private AmazonEC2Client ec2;

	/**
	 * Starts the auto discovery component.
	 * 
	 * @return this instance for chaining
	 */
	public AwsAutoDiscovery start() throws Exception {
		// start up a new ec2 client with the region specific endpoint.
		ec2 = new AmazonEC2Client(credentialProvider);
		ec2.setEndpoint(instanceEnvironment.getEndpoint());

		// Lets do some good old reflection work to add a unmarshaller to the
		// AmazonEC2Client just to log the exceptions from soap.
		if (faultListener != null) {
			addExceptionUnmarshaller(ec2, new FaultAdapter(faultListener));
		}

		return this;
	}

	/**
	 * Quietly stops the auto discovery component.
	 * 
	 * @return this instance for chaining
	 */
	public AwsAutoDiscovery stop() {
		try {
			close();
		} catch (Exception e) {
			// 
		}

		return this;
	}
	

	@Override
	public void close() throws IOException {
		if (ec2 != null) {
			try {
			  ec2.shutdown();
			}
			finally {
			  ec2 = null;
			}
		}
	}

	/**
	 * Gets the list of private IP addresses found in AWS based on the filters
	 * and tag names defined.
	 * 
	 * @return the list of private IP addresses found on AWS
	 */
	public List<String> getPrivateIpAddresses() {
		List<String> result = new ArrayList<String>();

		List<Filter> filters = new ArrayList<Filter>();

		// if there are aws tags defined, then look them up and create filters.
		if (tagNames != null) {
			filters.addAll(asFilters(requestInstanceTags()));
		}

		// if there are aws filters defined, then add them to the list.
		if (this.filters != null) {
			filters.addAll(this.filters);
		}
		DescribeInstancesRequest request = new DescribeInstancesRequest()
				.withFilters(filters);

		// NOTE: the reservations group nodes together by when they were
		// started. We need to dig through all of the reservations.
		DescribeInstancesResult filterResult = ec2.describeInstances(request);
		for (Reservation reservation : filterResult.getReservations()) {
			for (Instance instance : reservation.getInstances()) {
				result.add(instance.getPrivateIpAddress());
			}
		}
		return result;
	}

	static List<Filter> asFilters(Collection<Tag> tags) {
		List<Filter> result = new ArrayList<Filter>();
		for (Tag instanceTag : tags) {
			if (tags.contains(instanceTag.getKey())) {
				result.add(new Filter("tag:" + instanceTag.getKey(), Arrays
						.asList(instanceTag.getValue())));
			}
		}
		return result;
	}

	/**
	 * Returns all of the tags defined on the EC2 instance with the specified
	 * instanceId.
	 * 
	 * @return a list of the Tag objects that were found on the instance
	 */
	protected List<Tag> requestInstanceTags() {
		List<Tag> tags = new ArrayList<Tag>();
		DescribeInstancesResult response = ec2
				.describeInstances(new DescribeInstancesRequest()
						.withInstanceIds(Arrays.asList(instanceEnvironment
								.getInstanceId())));
		for (Reservation res : response.getReservations()) {
			for (Instance inst : res.getInstances()) {
				List<Tag> instanceTags = inst.getTags();
				if (instanceTags != null && instanceTags.size() > 0) {
					tags.addAll(instanceTags);
				}
			}
		}
		return tags;
	}

	/**
	 * Sets up the AmazonEC2Client to log soap faults from the AWS EC2 api
	 * server.
	 */
	static void addExceptionUnmarshaller(AmazonEC2Client ec2,
			Unmarshaller<AmazonServiceException, Node> unmarshaller) {
		boolean accessible = false;
		Field exceptionUnmarshallersField = null;
		try {
			exceptionUnmarshallersField = AmazonEC2Client.class
					.getDeclaredField("exceptionUnmarshallers");
			accessible = exceptionUnmarshallersField.isAccessible();
			exceptionUnmarshallersField.setAccessible(true);
			@SuppressWarnings("unchecked")
			List<Unmarshaller<AmazonServiceException, Node>> exceptionUnmarshallers = (List<Unmarshaller<AmazonServiceException, Node>>) exceptionUnmarshallersField
					.get(ec2);
			exceptionUnmarshallers.add(0, unmarshaller);
			ec2.addRequestHandler((RequestHandler) exceptionUnmarshallers
					.get(0));
		} catch (Throwable t) {
			// I don't care about this.
		} finally {
			if (exceptionUnmarshallersField != null) {
				try {
					exceptionUnmarshallersField.setAccessible(accessible);
				} catch (SecurityException se) {
					// I don't care about this.
				}
			}
		}
	}

	/**
	 * This class will adapts a FaultListener to the AWS APIs.
	 * 
	 * @author John McEntire
	 *
	 */
	private class FaultAdapter implements
			Unmarshaller<AmazonServiceException, Node>, RequestHandler {
		private final ThreadLocal<Request<?>> request = new ThreadLocal<Request<?>>();
		private FaultListener listener;

		public FaultAdapter(FaultListener listener) {
			this.listener = listener;
		}

		@Override
		public AmazonServiceException unmarshall(Node node) throws Exception {
			try {
				javax.xml.transform.TransformerFactory tfactory = javax.xml.transform.TransformerFactory
						.newInstance();
				javax.xml.transform.Transformer xform = tfactory
						.newTransformer();

				javax.xml.transform.Source src = new javax.xml.transform.dom.DOMSource(
						node);
				java.io.StringWriter writer = new java.io.StringWriter();
				javax.xml.transform.Result result = new javax.xml.transform.stream.StreamResult(
						writer);
				xform.transform(src, result);
				listener.fault("AWS Exception: [" + writer.toString()
						+ "] For request [" + request.get() + "]");
			} catch (Throwable t) {
				//
			} finally {
				request.remove();
			}
			return null;
		}

		@Override
		public void afterError(Request<?> request, Exception e) {
			this.request.remove();
		}

		@Override
		public void afterResponse(Request<?> request, Object obj,
				TimingInfo timing) {
			this.request.remove();
		}

		@Override
		public void beforeRequest(Request<?> request) {
			this.request.set(request);
		}
	}

}
