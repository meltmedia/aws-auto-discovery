package com.meltmedia.aws.discovery;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.net.URI;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class RequestsTest {
	  @Rule
	  public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(8089));

	  HttpClient client = null;
	  @Before
	  public void setUp() {
		  client = new DefaultHttpClient();
	  }
	  
	  @After
	  public void tearDown() {
		  client.getConnectionManager().shutdown();
	  }

	@Test(expected=InvalidResponse.class)
	public void shouldFailOnNon200() {
		stubFor(get(urlEqualTo("/somepath"))
			      .willReturn(aResponse()
			        .withStatus(404)));
		
	  Requests.getBody(client, URI.create("http://localhost:8089/somepath"));
	}
	
	@Test
	public void shouldReturnBody() {
		stubFor(get(urlEqualTo("/somepath"))
			      .willReturn(aResponse()
			        .withStatus(200)
			        .withBody("value")));
		
		String body = Requests.getBody(client, URI.create("http://localhost:8089/somepath"));
		
		assertThat("body returned", body, equalTo("value"));
	}
	
	@Test(expected=RequestFailedException.class)
	public void shouldFailIfNoServer() {
		Requests.getBody(client, URI.create("http://localhost:8090/somepath"));
	}

}
