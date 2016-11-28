package org.xululabs.search;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.xululabs.datasources.ElasticsearchApi;
import org.xululabs.datasources.Twitter4jApi;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import twitter4j.Twitter;

public class DeployServer extends AbstractVerticle {
	HttpServer server;
	Router router;
	ElasticsearchApi elasticsearch;
	Twitter4jApi twitter4jApi;
	String host;
	String esHost;
	String esIndex;
	int port;
	int esPort;
	int documentsSize;

	public DeployServer() {
		this.elasticsearch = new ElasticsearchApi();
		this.host = "localhost";
		this.port = 8383;
		this.esHost = "localhost";
		this.esPort = 9300;
		this.esIndex = "twitter";
		this.documentsSize = 1000;
		this.twitter4jApi = new Twitter4jApi();
	}

	/**
	 * Deploying the verical
	 */
	@Override
	public void start() {
		server = vertx.createHttpServer();
		router = Router.router(vertx);
		// Enable multipart form data parsing
		router.route().handler(BodyHandler.create());
		router.route().handler(CorsHandler.create("*").allowedMethod(HttpMethod.GET).allowedMethod(HttpMethod.POST)
				.allowedMethod(HttpMethod.OPTIONS).allowedHeader("Content-Type, Authorization"));
		// registering different route handlers
		this.registerHandlers();
		server.requestHandler(router::accept).listen(port, host);
	}

	/**
	 * For Registering different Routes
	 */
	public void registerHandlers() {
		router.route(HttpMethod.GET, "/").handler(this::welcomeRoute);
		router.route(HttpMethod.POST, "/search").blockingHandler(this::search);
		router.route(HttpMethod.POST, "/retweet").blockingHandler(this::retweet);

	}

	/**
	 * welcome route
	 * 
	 * @param routingContext
	 */
	public void welcomeRoute(RoutingContext routingContext) {
		routingContext.response().end("<h1> Welcome Route </h1>");
	}

	/**
	 * use to search documents
	 * 
	 * @param routingContext
	 */
	public void search(RoutingContext routingContext) {
		Map<String, Object> responseMap = new HashMap<String, Object>();
		String response;
		ObjectMapper mapper = new ObjectMapper();
		String keyword = (routingContext.request().getParam("keyword") == null) ? "cat"
				: routingContext.request().getParam("keyword");
		String searchIn = (routingContext.request().getParam("searchIn") == null) ? "tweet"
				: routingContext.request().getParam("searchIn");
		try {
			String[] fields = mapper.readValue(searchIn, String[].class);
			ArrayList<Map<String, Object>> documents = elasticsearch.searchDocuments(
					elasticsearch.getESInstance(esHost, esPort), esIndex, fields, keyword, documentsSize);
			responseMap.put("status", "scusses");
			responseMap.put("documents", documents);
			responseMap.put("size", documents.size());
			response = mapper.writeValueAsString(responseMap);

		} catch (Exception e) {
			response = "{\"status\" : \"error\", \"msg\" :" + e.getMessage() + "}";
		}

		routingContext.response().end(response);

	}

	/**
	 * route for retweets
	 * 
	 * @param routingContext
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	public void retweet(RoutingContext routingContext) {
		
	    String response;
		ObjectMapper mapper = new ObjectMapper();
		String tweetsId = (routingContext.request().getParam("tweetsId") == null) ? "[]": routingContext.request().getParam("tweetsId");
		String credentialsjson = (routingContext.request().getParam("credentials") == null) ? "[]": routingContext.request().getParam("credentials");
		try {
			TypeReference<ArrayList<Long>> typeRef =  new TypeReference<ArrayList<Long>>() {};
			ArrayList<Long> tweetsIdsList = mapper.readValue(tweetsId, typeRef);
			
			 TypeReference<HashMap<String, Object>> credentialsTypeReference =  new TypeReference<HashMap<String,Object>>() {};
	         HashMap<String, Object> credentials = mapper.readValue(credentialsjson, credentialsTypeReference);
			
	         ArrayList<Long> retweetIds =  twitter4jApi.retweet(this.getTwitterInstance(credentials.get("consumerKey").toString(), credentials.get("consumerSecret").toString(), credentials.get("accessToken").toString(), credentials.get("accessTokenSecret").toString()), tweetsIdsList);
	         response = mapper.writeValueAsString(retweetIds);     
		
		} catch (Exception ex) {
             response = "{\"status\" : \"error\", \"msg\" :"+ex.getMessage()+ "}";
		}
		routingContext.response().end(response);
	}
	/**
	 * use to get twitter instance
	 * @return Twitter instance
	 * @throws Exception 
	 */
	public Twitter getTwitterInstance(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) throws Exception{
		return twitter4jApi.getTwitterInstance(consumerKey, consumerSecret, accessToken, accessTokenSecret);
	}
}
