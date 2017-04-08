package com.bluemix.objects;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.response.CollectionAdminResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

import com.bluemix.dataparser.PageData;

public class BCollection {
	private String name;
	
	public String getName() {
		return this.name;
	}
	public void setName(String collectionName) {
		this.name = collectionName;
	}
	
	public BCollection() {
	}

	/**
	 * <p>Tries to create a collection (OBS! Prerequisite is that cluster is already available!).</p>
	 * 
	 * @param collectionName			Name for the created collection
	 * @param collectionConfigName		Name of the SOLR_CONFIGURATION_FILE that have been uploaded to Bluemix previously by {@link uploadSolrConfiguration}
	 * @return	<table><tr><td><b>true</b></td><td>if creation successful</td></tr><tr><td><b>false</b></td><td>if not successful.</td></tr></table> 
	 */
	public boolean create(String collectionName, String collectionConfigName, HttpSolrClient solrClient) {
		this.name = collectionName;
		try {
			CollectionAdminRequest.Create createCollectionRequest = new CollectionAdminRequest.Create();
			createCollectionRequest.setCollectionName(collectionName);
			createCollectionRequest.setConfigName(collectionConfigName);

			System.out.println("Creating collection...");
			CollectionAdminResponse response = createCollectionRequest.process(solrClient);
		    if (!response.isSuccess()) {
		      System.out.println(response.getErrorMessages());
		      throw new IllegalStateException("Failed to create collection: " + response.getErrorMessages().toString());
		    }
		    
			System.out.println("Collection created.");
			System.out.println(response);
		} catch (Exception e) {
			System.out.println( "Error: " + e.getLocalizedMessage() );
			return false;
		}
		
		return true;
	}

	public boolean delete(String clusterId, HttpSolrClient solrClient) {
		final CollectionAdminRequest.Delete deleteCollectionRequest = new CollectionAdminRequest.Delete();
	    deleteCollectionRequest.setCollectionName(this.name);

	    // Send the deletion request and throw an exception if the response is not successful
	    CollectionAdminResponse response;
		try {
			response = deleteCollectionRequest.process(solrClient);
			
		    if (!response.isSuccess()) {
		    	System.out.println( "Collection delete error: " + response.getErrorMessages().toString() );
		    	return false;
		    }
		    
		} catch (SolrServerException e) {
			System.out.println("Collection delete error: " + e.getLocalizedMessage() );
		} catch (IOException e) {
			System.out.println("Collection delete error: " + e.getLocalizedMessage() );
		}
	    
		System.out.println("Collection delete successfull!");
		return true;
	}
	
	public boolean addDocuments(String collectionConfigName, HttpSolrClient solrClient, ArrayList<PageData> docs) {
		System.out.println("Indexing documents...");
		UpdateResponse addResponse = null;
		
		try {			
			for( PageData d : docs ) {
				SolrInputDocument document = new SolrInputDocument();
				document.addField( "title", d.getTitle() );
				document.addField( "pageId", d.getPageId() );
				document.addField( "revId", d.getRevId() );
				document.addField( "body", d.getText() );
				addResponse = solrClient.add(collectionConfigName, document);
			}
			System.out.println(addResponse);
	
			// Commit the document to the index so that it will be available for searching.
			solrClient.commit(collectionConfigName);
			
		} catch (IOException e) {
			System.out.println( "Document addition failed: " + e.getLocalizedMessage() );
			return false;
		} catch (SolrServerException e) {
			System.out.println( "Document addition failed: " + e.getLocalizedMessage() );
			return false;
		}
		
		System.out.println("Indexed and committed document.");
		return true;
	}
}
