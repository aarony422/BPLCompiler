
public class TypeCheckerTest {

  public static void main(String[] args) throws BPLTypeCheckerException, BPLParserException {
    // TODO Auto-generated method stub
    //String inputFileName = "sample_programs/selectionSort.bpl";
    //String inputFileName = "sample_programs/P10.bpl";
    String inputFileName = args[0];
    BPLTypeChecker typeChecker = new BPLTypeChecker(inputFileName, true);

    try {
      typeChecker.runTypeChecker();
    } catch (BPLTypeCheckerException e) {
      System.out.println(e);
      e.printStackTrace();
    }


    if (typeChecker.getRoot() != null) {
      printTree(typeChecker.getRoot(), 0);
    }

  }

  public static void printTree(TreeNode root, int spaces) {
    for (int i = 0; i < spaces; i++) {
      System.out.print("  ");
    }
    System.out.print(root);
    System.out.println("{");

    for (TreeNode child : root.getChildren()) {
      printTree(child, spaces+1);
    }

    for (int i = 0; i < spaces; i++) {
      System.out.print("  ");
    }
    System.out.println("}");
  }
}
