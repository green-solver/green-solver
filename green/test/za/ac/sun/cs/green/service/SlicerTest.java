package za.ac.sun.cs.green.service;

import static org.junit.Assert.*;

import org.junit.Test;

import za.ac.sun.cs.green.EntireSuite;
import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Solver;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.IntVariable;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.service.Slicer;
import za.ac.sun.cs.green.store.NullStore;

public class SlicerTest {

	public static final boolean DETAILED_LOG = false || EntireSuite.DETAILED_LOG;

	private Solver createSolver() {
		Solver solver = new Solver();
		if (DETAILED_LOG) {
			EntireSuite.setDetailedLogger(solver);
		}
		new NullStore(solver);
		new Slicer(solver);
		return solver;
	}

	private void finalCheckSlicing(String sliced, String[] slicedStrs) {
		for (String s : slicedStrs) {
			int p = sliced.indexOf(s);
			assertTrue(p >= 0);
			if (p == 0) {
				sliced = sliced.substring(p + s.length());
			} else if (p > 0) {
				sliced = sliced.substring(0, p - 1) + sliced.substring(p + s.length());
			}
		}
		sliced = sliced.replaceAll("[()&]", "");
		assertEquals("", sliced);
	}

	private void checkSlicing(Solver solver, Expression expression, String fullStr, String... slicedStrs) {
		Instance i = new TestInstance(solver, null, expression);
		Expression e = i.getExpression();
		assertTrue(e.equals(expression));
		assertEquals(expression.toString(), e.toString());
		assertEquals(fullStr, i.getFullExpression().toString());
		solver.issueRequest(null, i);
		finalCheckSlicing(i.getFullExpression().toString(), slicedStrs);
	}

	private void checkSlicing(Solver solver, Expression expression, Expression parentExpression, String fullStr, String... slicedStrs) {
		Instance i1 = new TestInstance(solver, null, parentExpression);
		Instance i2 = new TestInstance(solver, i1, expression);
		Expression e = i2.getExpression();
		assertTrue(e.equals(expression));
		assertEquals(expression.toString(), e.toString());
		assertEquals(fullStr, i2.getFullExpression().toString());
		solver.issueRequest(null, i2);
		finalCheckSlicing(i2.getFullExpression().toString(), slicedStrs);
	}

	@Test
	public void testSlicing01() {
		Solver s = createSolver();
		IntVariable v = new IntVariable("v", 0, 99);
		IntConstant c = new IntConstant(0);
		Operation o = new Operation(Operation.Operator.EQ, v, c);
		checkSlicing(s, o, "v==0", "v==0");
	}

	@Test
	public void testSlicing02() {
		Solver s = createSolver();
		IntVariable v1 = new IntVariable("v1", 0, 99);
		IntConstant c1 = new IntConstant(0);
		Operation o1 = new Operation(Operation.Operator.EQ, v1, c1);
		IntVariable v2 = new IntVariable("v2", 0, 99);
		IntConstant c2 = new IntConstant(1);
		Operation o2 = new Operation(Operation.Operator.NE, v2, c2);
		Operation o3 = new Operation(Operation.Operator.AND, o1, o2);
		checkSlicing(s, o3, "(v1==0)&&(v2!=1)", "v1==0", "v2!=1");
	}

	@Test
	public void testSlicing03() {
		Solver s = createSolver();
		IntVariable v1 = new IntVariable("v1", 0, 99);
		IntConstant c1 = new IntConstant(0);
		Operation o1 = new Operation(Operation.Operator.EQ, v1, c1);
		IntVariable v2 = new IntVariable("v2", 0, 99);
		IntConstant c2 = new IntConstant(1);
		Operation o2 = new Operation(Operation.Operator.NE, v2, c2);
		checkSlicing(s, o1, o2, "(v1==0)&&(v2!=1)", "v1==0");
	}

	@Test
	public void testSlicing04() {
		Solver s = createSolver();
		IntVariable v1 = new IntVariable("v1", 0, 99);
		IntConstant c1 = new IntConstant(0);
		Operation o1 = new Operation(Operation.Operator.EQ, v1, c1);
		IntConstant c2 = new IntConstant(1);
		Operation o2 = new Operation(Operation.Operator.NE, v1, c2);
		checkSlicing(s, o1, o2, "(v1==0)&&(v1!=1)", "v1==0", "v1!=1");
	}

	@Test
	public void testSlicing05() {
		Solver s = createSolver();
		IntVariable v1 = new IntVariable("v1", 0, 99);
		IntVariable v2 = new IntVariable("v2", 0, 99);
		Operation o1 = new Operation(Operation.Operator.EQ, v1, v2);
		IntVariable v3 = new IntVariable("v3", 0, 99);
		Operation o2 = new Operation(Operation.Operator.EQ, v2, v3);
		IntVariable v4 = new IntVariable("v4", 0, 99);
		Operation o3 = new Operation(Operation.Operator.EQ, v3, v4);
		IntVariable v5 = new IntVariable("v5", 0, 99);
		Operation o4 = new Operation(Operation.Operator.EQ, v4, v5);
		Operation o34 = new Operation(Operation.Operator.AND, o3, o4);
		Operation o234 = new Operation(Operation.Operator.AND, o2, o34);
		checkSlicing(s, o1, o234, "(v1==v2)&&((v2==v3)&&((v3==v4)&&(v4==v5)))", "v1==v2", "v2==v3", "v3==v4", "v4==v5");
	}

	@Test
	public void testSlicing06() {
		Solver s = createSolver();
		IntVariable v1 = new IntVariable("v1", 0, 99);
		IntVariable v2 = new IntVariable("v2", 0, 99);
		Operation o1 = new Operation(Operation.Operator.EQ, v1, v2);
		IntVariable v3 = new IntVariable("v3", 0, 99);
		Operation o2 = new Operation(Operation.Operator.EQ, v2, v3);
		IntVariable v4 = new IntVariable("v4", 0, 99);
		Operation o3 = new Operation(Operation.Operator.EQ, v3, v4);
		IntVariable v5 = new IntVariable("v5", 0, 99);
		IntVariable v6 = new IntVariable("v6", 0, 99);
		Operation o4 = new Operation(Operation.Operator.EQ, v5, v6);
		Operation o34 = new Operation(Operation.Operator.AND, o3, o4);
		Operation o234 = new Operation(Operation.Operator.AND, o2, o34);
		checkSlicing(s, o1, o234, "(v1==v2)&&((v2==v3)&&((v3==v4)&&(v5==v6)))", "v2==v3", "v3==v4", "v1==v2");
	}

	@Test
	public void testSlicing07() {
		Solver s = createSolver();
		IntVariable v1 = new IntVariable("v1", 0, 99);
		IntVariable v2 = new IntVariable("v2", 0, 99);
		IntVariable v3 = new IntVariable("v3", 0, 99);
		IntVariable v4 = new IntVariable("v4", 0, 99);
		IntVariable v5 = new IntVariable("v5", 0, 99);
		IntVariable v6 = new IntVariable("v6", 0, 99);
		IntVariable v7 = new IntVariable("v7", 0, 99);
		Operation o1 = new Operation(Operation.Operator.LT, v1, new Operation(Operation.Operator.ADD, v2, v3));
		Operation o2 = new Operation(Operation.Operator.LT, v2, new Operation(Operation.Operator.ADD, v4, v5));
		Operation o3 = new Operation(Operation.Operator.LT, v3, new Operation(Operation.Operator.ADD, v6, v7));
		Operation o23 = new Operation(Operation.Operator.AND, o2, o3);
		checkSlicing(s, o1, o23, "(v1<(v2+v3))&&((v2<(v4+v5))&&(v3<(v6+v7)))", "v1<(v2+v3)", "v3<(v6+v7)", "v2<(v4+v5)");
	}

	@Test
	public void testSlicing08() {
		Solver s = createSolver();
		IntVariable v1 = new IntVariable("v1", 0, 99);
		IntVariable v2 = new IntVariable("v2", 0, 99);
		IntVariable v3 = new IntVariable("v3", 0, 99);
		IntVariable v4 = new IntVariable("v4", 0, 99);
		IntVariable v5 = new IntVariable("v5", 0, 99);
		IntVariable v6 = new IntVariable("v6", 0, 99);
		IntVariable v7 = new IntVariable("v7", 0, 99);
		IntVariable v8 = new IntVariable("v8", 0, 99);
		Operation o1 = new Operation(Operation.Operator.LT, v1, new Operation(Operation.Operator.ADD, v2, v3));
		Operation o2 = new Operation(Operation.Operator.LT, v2, new Operation(Operation.Operator.ADD, v4, v5));
		Operation o3 = new Operation(Operation.Operator.LT, v6, new Operation(Operation.Operator.ADD, v7, v8));
		Operation o23 = new Operation(Operation.Operator.AND, o2, o3);
		checkSlicing(s, o1, o23, "(v1<(v2+v3))&&((v2<(v4+v5))&&(v6<(v7+v8)))", "v1<(v2+v3)", "v2<(v4+v5)");
	}

	private class TestInstance extends Instance {

		public TestInstance(Solver solver, Instance parent,
				Expression expression) {
			super(solver, parent, expression);
		}
		
	}

}
