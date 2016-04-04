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
			findReferencesStmt(stmt.getChildren().get(0), localDecs);
			stmtList = getDecList(stmtList);
		}
	}
	
	private void findReferencesStmt(TreeNode stmt, LinkedList<TreeNode> localDecs) throws BPLTypeCheckerException {
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
		findReferencesExpression(ifStmt.getChildren().get(0), localDecs);
		findReferencesStmt(ifStmt.getChildren().get(1), localDecs);
		if (ifStmt.getChildren().size() > 2) {
			findReferencesStmt(ifStmt.getChildren().get(2), localDecs);
		}
	}

	private void findReferencesWhileStmt(TreeNode whileStmt, LinkedList<TreeNode> localDecs) throws BPLTypeCheckerException {
		findReferencesExpression(whileStmt.getChildren().get(0), localDecs);
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
	
	private void findReferencesExpression(TreeNode exp, LinkedList<TreeNode> localDecs) throws BPLTypeCheckerException {
		if (exp.getChildren().get(0).getKind() == TreeNodeKind.ASSIGN_EXP) {
			findReferencesAssignExp(exp.getChildren().get(0), localDecs);
		} else {
			findReferencesCompExp(exp.getChildren().get(0), localDecs);
		}
	}
	
	private void findReferencesCompExp(TreeNode compExp, LinkedList<TreeNode> localDecs) throws BPLTypeCheckerException {
		if (compExp.getChildren().size() == 1) {
			findReferencesE(compExp.getChildren().get(0), localDecs);
		} else {
			findReferencesE(compExp.getChildren().get(0), localDecs);
			findReferencesE(compExp.getChildren().get(2), localDecs);
		}
		
	}
	
	private void findReferencesE(TreeNode E, LinkedList<TreeNode> localDecs) throws BPLTypeCheckerException {
		if (E.getChildren().size() == 1) {
			findReferencesT(E.getChildren().get(0), localDecs);
		} else {
			findReferencesE(E.getChildren().get(0), localDecs);
			findReferencesT(E.getChildren().get(2), localDecs);
		}
	}
	
	private void findReferencesT(TreeNode T, LinkedList<TreeNode> localDecs) throws BPLTypeCheckerException {
		if (T.getChildren().size() == 1) {
			findReferencesF(T.getChildren().get(0), localDecs);
		} else {
			findReferencesT(T.getChildren().get(0), localDecs);
			findReferencesF(T.getChildren().get(2), localDecs);
		}
	}
	
	private void findReferencesF(TreeNode F, LinkedList<TreeNode> localDecs) throws BPLTypeCheckerException {
		if (F.getKind() == TreeNodeKind.NEG_F) {
			findReferencesF(F.getChildren().get(0), localDecs);
		}
		findReferencesFactor(F.getChildren().get(0), localDecs);
	}
	
	private void findReferencesFactor(TreeNode factor, LinkedList<TreeNode> localDecs) throws BPLTypeCheckerException {
		TreeNode fac = factor.getChildren().get(0);
		System.out.println("FindReferenceFactor = " + factor);
		if (factor.getKind() == TreeNodeKind.ARRAY_FACTOR) {
			System.out.println("FOUND Array Factor: " + fac);
			findReferencesID(factor.getChildren().get(0), localDecs, factor.getChildren().get(0).getValue());
			findReferencesExpression(factor.getChildren().get(1), localDecs);
		} else if (fac.getKind() == TreeNodeKind.ID) {
			findReferencesID(fac, localDecs, fac.getValue());
		} else if (fac.getKind() == TreeNodeKind.EXPRESSION) {
			findReferencesExpression(fac, localDecs);
		} else if (fac.getKind() == TreeNodeKind.FUN_CALL) {
			findReferencesID(fac.getChildren().get(0), localDecs, fac.getChildren().get(0).getValue());
			findReferencesArgs(fac.getChildren().get(1), localDecs);
		}
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
		findReferencesExpression(assignExp.getChildren().get(1), localDecs);
	}
	
	private void findReferencesVar(TreeNode var, LinkedList<TreeNode> localDecs) throws BPLTypeCheckerException {
		TreeNode ID = var.getChildren().get(0);
		String id = ID.getValue();
		findReferencesID(var, localDecs, id);
	}
	
	private void findReferencesID(TreeNode var, LinkedList<TreeNode> localDecs, String id) throws BPLTypeCheckerException {
		TreeNode reference = findLocalReference(id, localDecs);
		if (reference == null) {
			reference = findGlobalReference(id);
		}
		if (reference == null) {
			throw new BPLTypeCheckerException("TypeChecker Error: Variable " + id + " referenced before assignment.");
		}
		var.setDec(reference);
		
		if (debug) {
			System.out.println(var.getKind() + " " + id + " linked to declaration " + reference);
		}
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
}
