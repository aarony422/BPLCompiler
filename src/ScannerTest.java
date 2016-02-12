/* Program takes the name of a BPL file, opens file, and repeatedly calls
 * getNextToken(), printing each token found until it gets to the end of the
 * file.
 * 
 * Name: Aaron (Shang Wei) Young
 * CSCI 331 Compilers Spring 2016
*/

public class ScannerTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String inputFileName = "sample_programs/selectionSort.bpl";
		BPLScanner scanner;
		
		//inputFileName = args[0];
		scanner = new BPLScanner(inputFileName);
		while (scanner.hasNextToken()) {
			try {
				scanner.getNextToken();
				System.out.println(scanner.nextToken);
			} catch (BPLScannerException e) {
				System.out.println(e);
			}
		}

	}

}
