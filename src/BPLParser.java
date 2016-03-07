import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;

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
		TreeNode decList = declaractionList();
		program.addChild(decList);
		return program;
	}
	
	private TreeNode declaractionList() throws BPLParserException {
		Token token = getNextToken();
		cacheToken(token);
		if (token.getKind() == Kind.T_EOF) {
			return new TreeNode(TreeNodeKind.EMPTY, currLine, null);
		}
		TreeNode decList = new TreeNode(TreeNodeKind.DECLARATION_LIST, currLine, null);
		TreeNode dec = declaration();
		TreeNode decList2 = declaractionList();
		decList.addChild(decList2);
		decList.addChild(dec);
		return decList;
	}
	
	private TreeNode declaration() throws BPLParserException {
		TreeNode dec = new TreeNode(TreeNodeKind.DECLARATION, currLine, null);
		Token token1 = getNextToken();
		if (token1.getKind() != Kind.T_INT && token1.getKind() != Kind.T_VOID && token1.getKind() != Kind.T_STR) {
			assertToken(token1, Kind.T_INT, "int, void or string");
		}
		Token token2 = getNextToken();
		if (token2.getKind() == Kind.T_ASTERISK) {
			cacheToken(token1);
			cacheToken(token2);
			TreeNode var = varDec();
			dec.addChild(var);
		} else {
			assertToken(token2, Kind.T_ID, "<id>");
			Token token3 = getNextToken();
			cacheToken(token1);
			cacheToken(token2);
			cacheToken(token3);
			if (token3.getKind() == Kind.T_LPAREN) {
				TreeNode fun = funDec();
				dec.addChild(fun);
			} else if (token3.getKind() == Kind.T_SEMICOLON || token3.getKind() == Kind.T_LBRACKET){
				TreeNode var = varDec();
				dec.addChild(var);
			} else {
				assertToken(token3, Kind.T_SEMICOLON, "(, [, or ;");
			}
		}
		return dec;
	}
	
	private TreeNode varDec() throws BPLParserException {
		int line = currLine;
		TreeNode varDec = null;
		TreeNode type = typeSpecifier();
		//varDec.addChild(type);
		Token token = getNextToken();
		if (token.getKind() == Kind.T_ASTERISK) {
			varDec = new TreeNode(TreeNodeKind.POINTER_VAR_DEC, line, null);
			varDec.addChild(type);
		} else {
			cacheToken(token);
		}
		TreeNode id = id();
		//varDec.addChild(id);
		token = getNextToken();
		if (token.getKind() == Kind.T_LBRACKET) {
			varDec = new TreeNode(TreeNodeKind.ARRAY_VAR_DEC, line, null);
			TreeNode num = num();
			token = getNextToken();
			assertToken(token, Kind.T_RBRACKET, "]");
			varDec.addChild(type);
			varDec.addChild(id);
			varDec.addChild(num);
			token = getNextToken();
		}
		if (varDec == null) {
			varDec = new TreeNode(TreeNodeKind.VAR_DEC, line, null);
			varDec.addChild(type);
			varDec.addChild(id);
		} else if (varDec.getKind() == TreeNodeKind.POINTER_VAR_DEC) {
			varDec.addChild(id);
		}
		
		assertToken(token, Kind.T_SEMICOLON, ";");
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
			cacheToken(token);
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
			assertToken(token, Kind.T_INT, "int, void or string");
		}
		typeSpecifier.addChild(type);
		return typeSpecifier;
	}
	
	private TreeNode funDec() throws BPLParserException {
		TreeNode funDec = new TreeNode(TreeNodeKind.FUN_DEC, currLine, null);
		TreeNode type = typeSpecifier();
		funDec.addChild(type);
		TreeNode id = id();
		funDec.addChild(id);
		Token token = getNextToken();
		assertToken(token, Kind.T_LPAREN, "(");
		TreeNode params = params();
		token = getNextToken();
		assertToken(token, Kind.T_RPAREN, ")");
		funDec.addChild(params);
		TreeNode compoundStmt = compoundStatement();
		funDec.addChild(compoundStmt);
		return funDec;
	}
	
	private TreeNode params() throws BPLParserException {
		TreeNode params = new TreeNode(TreeNodeKind.PARAMS, currLine, null);
		Token token = getNextToken();
		if (token.getKind() == Kind.T_VOID) {
			TreeNode v = new TreeNode(TreeNodeKind.VOID, currLine, token.getValue());
			params.addChild(v);
		} else {
			cacheToken(token);
			TreeNode paramList = paramList();
			params.addChild(paramList);
		}
		return params;
	}
	
	private TreeNode paramList() throws BPLParserException {
		Token token = getNextToken();
		cacheToken(token);
		if (token.getKind() == Kind.T_RPAREN) {
			return new TreeNode(TreeNodeKind.EMPTY, currLine, null);
		}
		TreeNode paramList = new TreeNode(TreeNodeKind.PARAM_LIST, currLine, null);
		TreeNode param = param();
		token = getNextToken();
		if (token.getKind() == Kind.T_RPAREN) {
			cacheToken(token);
		} else {
			assertToken(token, Kind.T_COMMA, ",");
		}
		TreeNode paramList2 = paramList();

		paramList.addChild(paramList2);
		paramList.addChild(param);
		return paramList;
	}
	
	private TreeNode param() throws BPLParserException {
		int line = currLine;
		TreeNode param = null;
		TreeNode type = typeSpecifier();
		Token token = getNextToken();
		if (token.getKind() == Kind.T_ASTERISK) {
			param = new TreeNode(TreeNodeKind.POINTER_PARAM, line, null);
		} else {
			cacheToken(token);
		}
		TreeNode id = id();
		token = getNextToken();
		if (token.getKind() == Kind.T_LBRACKET) {
			param = new TreeNode(TreeNodeKind.ARRAY_PARAM, line, null);
			token = getNextToken();
			assertToken(token, Kind.T_RBRACKET, "]");
		} else {
			cacheToken(token);
		}
		if (param == null) {
			param = new TreeNode(TreeNodeKind.PARAM, line, null);
		}
		param.addChild(type);
		param.addChild(id);
		return param;
	}
	
	private TreeNode compoundStatement() throws BPLParserException {
		Token token = getNextToken();
		assertToken(token, Kind.T_LBRACE, "{");
		token = getNextToken();
		TreeNode compoundStmt = new TreeNode(TreeNodeKind.COMPOUND_STMT, currLine, null);
		// Local decs start with Type specifier 
		if (token.getKind() == Kind.T_INT || token.getKind() == Kind.T_VOID || token.getKind() == Kind.T_STR) {
			cacheToken(token);
			TreeNode localDecs = localDecs();
			compoundStmt.addChild(localDecs);
			token = getNextToken();
		}
		if (token.getKind() != Kind.T_RBRACE) {
			cacheToken(token);
			TreeNode statementList = statementList();
			token = getNextToken();
			assertToken(token, Kind.T_RBRACE, "}");
			compoundStmt.addChild(statementList);
		}
		return compoundStmt;
	}
	
	private TreeNode whileStatement() throws BPLParserException {
		TreeNode whileStmt = new TreeNode(TreeNodeKind.WHILE_STMT, currLine, null);
		Token token = getNextToken();
		assertToken(token, Kind.T_LPAREN, "(");
		TreeNode expression = expression();
		token = getNextToken();
		assertToken(token, Kind.T_RPAREN, ")");
		TreeNode stmt = statement();
		whileStmt.addChild(expression);
		whileStmt.addChild(stmt);
		return whileStmt;
	}
	
	private TreeNode ifStatement() throws BPLParserException {
		TreeNode ifStmt = new TreeNode(TreeNodeKind.IF_STMT, currLine, null);
		Token token = getNextToken();
		assertToken(token, Kind.T_LPAREN, "(");
		TreeNode expression = expression();
		token = getNextToken();
		assertToken(token, Kind.T_RPAREN, ")");
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
		assertToken(token, Kind.T_SEMICOLON, ";");
		returnStmt.addChild(expression);
		return returnStmt;
	}
	
	private TreeNode writeStatement() throws BPLParserException {
		TreeNode writeStmt = new TreeNode(TreeNodeKind.WRITE_STMT, currLine, null);
		Token token = getNextToken();
		
		if (token.getKind() == Kind.T_WRITE) {
			token = getNextToken();
			assertToken(token, Kind.T_LPAREN, "(");
			TreeNode expression = expression();
			token = getNextToken();
			assertToken(token, Kind.T_RPAREN, ")");
			token = getNextToken();
			assertToken(token, Kind.T_SEMICOLON, ";");
			writeStmt.addChild(expression);
		} else {
			token = getNextToken();
			assertToken(token, Kind.T_LPAREN, "(");
			token = getNextToken();
			assertToken(token, Kind.T_RPAREN, ")");
			token = getNextToken();
			assertToken(token, Kind.T_SEMICOLON, ";");
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
		assertToken(token, Kind.T_SEMICOLON, ";");
		expressionStmt.addChild(expression);
		return expressionStmt;
	}
	
	private TreeNode expression() throws BPLParserException {
		/*
		TreeNode expression = new TreeNode(TreeNodeKind.EXPRESSION, currLine, null);
		TreeNode id = id();
		expression.addChild(id);
		return expression;
		*/
		TreeNode expression = new TreeNode(TreeNodeKind.EXPRESSION, currLine, null);
		boolean isComp = false;
		Stack<Token> parenStack = new Stack<Token>();
		ArrayList<Token> seenTokens = new ArrayList<Token>();
		Token token = getNextToken();
		seenTokens.add(token);
		
		while (token.getKind() != Kind.T_EQ || !parenStack.isEmpty()) {
			if (token.getKind() == Kind.T_SEMICOLON) {
				// COMP_EXP
				isComp = true;
				break;
			} else if (token.getKind() == Kind.T_COMMA && parenStack.isEmpty()) {
				// COMP_EXP
				isComp = true;
				break;
			} else if (token.getKind() == Kind.T_RPAREN || token.getKind() == Kind.T_RBRACKET) {
				if (parenStack.isEmpty()) {
					// COMP_EXP
					isComp = true;
					break;
				}
				// take matching paren/bracket off stack
				if (token.getKind() == Kind.T_RPAREN) {
					assertToken(parenStack.pop(), Kind.T_LPAREN, "]");
				} else { //token.getKind() == Kind.T_RBRACKET
					assertToken(parenStack.pop(), Kind.T_LBRACKET, ")"); 
					parenStack.pop();
				}
			} else if (token.getKind() == Kind.T_LPAREN || token.getKind() == Kind.T_LBRACKET) {
				parenStack.push(token);
			}
			token = getNextToken();
			seenTokens.add(token);
		}
		
		for (Token t : seenTokens) {
			cacheToken(t);
		}
		
		if (isComp) {
			//TreeNode compExp = compExp();
			//expression.addChild(compExp);
			System.out.println("It is a COMP EXP!");
			printCache();
			TreeNode compExp = compExp();
			expression.addChild(compExp);
		} else { // found VAR = EXP
			TreeNode assignExp = assignmentExpression();
			expression.addChild(assignExp);
		}
		return expression;
	}
	
	private TreeNode assignmentExpression() throws BPLParserException {
		TreeNode assignExp = new TreeNode(TreeNodeKind.ASSIGN_EXP, currLine, null);
		TreeNode var = var();
		Token token = getNextToken();
		assertToken(token, Kind.T_EQ, "=");
		TreeNode expression = expression();
		assignExp.addChild(var);
		assignExp.addChild(expression);
		return assignExp;
	}
	
	/* private TreeNode compExp() throws BPLParserException */
	
	private TreeNode var() throws BPLParserException {
		int line = currLine;
		TreeNode var = null;
		Token token = getNextToken();
		if (token.getKind() == Kind.T_ASTERISK) {
			var = new TreeNode(TreeNodeKind.POINTER_VAR, line, null);
		} else {
			cacheToken(token);
		}
		TreeNode id = id();
		token = getNextToken();
		
		if (token.getKind() == Kind.T_LBRACKET) {
			var = new TreeNode(TreeNodeKind.ARRAY_VAR, line, null);
			TreeNode exp = expression();
			token = getNextToken();
			assertToken(token, Kind.T_RBRACKET, "]");
			var.addChild(id);
			var.addChild(exp);
		} else {
			cacheToken(token);
		}
		
		if (var == null) {
			var = new TreeNode(TreeNodeKind.VAR, line, null);
		}
		if (var.getKind() != TreeNodeKind.ARRAY_VAR) {
			var.addChild(id);
		}
		return var;
	}
	
	private TreeNode compExp() throws BPLParserException {
		TreeNode comExp = new TreeNode(TreeNodeKind.COMP_EXP, currLine, null);
		TreeNode E = E();
		comExp.addChild(E);
		return comExp;
	}
	
	private TreeNode E() throws BPLParserException {
		TreeNode E = new TreeNode(TreeNodeKind.E, currLine, null);
		TreeNode T = T();
		E.addChild(T);
		return E;
	}
	
	private TreeNode T() throws BPLParserException {
		TreeNode T = new TreeNode(TreeNodeKind.T, currLine, null);
		TreeNode F = F();
		T.addChild(F);
		return T;
	}
	
	private TreeNode F() throws BPLParserException {
		TreeNode F = new TreeNode(TreeNodeKind.F, currLine, null);
		TreeNode factor = factor();
		F.addChild(factor);
		return F;
	}
	
	private TreeNode factor() throws BPLParserException {
		TreeNode factor = new TreeNode(TreeNodeKind.FACTOR, currLine, null);
		TreeNode id = id();
		factor.addChild(id);
		return factor;
	}

	private TreeNode id() throws BPLParserException {
		Token token = getNextToken();
		assertToken(token, Kind.T_ID, "<id>");
		return new TreeNode(TreeNodeKind.ID, currLine, token.getValue());
	}
	
	private TreeNode num() throws BPLParserException {
		Token token = getNextToken();
		assertToken(token, Kind.T_NUM, "<num>");
		return new TreeNode(TreeNodeKind.NUM, currLine, token.getValue());
	}
	
	private TreeNode relop() throws BPLParserException {
		TreeNode relop = new TreeNode(TreeNodeKind.RELOP, currLine, null);
		Token token = getNextToken();
		Kind tokenKind = token.getKind();
		if (tokenKind == Kind.T_LEQ || tokenKind == Kind.T_LESS || tokenKind == Kind.T_DOUBLEEQ
				|| tokenKind == Kind.T_NEQ || tokenKind == Kind.T_GREATER || tokenKind == Kind.T_GEQ) {
			TreeNode eqRelOp = new TreeNode(TreeNodeKind.EQ_REL_OP, currLine, token.getValue());
			relop.addChild(eqRelOp);
		} else {
			assertToken(token, Kind.T_LEQ, "<=, <, ==, !=, >, >=");
		}
		return relop;
	}
	
	private TreeNode addop() throws BPLParserException {
		TreeNode addop = new TreeNode(TreeNodeKind.ADDOP, currLine, null);
		Token token = getNextToken();
		Kind tokenKind = token.getKind();
		if (tokenKind == Kind.T_PLUS || tokenKind == Kind.T_MINUS) {
			TreeNode arthmop = new TreeNode(TreeNodeKind.ARITHM_OP, currLine, token.getValue());
			addop.addChild(arthmop);
		} else {
			assertToken(token, Kind.T_PLUS, "+, -");
		}
		return addop;
	}
	
	private TreeNode mulop() throws BPLParserException {
		TreeNode mulop = new TreeNode(TreeNodeKind.MULOP, currLine, null);
		Token token = getNextToken();
		Kind tokenKind = token.getKind();
		if (tokenKind == Kind.T_ASTERISK || tokenKind == Kind.T_FSLASH 
				|| tokenKind == Kind.T_PERCENT) {
			TreeNode arthmop = new TreeNode(TreeNodeKind.ARITHM_OP, currLine, token.getValue());
			mulop.addChild(arthmop);
		} else {
			assertToken(token, Kind.T_ASTERISK, "*, /, %");
		}
		return mulop;
	}
	
	private boolean assertToken(Token t, Kind expectedKind, String expectedString) throws BPLParserException {
		if (t.getKind() != expectedKind) {
			throw new BPLParserException("Parser Error: Expected " + expectedString + " but got " + t.getKind() + " on line " + t.getLine());
		}
		return true;
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
		int index = 0;
		int size = tokenCache.size();
		for (; index < size; index++) {
			if (tokenCache.get(index).getID() > t.getID()) {
				break;
			}
		}
		tokenCache.add(index, t);
	}
	
	private void printCache() {
		System.out.println("Current Token Cache:");
		for (Token t : tokenCache) {
			System.out.println(t);
		}
	}
	
	private void printStack(Stack<Token> stack) {
		for (Token t : stack) {
			System.out.println(t);
		}
	}
	
}
