
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
	NUM(19),
	DECLARATION_LIST(20),
	DECLARATION(21),
	FUN_DEC(22),
	PARAMS(23),
	PARAM_LIST(24),
	PARAM(25),
	POINTER_PARAM(26),
	ARRAY_PARAM(27),
	POINTER_VAR_DEC(28),
	ARRAY_VAR_DEC(29);
	
	
	private int id;
	
	private TreeNodeKind(int id) {
		this.id = id;
	}
}
