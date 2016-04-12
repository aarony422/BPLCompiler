
public enum Type {
	INT(0),
	STRING(1),
	VOID(2),
	INT_ARRAY(3),
	STRING_ARRAY(4),
	INT_PTR(5),
	STRING_PTR(6),
	INT_ADDRESS(7),
	STRING_ADDRESS(8),
	NONE(9);
	
	private int id;
	
	private Type(int id) {
		this.id = id;
	}
}
