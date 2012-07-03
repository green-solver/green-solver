package za.ac.sun.cs.green.expr;

import static org.junit.Assert.*;
import org.junit.Test;

public class ExprTest {

	@Test
	public void testIntConstant01() {
		IntConstant c = new IntConstant(0);
		assertEquals(0, c.getValue());
		assertEquals("0", c.toString());
	}

	@Test
	public void testIntConstant02() {
		IntConstant c = new IntConstant(1);
		assertEquals(1, c.getValue());
		assertEquals("1", c.toString());
	}

	@Test
	public void testIntConstant03() {
		IntConstant c = new IntConstant(-1);
		assertEquals(-1, c.getValue());
		assertEquals("-1", c.toString());
	}

	@Test
	public void testIntConstant04() {
		IntConstant c = new IntConstant(Integer.MAX_VALUE);
		assertEquals(Integer.MAX_VALUE, c.getValue());
		assertEquals("2147483647", c.toString());
	}

	@Test
	public void testIntConstant05() {
		IntConstant c = new IntConstant(Integer.MIN_VALUE);
		assertEquals(Integer.MIN_VALUE, c.getValue());
		assertEquals("-2147483648", c.toString());
	}

	@Test
	public void testRealConstant01() {
		RealConstant c = new RealConstant(0);
		assertTrue(0 == c.getValue());
		assertEquals("0.0", c.toString());
	}

	@Test
	public void testRealConstant02() {
		RealConstant c = new RealConstant(1);
		assertTrue(1 == c.getValue());
		assertEquals("1.0", c.toString());
	}

	@Test
	public void testRealConstant03() {
		RealConstant c = new RealConstant(-1);
		assertTrue(-1 == c.getValue());
		assertEquals("-1.0", c.toString());
	}

	@Test
	public void testRealConstant04() {
		RealConstant c = new RealConstant(3.14159);
		assertTrue(3.14159 == c.getValue());
		assertEquals("3.14159", c.toString());
	}

	@Test
	public void testRealConstant05() {
		RealConstant c = new RealConstant(-10.101112);
		assertTrue(-10.101112 == c.getValue());
		assertEquals("-10.101112", c.toString());
	}

	@Test
	public void testRealConstant06() {
		RealConstant c = new RealConstant(Double.MAX_VALUE);
		assertTrue(Double.MAX_VALUE == c.getValue());
		assertEquals("1.7976931348623157E308", c.toString());
	}

	@Test
	public void testRealConstant07() {
		RealConstant c = new RealConstant(Double.MIN_VALUE);
		assertTrue(Double.MIN_VALUE == c.getValue());
		assertEquals("4.9E-324", c.toString());
	}

	@Test
	public void testIntVariable01() {
		IntVariable v = new IntVariable("v1", -20, 10);
		assertEquals(-20, (int) v.getLowerBound());
		assertEquals(10, (int) v.getUpperBound());
		assertEquals("v1", v.getName());
		assertEquals("v1", v.toString());
	}

	@Test
	public void testIntVariable02() {
		IntVariable v = new IntVariable("x_abc", 2, 10);
		assertEquals(2, (int) v.getLowerBound());
		assertEquals(10, (int) v.getUpperBound());
		assertEquals("x_abc", v.getName());
		assertEquals("x_abc", v.toString());
	}

	@Test
	public void testRealVariable01() {
		RealVariable v = new RealVariable("x999", -20.0, 10.5);
		assertTrue(-20.0 == v.getLowerBound());
		assertTrue(10.5 == v.getUpperBound());
		assertEquals("x999", v.getName());
		assertEquals("x999", v.toString());
	}

	@Test
	public void testRealVariable02() {
		RealVariable v = new RealVariable("a", 2.9, 1000.999);
		assertTrue(2.9 == v.getLowerBound());
		assertTrue(1000.999 == v.getUpperBound());
		assertEquals("a", v.getName());
		assertEquals("a", v.toString());
	}

	private Expression checkOperation0(Operation.Operator op, Expression left, Expression right, String finalStr) {
		Operation o = new Operation(op, left, right);
		assertEquals(op, o.getOperator());
		assertEquals(left, o.getOperand(0));
		assertEquals(right, o.getOperand(1));
		assertEquals(finalStr, o.toString());
		return o;
	}

	private Expression checkOperation1(Operation.Operator op, String opStr) {
		IntConstant c1 = new IntConstant(0);
		IntConstant c2 = new IntConstant(1);
		return checkOperation0(op, c1, c2, "0" + opStr + "1");
	}

	private Expression checkOperation2(Operation.Operator op, Expression sub, String finalStr) {
		Operation o = new Operation(op, sub);
		assertEquals(op, o.getOperator());
		assertEquals(sub, o.getOperand(0));
		assertEquals(finalStr, o.toString());
		return o;
	}

	private Expression checkOperation3(Operation.Operator op, String opStr) {
		IntConstant c = new IntConstant(0);
		return checkOperation2(op, c, opStr + "(0)");
	}

	private Expression checkOperation3a(Operation.Operator op, String opStr) {
		IntConstant c = new IntConstant(0);
		return checkOperation2(op, c, opStr + "0");
	}
	
	@Test
	public void testOperation01() {
		checkOperation1(Operation.Operator.EQ, "==");
		checkOperation1(Operation.Operator.NE, "!=");
		checkOperation1(Operation.Operator.LT, "<");
		checkOperation1(Operation.Operator.LE, "<=");
		checkOperation1(Operation.Operator.GT, ">");
		checkOperation1(Operation.Operator.GE, ">=");
	}

	@Test
	public void testOperation02() {
		checkOperation1(Operation.Operator.AND, "&&");
		checkOperation1(Operation.Operator.OR, "||");
		checkOperation1(Operation.Operator.IMPLIES, "=>");
	}

	@Test
	public void testOperation03() {
		checkOperation1(Operation.Operator.ADD, "+");
		checkOperation1(Operation.Operator.SUB, "-");
		checkOperation1(Operation.Operator.MUL, "*");
		checkOperation1(Operation.Operator.DIV, "/");
		checkOperation1(Operation.Operator.MOD, "%");
	}

	@Test
	public void testOperation04() {
		checkOperation1(Operation.Operator.BIT_AND, "&");
		checkOperation1(Operation.Operator.BIT_OR, "|");
		checkOperation1(Operation.Operator.BIT_XOR, "^");
		checkOperation1(Operation.Operator.SHIFTL, "<<");
		checkOperation1(Operation.Operator.SHIFTR, ">>");
		checkOperation1(Operation.Operator.SHIFTUR, ">>>");
	}

	@Test
	public void testOperation05() {
		IntConstant c1 = new IntConstant(0);
		IntConstant c2 = new IntConstant(1);
		checkOperation0(Operation.Operator.ATAN2, c1, c2, "ATAN2(0,1)");
	}

	@Test
	public void testOperation06() {
		checkOperation3a(Operation.Operator.NOT, "!");
		checkOperation3a(Operation.Operator.NEG, "-");
		checkOperation3a(Operation.Operator.BIT_NOT, "~");
		checkOperation3(Operation.Operator.SIN, "SIN");
		checkOperation3(Operation.Operator.COS, "COS");
		checkOperation3(Operation.Operator.TAN, "TAN");
		checkOperation3(Operation.Operator.ASIN, "ASIN");
		checkOperation3(Operation.Operator.ACOS, "ACOS");
		checkOperation3(Operation.Operator.ATAN, "ATAN");
		checkOperation3(Operation.Operator.ROUND, "ROUND");
		checkOperation3(Operation.Operator.LOG, "LOG");
		checkOperation3(Operation.Operator.EXP, "EXP");
		checkOperation3(Operation.Operator.POWER, "POWER");
		checkOperation3(Operation.Operator.SQRT, "SQRT");
	}

	private Expression checkOperation4(Operation.Operator op, String opStr) {
		IntVariable v1 = new IntVariable("v1", 0, 99);
		IntVariable v2 = new IntVariable("v2", 100, 999);
		return checkOperation0(op, v1, v2, "v1" + opStr + "v2");
	}

	private Expression checkOperation5(Operation.Operator op, String opStr) {
		IntVariable v = new IntVariable("v1", 0, 99);
		return checkOperation2(op, v, opStr + "(v1)");
	}

	private Expression checkOperation5a(Operation.Operator op, String opStr) {
		IntVariable v = new IntVariable("v1", 0, 99);
		return checkOperation2(op, v, opStr + "v1");
	}
	
	@Test
	public void testOperation07() {
		checkOperation4(Operation.Operator.EQ, "==");
		checkOperation4(Operation.Operator.NE, "!=");
		checkOperation4(Operation.Operator.LT, "<");
		checkOperation4(Operation.Operator.LE, "<=");
		checkOperation4(Operation.Operator.GT, ">");
		checkOperation4(Operation.Operator.GE, ">=");
	}

	@Test
	public void testOperation08() {
		checkOperation4(Operation.Operator.AND, "&&");
		checkOperation4(Operation.Operator.OR, "||");
		checkOperation4(Operation.Operator.IMPLIES, "=>");
	}

	@Test
	public void testOperation09() {
		checkOperation4(Operation.Operator.ADD, "+");
		checkOperation4(Operation.Operator.SUB, "-");
		checkOperation4(Operation.Operator.MUL, "*");
		checkOperation4(Operation.Operator.DIV, "/");
		checkOperation4(Operation.Operator.MOD, "%");
	}

	@Test
	public void testOperation10() {
		checkOperation4(Operation.Operator.BIT_AND, "&");
		checkOperation4(Operation.Operator.BIT_OR, "|");
		checkOperation4(Operation.Operator.BIT_XOR, "^");
		checkOperation4(Operation.Operator.SHIFTL, "<<");
		checkOperation4(Operation.Operator.SHIFTR, ">>");
		checkOperation4(Operation.Operator.SHIFTUR, ">>>");
	}

	@Test
	public void testOperation11() {
		IntVariable v1 = new IntVariable("v1", 0, 99);
		IntVariable v2 = new IntVariable("v2", 100, 999);
		checkOperation0(Operation.Operator.ATAN2, v1, v2, "ATAN2(v1,v2)");
	}

	@Test
	public void testOperation12() {
		checkOperation5a(Operation.Operator.NOT, "!");
		checkOperation5a(Operation.Operator.NEG, "-");
		checkOperation5a(Operation.Operator.BIT_NOT, "~");
		checkOperation5(Operation.Operator.SIN, "SIN");
		checkOperation5(Operation.Operator.COS, "COS");
		checkOperation5(Operation.Operator.TAN, "TAN");
		checkOperation5(Operation.Operator.ASIN, "ASIN");
		checkOperation5(Operation.Operator.ACOS, "ACOS");
		checkOperation5(Operation.Operator.ATAN, "ATAN");
		checkOperation5(Operation.Operator.ROUND, "ROUND");
		checkOperation5(Operation.Operator.LOG, "LOG");
		checkOperation5(Operation.Operator.EXP, "EXP");
		checkOperation5(Operation.Operator.POWER, "POWER");
		checkOperation5(Operation.Operator.SQRT, "SQRT");
	}

	@Test
	public void testOperation13() {
		Expression e1 = checkOperation1(Operation.Operator.MUL, "*");
		Expression e2 = checkOperation4(Operation.Operator.MUL, "*");
		checkOperation0(Operation.Operator.ADD, e1, e2, "(0*1)+(v1*v2)");
		e1 = checkOperation1(Operation.Operator.ADD, "+");
		e2 = checkOperation4(Operation.Operator.ADD, "+");
		checkOperation0(Operation.Operator.MUL, e1, e2, "(0+1)*(v1+v2)");
		e1 = checkOperation1(Operation.Operator.MUL, "*");
		e2 = checkOperation4(Operation.Operator.ADD, "+");
		checkOperation0(Operation.Operator.MUL, e1, e2, "(0*1)*(v1+v2)");
		e1 = checkOperation1(Operation.Operator.MUL, "*");
		e2 = checkOperation4(Operation.Operator.ADD, "+");
		checkOperation0(Operation.Operator.EQ, e1, e2, "(0*1)==(v1+v2)");
		e1 = checkOperation1(Operation.Operator.ADD, "+");
		e2 = checkOperation4(Operation.Operator.MUL, "*");
		checkOperation0(Operation.Operator.ATAN2, e1, e2, "ATAN2(0+1,v1*v2)");
	}

}
