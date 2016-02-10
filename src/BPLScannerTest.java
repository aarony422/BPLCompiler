import static org.junit.Assert.*;

import org.junit.Test;

public class BPLScannerTest {

	@Test
	public void ScanIDWithSpace() {
		BPLScanner scanner = new BPLScanner(2, "num ", 0);
		try {
			scanner.getNextToken();
		} catch (BPLScannerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Token correctToken = new Token(Kind.T_ID, "num", 2);
		Token testToken = scanner.nextToken;
		assertEquals(correctToken, testToken);
	}
	
	@Test(expected=BPLScannerException.class)
	public void ScanThrowsBPLScannerException() throws BPLScannerException {
		BPLScanner scanner = new BPLScanner(2, "num", 0);
		scanner.getNextToken();
	}

}
