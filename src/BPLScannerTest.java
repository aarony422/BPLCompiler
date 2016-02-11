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
	
	@Test
	public void ScanIDWithoutSpace() {
		BPLScanner scanner = new BPLScanner(2, "num", 0);
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
	
	@Test
	public void ScanIDWithUnderScore() {
		BPLScanner scanner = new BPLScanner(2, "T_CONSTANT", 0);
		try {
			scanner.getNextToken();
		} catch (BPLScannerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Token correctToken = new Token(Kind.T_ID, "T_CONSTANT", 2);
		Token testToken = scanner.nextToken;
		assertEquals(correctToken, testToken);
	}
	
	@Test
	public void ScanNum() {
		BPLScanner scanner = new BPLScanner(2, "349      50", 0);
		try {
			scanner.getNextToken();
			scanner.getNextToken();
		} catch (BPLScannerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Token correctToken = new Token(Kind.T_NUM, "50", 2);
		Token testToken = scanner.nextToken;
		assertEquals(correctToken, testToken);
	}
	
	@Test
	public void ScanInt() {
		BPLScanner scanner = new BPLScanner(2, "int", 0);
		try {
			scanner.getNextToken();
		} catch (BPLScannerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Token correctToken = new Token(Kind.T_INT, "int", 2);
		Token testToken = scanner.nextToken;
		assertEquals(correctToken, testToken);
	}
	
	@Test
	public void ScanReturn() {
		BPLScanner scanner = new BPLScanner(2, "return", 0);
		try {
			scanner.getNextToken();
		} catch (BPLScannerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Token correctToken = new Token(Kind.T_RETURN, "return", 2);
		Token testToken = scanner.nextToken;
		assertEquals(correctToken, testToken);
	}
	
	@Test
	public void ScanSemiColon() {
		BPLScanner scanner = new BPLScanner(2, ";", 0);
		try {
			scanner.getNextToken();
		} catch (BPLScannerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Token correctToken = new Token(Kind.T_SEMICOLON, ";", 2);
		Token testToken = scanner.nextToken;
		assertEquals(correctToken, testToken);
	}
	
	@Test
	public void ScanReturnSemiColon() {
		BPLScanner scanner = new BPLScanner(2, "return;", 0);
		try {
			scanner.getNextToken();
			scanner.getNextToken();
		} catch (BPLScannerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Token correctToken = new Token(Kind.T_SEMICOLON, ";", 2);
		Token testToken = scanner.nextToken;
		assertEquals(correctToken, testToken);
	}
	
	@Test
	public void ScanLeq() {
		BPLScanner scanner = new BPLScanner(2, "num<=5", 0);
		try {
			scanner.getNextToken();
			scanner.getNextToken();
		} catch (BPLScannerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Token correctToken = new Token(Kind.T_LEQ, "<=", 2);
		Token testToken = scanner.nextToken;
		assertEquals(correctToken, testToken);
	}
	
	@Test
	public void ScanIDFollowedBySymbol() {
		BPLScanner scanner = new BPLScanner(2, "num!=5", 0);
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
	
	@Test
	public void ScanNEQ() {
		BPLScanner scanner = new BPLScanner(2, "num !=5", 0);
		try {
			scanner.getNextToken();
			scanner.getNextToken();
		} catch (BPLScannerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Token correctToken = new Token(Kind.T_NEQ, "!=", 2);
		Token testToken = scanner.nextToken;
		assertEquals(correctToken, testToken);
	}
	
	@Test
	public void ScanString() {
		BPLScanner scanner = new BPLScanner(2, "             string       \"Hello World\"", 0);
		try {
			scanner.getNextToken();
			scanner.getNextToken();
		} catch (BPLScannerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Token correctToken = new Token(Kind.T_STRING, "Hello World", 2);
		Token testToken = scanner.nextToken;
		assertEquals(correctToken, testToken);
	}
	
	@Test(expected=BPLScannerException.class)
	public void ScanThrowsBPLScannerException() throws BPLScannerException {
		BPLScanner scanner = new BPLScanner(2, "num!", 0);
		scanner.getNextToken();
		scanner.getNextToken();
	}
}
