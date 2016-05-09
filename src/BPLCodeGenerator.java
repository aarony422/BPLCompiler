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
    for (TreeNode t : compStmt.getChildren()) {
      if (t.getKind() == TreeNodeKind.LOCAL_DECS) {
        // genCodeLocalDecs(t);
      } else if (t.getKind() == TreeNodeKind.STATEMENT_LIST) {
        genCodeStatementList(t);
      }
    }
  }

  private void genCodeStatementList(TreeNode stmtList) {
    while (stmtList.getKind() != TreeNodeKind.EMPTY) {
      TreeNode stmt = stmtList.getChildren().get(1);
      genCodeStatment(stmt);
      stmtList = stmtList.getChildren().get(0);
    }
  }

  private void genCodeStatment(TreeNode statement) {
    TreeNode stmt = statement.getChildren().get(0);
    if (stmt.getKind() == TreeNodeKind.EXPRESSION_STMT) {

    } else if (stmt.getKind() == TreeNodeKind.COMPOUND_STMT) {

    } else if (stmt.getKind() == TreeNodeKind.IF_STMT) {

    } else if (stmt.getKind() == TreeNodeKind.WHILE_STMT) {

    } else if (stmt.getKind() == TreeNodeKind.RETURN_STMT) {

    } else if (stmt.getKind() == TreeNodeKind.WRITE_STMT) {
      genCodeWrite(stmt);
    }
  }

  private void genCodeWrite(TreeNode writeStmt) {
    if (writeStmt.getChildren().size() == 0) {
      genCodeWriteln();
    } else {
      genCodeWriteExp(writeStmt.getChildren().get(0));
    }
  }

  private void genCodeWriteExp(TreeNode exp) {
    if (exp.getChildren().get(0).getKind() == TreeNodeKind.ASSIGN_EXP) {
      // genCodeAssignExp() do something else
    } else {
      genCodeCompExp(exp.getChildren().get(0));
    }
    if (exp.getType() == Type.INT) {
      genRegReg("movl", "%eax", "%esi", "move value to print");
      genRegReg("movq", "$.WriteIntString", "%rdi", "set printf string");
      genRegReg("movl", "$0", "%eax", "clear return value");
      call("printf");
    } else if (exp.getType() == Type.STRING) {
      genRegReg("movq", "%rax", "%rsi", "setting up string to print");
      genRegReg("movq", "$.WriteStringString", "%rdi", "set printf string");
      genRegReg("movl", "$0", "%eax", "clear return value");
      call("printf");
    }
  }

  private void genCodeCompExp(TreeNode compExp) {
    if (compExp.getChildren().size() == 1) {
      genCodeE(compExp.getChildren().get(0));
    } else {
      // E RELOP E
    }
  }

  private void genCodeE(TreeNode E) {
    if (E.getChildren().size() == 1) {
      genCodeT(E.getChildren().get(0));
    } else {
      // E ADDOP t
    }
  }

  private void genCodeT(TreeNode T) {
    if (T.getChildren().size() == 1) {
      genCodeF(T.getChildren().get(0));
    } else {
      // T MULOP F
    }
  }

  private void genCodeF(TreeNode F) {
    if (F.getKind() == TreeNodeKind.NEG_F) {
      // -F genCodeF(F.getChildren().get(0));
    } else {
      if (F.getKind() == TreeNodeKind.ADDRESS_F || F.getKind() == TreeNodeKind.DEREF_F) {
        // genCodeFactor(F)
      } else {
        genCodeFactor(F.getChildren().get(0));
      }
    }
  }

  private void genCodeFactor(TreeNode factor) {
    TreeNode fac = factor.getChildren().get(0);
    if (factor.getKind() == TreeNodeKind.ARRAY_FACTOR) {
      // ID(factor.getChildren().get(0));
      // genCodeExpression(factor.getChildren().get(1));

    } else if (factor.getKind() == TreeNodeKind.ADDRESS_F) {
      // genCodeFactor(fac);

    } else if (factor.getKind() == TreeNodeKind.DEREF_F) {
      // genCodeFactor(fac);

    } else if (fac.getKind() == TreeNodeKind.ID) {
      // genCodeID(fac);

    } else if (fac.getKind() == TreeNodeKind.EXPRESSION) {
      // genCodeExpression(fac);

    } else if (fac.getKind() == TreeNodeKind.FUN_CALL) {
      // ID(fac.getChildren().get(0));
      // Args(fac.getChildren().get(1));
    } else if (fac.getKind() == TreeNodeKind.NUM) {
      genCodeNum(fac);
    } else if (fac.getKind() == TreeNodeKind.STR) {
      genCodeStr(fac);
    } else if (fac.getKind() == TreeNodeKind.READ) {

    }
  }

  private void genCodeStr(TreeNode str) {
    String string = str.getValue();
    String label = strMap.get(string);
    genRegReg("movq", "$"+label, "%rax", "putting string value" + " into ac");

  }

  private void genCodeNum(TreeNode num) {
    String numValue = num.getValue();
    genRegReg("movq", "$"+numValue, "%rax", "putting " + numValue + " into ac");
  }

  private void genCodeWriteln() {
    genRegReg("movl", "$0", "%eax", "Set eax to 0");
    genRegReg("movq", "$.WritelnString", "%rdi", "");
    call("printf");
  }

  private void call(String fun) {
    System.out.printf("\t %s %10s%n", "call " + fun, "");
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
