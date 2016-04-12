
public class TypeCheckerTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//String inputFileName = "sample_programs/selectionSort.bpl";
		String inputFileName = "sample_programs/P2.bpl";
		BPLTypeChecker typeChecker = new BPLTypeChecker(inputFileName, true);
		
		try {
			typeChecker.runTypeChecker();
		} catch (BPLTypeCheckerException e) {
			e.printStackTrace();
		}

	}

}
