/* Token object that stores the Kind, String value, and the line number it was 
 * found. 
 * 
 * Name: Aaron (Shang Wei) Young
 * CSCI 331 Compilers Spring 2016
*/

public class Token {
	private Kind kind;
	private String value;
	private int line;
	
	public Token(Kind kind, String value, int line) {
		this.kind = kind;
		this.value = value;
		this.line = line;
	}
	
	public String getValue() {
		return this.value;
	}
	
	public int getLine() {
		return this.line;
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
