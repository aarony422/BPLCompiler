
public enum TreeNodeKind {
	PROGRAM(0),
	STATEMENT(1),
	EXPRESSION_STMT(2),
	EXPRESSION(3),
	ID(4);
	
	private int id;
	
	private TreeNodeKind(int id) {
		this.id = id;
	}
}
