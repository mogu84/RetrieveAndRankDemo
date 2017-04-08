package com.bluemix.objects;

import java.io.File;
import java.util.List;

import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.RetrieveAndRank;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.model.SolrCluster;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.model.SolrClusterOptions;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.model.SolrConfigs;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.model.SolrCluster.Status;

public class BCluster {
	/* Cluster - Variables */
	private String id;
	private String name;
	private int size;
	private String solrConfigurationName;
	
	private RetrieveAndRank service;
	private boolean isCreated = false;
	
	public String getId() {
		return this.id;
	}
	public void setId(String clusterId) {
		this.id = clusterId;
	}
	public String getName() {
		return this.name;
	}
	public boolean isCreated() {
		if( this.solrConfigurationName != null ) {
			System.out.println("solrConfigs also uploaded!");
			return this.isCreated;
		} else {
			System.out.println("solrConfigs not uploaded!");
			return this.isCreated;
		}
	}
	public void setConfigName(String configName) {
		this.solrConfigurationName = configName;
	}
	public String getConfigName() {
		return this.solrConfigurationName;
	}
	
	/** 
	 * <p>Initializes all required variables for the class.</p><p>Note! the cluster creation is started by calling @link start function.</p>
	 * 
	 * @param clusterSize	-	Represents the size of the cluster that is wanted to be created.
	 * @param clusterName	-	
	 * @param RNRService	-	RetrieveAndRank service, where username and password has already been filled.
	 */
	public BCluster(int clusterSize, String clusterName, RetrieveAndRank RNRService) {
		this.name = clusterName;
		this.size = clusterSize;
		this.service = RNRService;
		
		if( this.size >= 1 && this.size <= 7 ) {
			this.size = clusterSize;
		} else {
			System.out.println("Error: Cluster size must be between 1 and 7.");
		}
	}
	
	/**
	 * <p>Creates a cluster in the IBM Bluemix -portal.</p>
	 * 
	 * @param clustername	A name for the cluster.
	 * @param clustersize	Preferred cluster size. Values from 1 - 7.
	 * 
	 * @return
	 * <table>
	 * 	<tr>
	 * 		<td><b>true</b></td>
	 * 		<td>If creation process went OK.</td>
	 * 	</tr>
	 *	<tr>
	 * 		<td><b>false</b></td>
	 * 		<td>If cluster is already created or if other problems were encountered.</td>
	 *	</tr>
	 * </table>
	 */
	public boolean create() {
		if( this.isCreated )
			return true;		// already created 
		
		if( this.size >= 1 && this.size <= 7 ) {
			try {
				SolrClusterOptions options = new SolrClusterOptions(this.name, this.size);
				SolrCluster cluster = service.createSolrCluster(options).execute();
				System.out.println("SolrCluster: " + cluster);
				this.id = cluster.getId();
		
				// wait until the cluster is available
				while (cluster.getStatus() == Status.NOT_AVAILABLE) {
					Thread.sleep(10000); // sleep 10 seconds
					cluster = service.getSolrCluster(cluster.getId()).execute();
					System.out.println("SolrCluster status: " + cluster.getStatus());
					System.out.println("SolrCluster ID: " + cluster.getId());
				}
			} catch (Exception e) {
				System.out.println("Error: " + e.getLocalizedMessage());
				return false;
			}
			
		} else {
			System.out.println("Error: Cluster size is wrong, give a value between 1 - 7.");
			return false;
		}
		this.isCreated = true;
		return true;
	}
	
	public boolean delete() {
		try {
			service.deleteSolrCluster( this.id ).execute();
			System.out.println("Cluster {" + this.id + "} deleted!");
		} catch(Exception e) {
			System.out.println("Error: Cluster not available! " + e.getLocalizedMessage() );
			return false;
		}
		
		this.isCreated = false;
		return true;
	}
	
	/** Retrieves the Solr Cluster information from the bluemix platform and prints the results to Command line.*/
	public boolean getInfo() {
		try {
			System.out.println(service.getSolrCluster( this.id ).execute() + "\n");
			return true;
			
		} catch(Exception e) {
			System.out.println("Error: Cluster not available! " + e.getLocalizedMessage() );
			return false;
		}
	}

	/** Uploads a zip/folder file containing the configuration files for your Solr collection. The zip file must include schema.xml, solrconfig.xml, and other files you need for your configuration. */
	public boolean uploadSolrConfiguration(String configName, String configPath) {
		try {
			this.solrConfigurationName = configName;

			if( configPath.trim().substring(configPath.length()-3).compareToIgnoreCase("zip") == 0 ) {
				File configZip = new File(configPath);
				service.uploadSolrClusterConfigurationZip(this.id, this.solrConfigurationName, configZip).execute();
			} else {
				File configLoc = new File(configPath);
				service.uploadSolrClusterConfigurationDirectory(this.id, this.solrConfigurationName, configLoc).execute();
			}
			System.out.println("SolrConfiguration upload for cluster {"+ this.id +"} complete.");
			
		} catch (Exception e) {
			System.out.println("Error: " + e.getLocalizedMessage());
			return false;
		}
		
		return true;
	}
	
	/**
	 * <p>Deletes the uploaded solr-configurations on the bluemix server.</p>
	 * @return <table><tr><td><b>true</b></td><td>if successfull<td></tr><tr><td>false</td><td><b>if failure occurred</b></td></tr></table>
	 */
	public boolean deleteSolrConfiguration() {
		try {
			service.deleteSolrClusterConfiguration(this.id, this.solrConfigurationName).execute();
			System.out.println("SolrClusterConfiguration {\"" + this.solrConfigurationName + "\"} deleted!");
		} catch (Exception e) {
			System.out.println("Config deletion error: " + e.getLocalizedMessage() );
			return false;
		}
		
		return true;
	}
	
	/** Retrieves the configuration for a cluster by its name and prints it to console. */
	public boolean getSolrConfigurations() {
		try {
			//RetrieveAndRank service = new RetrieveAndRank();
			//service.setUsernameAndPassword(conProp.getProperty("USERNAME"),conProp.getProperty("PASSWORD"));
			
			SolrConfigs clusterdata = service.getSolrClusterConfigurations(this.id).execute();
			//System.out.println(clusterdata.toString());
			
			List<String> data = clusterdata.getSolrConfigs();
			
			System.out.println("List of Solr-config names: ");
			for( int i = 0; i < data.size(); i++ ) {
				System.out.println(data.get(i));
			}
			
		} catch(Exception e) {
			System.out.println("Error: " + e.getLocalizedMessage() );
			return false;
		}
		
		return true;
	}

	/**
	 * <p>Check if cluster's status is READY.
	 * 
	 * @return <table><tr><td><b>true</b></td><td>if READY<td></tr><tr><td><b>false</b></td><td>if NOT_READY</td></tr></table>
	 */
	public boolean checkClusterStatus() {
		RetrieveAndRank service = new RetrieveAndRank();
		SolrCluster cluster = service.getSolrCluster(this.id).execute();
		
		if( cluster.getStatus() == Status.READY ) {
			return true;
		} else {
			return false;
		}
	}

}
