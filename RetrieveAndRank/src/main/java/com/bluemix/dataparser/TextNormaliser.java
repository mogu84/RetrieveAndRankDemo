package com.bluemix.dataparser;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * TextNormilizer's objective is to clearify the retrieved text from the Wikipedia xml dumps 
 * as they still have some useless texts such as <ref>data</ref> and [[data]] blocks 
 * that we can safely remove/ignore.
 *  
 * @author Ville Kontturi
 */
public class TextNormaliser {

	private PageData data;
	private String section = "";
	
	/**
	 * <h3>public TextNormilizer(PageData data)</h3>
	 * <p>Because raw wikipedia xml-data is not in plain text or in html format, we need to normalise the data.</p>
	 *  
	 * @param data	The PageData object which start() will normalise.
	 */
	public TextNormaliser(PageData data) {
		this.data = data;
	}
	
	/**
	 * <h3>public boolean start()</h3>
	 * <p>Once the data has been stored (in <u>public TextNormaliser(PageData data)</u>) this starts the process.<br /><br />
	 * 
	 * The process will normalise the original </p>
	 * 
	 * @return
	 * <table>
	 * 	<tr>
	 * 		<td><b>true</b></td>
	 * 		<td>If normalisation went through without any problems.</td>
	 *	<tr>
	 *		<td><b>false</b></td>
	 *		<td>If normaliser encountered errors during the process. The error messages are printed on the console.</td>
	 *	</tr>
	 * </table>
	 */
	public boolean start() {
		int refStart = 0;
		int refEnd = 0;
		
		try {
			// {{snds}} to '-'
			while( ( refStart = data.getText1().indexOf( "{{snds}}", refStart ) ) != -1 ) {
				section = "{{snds}}";
				data.getText1().replace(refStart, refStart+8, " - ");
			}

			// &nbsp; to ' '
			while( ( refStart = data.getText1().indexOf( "&nbsp;", refStart ) ) != -1 ) {
				section = "&nbsp;";
				data.getText1().replace(refStart, refStart+6, " ");
			}
			
			// &ndash; to ' '
			while( ( refStart = data.getText1().indexOf( "&ndash;", refStart ) ) != -1 ) {
				section = "&nbsp;";
				data.getText1().replace(refStart, refStart+7, "-");
			}
			
			// <br /> to '\n'
			while( ( refStart = data.getText1().indexOf( "<br />", refStart ) ) != -1 ) {
				section = "<br />";
				data.getText1().replace(refStart, refStart+6, "\n");
			}
			
			// removing <ref texts
			while( ( refStart = data.getText1().indexOf( "<ref", refStart ) ) != -1 ) {
				section = "<ref";
				refEnd = data.getText1().indexOf(">", refStart+1) + 1;
				
				if ( data.getText1().charAt(refEnd - 2 ) == '/' ) {	// case of: <ref name="abc" />
					data.getText1().delete(refStart, refEnd);
				} else {											// case of: <ref>something</ref>
					refEnd = data.getText1().indexOf("</ref>", refStart) + 6;
					data.getText1().delete(refStart, refEnd);
				}
			}
			
			// removing <blockquote>abc</blockquote> texts
			while( ( refStart = data.getText1().indexOf( "<blockquote>", refStart ) ) != -1 ) {
				if( data.getPageId() == 823788) {
					section = "1";
				}
				section = "<blockquote>";
				
				int startInt = data.getText1().indexOf("</blockquote>", refStart);
				data.getText1().delete(startInt, startInt+14);
				
				data.getText1().delete(refStart, refStart + 12);
			}

			// removing {|abc|} section
			while( (refStart = data.getText1().indexOf("{|", refStart)) != -1) {
				section = "==Gallery==";
				refEnd = data.getText1().indexOf("|}", refStart + 2);
				data.getText1().delete(refStart, refEnd + 3);
			}
			
			// removing &lt;!--===Secondary Sources===--&gt; texts
			while( ( refStart = data.getText1().indexOf( "<!--===Secondary Sources===-->", refStart ) ) != -1 ) {
				section = "<!--===Secondary Sources===-->";
				data.getText1().delete(refStart, refStart+30);
			}

			// removing ==Gallery== section
			while( (refStart = data.getText1().indexOf("==Gallery==", refStart)) != -1) {
				section = "==Gallery==";
				if( ( refEnd = data.getText1().indexOf("==", refStart + 11) ) == -1 ) {
					refEnd = data.getText1().length();
				}
				data.getText1().delete(refStart, refEnd);
			}

			// removing ==Gallery of architectural works== section
			while( (refStart = data.getText1().indexOf("==Gallery of architectural works==", refStart)) != -1) {
				section = "==Gallery of architectural works==";
				if( ( refEnd = data.getText1().indexOf("==", refStart + 34) ) == -1 ) {
					refEnd = data.getText1().length();
				}
				data.getText1().delete(refStart, refEnd);
			}

			// removing <gallery texts
			while( ( refStart = data.getText1().indexOf( "<gallery", refStart ) ) != -1 ) {
				section = "<gallery";
					refEnd = data.getText1().indexOf("</gallery>", refStart + 9) + 10;
					data.getText1().delete(refStart, refEnd);
			}

			// removing ==Further reading== section
			while( (refStart = data.getText1().indexOf("==Further reading==", refStart)) != -1) {
				section = "==Furhter reading==";
				if( ( refEnd = data.getText1().indexOf("==", refStart + 19) ) == -1 ) {
					refEnd = data.getText1().length();
				}
				data.getText1().delete(refStart, refEnd);
			}
			
			// removing ==See also== section
			while( (refStart = data.getText1().indexOf("==See also==", refStart)) != -1) {
				section = "==See also==";
				if( ( refEnd = data.getText1().indexOf("==", refStart + 12) ) == -1 ) {
					refEnd = data.getText1().length();
				}
				data.getText1().delete(refStart, refEnd);
			}

			// removing == See also == section
			while( (refStart = data.getText1().indexOf("== See also ==", refStart)) != -1) {
				section = "== See also ==";
				if( ( refEnd = data.getText1().indexOf("==", refStart + 14) ) == -1 ) {
					refEnd = data.getText1().length();
				}
				data.getText1().delete(refStart, refEnd);
			}

			// removing ==Sources== section
			while( (refStart = data.getText1().indexOf("==Sources==", refStart)) != -1) {
				section = "==Sources==";
				if( ( refEnd = data.getText1().indexOf("==", refStart + 11) ) == -1 ) {
					refEnd = data.getText1().length();
				}
				data.getText1().delete(refStart, refEnd);
			}
			
			// removing ==Notes== section
			while( (refStart = data.getText1().indexOf("==Notes==", refStart)) != -1) {
				section = "==Notes==";
				if( ( refEnd = data.getText1().indexOf("==", refStart + 9) ) == -1 ) {
					refEnd = data.getText1().length();
				}
				data.getText1().delete(refStart, refEnd);
			}

			// removing ==References== section
			while( (refStart = data.getText1().indexOf("==References==", refStart)) != -1) {
				section = "==References==";
				if( (refEnd = data.getText1().indexOf("==", refStart + 14) ) == -1 ) {
					refEnd = data.getText1().length();
				}
				data.getText1().delete(refStart, refEnd);
			}

			// removing == References == section
			while( (refStart = data.getText1().indexOf("== References ==", refStart)) != -1) {
				section = "== References ==";
				if( (refEnd = data.getText1().indexOf("==", refStart + 16) ) == -1 ) {
					refEnd = data.getText1().length();
				}
				data.getText1().delete(refStart, refEnd);
			}
			
			// removing ==External links== section
			while( (refStart = data.getText1().indexOf("==External links==", refStart)) != -1) {
				section = "==External links==";
				if( (refEnd = data.getText1().indexOf("==", refStart + 18) ) == -1 ) {
					refEnd = data.getText1().length();
				}
				data.getText1().delete(refStart, refEnd);
			}

			// removing == External links == section
			while( (refStart = data.getText1().indexOf("== External links ==", refStart)) != -1) {
				section = "== External links ==";
				if( (refEnd = data.getText1().indexOf("==", refStart + 20) ) == -1 ) {
					refEnd = data.getText1().length();
				}
				data.getText1().delete(refStart, refEnd);
			}

			// removing == Footnotes == section
			while( (refStart = data.getText1().indexOf("== Footnotes ==", refStart)) != -1) {
				section = "== External links ==";
				if( (refEnd = data.getText1().indexOf("==", refStart + 15) ) == -1 ) {
					refEnd = data.getText1().length();
				}
				data.getText1().delete(refStart, refEnd);
			}
			
			// removing ==Bibliography== section
			while( (refStart = data.getText1().indexOf("==Bibliography==", refStart)) != -1) {
				section = "==Bibliography==";
				if( (refEnd = data.getText1().indexOf("==", refStart + 16) ) == -1 ) {
					refEnd = data.getText1().length();
				}
				data.getText1().delete(refStart, refEnd);
			}
			
			// removing == Bibliography == section
			while( (refStart = data.getText1().indexOf("== Bibliography ==", refStart)) != -1) {
				section = "== Bibliography ==";
				if( (refEnd = data.getText1().indexOf("==", refStart + 18) ) == -1 ) {
					refEnd = data.getText1().length();
				}
				data.getText1().delete(refStart, refEnd);
			}
			
			// removing ==Additional images== section
			while( (refStart = data.getText1().indexOf("==Additional images==", refStart)) != -1) {
				section = "==Additional images==";
				if( (refEnd = data.getText1().indexOf("==", refStart + 21) ) == -1 ) {
					refEnd = data.getText1().length();
				}
				data.getText1().delete(refStart, refEnd);
			}
			
			// fixing {{convert section
			while( (refStart = data.getText1().indexOf("{{convert", refStart)) != -1) {
				section = "{{convert";
				if( (refEnd = data.getText1().indexOf("}}", refStart + 9) ) == -1 ) {
					refEnd = data.getText1().length();
				}
				String temp = data.getText1().substring(refStart+10, refEnd);
				
				int latest;
				int last = 0;
				String from = "";
				String to = "";
				int unit = 0;
				ArrayList<Double> measure = new ArrayList<Double>();
				ArrayList<String> middle = new ArrayList<String>();
				
				String text = "";
				
				boolean cont = true;
				// figure out the units that need converting
				do {
					latest = temp.indexOf('|', last);
					if( latest == -1) {
						latest = temp.length();
						cont = false;
					}
					
					try {
						measure.add( eval( temp.substring(last, latest) ) );
						last = latest + 1;
					} catch (Exception e) {
						//System.out.println("Error: " + e.getLocalizedMessage());
						String t = temp.substring(last, latest);
						switch( t ) {
						case "in":
						case "inch":
						case "inches":
							if( unit <= 0) {
								from = t;
							} else {
								to = t;
								latest = -1;
							}
							unit++;
							break;
						case "m":
							if( unit <= 0) {
								from = t;
							} else {
								to = t;
								latest = -1;
							}
							unit++;
							break;
						case "mm":
							if( unit <= 0) {
								from = t;
							} else {
								to = t;
								latest = -1;
							}
							unit++;
							break;
						case "ft":
							if( unit <= 0) {
								from = t;
							} else {
								to = t;
								latest = -1;
							}
							unit++;
							break;
						default:
							if( unit <= 0 ) {
								middle.add(t);
							} else {
								last = temp.lastIndexOf("|");
							}
							break;
						}
						last = latest+1;
					}
				
				} while( latest != -1 && cont );
				
				// text didn't tell to which unit to convert
				if( to.length() == 0) {
					switch (from) {
					case "in":
					case "inch":
					case "inches":
						to = "mm";
						break;
					case "ft":
					case "feet":
					case "foot":
						to = "m";
						break;
					case "mm":
						to = "in";
						break;
					}
				}
				
	            DecimalFormat df = new DecimalFormat("#.#");
	            df.setRoundingMode(RoundingMode.HALF_UP);
				
				if( measure.size() > 1 ) {
					for( int i = 0; i < measure.size(); i++) {
						text += df.format(measure.get(i)) + " " + from + " ";
						
						if( i < middle.size() ) {
							text += middle.get(i) + " ";
						} else {
							text += "(";
							for( int a = 0; a < measure.size(); a++) {
								if( a == (measure.size()-1) ) 
									text += df.format(convertValue(measure.get(a), from, to)) + " " + to + ")"; 
								else 
									text += df.format(convertValue(measure.get(a), from, to)) + " " + to + " ";
								
								if( a < middle.size() )
									text += middle.get(a) + " ";
							}
						}

					}
				} else {
					text += df.format(measure.get(measure.size()-1)) + " " + from +" (";
					text += df.format(convertValue(measure.get(measure.size()-1), from, to)) + " " + to + ")";
				}
				
				data.getText1().replace(refStart, refEnd+2, text);
			}
			
			// removing {{Authority control}} section
			while( (refStart = data.getText1().indexOf("{{Authority control}}", refStart)) != -1) {
				section = "{{Authority control}}";
				refEnd = data.getText1().length();
				data.getText1().delete(refStart, refEnd);
			}
			
			// correcting {{}} texts
			while( (refStart = data.getText1().indexOf("{{", refStart)) != -1) {
				section = "{{}}";
				refEnd = data.getText1().indexOf("}}", refStart) + 3;
				
				int checkLast = refStart;
				while( (checkLast = data.getText1().indexOf("{{", checkLast+2)) < refEnd && checkLast > 0 ) {
					refEnd = data.getText1().indexOf("}}", refEnd-1) + 3;
				}
				
				if( data.getText1().length() < refEnd || data.getText1().charAt(refEnd-1) != '\n' ) {
					refEnd--;
				}
				
				if( data.getText1().substring(refStart, refStart + 7).compareToIgnoreCase("{{quote") == 0 ) { // correcting {{quote texts
					int point = 0;
					
					while ( (point = data.getText1().substring(refStart, refEnd).indexOf("|", 0)) > 0) {
						point += refStart;
						
						if( data.getText1().substring(point, point+6).equalsIgnoreCase("|text=") ) {
							data.getText1().replace(point, point+6, "\"");
							refEnd = data.getText1().indexOf("}}", refStart) + 3;
							int point2 = data.getText1().substring(refStart, refEnd).indexOf("|", 0);
							
							point2 = data.getText1().substring(refStart, refStart + point2).lastIndexOf("\n");
							data.getText1().insert(refStart + point2, "\"");
						} else {
							data.getText1().delete(point, point+1);
							refEnd--;
						}
					}
					
					data.getText1().delete(refEnd-3, refEnd);
					data.getText1().delete(refStart, refStart + 8);
					
				} else { // back to {{}} correcting
					if( refStart > 0 && data.getText1().charAt(refStart-1) == '*') {
						refStart--;
					}
					data.getText1().delete(refStart, refEnd);
				}
			}
			
			// correcting [[]] texts
			while( (refStart = data.getText1().indexOf("[[", refStart)) != -1 ) {
				section = "[[]]";
				int block = 0;
				int refEndStart = 0;
				refEnd = data.getText1().indexOf("]]", refStart) + 3;
				
				int checkLast = refStart;
				while( (checkLast = data.getText1().indexOf("[[", checkLast+2)) < refEnd && checkLast > 0 ) {
					refEnd = data.getText1().indexOf("]]", refEnd-1) + 3;
				}
				
				refEndStart = refEnd-3;
				if( data.getText1().length() < refEnd || data.getText1().charAt(refEnd-1) != '\n' ) {
					refEnd--;
					refEndStart = refEnd-2;
				}
				
				if( data.getText1().substring(refStart + 2, refStart + 8).compareToIgnoreCase("image:") == 0 ) {
					section = "Image:";
					data.getText1().delete(refStart, refEnd);
				} 
				else if( data.getText1().substring(refStart+2, refStart + 7).compareToIgnoreCase("file:") == 0 ) {
					section = "File:";
					data.getText1().delete(refStart, refEnd);
				}
				else if( data.getText1().substring(refStart+2, refStart + 11).compareToIgnoreCase("category:") == 0 ) {
					section = "Category:";
					data.getText1().delete(refStart, refEnd);
				}
				else if( data.getText1().substring(refStart+2, refStart + 5).compareToIgnoreCase("nl:") == 0 ) {
					section = "nl:";
					data.getText1().delete(refStart, refEnd);
				}
				else if( (block = data.getText1().substring(refStart, refEnd-1).indexOf("|", 0)) != -1 ) {
					section = "[[a|b]]";
					block += refStart;
					data.getText1().delete(refEndStart, refEnd);
					data.getText1().delete(refStart, block+1);
				} else {
					section = "[[a]]";
					if( data.getText1().charAt(refEnd+1) == '*') {
						refEnd--;
					}
					data.getText1().delete(refEndStart, refEnd);
					data.getText1().delete(refStart, refStart + 2);
				}
				
			}
			
			// correcting [http: data] texts
			while( (refStart = data.getText1().indexOf("[", refStart)) != -1) {
				section = "[http:]";
				refEnd = data.getText1().indexOf("]", refStart) + 1;
				
				if( data.getText1().substring(refStart+1, refStart + 5).compareToIgnoreCase("http") == 0) {
					int block = 0;
					
					if( (block = data.getText1().substring(refStart, refEnd).indexOf(" ", 0) ) != -1) {
						block += refStart;
						data.getText1().deleteCharAt(refEnd-1);
						data.getText1().delete(refStart, block+1);
					} else {
						data.getText1().delete(refStart, refEnd);
					}
				} else {	// is not [http:...] but for example [sculpture]
					data.getText1().deleteCharAt(refEnd-1);
					data.getText1().delete(refStart, refStart+1);
				}
			}
			
			// removing <!--- ---> texts
			while( ( refStart = data.getText1().indexOf( "<!---", refStart ) ) != -1 ) {
				section = "<!---";
				refEnd = data.getText1().indexOf("--->", refStart+4) + 1;
				data.getText1().delete(refStart, refEnd+4);
			}
			
			// removing <!-- --> texts
			while( ( refStart = data.getText1().indexOf( "<!--", refStart ) ) != -1 ) {
				section = "<!--";
				
				if( (refEnd = data.getText1().indexOf("-->", refStart+4) + 1) == 0 ) {
					refEnd = refStart;
				}

				data.getText1().delete(refStart, refEnd + 3);
			}
			
			// trimming line breaks at the start of text
			while ( data.getText1().length() > 0 && data.getText1().charAt(0) == '\n' ) {
				data.getText1().deleteCharAt(0);
			}
			
			// trimming line breaks at the end of text
			while ( data.getText1().length() > 0 && data.getText1().charAt(data.getText1().length()-1) == '\n' ) {
				data.getText1().deleteCharAt(data.getText1().length()-1);
			}
			
		} catch (Exception e) {
			System.out.println("\t/*******************"
					+ "\n\t* TextNormilizer failed: " + e.getMessage() 
					+ "\n\t* Of: " + section
					+ "\n\t* In: " + data.getTitle() + "\t" + data.getPageId()
					+ "\n\t* Parsing: " + data.getText1().substring(refStart, refEnd-1)
					+ "\n\t* Start at: " + refStart
					+ "\n\t* End at: " + refEnd
					+ "\n\t*******************/" );
			return false;
		}
		
		return true;
	}
	
	
	private static double eval(final String str) {
	    return new Object() {
	        int pos = -1, ch;

	        void nextChar() {
	            ch = (++pos < str.length()) ? str.charAt(pos) : -1;
	        }

	        boolean eat(int charToEat) {
	            while (ch == ' ') nextChar();
	            if (ch == charToEat) {
	                nextChar();
	                return true;
	            }
	            return false;
	        }

	        double parse() {
	            nextChar();
	            double x = parseExpression();
	            if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
	            return x;
	        }

	        // Grammar:
	        // expression = term | expression `+` term | expression `-` term
	        // term = factor | term `*` factor | term `/` factor
	        // factor = `+` factor | `-` factor | `(` expression `)`
	        //        | number | functionName factor | factor `^` factor

	        double parseExpression() {
	            double x = parseTerm();
	            for (;;) {
	                if      (eat('+')) x += parseTerm(); // addition
	                else if (eat('-')) x -= parseTerm(); // subtraction
	                else return x;
	            }
	        }

	        double parseTerm() {
	            double x = parseFactor();
	            for (;;) {
	                if      (eat('*')) x *= parseFactor(); // multiplication
	                else if (eat('/')) x /= parseFactor(); // division
	                else return x;
	            }
	        }

	        double parseFactor() {
	            if (eat('+')) return parseFactor(); // unary plus
	            if (eat('-')) return -parseFactor(); // unary minus

	            double x;
	            int startPos = this.pos;
	            if (eat('(')) { // parentheses
	                x = parseExpression();
	                eat(')');
	            } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
	                while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
	                x = Double.parseDouble(str.substring(startPos, this.pos));
	            } else if (ch >= 'a' && ch <= 'z') { // functions
	                while (ch >= 'a' && ch <= 'z') nextChar();
	                String func = str.substring(startPos, this.pos);
	                x = parseFactor();
	                if (func.equals("sqrt")) x = Math.sqrt(x);
	                else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
	                else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
	                else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
	                else throw new RuntimeException("Unknown function: " + func);
	            } else {
	                throw new RuntimeException("Unexpected: " + (char)ch);
	            }

	            if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation
	            
	            return x;
	        }
	    }.parse();
	}
	
	private static double convertValue(double value, String from, String to) {
		double newValue = 0.0;
		
		switch(from) {
		case "in":
			switch(to) {
			case "mm":
				newValue = value * 25.4;
				break;
			case "m":
				newValue = value * 0.0254;
				break;
			default:
				newValue = value * 25.4;
				break;
			}
			break;
		case "inch":
			switch(to) {
			case "mm":
				newValue = value * 25.4;
				break;
			case "m":
				newValue = value * 0.0254;
				break;
			default:
				newValue = value * 25.4;
				break;
			}
			break;
		case "inches":
			switch(to) {
			case "mm":
				newValue = value * 25.4;
				break;
			case "m":
				newValue = value * 0.0254;
				break;
			default:
				newValue = value * 25.4;
				break;
			}
			break;
		case "mm":
			switch (to) {
			case "in":
				newValue = value * 0.0393700787;
				break;
			case "inch":
				newValue = value * 0.0393700787;
				break;
			case "inches":
				newValue = value * 0.0393700787;
				break;
			default:
				newValue = value * 0.0393700787;
				break;
			}
			break;
		case "ft":
			switch (to) {
			case "m":
				newValue = value * 0.3048;
				break;
			case "cm":
				newValue = value * 30.48;
				break;
			default:
				newValue = value * 0.3048;
				break;
			}
			break;
		}
		return newValue;
	}
	
}
