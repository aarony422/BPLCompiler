/* enum class that defines all token kinds, and initialize them with an integer ID
 * 
 * Name: Aaron (Shang Wei) Young
 * CSCI 331 Compilers Spring 2016
*/
public enum Kind {
	// All Kinds list here. i.e. T_String(1)
	T_ID(0),
	T_NUM(1),
	T_INT(2),
	T_VOID(3),
	T_STR(4), // string
	T_IF(5),
	T_ELSE(6),
	T_WHILE(7),
	T_RETURN(8),
	T_WRITE(9),
	T_WRITELN(10),
	T_READ(11),
	T_SEMICOLON(12),
	T_COMMA(13),
	T_LBRACKET(14), // [
	T_RBRACKET(15), // ]
	T_LBRACE(16), // {
	T_RBRACE(17), // }
	T_LPAREN(18), // (
	T_RPAREN(19), // )
	T_LESS(20), // <
	T_LEQ(21), // <=
	T_DOUBLEEQ(22), // ==
	T_NEQ(23), // !=
	T_GEQ(24), // >=
	T_GREATER(25), // >
	T_PLUS(26), // +
	T_MINUS(27), // -
	T_ASTERISK(28), // *
	T_FSLASH(29), // /
	T_EQ(30), // =
	T_PERCENT(31), // %
	T_AMPERSAND(32), // &
	T_LCOMMENT(33), // /*
	T_RCOMMENT(34), // */
	T_STRING(35), // "hello world"
	T_EOF(36);
	
	private int id;
	
	private Kind(int id) {
		this.id = id;
	}
	
}
