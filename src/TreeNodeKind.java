
public enum TreeNodeKind {
	PROGRAM(0),
	STATEMENT(1),
	EXPRESSION_STMT(2),
	EXPRESSION(3),
	ID(4),
	COMPOUND_STMT(5),
	STATEMENT_LIST(6),
	EMPTY(7),
	WHILE_STMT(8),
	IF_STMT(9);
	
	
	private int id;
	
	private TreeNodeKind(int id) {
		this.id = id;
	}
}
