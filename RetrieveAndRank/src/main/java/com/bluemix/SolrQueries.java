package com.bluemix;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;


public class SolrQueries {

	public SolrQueries() {
		
	}
	
	public void createQuery(String queryMsg, String collectionConfigName, HttpSolrClient solrClient ) {
		
		SolrQuery query = new SolrQuery( "doc:(" + queryMsg + ")" )
				.setParam("fl", "pageId")
				.setIncludeScore(true)
				.setRows(3)
				.setRequestHandler("select");
		
		QueryResponse response;
		
		try {
			response = solrClient.query(collectionConfigName, query);
			System.out.println(response);
			
		} catch (SolrServerException e) {
			System.out.println( "Error occurred: " + e.getLocalizedMessage() );
		} catch (IOException e) {
			System.out.println( "Error occurred: " + e.getLocalizedMessage() );
		}
	}
	
	public void generateTrainingData(String collectionConfigName, HttpSolrClient solrClient, String fileLocOfQuestionCSV, String trainingDataSaveLoc ) {
		
		try {
			File f = new File(trainingDataSaveLoc);
			if( f.exists() && !f.isDirectory() )
				f.delete();
			
			BufferedReader bufReader = new BufferedReader( new FileReader( fileLocOfQuestionCSV ) );
			BufferedWriter bufWriter = new BufferedWriter( new FileWriter( f ) );
			
			String line = "";
			String[] items = null;
			boolean isFirstRound = true;
			System.out.println("training-data generation started...");
			int a = 0;
			while ( (line = bufReader.readLine()) != null ) {
				if( a % 3 == 0) {
					System.out.print(".");
				}
				items = line.split(";");
				
				SolrQuery query = new SolrQuery().setQuery(items[0]);
				
				String gt = "";
				
				for( int i = 1; i < items.length; i++ ) {
					gt += items[i];
					if( i < (items.length-1) )
						gt += ",";
				}
				query.setParam("gt", gt)
				.setRows(10)
				.setRequestHandler("/fcselect")
				.setParam("fl", "pageId,title")
				.setParam("returnRSInput", true)
				.setParam("wt", "json");
				

				if( isFirstRound ) {
					query.setParam("generateHeader", true);
					isFirstRound = false;
				}
				
				QueryResponse response;
				response = solrClient.query(collectionConfigName, query);
				bufWriter.write( response.getResponse().get("RSInput").toString() );
				//System.out.println(query.toString());
				bufWriter.flush();
				a++;
			}
			System.out.println("\ntraining-data creationg complete");
			
			bufReader.close();
			bufWriter.close();
			
		} catch (FileNotFoundException e) {
			System.out.println( "Error reading CSV-file: " + e.getLocalizedMessage() );
		} catch (IOException e) {
			System.out.println( "IO error: " + e.getLocalizedMessage() );
		} catch (SolrServerException e) {
			System.out.println( "Error getting feature vectors: " + e.getLocalizedMessage() );
		}
		
	}
	
	public void searchAndRank(String queryMsg, String collectionConfigName, String rankerId, HttpSolrClient solrClient) {
		SolrQuery query = new SolrQuery( "body:(" + queryMsg + ")" )
				.setParam( "ranker_id", rankerId )
				.setRequestHandler("/fcselect")
				.setIncludeScore(true)
				.setParam("fl", "pageId,ranker.confidence")
				.setRows(3);
		//System.out.println(query.toString());
		QueryResponse response;
		
		try {
			response = solrClient.query(collectionConfigName, query);
			System.out.println(response);
			
		} catch (SolrServerException e) {
			System.out.println("Query error: " + e.getLocalizedMessage() );
		} catch (IOException e) {
			System.out.println("IO error: " + e.getLocalizedMessage() );
		}
	}
}
