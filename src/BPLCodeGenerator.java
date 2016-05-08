import java.util.HashMap;
import java.util.ArrayList;

public class BPLCodeGenerator {
  private TreeNode root;
  private int labelNum;
  private HashMap<String, String> strMap;
  private String fp = "%rbx";
  private String sp = "%rsp";

  public BPLCodeGenerator(String inputFileName) {
    BPLTypeChecker typeChecker = new BPLTypeChecker(inputFileName, false);
    try {
      typeChecker.runTypeChecker();
    } catch (BPLTypeCheckerException e) {
      System.out.println(e);
    }
    this.root = typeChecker.getRoot();
    this.labelNum = 0;
    this.strMap = new HashMap<String, String>();
  }

  public void generate() throws BPLCodeGeneratorException {
    header();
  }

  private void header() throws BPLCodeGeneratorException {
    genCodeGlobalDecs(root.getChildren().get(0));
  }

  private void genCodeGlobalDecs(TreeNode declist) throws BPLCodeGeneratorException {
    System.out.printf("%s%10s%n", ".section", ".rodata");
    System.out.printf("%s%n", ".WriteIntString: .string \"%d \"");
    System.out.printf("%s%n", ".WritelnString: .string \"\\n\"");
    System.out.printf("%s%n", ".WriteStringString: .string \"%s \"");
    System.out.printf("%s%n", ".ReadIntString: .string \"%d\"");
    ArrayList<TreeNode> funDecs = new ArrayList<TreeNode>();
    TreeNode main = null;
	  while (declist.getKind() != TreeNodeKind.EMPTY) {
		  TreeNode dec = declist.getChildren().get(1).getChildren().get(0);
		  if (dec.getKind() == TreeNodeKind.FUN_DEC) {
        if (dec.getChildren().get(1).getValue().equals("main")) {
          main = dec;
        }
        funDecs.add(dec);
		  } else if (dec.getKind() == TreeNodeKind.ARRAY_VAR_DEC) {
		    String id = dec.getChildren().get(1).getValue();
		    String arrayLen = dec.getChildren().get(2).getValue();
		    System.out.printf("%s%n", ".comm " + id + ", " + 8* Integer.parseInt(arrayLen));
		  } else {
			  String id = dec.getChildren().get(1).getValue();
			  System.out.printf("%s%n", ".comm " + id + ", 8");
		  }
		  declist = declist.getChildren().get(0);
	  }
    genCodeStringLiterals(root);
    System.out.printf("%s%n", ".text");

    if (main == null) {
      throw new BPLCodeGeneratorException("no main function declared");
    } else {
      System.out.printf("%s%n%n", ".global main ");
    }

    for (TreeNode f : funDecs) {
      genCodeFunction(f);
    }
  }

  private void genCodeFunction(TreeNode fun) {
    String id = fun.getChildren().get(1).getValue();
    System.out.printf("%s%n", id + ":");

    // move stack pointer to frame pointer
    genRegReg("movq", sp, fp, "setup fp");

    // allocate temporary variables

    // genCodeStatement
    genCodeCompStmt(fun.getChildren().get(3));

    // deallocate temporary variables

    // return
  }

  private void genCodeCompStmt(TreeNode compStmt) {
    if (compStmt.getChildren().size() == 0) {
      return;
    }

    // local dec
    TreeNode statementList = null;
    if (compStmt.getChildren().size() > 1) {
      
      // allocate local variables
    }

    // Statement List
    //genCodeStatementList()


  }

  private void genRegReg(String opcode, String r1, String r2, String comment) {
    System.out.printf("\t %4s %4s, %4s %10s #%s%n", opcode, r1, r2, "", comment);
  }

  private void genCodeStringLiterals(TreeNode root) {
    if (root.getKind() == TreeNodeKind.STR && root.getValue() != null) {
      String label = ".S" + nextLabelNum();
      strMap.put(root.getValue(), label);
      System.out.printf("%s%n", label + ": .string \"" + root.getValue() + "\"");
    }
    for (TreeNode child : root.getChildren()) {
      genCodeStringLiterals(child);
    }
  }

  private int nextLabelNum() {
    return labelNum++;
  }

  public static void main(String[] args) {
    BPLCodeGenerator codeGenerator = new BPLCodeGenerator(args[0]);
	//BPLCodeGenerator codeGenerator = new BPLCodeGenerator("sample_programs/P1.bpl");
    try {
      codeGenerator.generate();
    } catch (BPLCodeGeneratorException e) {
      e.printStackTrace();
    }

  }
}
