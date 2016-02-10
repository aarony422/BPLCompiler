import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.StringBuilder;

public class BPLScanner {
	private BufferedReader bufferedReader;
	private String inputFileName;
	public Token nextToken;
	private int currLineNum;
	private String currLine;
	private int currIndex;
	private StringBuilder currTokenValue;
	
	public BPLScanner(String inputFileName) {
		this.inputFileName = inputFileName;
		this.bufferedReader = getBufferedReader();
		this.nextToken = null;
		this.currLineNum = 0;
		this.currLine = "";
		this.currIndex = 0;
		this.currTokenValue = new StringBuilder();
	}
	
	// constructor for testing
	public BPLScanner(int currLineNum, String currLine, int currIndex) {
		this.nextToken = null;
		this.currLineNum = currLineNum;
		this.currLine = currLine;
		this.currIndex = currIndex;
		this.currTokenValue = new StringBuilder();
	}
	
	public void getNextToken() throws BPLScannerException {
		if (currLineNum == 0 || endOfLine() ) {
			readNextLine();
		}
		
		if (isCurrLetter()) {
			while (!endOfLine() && isCurrLetterOrDigit()) {
				currTokenValue.append(currChar());
				currIndex++;
			}
			if (!endOfLine() && isCurrSpace()) {
				nextToken = new Token(Kind.T_ID, currTokenValue.toString(), currLineNum);
				currIndex++;
				currTokenValue = new StringBuilder();
			} else {
				throw new BPLScannerException("Scanner Error: Scanning " + Kind.T_ID + ": Line " + currLineNum);
			}
		}
	}
	
	private boolean isCurrSpace() {
		return Character.isWhitespace(currChar());
	}
	
	private char currChar() {
		return currLine.charAt(currIndex);
	}
	
	private boolean endOfLine() {
		return currIndex >= currLine.length();
	}
	
	private boolean isCurrLetter() {
		return Character.isLetter(currLine.charAt(currIndex));
	}
	
	private boolean isCurrLetterOrDigit() {
		return Character.isLetterOrDigit(currLine.charAt(currIndex));
	}
	
	private void readNextLine() {
		try {
			currLine = bufferedReader.readLine();
		} catch (IOException e) {
			System.out.println("Error: Error reading file '" + inputFileName + "'");
			System.exit(1);
		}
		currLineNum++;
		currIndex = 0;
	}
	
	private BufferedReader getBufferedReader() {
		BufferedReader bufferedReader = null;
		try {
			FileReader filereader = new FileReader(this.inputFileName);
			bufferedReader = new BufferedReader(filereader);
		} catch (FileNotFoundException e) {
			System.out.println("Error: Unable to open file '" + this.inputFileName + "'");
			System.exit(1);
		}
		return bufferedReader;
	}
}
