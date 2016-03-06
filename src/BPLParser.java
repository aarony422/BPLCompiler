import java.util.LinkedList;

public class BPLParser {
	private BPLScanner scanner;
	private LinkedList<Token> tokenCache;
	private int currLine;
	
	public BPLParser(String inputFileName) {
		this.scanner = new BPLScanner(inputFileName);
		this.tokenCache = new LinkedList<Token>();
		this.currLine = 1;
	}
	
	private TreeNode program() throws BPLParserException {
		TreeNode program = new TreeNode(TreeNodeKind.PROGRAM, currLine, null);
		TreeNode statement = statement();
		program.addChild(statement);
		return program;
	}
	
	private TreeNode varDec() throws BPLParserException {
		TreeNode varDec = new TreeNode(TreeNodeKind.VAR_DEC, currLine, null);
		TreeNode type = typeSpecifier();
		varDec.addChild(type);
		Token token = getNextToken();
		if (token.getKind() == Kind.T_ASTERISK) {
			TreeNode asterisk = new TreeNode(TreeNodeKind.ASTERISK, currLine, token.getValue());
			varDec.addChild(asterisk);
		}
		cacheToken(token);
		TreeNode id = id();
		varDec.addChild(id);
		token = getNextToken();
		if (token.getKind() == Kind.T_LBRACKET) {
			TreeNode num = num();
			token = getNextToken();
			if (token.getKind() != Kind.T_RBRACKET) {
				throw new BPLParserException("Parser Error: at BPLParser.varDec: Expected ] but got " + token.getKind() + " on line " + token.getLine());
			}
			varDec.addChild(num);
			token = getNextToken();
		}
		if (token.getKind() != Kind.T_SEMICOLON) {
			throw new BPLParserException("Parser Error: at BPLParser.varDec: Expected ; but got " + token.getKind() + " on line " + token.getLine());
		}
		return varDec;
	}
	
	private TreeNode localDecs() throws BPLParserException {
		Token token = getNextToken();
		cacheToken(token);
		if (token.getKind() != Kind.T_INT && token.getKind() != Kind.T_VOID && token.getKind() != Kind.T_STR) {
			return new TreeNode(TreeNodeKind.EMPTY, currLine, null);
		}
		TreeNode localDecs = new TreeNode(TreeNodeKind.LOCAL_DECS, currLine, null);
		TreeNode varDec = varDec();
		TreeNode localDecs2 = localDecs();
		localDecs.addChild(localDecs2);
		localDecs.addChild(varDec);
		return localDecs;
	}
	
	private TreeNode statement() throws BPLParserException {
		TreeNode statement = new TreeNode(TreeNodeKind.STATEMENT, currLine, null);
		Token token = getNextToken();
		TreeNode stmt = null;
		if (token.getKind() == Kind.T_LBRACE) {
			stmt = compoundStatement();
		} else if (token.getKind() == Kind.T_WHILE) {
			stmt = whileStatement();
		} else if (token.getKind() == Kind.T_IF){
			stmt = ifStatement();
		} else if (token.getKind() == Kind.T_RETURN) {
			stmt = returnStatement();
		} else if (token.getKind() == Kind.T_WRITE || token.getKind() == Kind.T_WRITELN) {
			cacheToken(token);
			stmt = writeStatement();
		} else {
			cacheToken(token);
			stmt = expressionStatement();
		}
		statement.addChild(stmt);
		return statement;
	}
	
	private TreeNode typeSpecifier() throws BPLParserException {
		TreeNode typeSpecifier = new TreeNode(TreeNodeKind.TYPE_SPECIFIER, currLine, null);
		Token token = getNextToken();
		TreeNode type = null;
		if (token.getKind() == Kind.T_INT) {
			type = new TreeNode(TreeNodeKind.INT, currLine, token.getValue());
		} else if (token.getKind() == Kind.T_VOID) {
			type = new TreeNode(TreeNodeKind.VOID, currLine, token.getValue());
		} else if (token.getKind() == Kind.T_STR) {
			type = new TreeNode(TreeNodeKind.STR, currLine, token.getValue());
		} else {
			throw new BPLParserException("Parser Error: at BPLParser.typeSpecifier: Expected int, void, or string but got " + token.getKind() + " on line " + token.getLine());
		}
		typeSpecifier.addChild(type);
		return typeSpecifier;
	}
	
	private TreeNode compoundStatement() throws BPLParserException {
		TreeNode compoundStmt = new TreeNode(TreeNodeKind.COMPOUND_STMT, currLine, null);
		Token token = getNextToken();	
		// Local decs start with Type specifier 
		if (token.getKind() == Kind.T_INT || token.getKind() == Kind.T_VOID || token.getKind() == Kind.T_STR) {
			cacheToken(token);
			TreeNode localDecs = localDecs();
			compoundStmt.addChild(localDecs);
		}
		token = getNextToken();
		if (token.getKind() != Kind.T_RBRACE) {
			cacheToken(token);
			TreeNode statementList = statementList();
			token = getNextToken();
			if (token.getKind() != Kind.T_RBRACE) {
				throw new BPLParserException("Parser Error: at BPLParser.compoundStatement: Expected } but got " + token.getKind() + " on line " + token.getLine());
			}
			compoundStmt.addChild(statementList);
		}
		return compoundStmt;
	}
	
	private TreeNode whileStatement() throws BPLParserException {
		TreeNode whileStmt = new TreeNode(TreeNodeKind.WHILE_STMT, currLine, null);
		Token token = getNextToken();
		if (token.getKind() != Kind.T_LPAREN) {
			throw new BPLParserException("Parser Error: at BPLParser.whileStatement: Expected ( but got " + token.getKind() + " on line " + token.getLine());
		}
		TreeNode expression = expression();
		token = getNextToken();
		if (token.getKind() != Kind.T_RPAREN) {
			throw new BPLParserException("Parser Error: at BPLParser.whileStatement: Expected ) but got " + token.getKind() + " on line " + token.getLine());
		}
		TreeNode stmt = statement();
		whileStmt.addChild(expression);
		whileStmt.addChild(stmt);
		return whileStmt;
	}
	
	private TreeNode ifStatement() throws BPLParserException {
		TreeNode ifStmt = new TreeNode(TreeNodeKind.IF_STMT, currLine, null);
		Token token = getNextToken();
		if (token.getKind() != Kind.T_LPAREN) {
			throw new BPLParserException("Parser Error: at BPLParser.ifStatement: Expected ( but got " + token.getKind() + " on line " + token.getLine());
		}
		TreeNode expression = expression();
		token = getNextToken();
		if (token.getKind() != Kind.T_RPAREN) {
			throw new BPLParserException("Parser Error: at BPLParser.ifStatement: Expected ) but got " + token.getKind() + " on line " + token.getLine());
		}
		TreeNode stmt = statement();
		ifStmt.addChild(expression);
		ifStmt.addChild(stmt);
		
		token = getNextToken();
		if (token.getKind() != Kind.T_ELSE) {
			return ifStmt;
		}
		TreeNode elseStmt = statement();
		ifStmt.addChild(elseStmt);
		return ifStmt;
	}
	
	private TreeNode returnStatement() throws BPLParserException {
		TreeNode returnStmt = new TreeNode(TreeNodeKind.RETURN_STMT, currLine, null);
		Token token = getNextToken();
		if (token.getKind() == Kind.T_SEMICOLON) {
			return returnStmt;
		}
		cacheToken(token);
		TreeNode expression = expression();
		token = getNextToken();
		if (token.getKind() != Kind.T_SEMICOLON) {
			throw new BPLParserException("Parser Error: at BPLParser.returnStatement: Expected ; but got " + token.getKind() + " on line " + token.getLine());
		}
		returnStmt.addChild(expression);
		return returnStmt;
	}
	
	private TreeNode writeStatement() throws BPLParserException {
		TreeNode writeStmt = new TreeNode(TreeNodeKind.WRITE_STMT, currLine, null);
		Token token = getNextToken();
		
		if (token.getKind() == Kind.T_WRITE) {
			token = getNextToken();
			if (token.getKind() != Kind.T_LPAREN) {
				throw new BPLParserException("Parser Error: at BPLParser.writeStatement: Expected ( but got " + token.getKind() + " on line " + token.getLine());
			}
			TreeNode expression = expression();
			token = getNextToken();
			if (token.getKind() != Kind.T_RPAREN) {
				throw new BPLParserException("Parser Error: at BPLParser.writeStatement: Expected ) but got " + token.getKind() + " on line " + token.getLine());
			}
			token = getNextToken();
			if (token.getKind() != Kind.T_SEMICOLON) {
				throw new BPLParserException("Parser Error: at BPLParser.writeStatement: Expected ; but got " + token.getKind() + " on line " + token.getLine());
			}
			writeStmt.addChild(expression);
		} else {
			token = getNextToken();
			if (token.getKind() != Kind.T_LPAREN) {
				throw new BPLParserException("Parser Error: at BPLParser.writeStatement: Expected ( but got " + token.getKind() + " on line " + token.getLine());
			}
			token = getNextToken();
			if (token.getKind() != Kind.T_RPAREN) {
				throw new BPLParserException("Parser Error: at BPLParser.writeStatement: Expected ) but got " + token.getKind() + " on line " + token.getLine());
			}
			token = getNextToken();
			if (token.getKind() != Kind.T_SEMICOLON) {
				throw new BPLParserException("Parser Error: at BPLParser.writeStatement: Expected ; but got " + token.getKind() + " on line " + token.getLine());
			}
		}
		return writeStmt;
	}
	
	private TreeNode statementList() throws BPLParserException {
		Token token = getNextToken();
		cacheToken(token);
		if (token.getKind() == Kind.T_RBRACE) {
			return new TreeNode(TreeNodeKind.EMPTY, currLine, null);
		}
		TreeNode statementList = new TreeNode(TreeNodeKind.STATEMENT_LIST, currLine, null);
		TreeNode statement = statement();
		TreeNode statementList2 = statementList();
		statementList.addChild(statementList2);
		statementList.addChild(statement);
		return statementList;
	}
	
	private TreeNode expressionStatement() throws BPLParserException {
		TreeNode expressionStmt = new TreeNode(TreeNodeKind.EXPRESSION_STMT, currLine, null);
		Token token = getNextToken();
		// check if next token is ;
		if (token.getKind() == Kind.T_SEMICOLON) {
			return expressionStmt;
		}
		// check if expression is followed by a ;
		cacheToken(token);
		TreeNode expression = expression();
		token = getNextToken();
		if (token.getKind() != Kind.T_SEMICOLON) {
			throw new BPLParserException("Parser Error: at BPLParser.expression: Expected ; but got " + token.getKind() + " on line " + token.getLine());
		}
		expressionStmt.addChild(expression);
		return expressionStmt;
	}
	
	private TreeNode expression() throws BPLParserException {
		TreeNode expression = new TreeNode(TreeNodeKind.EXPRESSION, currLine, null);
		TreeNode id = id();
		expression.addChild(id);
		return expression;
	}

	private TreeNode id() throws BPLParserException {
		Token token = getNextToken();
		
		if (token.getKind() != Kind.T_ID) {
			throw new BPLParserException("Parser Error: at BPLParser.id: Expected <id> but got " + token.getKind() + " on line " + token.getLine());
		}
		return new TreeNode(TreeNodeKind.ID, currLine, token.getValue());
	}
	
	private TreeNode num() throws BPLParserException {
		Token token = getNextToken();
		if (token.getKind() != Kind.T_NUM) {
			throw new BPLParserException("Parser Error: at BPLParser.num: Expected <num> but got " + token.getKind() + " on line " + token.getLine());
		}
		return new TreeNode(TreeNodeKind.NUM, currLine, token.getValue());
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
			Token token = tokenCache.remove();
			currLine = token.getLine();
			return token;
		}
		try {
			scanner.getNextToken();
		} catch (BPLScannerException e) {
			throw new BPLParserException("Parser Error: " + e.getMessage());
		}
		currLine = scanner.nextToken.getLine();
		return scanner.nextToken;
	}
	
	private void cacheToken(Token t) throws BPLParserException {
		tokenCache.add(t);
	}
	
}
