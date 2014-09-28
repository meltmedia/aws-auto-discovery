package com.meltmedia.aws.discovery;

import java.util.List;

import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.amazonaws.services.ec2.model.Filter;

public class ParsersTest {
  @Test
  public void shouldParseEmptyFilters() {
	  List<Filter> filters = Parsers.filters("");
	  assertThat("The filters are empty", filters.size(), equalTo(0));
  }
  
  @Test
  public void shouldParseSingleFilter() {
	  List<Filter> filters = Parsers.filters("name=value");
	  
	  assertThat("correct filter count", filters.size(), equalTo(1));
	  assertThat("correct name", filters.get(0).getName(), equalTo("name"));
	  assertThat("correct values", filters.get(0).getValues(), contains("value"));
  }
  
  @Test
  public void shouldParseComplexFilters() {
	  List<Filter> filters = Parsers.filters("name1=value1,value2;name2=value3");
	  
	  assertThat("correct filter count", filters.size(), equalTo(2));
	  assertThat("correct first name", filters.get(0).getName(), equalTo("name1"));
	  assertThat("correct first values", filters.get(0).getValues(), contains("value1", "value2"));
	  assertThat("correct second name", filters.get(1).getName(), equalTo("name2"));
	  assertThat("correct second values", filters.get(1).getValues(), contains("value3"));
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void shouldFailOnFilterWithoutValue() {
	  Parsers.filters("name1=value1,value2;name2");
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void shouldFailOnFilterWithEmptyValue() {
	  Parsers.filters("name1=value1,value2;name2=");
  }
  
  @Test
  public void shouldParseEmptyTags() {
	  List<String> tags = Parsers.tagNames("");
	  
	  assertThat("the list is empty", tags.size(), equalTo(0));
  }
  
  @Test
  public void shouldParseOneTag() {
	  List<String> tags = Parsers.tagNames("tag1");
	  
	  assertThat("correct tags returned", tags, contains("tag1")); 
  }
  @Test
  public void shouldParseMultipleTags() {
	  List<String> tags = Parsers.tagNames("tag1 ,tag2, tag3 ");
	  
	  assertThat("correct tags returned", tags, contains("tag1", "tag2", "tag3")); 
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void shouldFailOnEmptyTag() {
	  Parsers.tagNames("tag1 , , tag3 ");
  }
}
