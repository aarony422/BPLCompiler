import static org.junit.Assert.*;

import org.junit.Test;

public class TokenTest {

	@Test
	public void testEquals() {
		Token t1 = new Token(Kind.T_NUM, "23", 5);
		Token t2 = new Token(Kind.T_NUM, "23", 5);
		assertEquals(t1, t2);
	}
	
}
