# AWS Auto Discovery

## Overview

This package provides an auto discovery component for finding custer members on AWS using both tag matching and filters.  It
is designed for building clusters on AWS where multiple clusters can be built from a single AMI, without the need for tooling
to rewrite configuration.

## Usage

To use AWS auto discovery, you need to add a dependency to this package in your pom:

```
<dependency>
  <groupId>com.meltmedia.aws</groupId>
  <artifactId>aws-auto-discovery</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

and then use the builder to implement node discovery:

```
import java.util.List;
import com.meltmedia.aws.discovery.AwsAutoDiscovery;
import static com.meltmedia.aws.discovery.Parsers.tagNames;
...
AwsAutoDiscovery discovery = AwsAutoDiscovery.builder()
  .withTagNames(Parsers.tagNames("cluster,environment"))
  .build()
  .start();
...
List<String> privateAddresses = discovery.findMembers();
...
discovery.stop();
```

This implementation will only work from inside EC2, since it uses environment information to auto wire itself.  See the `Setting Up EC2` section for more information.

## Builder Options

* withTagNames - A collection of tag names.  Other members will be added to the cluster if they have the same value as the current node.
* withFilters - A list of filters.  Other members will be added to the cluster if they match all of the filters.
* withCredentials - the access key and secret key for an AWS user with permission to the "ec2:Describe*" action.  If credentials are not specified, then the instance profile for the EC2 instance will be used.
* withCredentialsProvider - a com.amazonaws.auth.AWSCredentialsProvider to use.  This option can
only be used when the access_key and secret_key options are not provided.
* withFaultListener - a listener that will be notified if faults occure when communicating with the AWS endpoint.
* withInstanceDetails - details about the instance that are required for connecting to AWS.  This option is intended for testing.

## Setting Up EC2

You will need to setup the following in EC2, before using this package:

* The EC2 instances will need permission to the "ec2:Describe*" action.  You can either create an IAM user with this permission
and pass the users credentials with the accessKey and secretKey properites or associate an instance profile with that permission
to the instances and not specify the accessKey and secretKey attributes.
* In the EC2 console, you will need to create a security group for your instances.  This security group will need a TCP_ALL rule,
with itself as the source (put the security group's name in the source field.)  This will allow all of the nodes in that security
group to communicate with each other.
* Create two EC2 nodes, making sure to include the security group granting TCP communication.
* If you are going to use the tag matching feature, then define a few tags on the nodes with matching values.

