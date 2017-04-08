package com.bluemix.dataparser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * XmlParser objective is to parse Wikipedia XML dumps into readable author blocks.
 * 
 * The end product will be ArrayList<PageData> object.
 * 
 * @author Ville Kontturi
 */
public class XmlParser {
	
	private String xml_address;
	private ArrayList<PageData> data;
	
	public XmlParser() {}
	
	/** 
	 * <h3>public XmlParser(String fileAddress)</h3>
	 * File address in format: "src/main/resources/wikipedia.xml".
	 * <br /><br />
	 * @param fileAddress	URL-location of the xml-file in String-format.
	 */	
	public XmlParser(String fileAddress) {
		this.xml_address = fileAddress;
		data = new ArrayList<PageData>();
	}
	
	/** 
	 * <h3>public boolean start()</h3>
	 * Starts the xml-document parsing.<br /><br />
	 * 
	 * @return 	<table>
	 * 				<tr>
	 * 					<td><b>true</b></td>
	 * 					<td>if no errors existed within the parsing.</td>
	 * 				</tr>
	 * 				<tr>
	 * 					<td><b>false</b></td>
	 * 					<td>if errors were encountered.</td>
	 * 				</tr>
	 * 			</table>
	 */
	public boolean start() {
		boolean bTitle = false; 
		boolean bId = false;
		boolean bRevid = false; 
		boolean bText = false; 
		boolean gId = false;
		boolean getChars = false;
		int numberOfErrors = 0;
		
		
		if ( !xml_address.isEmpty() ) {
			try {
				XMLInputFactory factory = XMLInputFactory.newInstance();
				XMLStreamReader streamReader = factory.createXMLStreamReader( new FileInputStream(xml_address), "utf-8" );
		 
				PageData d = new PageData();
		 
				while (streamReader.hasNext()) { 
					int eventType = streamReader.next();
		 
					switch (eventType) {
					case XMLStreamConstants.START_ELEMENT: 
						String qName = streamReader.getName().getLocalPart();
					
						// Start Element : page
						if (qName.equalsIgnoreCase("page")) { 
							gId = true; 
							
						// Start Element : revision
						} else if (qName.equalsIgnoreCase("revision")) { 
							gId = false; 
						} else if (qName.equalsIgnoreCase("title")) { 
							bTitle = true; 
							getChars = true;
						} else if (qName.equalsIgnoreCase("id") && gId) { 
							bId = true; getChars = true; 
						} else if (qName.equalsIgnoreCase("id") && !gId) { 
							bRevid = true;
							getChars = true; 
						} else if (qName.equalsIgnoreCase("text")) { 
							bText = true; 
							getChars = true; 
						}
					break;
					
					case XMLStreamConstants.CHARACTERS: 
						if (getChars) { 
							// Title:
							if (bTitle) { 
								String temp = streamReader.getText(); 
								d.setTitle(temp); 
								bTitle = false; 
								getChars = false; 
							} 
							
							// Page Id:
							if (bId && gId) { 
								int temp = Integer.parseInt(streamReader.getText());  
								d.setPageId(temp); 
								getChars = false; 
								
							// Revision Id:
							} if (bId && bRevid && !gId) {
								int temp = Integer.parseInt(streamReader.getText());
								d.setRevId(temp);
								bRevid = false; 
								bId = false;
								getChars = false;
								
							// Text
							} if (bText) {
								StringBuilder temp = new StringBuilder();
								
								while (streamReader.isCharacters()) {
									streamReader.next();
								}
								d.setText(temp); 
								bText = false; 
								getChars = false;
							}
						}
					break;
				
					case XMLStreamConstants.END_ELEMENT:
						// End Element : page
						if (streamReader.getName().getLocalPart().equalsIgnoreCase("page")) { 
							TextNormaliser normalizer = new TextNormaliser(d);
							normalizer.start();
							data.add(d);
							d = new PageData();
							
						// End Element : revision
						} else if (streamReader.getName().getLocalPart().equalsIgnoreCase("revision")) { 
							gId = false; 
						} 
					break;
				}
			}
		 
			streamReader.close();
			data.trimToSize();
			 
			} catch (FileNotFoundException e) {
				 e.printStackTrace();
				 numberOfErrors++;
			} catch (XMLStreamException e) { 
				 e.printStackTrace();
				 numberOfErrors++;
			}
				
			
			if( numberOfErrors > 0 ) {
				System.out.println("Parser encountered " + numberOfErrors + " errors.");
				return false;
			}
			
			/* THIS TO WRITE THE PARSED DOCUMENTS INTO ONE HTML-DOCUMENT.  Intended only for testing, verification and generation of training questions(easier from a html-file) purposes.
			try {
			PrintWriter out = new PrintWriter("wikipedia.txt");
			
			out.println("<!DOCTYPE html>\n"
						+ "<html>\n"
							+ "<head>\n"
								+ "<meta charset=\"UTF-8\">\n"
								+ "<title>Wikipedia articles</title>\n"
							+ "</head>\n"
							+ "<style>\n"
							+ "\tp {\n"
							+ "\t\tfont: 18px \"Lucida Sans Unicode\", \"Lucida Grande\", sans-serif;\n"
							+ "\t\tmargin-left: 20px;}\n\n"
							+ "\tbody {\n"
							+ "\t\twidth: 900px;} \n\n"
							+ "\tdiv {\n"
							+ "\t\tpadding: 15px 50px 25px 25px;\n"
							+ "\t\tbackground-color: #f0f0bb;\n"
							+ "\t\tmargin: 15px 0; }\n"
							+ "</style>"
						+ "<body>");
			for( PageData d : data ) {
					out.println("<div>");
						out.print("<h1>");
							out.print(d.getTitle() + " [" + d.getPageId() + "]");
						out.println("</h1>");
						
						out.print("<p>");
							out.print(d.getText().replaceAll("\n", "<br />"));
						out.println("</p>");
					out.println("</div>");
					out.println();
			}
			out.print("</body>\n"
					+ "</html>");
			
			out.close();
			
			} catch( FileNotFoundException e) {
				System.out.println("Error: " + e.getLocalizedMessage() );
			}
			*/
			
			
			return true;
		} else {
			System.out.println("Error: the xml-file address is wrong/missing (" + xml_address + ").");
			return false;
		}	
	}
	
	/**
	 * <h3>public void setFileAddress(String fileAddress)</h3>
	 * <p>Sets the fileAddress location, if it wasn't set by the public XmlParser(String fileAddress) construtor.</p>
	 * 
	 * @param fileAddress
	 */
	public void setFileAddress(String fileAddress) {
		this.xml_address = fileAddress;
		data = new ArrayList<PageData>();
	}

	public ArrayList<PageData> getList() {
		return this.data;
	}
}