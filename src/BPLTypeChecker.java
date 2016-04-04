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
	
	public void runTypeChecker() {
		if (root != null) {
			findReferences(root.getChildren().get(0));
		}
	}
	
	private void findReferences(TreeNode declist) {
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
	
	private void findReferencesFunDec(TreeNode dec) {
		TreeNode funDec = dec.getChildren().get(0);
		LinkedList<TreeNode> localDecs = new LinkedList<TreeNode>();
		findReferencesParams(funDec, localDecs);
		TreeNode compStmt = funDec.getChildren().get(3);
		findReferencesCompoundStmt(compStmt, localDecs);
	}
	
	private void findReferencesCompoundStmt(TreeNode compStmt, LinkedList<TreeNode> localDecs) {
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
	
	private void findReferencesStmtList(TreeNode stmtList, LinkedList<TreeNode> localDecs) {
		
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
	
	private void addFunDecToGlobalDecs(TreeNode dec) {
		TreeNode funDec = dec.getChildren().get(0);
		globalDecs.put(getDecId(funDec), funDec);
		
		if (debug) {
			System.out.println("Added " + funDec.getKind() + " " + getDecType(funDec) + " " + getDecId(funDec) + " to Global Declarations");
		}
	}
	
	private void addVarDecToGlobalDecs(TreeNode dec) {
		TreeNode varDec = dec.getChildren().get(0);
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
