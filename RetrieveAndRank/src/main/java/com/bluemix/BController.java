package com.bluemix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.http.Header;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import com.bluemix.dataparser.XmlParser;
import com.bluemix.objects.BCluster;
import com.bluemix.objects.BCollection;
import com.bluemix.objects.BRanker;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.RetrieveAndRank;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.model.Ranker;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.model.SolrClusters;


/** 
 * <h1>BController() - BluemixController</h1>
 * 
 * <p>This class is the upper class for handling Xml parser, Bluemix connection, creating Cluster, document, fields and handling of Ranker.</p>
 */
public class BController {
	/** BController - VARIABLES */
	private XmlParser parser;
	
	private HttpSolrClient solrClient;
	private RetrieveAndRank service;
	private Properties conProp;
	
	private BCluster cluster;
	private BCollection collection;
	private BRanker ranker;
	private SolrQueries query;
	
	/*---------CONSTRUCTOR------*/
	public BController(String connectionParameterPath) {
		this.loadConnectionParameters(connectionParameterPath);
		this.service = new RetrieveAndRank();
		this.service.setUsernameAndPassword(conProp.getProperty("USERNAME"), conProp.getProperty("PASSWORD"));
		
		// Initialise Cluster variables.
		int size = Integer.parseInt( conProp.getProperty("CLUSTER_SIZE") );
		this.cluster = new BCluster(size, conProp.getProperty("CLUSTER_NAME"), service);
		String clusterId = conProp.getProperty("SOLR_CLUSTER_ID");
		String configName = conProp.getProperty("CONFIG_NAME");
		if( clusterId != null && !clusterId.equalsIgnoreCase("") ) {	cluster.setId(clusterId);	}
		if( configName != null && !configName.equalsIgnoreCase("") ) {	cluster.setConfigName(configName);	}
		
		
		// Initialise Collection variables
		collection = new BCollection();
		String collectionName = conProp.getProperty("COLLECTION_NAME");
		if( collectionName != null && !collectionName.equalsIgnoreCase("") ) {	collection.setName(collectionName);	}
		
		// Initialise the basic SolrQueries variables
		query = new SolrQueries();
		
		// Initialise the Ranker variables
		String rankerName = conProp.getProperty("RANKER_NAME");
		String rankerId = conProp.getProperty("RANKER_ID");
		ranker = new BRanker(service);
		if( rankerName != null && !rankerName.equalsIgnoreCase("") ) {	ranker.setName( rankerName ); }
		if( rankerId != null && !rankerId.equalsIgnoreCase("") ) {	
			ranker.setId(rankerId); 
		}
		
		// create the HttpSolrClient for the connections used in the program
		solrClient = getSolrClient(service.getSolrUrl(conProp.getProperty("SOLR_CLUSTER_ID")), conProp.getProperty("USERNAME"), conProp.getProperty("PASSWORD"));
	}
	
	/*---------PARSER--------------*/	
	/**
	 * <h3>public boolean startParser()</h3>
	 * <p>A function which starts parsing of pre-downloaded wikipedia xml-dumb from address "src/main/resources/wikipedia.xml".<br />
	 * The parser parses only the readable text from the pages.</p>
	 *  
	 * @return	<b>true</b>, if no errors is encountered.
	 */
	public boolean startParser() {
		parser = new XmlParser("src/main/resources/wikipedia.xml");
		return parser.start();
	}
		/**
	 * <h3>public boolean startParser(String xmlAddress)</h3>
	 * <p>A function which starts parsing of pre-downloaded wikipedia xml-dumb.<br />
	 * The parser parses only the readable text from the pages.</p>
	 *  
	 * @param <b>xmlAddress</b>, is the location of the xml-file (in computer)
	 * @return	<b>true</b>, if no errors is encountered.
	 */
	public boolean startParser(String xmlAddress) {
		parser = new XmlParser(xmlAddress);
		return parser.start();
	}
	/*-----------------------------*/

	
	/*---------CLUSTER-------------*/
	public boolean createCluster() {
		try {
			if( cluster.create() ) {
				if( conProp.replace("SOLR_CLUSTER_ID", this.cluster.getId()) == null) {
					conProp.setProperty("SOLR_CLUSTER_ID", cluster.getId());
				}
				this.saveParamChanges();
				
				if( cluster.uploadSolrConfiguration( conProp.getProperty("CONFIG_NAME"), conProp.getProperty("CONFIG_PATH")) ) {
					return true;
				}
			}
			return false;
			
		} catch (NumberFormatException e) {
			System.out.println("Error: " + e.getLocalizedMessage() );
			return false;
		}
	}
	public boolean reuploadSolrConfigurations() {
		return cluster.uploadSolrConfiguration( conProp.getProperty("CONFIG_NAME"), conProp.getProperty("CONFIG_PATH"));
	}
	public boolean deleteCluster() {
		if( this.cluster.deleteSolrConfiguration() ) {
			if( this.cluster.delete() ) {
				return true;
			}
			return false;
		}
		return false;
	}
	public boolean deleteSolrConfigurations() {
		return this.cluster.deleteSolrConfiguration();
	}
	public boolean clusterStatus() {
		return this.cluster.isCreated();
	}
	public boolean clusterInfo() {
		return this.cluster.getInfo();
	}
	/*-----------------------------*/
	
	
	/*---------COLLECTION----------*/
	public boolean createCollection() {
		
		return collection.create(conProp.getProperty("COLLECTION_NAME"), conProp.getProperty("CONFIG_NAME"), solrClient);
	}
	public boolean deleteCollection() {
		return collection.delete(conProp.getProperty("SOLR_CLUSTER_ID"), solrClient);
	}
	public boolean addDocuments() {
		if( parser != null ) {
			return this.collection.addDocuments(conProp.getProperty("COLLECTION_NAME"), solrClient, parser.getList() );
		} else {
			System.out.println( "Error: Document can be added only after the xmlParser has been run!" );
			return false;
		}
	}
	/*-----------------------------*/

	
	/*---------RANKER--------------*/
	public void generateTrainingData() {
		this.query.generateTrainingData( conProp.getProperty("COLLECTION_NAME"), solrClient, conProp.getProperty("TRAINING_QUESTIONS_PATH"), conProp.getProperty("TRAINING_DATA_PATH") );
	}
	/**Creates a new ranker in Bluemix platform. Note of warning: if generateTrainingData() function is not used before, then this method will fail. */
	public boolean createRanker() {
		String rankerId = ranker.create(conProp.getProperty("RANKER_NAME"), conProp.getProperty("TRAINING_DATA_PATH"));
		if( !rankerId.equalsIgnoreCase("") ) {
			if( conProp.replace("RANKER_ID", this.ranker.getId()) == null) {
				conProp.setProperty("RANKER_ID", rankerId);
			}
			this.saveParamChanges();
		}
		return true;
	}
	/**remId can be empty "", or it can have Ranker id. */
	public boolean removeRanker(String remId) {
		
		boolean value = ranker.remove(remId);
		conProp.replace("RANKER_ID", "");
		this.saveParamChanges();
		
		return value;
	}
	public void getRankerInfo() {
		ranker.getRankerInfo();
	}
	/*-----------------------------*/
	
	
	/*---------QUERYING------------*/
	public void makeStandardQuery( String queryMsg ) {
		this.query.createQuery( queryMsg, conProp.getProperty("COLLECTION_NAME"), solrClient );
	}
	public void searchAndRankQuery(String queryMsg) {
		this.query.searchAndRank(queryMsg, conProp.getProperty("COLLECTION_NAME"), this.ranker.getId(), solrClient);
	}
	/*-----------------------------*/

	
	/** Retrieves and prints to cli all created solr_clusters. */
	public boolean listAllClusters() {
		try {
			SolrClusters clusters = service.getSolrClusters().execute();
			System.out.println(clusters);
			
			return true;
			
		} catch(Exception e) {
			System.out.println("Error: Cluster not available! " + e.getLocalizedMessage() );
			return false;
		}
	}
	public void listAllRankers() {
		List<Ranker> rankers = service.getRankers().execute().getRankers();
		
		if ( rankers.size() <= 0 ) {
			System.out.println("No rankers yet created!");
		} else {
			for( Ranker r : rankers ) {
				System.out.println("----------------");
				System.out.println( r.getId() );
				System.out.println( r.getName() );
				System.out.println( r.getStatusDescription() );
				System.out.println("----------------");
			}
		}
	}
	
	/** Loads the connection parameters from a file 'connection.dat'.
	 * */
	private void loadConnectionParameters(String path) {	// Properties properties
		if( conProp == null ) {
			conProp = new Properties();
			try {
				conProp.load( BController.class.getResourceAsStream(path) );
				
				String username = null;
			    String password = null;
			    String rnrUrl = null;
				JsonObject services = new JsonParser().parse(conProp.getProperty("VCAP_SERVICES")).getAsJsonObject();
				JsonArray arr = services.get("Thesis_demo").getAsJsonArray();
				JsonObject credentials = arr.get(0).getAsJsonObject().get("credentials").getAsJsonObject();
				rnrUrl = credentials.get("url").getAsString().trim();
				username = credentials.get("username").getAsString().trim();
				password = credentials.get("password").getAsString().trim();
				
				conProp.setProperty("USERNAME", username);
				conProp.setProperty("RNR_URL", rnrUrl);
				conProp.setProperty("PASSWORD", password);
				
/*
				for( Entry<Object, Object> e : conProp.entrySet() ) {
					System.out.println("Key " + ": " + e.getKey() + "\t" + e.getValue());
				}
*/
			} catch(IOException e) {
				System.out.println("Errors: " + e.getLocalizedMessage() );
			}
		} else {
			System.out.println("Properties is not empty.");
		}
	}
	private void saveParamChanges()  {
		try {
			File f = new File("src/main/resources/connection.dat");
			OutputStream out = new FileOutputStream( f );
			conProp.store(out, "");
			out.close();
		} catch(IOException e) {
			System.out.println( "Properties saving error: " + e.getLocalizedMessage() );
		}
		
	}


	/** <p>These two methods below are responsible of making and maintaining the HTTP (TCP) connection into the bluemix-server.</p> 
	 * <p>The commented .addInterceptorFirst() function can manually handle incoming 'HTTP Request' messages if implemented.
	 * For now it was left commented.</p> */
	private static HttpSolrClient getSolrClient(String uri, String username, String password) {
	    return new HttpSolrClient( uri, createHttpClient(uri, username, password) );
	}
	private static HttpClient createHttpClient(String uri, String username, String password) {
	    final URI scopeUri = URI.create(uri);

	    final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
	    credentialsProvider.setCredentials(new AuthScope(scopeUri.getHost(), scopeUri.getPort()),
	        new UsernamePasswordCredentials(username, password));
	    
	    
	    
	    /* By default, Bluemix collects data from all requests and uses the data to improve the services. 
	     * If you do not want to share your data, specify the header parameter x-watson-learning-opt-out 
	     * with the value 1 or true for all requests. 
	     * If you do not specify this header in all payload data, data is collected and used to improve the service. */
	    ArrayList<Header> h = new ArrayList<Header>();
	    h.add(new BasicHeader("x-watson-learning-opt-out", "true") );
	     
	    final HttpClientBuilder builder = HttpClientBuilder.create()
	        .setMaxConnTotal(128)
	        .setMaxConnPerRoute(32)
	        .setDefaultRequestConfig(
	        		RequestConfig.copy(RequestConfig.DEFAULT)
	        		.setRedirectsEnabled(true)
	        		.build()
	        )
	        .setDefaultCredentialsProvider(credentialsProvider)
	        .setDefaultHeaders( h )
	        /*.addInterceptorFirst(new PreemptiveAuthInterceptor())*/;
	    return builder.build();
	}
}
