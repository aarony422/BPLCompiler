
public class TypeCheckerTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//String inputFileName = "sample_programs/selectionSort.bpl";
		String inputFileName = "parser_test_files/test9.bpl";
		BPLTypeChecker typeChecker = new BPLTypeChecker(inputFileName, true);
		typeChecker.runTypeChecker();
		/*
		try {
			typeChecker.runTypeChecker();
		} catch (BPLTypeCheckerException e) {
			e.printStackTrace();
		}
		*/

	}

}
