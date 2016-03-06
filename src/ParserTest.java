
/* Program takes the name of a BPL file, opens file, and repeatedly calls
 * getNextToken(), printing each token found until it gets to the end of the
 * file.
 * 
 * Name: Aaron (Shang Wei) Young
 * CSCI 331 Compilers Spring 2016
*/

public class ParserTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String inputFileName = "parser_test_files/test9.bpl";
		BPLParser parser = new BPLParser(inputFileName);
		TreeNode node = null;
		try {
			node = parser.parse();
		} catch (BPLParserException e) {
			e.printStackTrace();
		}
		
		// TODO: Print Tree
		if (node != null) {
			printTree(node, 0);
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