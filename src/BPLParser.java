
public class BPLParser {
	private BPLScanner scanner;
	private Token nextToken;
	
	public BPLParser(String inputFileName) {
		this.scanner = new BPLScanner(inputFileName);
		this.nextToken = null;
	}
	
	private TreeNode program() throws BPLParserException {
		TreeNode program = new TreeNode(TreeNodeKind.PROGRAM, -1, null);
		TreeNode statement = statement();
		program.setLine(statement.getLine());
		program.addChild(statement);
		return program;
	}
	
	// TODO: change line number
	private TreeNode statement() throws BPLParserException {
		TreeNode statement = new TreeNode(TreeNodeKind.STATEMENT, -1, null);
		TreeNode expressionStmt = expressionStatement();
		statement.setLine(expressionStmt.getLine());
		statement.addChild(expressionStmt);
		return statement;
	}
	
	private TreeNode expressionStatement() throws BPLParserException {
		TreeNode expressionStmt = new TreeNode(TreeNodeKind.EXPRESSION_STMT, -1, null);
		TreeNode expression = expression();
		expressionStmt.setLine(expression.getLine());
		expressionStmt.addChild(expression);
		return expressionStmt;
	}
	
	private TreeNode expression() throws BPLParserException {
		TreeNode expression = new TreeNode(TreeNodeKind.EXPRESSION, -1, null);
		TreeNode id = id();
		expression.setLine(id.getLine());
		expression.addChild(id);
		return expression;
	}
	
	private TreeNode id() throws BPLParserException {
		TreeNode id = null;
		int line = nextToken.getLine();
		
		if (nextToken.getKind() == Kind.T_ID) {
			id = new TreeNode(TreeNodeKind.ID, nextToken.getLine(), nextToken.getValue());
		} else {
			throw new BPLParserException("Parser Error: at BPLParser.expression: Expected <id> but got " + nextToken.getKind() + " on line " + line);
		}
		getNextToken();
		if (nextToken.getKind() != Kind.T_SEMICOLON) {
			throw new BPLParserException("Parser Error: at BPLParser.expression: Missing ';' on line " + line);
		}
		getNextToken();
		return id;
	}
	
	public TreeNode parse() throws BPLParserException {
		getNextToken();
		TreeNode node = program();
		if (nextToken.getKind() != Kind.T_EOF) {
			return null;
		}
		return node;
	}
	
	private void getNextToken() throws BPLParserException {
		try {
			scanner.getNextToken();
		} catch (BPLScannerException e) {
			throw new BPLParserException("Parser Error: " + e.getMessage());
		}
		nextToken = scanner.nextToken;
	}
}
