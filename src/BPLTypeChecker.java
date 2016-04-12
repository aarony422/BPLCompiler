import java.util.HashMap;
import java.util.LinkedList;

public class BPLTypeChecker {
	private boolean debug;
	private HashMap<String, TreeNode> globalDecs;
	private BPLParser parser;
	private TreeNode root;
	
	public BPLTypeChecker(String inputFileName, boolean debug) {
		this.debug = debug;
		this.globalDecs = new HashMap<String, TreeNode>();
		parser = new BPLParser(inputFileName);
		root = null;
		
		try {
			root = parser.parse();
		} catch (BPLParserException e) {
			e.printStackTrace();
		}
	}
	
	public void runTypeChecker() throws BPLTypeCheckerException {
		if (root != null) {
			findReferences(root.getChildren().get(0));
		}
	}
	
	private void findReferences(TreeNode declist) throws BPLTypeCheckerException {
		while (!isEmpty(declist)) {
			TreeNode dec = getDec(declist);
			if (isVarDec(dec)) {
				addVarDecToGlobalDecs(dec);
			} else {
				addFunDecToGlobalDecs(dec);
				findReferencesFunDec(dec);
			}
			declist = getDecList(declist);
		}
	}
	
	private void findReferencesFunDec(TreeNode dec) throws BPLTypeCheckerException {
		TreeNode funDec = dec.getChildren().get(0);
		LinkedList<TreeNode> localDecs = new LinkedList<TreeNode>();
		findReferencesParams(funDec, localDecs);
		TreeNode compStmt = funDec.getChildren().get(3);
		// TODO pass in function return type for use in findReferencesReturnStmt
		findReferencesCompoundStmt(compStmt, localDecs);
	}
	
	private void findReferencesCompoundStmt(TreeNode compStmt, LinkedList<TreeNode> localDecs) throws BPLTypeCheckerException {
		
		for (TreeNode t : compStmt.getChildren()) {
			if (t.getKind() == TreeNodeKind.LOCAL_DECS) {
				findReferencesLocalDecs(t, localDecs);
			} else if (t.getKind() == TreeNodeKind.STATEMENT_LIST) {
				findReferencesStmtList(t, localDecs);
			}

		}
		
	}
	
	private void findReferencesLocalDecs(TreeNode local_decs, LinkedList<TreeNode> localDecs) {
		while (!isEmpty(local_decs)) {
			TreeNode varDec = getDec(local_decs);
			addVarDecToLocalDecs(varDec, localDecs);
			local_decs = getDecList(local_decs);
		}
	}
	
	private void findReferencesStmtList(TreeNode stmtList, LinkedList<TreeNode> localDecs) throws BPLTypeCheckerException {
		while (!isEmpty(stmtList)) {
			TreeNode stmt = getDec(stmtList);
			findReferencesStmt(stmt, localDecs);
			stmtList = getDecList(stmtList);
		}
	}
	
	private void findReferencesStmt(TreeNode statement, LinkedList<TreeNode> localDecs) throws BPLTypeCheckerException {
		TreeNode stmt = statement.getChildren().get(0);
		if (stmt.getKind() == TreeNodeKind.EXPRESSION_STMT) {
			findReferencesExpStmt(stmt, localDecs);
		} else if (stmt.getKind() == TreeNodeKind.COMPOUND_STMT) {
			findReferencesCompoundStmt(stmt, localDecs);
		} else if (stmt.getKind() == TreeNodeKind.IF_STMT) {
			findReferencesIfStmt(stmt, localDecs);
		} else if (stmt.getKind() == TreeNodeKind.WHILE_STMT) {
			findReferencesWhileStmt(stmt, localDecs);
		} else if (stmt.getKind() == TreeNodeKind.RETURN_STMT) {
			findReferencesReturnStmt(stmt, localDecs);
		} else if (stmt.getKind() == TreeNodeKind.WRITE_STMT) {
			findReferencesWriteStmt(stmt, localDecs);
		}
	}
	
	private void findReferencesExpStmt(TreeNode expStmt, LinkedList<TreeNode> localDecs) throws BPLTypeCheckerException {
		if (expStmt.getChildren().size() == 0) {
			return;
		}
		findReferencesExpression(expStmt.getChildren().get(0), localDecs);
	}
	
	private void findReferencesIfStmt(TreeNode ifStmt, LinkedList<TreeNode> localDecs) throws BPLTypeCheckerException {
		Type conditionType = findReferencesExpression(ifStmt.getChildren().get(0), localDecs);
		if (debug) {
			System.out.println("If Condition assigned type " + conditionType + " on line " + ifStmt.getLine());
		}
		assertType(conditionType, Type.INT, ifStmt.getLine());
		findReferencesStmt(ifStmt.getChildren().get(1), localDecs);
		if (ifStmt.getChildren().size() > 2) {
			findReferencesStmt(ifStmt.getChildren().get(2), localDecs);
		}
	}

	private void findReferencesWhileStmt(TreeNode whileStmt, LinkedList<TreeNode> localDecs) throws BPLTypeCheckerException {
		Type conditionType = findReferencesExpression(whileStmt.getChildren().get(0), localDecs);
		if (debug) {
			System.out.println("While Condition assigned type " + conditionType + "on line " + whileStmt.getLine());
		}
		assertType(conditionType, Type.INT, whileStmt.getLine());
		findReferencesStmt(whileStmt.getChildren().get(1), localDecs);
	}

	private void findReferencesReturnStmt(TreeNode returnStmt, LinkedList<TreeNode> localDecs) throws BPLTypeCheckerException {
		if (returnStmt.getChildren().size() == 0) {
			return;
		}
		findReferencesExpression(returnStmt.getChildren().get(0), localDecs);
	}
	
	private void findReferencesWriteStmt(TreeNode writeStmt, LinkedList<TreeNode> localDecs) throws BPLTypeCheckerException {
		if (writeStmt.getChildren().size() == 0) {
			return;
		}
		findReferencesExpression(writeStmt.getChildren().get(0), localDecs);
	}
	
	private Type findReferencesExpression(TreeNode exp, LinkedList<TreeNode> localDecs) throws BPLTypeCheckerException {
		Type expressionType = Type.NONE;
		if (exp.getChildren().get(0).getKind() == TreeNodeKind.ASSIGN_EXP) {
			findReferencesAssignExp(exp.getChildren().get(0), localDecs);
		} else {
			expressionType = findReferencesCompExp(exp.getChildren().get(0), localDecs);
		}
		return expressionType;
	}
	
	private Type findReferencesCompExp(TreeNode compExp, LinkedList<TreeNode> localDecs) throws BPLTypeCheckerException {
		Type compExpType = Type.NONE;
		if (compExp.getChildren().size() == 1) {
			compExpType = findReferencesE(compExp.getChildren().get(0), localDecs);
		} else {
			Type E1Type = findReferencesE(compExp.getChildren().get(0), localDecs);
			assertType(E1Type, Type.INT, compExp.getLine());
			Type E2Type = findReferencesE(compExp.getChildren().get(2), localDecs);
			assertType(E2Type, Type.INT, compExp.getLine());
			if (debug) {
				System.out.println(compExp.getChildren().get(1).getChildren().get(0).getValue() + " assigned Type " + Type.INT + " on line " + compExp.getLine());
			}
			compExpType = Type.INT;
		}
		return compExpType;
	}
	
	private Type findReferencesE(TreeNode E, LinkedList<TreeNode> localDecs) throws BPLTypeCheckerException {
		Type EType = Type.NONE;
		if (E.getChildren().size() == 1) {
			EType = findReferencesT(E.getChildren().get(0), localDecs);
		} else {
			Type E1 = findReferencesE(E.getChildren().get(0), localDecs);
			assertType(E1, Type.INT, E.getLine());
			Type T = findReferencesT(E.getChildren().get(2), localDecs);
			assertType(T, Type.INT, E.getLine());
			if (debug) {
				System.out.println(E.getChildren().get(1).getChildren().get(0).getValue() + " assigned Type " + Type.INT + " on line " + E.getLine());
			}
			EType = Type.INT;
		}
		return EType;
	}
	
	private Type findReferencesT(TreeNode T, LinkedList<TreeNode> localDecs) throws BPLTypeCheckerException {
		Type TType = Type.NONE;
		if (T.getChildren().size() == 1) {
			TType = findReferencesF(T.getChildren().get(0), localDecs);
		} else {
			Type T1 = findReferencesT(T.getChildren().get(0), localDecs);
			assertType(T1, Type.INT, T.getLine());
			Type F = findReferencesF(T.getChildren().get(2), localDecs);
			assertType(F, Type.INT, T.getLine());
			if (debug) {
				System.out.println(T.getChildren().get(1).getChildren().get(0).getValue() + " assigned Type " + Type.INT + " on line " + T.getLine());
			}
			TType = Type.INT;
		}
		return TType;
	}
	
	private Type findReferencesF(TreeNode F, LinkedList<TreeNode> localDecs) throws BPLTypeCheckerException {
		Type FType = Type.NONE;
		if (F.getKind() == TreeNodeKind.NEG_F) {
			Type F1 = findReferencesF(F.getChildren().get(0), localDecs);
			assertType(F1, Type.INT, F.getLine());
		} else {
			FType = findReferencesFactor(F.getChildren().get(0), localDecs);
		}
		return FType;
	}
	
	private Type findReferencesFactor(TreeNode factor, LinkedList<TreeNode> localDecs) throws BPLTypeCheckerException {
		Type factorType = Type.NONE;
		TreeNode fac = factor.getChildren().get(0);
		if (factor.getKind() == TreeNodeKind.ARRAY_FACTOR) {
			findReferencesID(factor.getChildren().get(0), localDecs, factor.getChildren().get(0).getValue());
			findReferencesExpression(factor.getChildren().get(1), localDecs);
			// TODO check expression is type int 
		} else if (factor.getKind() == TreeNodeKind.ADDRESS_F) {
			// Handle & operator
		} else if (factor.getKind() == TreeNodeKind.DEREF_F) {
			// Handle * operator
		} else if (fac.getKind() == TreeNodeKind.ID) {
			factorType = findReferencesID(fac, localDecs, fac.getValue());
		} else if (fac.getKind() == TreeNodeKind.EXPRESSION) {
			findReferencesExpression(fac, localDecs);
		} else if (fac.getKind() == TreeNodeKind.FUN_CALL) {
			findReferencesID(fac.getChildren().get(0), localDecs, fac.getChildren().get(0).getValue());
			findReferencesArgs(fac.getChildren().get(1), localDecs);
		} else if (fac.getKind() == TreeNodeKind.NUM) {
			factorType = Type.INT;
		} else if (fac.getKind() == TreeNodeKind.STR) {
			factorType = Type.INT;
		} else if (fac.getKind() == TreeNodeKind.READ) {
			factorType = Type.INT;
		}
		return factorType;
	}
	
	private void findReferencesArgs(TreeNode args, LinkedList<TreeNode> localDecs) throws BPLTypeCheckerException {
		TreeNode argList = args.getChildren().get(0);
		while (!isEmpty(argList)) {
			TreeNode exp = argList.getChildren().get(1);
			findReferencesExpression(exp, localDecs);
			argList = argList.getChildren().get(0);
		}
	}
	
	private void findReferencesAssignExp(TreeNode assignExp, LinkedList<TreeNode> localDecs) throws BPLTypeCheckerException {
		findReferencesVar(assignExp.getChildren().get(0), localDecs);
		// check L-value
		findReferencesExpression(assignExp.getChildren().get(1), localDecs);
		// TODO check assignment agreement
	}
	
	private Type findReferencesVar(TreeNode var, LinkedList<TreeNode> localDecs) throws BPLTypeCheckerException {
		Type varType = Type.NONE;
		TreeNode ID = var.getChildren().get(0);
		String id = ID.getValue();
		varType = findReferencesID(var, localDecs, id);
		if (var.getKind() == TreeNodeKind.ARRAY_VAR) {
			findReferencesExpression(var.getChildren().get(1), localDecs);
			// TODO: check exp type is int
		}
		return varType;
	}
	
	private Type findReferencesID(TreeNode var, LinkedList<TreeNode> localDecs, String id) throws BPLTypeCheckerException {
		Type varType = Type.NONE;
		TreeNode reference = findLocalReference(id, localDecs);
		if (reference == null) {
			reference = findGlobalReference(id);
		}
		if (reference == null) {
			throw new BPLTypeCheckerException("TypeChecker Error: Variable " + id + " referenced before assignment.");
		}
		var.setDec(reference);
		varType = findVarType(reference);
		if (debug) {
			System.out.println(var + " " + id + " linked to declaration " + reference);
			System.out.println(var + " " + id + " assigned Type " + varType);
		}
		return varType;
	}
	
	private Type findVarType(TreeNode reference) {
		Type refType = Type.NONE;
		if (reference.getKind() == TreeNodeKind.ARRAY_VAR_DEC) {
			if (reference.getChildren().get(0).getChildren().get(0).getKind() == TreeNodeKind.INT) {
				refType = Type.INT_ARRAY;
			} else if (reference.getChildren().get(0).getChildren().get(0).getKind() == TreeNodeKind.STR) {
				refType = Type.STRING_ARRAY;
			}
		} else if (reference.getKind() == TreeNodeKind.POINTER_VAR_DEC) {
			if (reference.getChildren().get(0).getChildren().get(0).getKind() == TreeNodeKind.INT) {
				refType = Type.INT_PTR;
			} else if (reference.getChildren().get(0).getChildren().get(0).getKind() == TreeNodeKind.STR) {
				refType = Type.STRING_PTR;
			}
		} else {
			if (reference.getChildren().get(0).getChildren().get(0).getKind() == TreeNodeKind.INT) {
				refType = Type.INT;
			} else if (reference.getChildren().get(0).getChildren().get(0).getKind() == TreeNodeKind.STR) {
				refType = Type.STRING;
			}
		}
		return refType;
	}
	
	private TreeNode findLocalReference(String id, LinkedList<TreeNode> localDecs) {
		for (TreeNode t : localDecs) {
			if (getDecId(t).equals(id)) {
				return t;
			}
		}
		return null;
	}
	
	private TreeNode findGlobalReference(String id) {
		if (globalDecs.containsKey(id)) {
			return globalDecs.get(id);
		}
		return null;
	}
	
	private void addVarDecToLocalDecs(TreeNode varDec, LinkedList<TreeNode> localDecs) {
		localDecs.addFirst(varDec);
		
		if (debug) {
			System.out.println("Added " + varDec.getKind() + " " + getDecType(varDec) + " " + getDecId(varDec) + " to Local Declarations");
		}
	}
	
	private void findReferencesParams(TreeNode funDec, LinkedList<TreeNode> localDecs) {
		TreeNode params = funDec.getChildren().get(2);
		
		if (!areParams(params)) {
			return;
		}
		
		TreeNode paramList = params.getChildren().get(0);
		while (!isEmpty(paramList)) {
			TreeNode param = getParam(paramList);
			addParamToLocalDec(param, localDecs);
			paramList = getParamList(paramList);
		}
		
	}
	
	private TreeNode getParamList(TreeNode paramList) {
		return paramList.getChildren().get(0);
	}
	
	private void addParamToLocalDec(TreeNode param, LinkedList<TreeNode> localDecs) {
		localDecs.addFirst(param);
		
		if (debug) {
			System.out.println("Added " + param.getKind() + " " + getParamType(param) + " " + getParamId(param) + " to Local Declarations");
		}
	}
	
	private String getParamId(TreeNode param) {
		return param.getChildren().get(1).getValue();
	}
	
	private String getParamType(TreeNode param) {
		return param.getChildren().get(0).getChildren().get(0).getValue();
	}
	
	private TreeNode getParam(TreeNode paramList) {
		return paramList.getChildren().get(1);
	}
	
	private boolean areParams(TreeNode params) {
		TreeNode paramList = params.getChildren().get(0);
		if (paramList.getKind() == TreeNodeKind.EMPTY || paramList.getKind() == TreeNodeKind.VOID) {
			return false;
		}
		return true;
	}
	
	private void addFunDecToGlobalDecs(TreeNode dec) throws BPLTypeCheckerException {
		TreeNode funDec = dec.getChildren().get(0);
		if (globalDecs.containsKey(getDecId(funDec))) {
			throw new BPLTypeCheckerException("TypeChecker Error: Variable " + getDecId(funDec) + " already assigned.");
		}
		globalDecs.put(getDecId(funDec), funDec);
		
		if (debug) {
			System.out.println("Added " + funDec.getKind() + " " + getDecType(funDec) + " " + getDecId(funDec) + " to Global Declarations");
		}
	}
	
	private void addVarDecToGlobalDecs(TreeNode dec) throws BPLTypeCheckerException {
		TreeNode varDec = dec.getChildren().get(0);
		if (globalDecs.containsKey(getDecId(varDec))) {
			throw new BPLTypeCheckerException("TypeChecker Error: Variable " + getDecId(varDec) + " already assigned.");
		}
		globalDecs.put(getDecId(varDec), varDec);
		
		if (debug) {
			System.out.println("Added " + varDec.getKind() + " " + getDecType(varDec) + " " + getDecId(varDec) + " to Global Declarations");
		}
	}
	
	private String getDecType(TreeNode dec) {
		return dec.getChildren().get(0).getChildren().get(0).getValue();
	}
	
	private String getDecId(TreeNode dec) {
		TreeNode id = dec.getChildren().get(1);
		return id.getValue();
	}
	
	private boolean isVarDec(TreeNode dec) {
		TreeNodeKind decKind = dec.getChildren().get(0).getKind();
		return decKind == TreeNodeKind.VAR_DEC || decKind == TreeNodeKind.POINTER_VAR_DEC || decKind == TreeNodeKind.ARRAY_VAR_DEC;
	}
	
	private boolean isEmpty(TreeNode t) {
		return t.getKind() == TreeNodeKind.EMPTY;
	}
	
	private TreeNode getDec(TreeNode t) {
		return t.getChildren().get(1);
	}
	
	private TreeNode getDecList(TreeNode t) {
		return t.getChildren().get(0);
	}
	
	private void assertType(Type type, Type expected, int line) throws BPLTypeCheckerException {
		if (type != expected) {
			throw new BPLTypeCheckerException("TypeChecker Error: Expected " + expected + " but got " + type + " on line " + line);
		}
		
		if (debug) {
			
		}
	}
}
