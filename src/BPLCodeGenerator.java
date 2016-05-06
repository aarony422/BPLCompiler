import java.util.HashMap;

public class BPLCodeGenerator {
  private TreeNode root;
  private int labelNum;
  private HashMap<String, String> strMap;

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

  public void generate() {
    header();
  }

  private void header() {
    globalVariables(root.getChildren().get(0));
    System.out.printf("%s%10s%n", ".section", ".rodata");
    System.out.printf("%s%n", ".WriteIntString: .string \"%d \"");
    System.out.printf("%s%n", ".WritelnString: .string \"\\n\"");
    System.out.printf("%s%n", ".WriteStringString: .string \"%s \"");
    System.out.printf("%s%n", ".ReadIntString: .string \"%d\"");
    stringLiterals(root);
    System.out.printf("%s%n", ".text");
  }
  
  private void globalVariables(TreeNode declist) {
	  while (declist.getKind() != TreeNodeKind.EMPTY) {
		  TreeNode dec = declist.getChildren().get(1).getChildren().get(0);
		  if (dec.getKind() == TreeNodeKind.FUN_DEC) {
			  
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
  }

  private void stringLiterals(TreeNode root) {
    if (root.getKind() == TreeNodeKind.STR && root.getValue() != null) {
      String label = ".S" + nextLabelNum();
      strMap.put(root.getValue(), label);
      System.out.printf("%s%n", label + ": .string \"" + root.getValue() + "\"");
    }
    for (TreeNode child : root.getChildren()) {
      stringLiterals(child);
    }
  }
  
  private int nextLabelNum() {
    return labelNum++;  
  }

  public static void main(String[] args) {
    //BPLCodeGenerator codeGenerator = new BPLCodeGenerator(args[0]);
	BPLCodeGenerator codeGenerator = new BPLCodeGenerator("sample_programs/P1.bpl");
    codeGenerator.generate();
  }
}
