
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
	IF_STMT(9),
	RETURN_STMT(10),
	WRITE_STMT(11),
	INT(12),
	VOID(13),
	STR(14),
	TYPE_SPECIFIER(15),
	LOCAL_DECS(16),
	VAR_DEC(17),
	ASTERISK(18),
	NUM(19);
	
	
	private int id;
	
	private TreeNodeKind(int id) {
		this.id = id;
	}
}