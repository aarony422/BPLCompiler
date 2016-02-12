/* BPLScannerException that extends the java Exception class
 * 
 * Name: Aaron (Shang Wei) Young
 * CSCI 331 Compilers Spring 2016
*/

@SuppressWarnings("serial")
public class BPLScannerException extends Exception {
	public BPLScannerException() {}
	
	public BPLScannerException(String message) {
		super(message);
	}
}
