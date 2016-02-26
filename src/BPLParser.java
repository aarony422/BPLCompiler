import java.util.LinkedList;

public class BPLParser {
	private BPLScanner scanner;
	private LinkedList<Token> tokenCache;
	
	public BPLParser(String inputFileName) {
		this.scanner = new BPLScanner(inputFileName);
		this.tokenCache = new LinkedList<Token>();
	}
	
	private TreeNode program() throws BPLParserException {
		TreeNode program = new TreeNode(TreeNodeKind.PROGRAM, -1, null);
		TreeNode statement = statement();
		program.setLine(statement.getLine());
		program.addChild(statement);
		return program;
	}
	
	private TreeNode statement() throws BPLParserException {
		TreeNode statement = new TreeNode(TreeNodeKind.STATEMENT, -1, null);
		Token token = getNextToken();
		TreeNode stmt = null;
		if (token.getKind() == Kind.T_LBRACE) {
			stmt = compoundStatement();
		} else {
			cacheToken(token);
			stmt = expressionStatement();
		}
		statement.setLine(stmt.getLine());
		statement.addChild(stmt);
		return statement;
	}
	
	private TreeNode compoundStatement() throws BPLParserException {
		TreeNode compoundStmt = new TreeNode(TreeNodeKind.COMPOUND_STMT, -1, null);
		TreeNode statementList = statementList();
		Token token = getNextToken();
		if (token.getKind() != Kind.T_RBRACE) {
			throw new BPLParserException("Parser Error: at BPLParser.compoundStatement: Expected } but got " + token.getKind() + " on line " + token.getLine());
		}
		compoundStmt.setLine(statementList.getLine());
		compoundStmt.addChild(statementList);
		return compoundStmt;
	}
	
	private TreeNode statementList() throws BPLParserException {
		Token token = getNextToken();
		if (token.getKind() == Kind.T_RBRACE) {
			cacheToken(token);
			return new TreeNode(TreeNodeKind.EMPTY, token.getLine(), null);
		}
		cacheToken(token);
		TreeNode statementList = new TreeNode(TreeNodeKind.STATEMENT_LIST, -1, null);
		TreeNode statement = statement();
		TreeNode statementList2 = statementList();
		statementList.addChild(statementList2);
		statementList.setLine(statement.getLine());
		statementList.addChild(statement);
		return statementList;
	}
	
	private TreeNode expressionStatement() throws BPLParserException {
		TreeNode expressionStmt = new TreeNode(TreeNodeKind.EXPRESSION_STMT, -1, null);
		Token token = getNextToken();
		// check if next token is ;
		if (token.getKind() == Kind.T_SEMICOLON) {
			expressionStmt.setLine(token.getLine());
			System.out.println("i got a semicolon");
			return expressionStmt;
		}
		// check if expression is followed by a ;
		cacheToken(token);
		TreeNode expression = expression();
		token = getNextToken();
		if (token.getKind() != Kind.T_SEMICOLON) {
			throw new BPLParserException("Parser Error: at BPLParser.expression: Expected ; but got " + token.getKind() + " on line " + token.getLine());
		}
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
		Token token = getNextToken();
		int line = token.getLine();
		
		if (token.getKind() == Kind.T_ID) {
			id = new TreeNode(TreeNodeKind.ID, token.getLine(), token.getValue());
		} else {
			throw new BPLParserException("Parser Error: at BPLParser.expression: Expected <id> but got " + token.getKind() + " on line " + line);
		}
		return id;
	}

	
	public TreeNode parse() throws BPLParserException {
		//getNextToken();
		TreeNode node = program();
		if (getNextToken().getKind() != Kind.T_EOF) {
			return null;
		}
		return node;
	}
	
	private Token getNextToken() throws BPLParserException {
		if (tokenCache.size() > 0) {
			return tokenCache.remove();
		}
		try {
			scanner.getNextToken();
		} catch (BPLScannerException e) {
			throw new BPLParserException("Parser Error: " + e.getMessage());
		}
		return scanner.nextToken;
	}
	
	private void cacheToken(Token t) throws BPLParserException {
		tokenCache.add(t);
	}
	
}
