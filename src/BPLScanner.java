import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.StringBuilder;
import java.util.HashMap;

public class BPLScanner {
	private BufferedReader bufferedReader;
	private String inputFileName;
	public Token nextToken;
	private int currLineNum;
	private String currLine;
	private int currIndex;
	private StringBuilder currTokenValue;
	private HashMap<String, Kind> keywordMap;
	private HashMap<String, Kind> symbolMap;
	
	public BPLScanner(String inputFileName) {
		this.inputFileName = inputFileName;
		this.bufferedReader = getBufferedReader();
		this.nextToken = null;
		this.currLineNum = 0;
		this.currLine = "";
		this.currIndex = 0;
		this.currTokenValue = new StringBuilder();
		this.keywordMap = new HashMap<String, Kind>();
		this.symbolMap = new HashMap<String, Kind>();
		
		String[] keywords = {"int", "void", "string", "if", "else", "while", "return", "write", "writeln", "read"};
		Kind[] keywordKinds = {Kind.T_INT, Kind.T_VOID, Kind.T_STR, Kind.T_IF, Kind.T_ELSE, Kind.T_WHILE, 
				Kind.T_RETURN, Kind.T_WRITE, Kind.T_WRITELN, Kind.T_READ};
		
		for (int i = 0; i < keywords.length; i++) {
			keywordMap.put(keywords[i], keywordKinds[i]);
		}
		
		String[] symbols = {";", ",", "[", "]", "{", "}", "(", ")", "<", "<=", "==", "!=", ">=", ">", "+", "-", "*", 
				"/", "=", "%", "&", "/*", "*/"};
		Kind[] symbolKinds = {Kind.T_SEMICOLON, Kind.T_COMMA, Kind.T_LBRACKET, Kind.T_RBRACKET, Kind.T_LBRACE, Kind.T_RBRACE, 
				Kind.T_LPAREN, Kind.T_RPAREN, Kind.T_LESS, Kind.T_LEQ, Kind.T_DOUBLEEQ, Kind.T_NEQ, Kind.T_GEQ, Kind.T_GREATER,
				Kind.T_PLUS, Kind.T_MINUS, Kind.T_ASTERISK, Kind.T_FSLASH, Kind.T_EQ, Kind.T_PERCENT, Kind.T_AMPERSAND, Kind.T_LCOMMENT,
				Kind.T_RCOMMENT};
		
		for (int i = 0; i < symbols.length; i++) {
			symbolMap.put(symbols[i], symbolKinds[i]);
		}
	}
	
	// constructor for testing
	public BPLScanner(int currLineNum, String currLine, int currIndex) {
		this.nextToken = null;
		this.currLineNum = currLineNum;
		this.currLine = currLine;
		this.currIndex = currIndex;
		this.currTokenValue = new StringBuilder();
		this.keywordMap = new HashMap<String, Kind>();
		this.symbolMap = new HashMap<String, Kind>();
		
		String[] keywords = {"int", "void", "string", "if", "else", "while", "return", "write", "writeln", "read"};
		Kind[] keywordKinds = {Kind.T_INT, Kind.T_VOID, Kind.T_STR, Kind.T_IF, Kind.T_ELSE, Kind.T_WHILE, 
				Kind.T_RETURN, Kind.T_WRITE, Kind.T_WRITELN, Kind.T_READ};
		
		for (int i = 0; i < keywords.length; i++) {
			keywordMap.put(keywords[i], keywordKinds[i]);
		}
		
		String[] symbols = {";", ",", "[", "]", "{", "}", "(", ")", "<", "<=", "==", "!=", ">=", ">", "+", "-", "*", 
				"/", "=", "%", "&", "/*", "*/"};
		Kind[] symbolKinds = {Kind.T_SEMICOLON, Kind.T_COMMA, Kind.T_LBRACKET, Kind.T_RBRACKET, Kind.T_LBRACE, Kind.T_RBRACE, 
				Kind.T_LPAREN, Kind.T_RPAREN, Kind.T_LESS, Kind.T_LEQ, Kind.T_DOUBLEEQ, Kind.T_NEQ, Kind.T_GEQ, Kind.T_GREATER,
				Kind.T_PLUS, Kind.T_MINUS, Kind.T_ASTERISK, Kind.T_FSLASH, Kind.T_EQ, Kind.T_PERCENT, Kind.T_AMPERSAND, Kind.T_LCOMMENT,
				Kind.T_RCOMMENT};
		
		for (int i = 0; i < symbols.length; i++) {
			symbolMap.put(symbols[i], symbolKinds[i]);
		}
	}
	
	// TODO: hasNextToken
	public boolean hasNextToken() {
		return (nextToken.getKind() != Kind.T_EOF);
	}
	
	public void getNextToken() throws BPLScannerException {
		if (currLineNum == 0 || endOfLine() && currLine != null) {
			readNextLine();
		}
		
		while (isCurrSpace() && currLine != null) {
			if (endOfLine()) {
				readNextLine();
			}
			while (!endOfLine() && isCurrSpace() && currLine != null) {
				currIndex++;
			}
		}
		
		if (currLine == null) {
			nextToken = new Token(Kind.T_EOF, "EOF", currLineNum);
			return;
		}
		
		if (isCurrLetter()) {
			while (!endOfLine() && (isCurrLetterOrDigit() || isCurrUnderScore())) {
				currTokenValue.append(currChar());
				currIndex++;
			}
			if (keywordMap.containsKey(currTokenValue.toString())) {
				nextToken = new Token(keywordMap.get(currTokenValue.toString()), currTokenValue.toString(), currLineNum);
				currTokenValue = new StringBuilder();
			} else {
				nextToken = new Token(Kind.T_ID, currTokenValue.toString(), currLineNum);
				currTokenValue = new StringBuilder();
			}
			
		} else if (isCurrDigit()) {
			while (!endOfLine() && isCurrDigit()) {
				currTokenValue.append(currChar());
				currIndex++;
			}
			nextToken = new Token(Kind.T_NUM, currTokenValue.toString(), currLineNum);
			currTokenValue = new StringBuilder();
		} else {
			currTokenValue.append(currChar());
			currIndex++;
			if (!endOfLine()) {
				currTokenValue.append(currChar());
				currIndex++;
			}
			if (currTokenValue.length() == 2 && symbolMap.containsKey(currTokenValue.toString())) {
				nextToken = new Token(symbolMap.get(currTokenValue.toString()), currTokenValue.toString(), currLineNum);
				currTokenValue = new StringBuilder();
			} else {
				if (currTokenValue.length() == 2) {
					currTokenValue.setLength(currTokenValue.length()-1);
					currIndex--;
				}
				if (symbolMap.containsKey(currTokenValue.toString())) {
					nextToken = new Token(symbolMap.get(currTokenValue.toString()), currTokenValue.toString(), currLineNum);
					currTokenValue = new StringBuilder();
				} else if (currTokenValue.toString().equals("\"")){
					currTokenValue.setLength(0);
					while (!endOfLine() && !isCurrQuote()) {
						currTokenValue.append(currChar());
						currIndex++;
					}
					
					if (!endOfLine()) {
						currIndex++;
						nextToken = new Token(Kind.T_STRING, currTokenValue.toString(), currLineNum);
						currTokenValue = new StringBuilder();
					} else {
						throw new BPLScannerException("Scanner Error: at BPLScanner.getNextToken: Unclosed quotes or unallowed multi-line string (" + inputFileName + ":"+ currLineNum + ")");
					}
				} else {
					throw new BPLScannerException("Scanner Error: at BPLScanner.getNextToken: Scanning symbol (" + inputFileName + ":"+ currLineNum + ")");
				}
			}
		}
	}
	
	private boolean isCurrQuote() {
		return currChar() == '\"';
	}
	
	private boolean isCurrUnderScore() {
		return currChar() == '_';
	}
	
	private boolean isCurrDigit() {
		return Character.isDigit(currChar());
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
