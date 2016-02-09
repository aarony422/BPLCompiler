
public class Token {
	Kind kind;
	String value;
	int line;
	
	public Token(Kind kind, String value, int line) {
		this.kind = kind;
		this.value = value;
		this.line = line;
	}
	
	@Override
	public String toString() {
		String s = "Token " + this.kind + ", string " + this.value + ", line number " + this.line;
		return s;
	}
}
