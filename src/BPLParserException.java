/* BPLParserException that extends the java Exception class
 * 
 * Name: Aaron (Shang Wei) Young
 * CSCI 331 Compilers Spring 2016
*/


@SuppressWarnings("serial")
public class BPLParserException extends Exception {
	public BPLParserException() {}
	
	public BPLParserException(String message) {
		super(message);
	}
}

