
public class Token {
	Kind kind;
	String value;
	int line;
	
	public Token(Kind kind, String value, int line) {
		this.kind = kind;
		this.value = value;
		this.line = line;
	}
}
