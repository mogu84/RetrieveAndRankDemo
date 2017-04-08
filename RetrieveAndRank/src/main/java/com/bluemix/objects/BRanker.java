package com.bluemix.objects;

import java.io.File;

import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.RetrieveAndRank;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.model.Ranker;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.model.Ranker.Status;

public class BRanker {
	private RetrieveAndRank service;
	private String name;
	private String id;
	
	public BRanker(RetrieveAndRank service) {
		this.service = service;
	}
	
	public String getId() {
		return this.id;
	}
	public void setId(String rankerId) {
		this.id = rankerId;
	}
	public String getName() {
		return this.name;
	}
	public void setName(String rankerName) {
		this.name = rankerName;
	}
	
	
	public String create(String rankerName, String trainingDataLocation) {
		this.name = rankerName;
		
		File f = new File(trainingDataLocation);
		Ranker ranker = service.createRanker(rankerName, f).execute();
		
		System.out.println(ranker);
		
		this.id = ranker.getId();
		
		if( ranker.getStatus() == Status.TRAINING )
			return ranker.getId();
		else 
			return "";
	}
	public boolean remove(String id) {
		if( id.compareToIgnoreCase("") == 0 ) {
			service.deleteRanker(this.id).execute();
			id = this.id;
		} else {
			service.deleteRanker(id).execute();
		}
		
		System.out.println("Ranker {" + id + "} is now removed!");
		return true;
	}
	
	public void getRankerInfo() {
		Ranker ranker = service.getRankerStatus(this.id).execute();
		System.out.println(ranker);
	}
}
