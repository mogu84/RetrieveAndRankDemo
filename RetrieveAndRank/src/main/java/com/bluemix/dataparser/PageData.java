package com.bluemix.dataparser;

/**
 * A holder for one Wikipedia Pages data.
 *  
 * @author Ville Kontturi
 */
public class PageData {
	private String title = "";
	private int id = 0;
	private int revId = 0;
	private StringBuilder text;
	
	public PageData() {
		text = new StringBuilder();
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setPageId(int id) {
		this.id = id;
	}
	
	public void setRevId(int revId) {
		this.revId = revId;
	}
	
	public void setText(StringBuilder text) {
		this.text = text;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public int getPageId() {
		return this.id;
	}
	
	public int getRevId() {
		return this.revId;
	}
	
	public String getText() {
		return this.text.toString();
	}
	
	public StringBuilder getText1() {
		return this.text;
	}
}
