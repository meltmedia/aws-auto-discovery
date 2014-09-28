package com.meltmedia.aws.discovery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.amazonaws.services.ec2.model.Filter;

/**
 * Parsers for filter and tag lists.
 * 
 * @author Christian Trimble
 *
 */
public class Parsers {
	  /**
	   * Parses a filter string into a list of Filter objects that is suitable for
	   * the AWS describeInstances method call.
	   * 
	   * ### Format:
	   * 
       * ```
	   *   FILTERS ::= &lt;FILTER&gt; ( ';' &lt;FILTER&gt; )*
	   *   FILTER ::= &lt;NAME&gt; '=' &lt;VALUE&gt; (',' &lt;VALUE&gt;)*
	   * ```
	   * 
	   * @param filters
	   *          the filter string to parse.
	   * @return the list of filters that represent the filter string.
	   */
	  public static List<Filter> filters(String filters) {
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
		        throw new IllegalArgumentException(
		          String.format("Could not process key value pair '%s'"
		            ,filterString));
		      
		      String[] values = keyValues[1].trim().split("\\s*,\\s*");
		      
		      for( String value : values ) {
		    	  if( "".equals(value) ) {
		    		  throw new IllegalArgumentException(String.format("empty value for filter %s", keyValues[0]));
		    	  }
		      }
		      // create the filter and add it to the list.
		      awsFilters.add(new Filter()
		        .withName(keyValues[0])
		        .withValues(values));
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
	  public static List<String> tagNames(String tagNames) {
		  if( tagNames == null ) return Collections.emptyList();
		  
		  String trimmedTagNames = tagNames.trim();
		  if( "".equals(trimmedTagNames)) return Collections.emptyList();
		  
		  String[] names = tagNames.trim().split("\\s*,\\s*");
		  for( String name : names) {
			  if( "".equals(name) ) {
				  throw new IllegalArgumentException(String.format("empty tag name in %s", tagNames));
			  }
		  }
	    return Arrays.asList(names);
	  }

}
