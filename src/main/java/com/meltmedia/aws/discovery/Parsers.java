package com.meltmedia.aws.discovery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.amazonaws.services.ec2.model.Filter;

public class Parsers {
	  /**
	   * Parses a filter string into a list of Filter objects that is suitable for
	   * the AWS describeInstances method call.
	   * 
	   * <h3>Format:</h3>
	   * <p>
	   * <blockquote>
	   * 
	   * <pre>
	   *   FILTERS ::= &lt;FILTER&gt; ( ';' &lt;FILTER&gt; )*
	   *   FILTER ::= &lt;NAME&gt; '=' &lt;VALUE&gt; (',' &lt;VALUE&gt;)*
	   * </pre>
	   * 
	   * </blockquote>
	   * </p>
	   * 
	   * @param filters
	   *          the filter string to parse.
	   * @return the list of filters that represent the filter string.
	   */
	  public static List<Filter> parseFilters(String filters) {
		    List<Filter> awsFilters = new ArrayList<Filter>();

		    for (String filterString : filters.split("\\s*;\\s*")) {
		      // clean up the filter, moving on if it is empty.
		      String trimmed = filterString.trim();
		      if (trimmed.length() == 0)
		        continue;

		      // isolate the key and the values, failing if there is a problem.
		      String[] keyValues = trimmed.split("\\s*=\\s*");
		      if (keyValues.length != 2 || keyValues[0].length() == 0
		          || keyValues[1].length() == 0)
		        throw new IllegalArgumentException("Could not process key value pair '"
		            + filterString + "'");

		      // create the filter and add it to the list.
		      awsFilters.add(new Filter().withName(keyValues[0]).withValues(
		          keyValues[1].split("\\s*,\\s")));
		    }
		    return awsFilters;
		  }
	  
	  /**
	   * Parses a comma separated list of tag names.
	   * 
	   * @param tagNames
	   *          a comma separated list of tag names.
	   * @return the list of tag names.
	   */
	  public static List<String> parseTagNames(String tagNames) {
	    return Arrays.asList(tagNames.split("\\s*,\\s*"));
	  }

}
