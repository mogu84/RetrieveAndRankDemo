package com.bluemix;


public class MainTester {

	public static void main(String[] args) {
		
		// Initialize BController. The Parameter is a file which has the predefined parameters used by the program.
		BController c = new BController("/connection.dat");
		/*
		// Start the parsing of the wikipediadump file.
		c.startParser("src/main/resources/technical_drawing_wikidump.xml");
		
		// Create a cluster into the Retrieve and Rank service and upload the solr_config.zip-file.
		c.createCluster();
		
		// Create a collection into the Retrieve and Rank -service.
		c.createCollection();
		
		// add the documents into the Retrieve and Rank -service.
		c.addDocuments();
		
		// generates the Trainingdata.csv-file used by the Retrieve and Rank -service.
		c.generateTrainingData();
		
		// create a Ranker and start it with the Trainingdata.csv training data.
		c.createRanker();
		
		// a test query string
		String queryMsg = "What is French curve?";
		
		// make a standard query into the Lucene database
		c.makeStandardQuery( queryMsg );
		
		// make Search and Rank query into the Retrieve and Rank -service.
		c.searchAndRankQuery( queryMsg );
		*/
	}

}
