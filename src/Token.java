
public class Token {
	private Kind kind;
	private String value;
	private int line;
	
	public Token(Kind kind, String value, int line) {
		this.kind = kind;
		this.value = value;
		this.line = line;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Token)) { return false; }
		if (obj == this) { return true; }
		
		Token t = (Token) obj;
		return (this.kind == t.kind) && this.value.equals(t.value) && (this.line == t.line);
	}
	
	public Kind getKind() {
		return this.kind;
	}
	
	@Override
	public String toString() {
		String s = "Token " + this.kind + ",\tstring " + this.value + ",\tline number " + this.line;
		return s;
	}
}
