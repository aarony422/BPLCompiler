
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
