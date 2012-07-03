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
import za.ac.sun.cs.green.store.NullStore;

public class ChocoTest {

	public static final boolean DETAILED_LOG = false || EntireSuite.DETAILED_LOG;

	private Solver solver1 = null;

	private Solver solver2 = null;

//	private Solver solver3 = null;

	private void createSolvers() {
		solver1 = new Solver();
		new NullStore(solver1);
		new Choco(solver1);
		if (DETAILED_LOG) {
			EntireSuite.setDetailedLogger(solver1);
		}

		solver2 = new Solver();
		new NullStore(solver2);
		new Slicer(solver2);
		new Choco(solver2);
		if (DETAILED_LOG) {
			EntireSuite.setDetailedLogger(solver2);
		}
		
//		solver3 = new Solver();
//		solver3.setSlicer(new DefaultSlicer());
//		solver3.setCanonizer(new DefaultCanonizer());
//		solver3.setDecisionProcedure(new Choco());
//		if (DETAILED_LOG) {
//			EntireSuite.setDetailedLogger(solver3);
//		}
	}

	private static class ChocoTestInstance extends Instance {

		public ChocoTestInstance(Solver solver, Instance parent, Expression expression) {
			super(solver, parent, expression);
		}

		public boolean isSatisfiable() {
			return (Boolean) issueRequest("ISSAT");
		}
	}

	private void checkSatisfiability(Expression expression, boolean result1, boolean result2, boolean result3) {
		createSolvers();
		ChocoTestInstance instance1 = new ChocoTestInstance(solver1, null, expression);
		assertEquals(result1, instance1.isSatisfiable());
		ChocoTestInstance instance2 = new ChocoTestInstance(solver2, null, expression);
		assertEquals(result2, instance2.isSatisfiable());
//		ChocoTestInstance instance3 = new ChocoTestInstance(solver3, null, expression);
//		assertEquals(result3, instance3.isSatisfiable());
	}

	private void checkSatisfiability(Expression expression, boolean result) {
		checkSatisfiability(expression, result, result, result);
	}

	private void checkSatisfiability(Expression expression, Expression parentExpression, boolean result1, boolean result2, boolean result3) {
		createSolvers();
		ChocoTestInstance instance1p = new ChocoTestInstance(solver1, null, parentExpression);
		ChocoTestInstance instance1 = new ChocoTestInstance(solver1, instance1p, expression);
		assertEquals(result1, instance1.isSatisfiable());
		ChocoTestInstance instance2p = new ChocoTestInstance(solver2, null, parentExpression);
		ChocoTestInstance instance2 = new ChocoTestInstance(solver2, instance2p, expression);
		assertEquals(result2, instance2.isSatisfiable());
//		ChocoTestInstance instance3p = new ChocoTestInstance(solver3, null, parentExpression);
//		ChocoTestInstance instance3 = new ChocoTestInstance(solver3, instance3p, expression);
//		assertEquals(result3, instance3.isSatisfiable());
	}

	private void checkSatisfiability(Expression expression, Expression parentExpression, boolean result) {
		checkSatisfiability(expression, parentExpression, result, result, result);
	}

	@Test
	public void testChoco01() {
		IntVariable v = new IntVariable("aa", 0, 99);
		IntConstant c = new IntConstant(0);
		Operation o = new Operation(Operation.Operator.EQ, v, c);
		checkSatisfiability(o, true);
	}

	@Test
	public void testChoco02() {
		IntVariable v = new IntVariable("aa", 0, 99);
		IntConstant c = new IntConstant(100);
		Operation o = new Operation(Operation.Operator.EQ, v, c);
		checkSatisfiability(o, false);
	}

	@Test
	public void testChoco03() {
		IntVariable v = new IntVariable("aa", 0, 99);
		IntConstant c1 = new IntConstant(10);
		IntConstant c2 = new IntConstant(10);
		Operation o1 = new Operation(Operation.Operator.EQ, v, c1);
		Operation o2 = new Operation(Operation.Operator.EQ, v, c2);
		Operation o3 = new Operation(Operation.Operator.AND, o1, o2);
		checkSatisfiability(o3, true);
	}

	@Test
	public void testChoco04() {
		IntVariable v = new IntVariable("aa", 0, 99);
		IntConstant c1 = new IntConstant(10);
		IntConstant c2 = new IntConstant(20);
		Operation o1 = new Operation(Operation.Operator.EQ, v, c1);
		Operation o2 = new Operation(Operation.Operator.EQ, v, c2);
		Operation o3 = new Operation(Operation.Operator.AND, o1, o2);
		checkSatisfiability(o3, false);
	}

	@Test
	public void testChoco05() {
		IntVariable v = new IntVariable("aa", 0, 99);
		IntConstant c1 = new IntConstant(10);
		IntConstant c2 = new IntConstant(20);
		Operation o1 = new Operation(Operation.Operator.GE, v, c1);
		Operation o2 = new Operation(Operation.Operator.LT, v, c2);
		Operation o3 = new Operation(Operation.Operator.AND, o1, o2);
		checkSatisfiability(o3, true);
	}

	@Test
	public void testChoco06() {
		IntVariable v = new IntVariable("aa", 0, 99);
		IntConstant c1 = new IntConstant(10);
		IntConstant c2 = new IntConstant(20);
		Operation o1 = new Operation(Operation.Operator.GE, v, c1);
		Operation o2 = new Operation(Operation.Operator.LT, v, c2);
		checkSatisfiability(o1, o2, true);
	}

	@Test
	public void testChoco07() {
		IntVariable v1 = new IntVariable("aa", 0, 99);
		IntVariable v2 = new IntVariable("bb", 0, 99);
		IntConstant c1 = new IntConstant(10);
		IntConstant c2 = new IntConstant(2012);
		Operation o1 = new Operation(Operation.Operator.GE, v1, c1);
		Operation o2 = new Operation(Operation.Operator.EQ, v2, c2);
		checkSatisfiability(o1, o2, false, true, true);
	}

	@Test
	public void testChoco08() {
		IntVariable v1 = new IntVariable("aa", 0, 99);
		IntVariable v2 = new IntVariable("bb", 0, 99);
		IntConstant c1 = new IntConstant(10);
		IntConstant c2 = new IntConstant(2012);
		Operation o1 = new Operation(Operation.Operator.GE, v1, c1);
		Operation o2 = new Operation(Operation.Operator.EQ, v2, c2);
		checkSatisfiability(o2, o1, false);
	}

	@Test
	public void testChoco09() {
		IntVariable v1 = new IntVariable("aa", 0, 99);
		IntVariable v2 = new IntVariable("bb", 0, 99);
		IntVariable v3 = new IntVariable("cc", 0, 99);
		IntVariable v4 = new IntVariable("dd", 0, 99);
		IntVariable v5 = new IntVariable("ee", 0, 99);
		Operation o1 = new Operation(Operation.Operator.LT, v1, v2);
		Operation o2 = new Operation(Operation.Operator.LT, v2, v3);
		Operation o3 = new Operation(Operation.Operator.LT, v3, v4);
		Operation o4 = new Operation(Operation.Operator.LT, v4, v5);
		Operation o5 = new Operation(Operation.Operator.LT, v5, v1);
		Operation o45 = new Operation(Operation.Operator.AND, o4, o5);
		Operation o345 = new Operation(Operation.Operator.AND, o3, o45);
		Operation o12 = new Operation(Operation.Operator.AND, o1, o2);
		checkSatisfiability(o12, o345, false);
	}

	@Test
	public void testChoco10() {
		IntVariable v1 = new IntVariable("aa", 0, 99);
		IntVariable v2 = new IntVariable("bb", 0, 99);
		IntVariable v3 = new IntVariable("cc", 0, 99);
		IntVariable v4 = new IntVariable("dd", 0, 99);
		IntVariable v5 = new IntVariable("ee", 0, 99);
		Operation o1 = new Operation(Operation.Operator.LE, v1, v2);
		Operation o2 = new Operation(Operation.Operator.LE, v2, v3);
		Operation o3 = new Operation(Operation.Operator.LE, v3, v4);
		Operation o4 = new Operation(Operation.Operator.LE, v4, v5);
		Operation o5 = new Operation(Operation.Operator.LE, v5, v1);
		Operation o45 = new Operation(Operation.Operator.AND, o4, o5);
		Operation o345 = new Operation(Operation.Operator.AND, o3, o45);
		Operation o12 = new Operation(Operation.Operator.AND, o1, o2);
		checkSatisfiability(o12, o345, true);
	}

	@Test
	public void testChoco11() {
		IntVariable v1 = new IntVariable("aa", 0, 99);
		IntVariable v2 = new IntVariable("bb", 0, 99);
		IntVariable v3 = new IntVariable("cc", 0, 99);
		IntVariable v4 = new IntVariable("dd", 0, 99);
		IntVariable v5 = new IntVariable("ee", 0, 99);
		IntConstant c1 = new IntConstant(2);
		Operation o1 = new Operation(Operation.Operator.EQ, v2, new Operation(Operation.Operator.MUL, c1, v1));
		Operation o2 = new Operation(Operation.Operator.EQ, v3, new Operation(Operation.Operator.MUL, c1, v2));
		Operation o3 = new Operation(Operation.Operator.EQ, v4, new Operation(Operation.Operator.MUL, c1, v3));
		Operation o4 = new Operation(Operation.Operator.EQ, v5, new Operation(Operation.Operator.MUL, c1, v4));
		Operation o12 = new Operation(Operation.Operator.AND, o1, o2);
		Operation o34 = new Operation(Operation.Operator.AND, o3, o4);
		checkSatisfiability(o12, o34, true);
	}

	@Test
	public void testChoco12() {
		IntVariable v1 = new IntVariable("aa", 0, 9);
		IntVariable v2 = new IntVariable("bb", 0, 9);
		IntVariable v3 = new IntVariable("cc", 0, 9);
		IntVariable v4 = new IntVariable("dd", 0, 9);
		IntVariable v5 = new IntVariable("ee", 0, 9);
		IntConstant c1 = new IntConstant(2);
		Operation o1 = new Operation(Operation.Operator.EQ, v2, new Operation(Operation.Operator.MUL, c1, v1));
		Operation o2 = new Operation(Operation.Operator.EQ, v3, new Operation(Operation.Operator.MUL, c1, v2));
		Operation o3 = new Operation(Operation.Operator.EQ, v4, new Operation(Operation.Operator.MUL, c1, v3));
		Operation o4 = new Operation(Operation.Operator.EQ, v5, new Operation(Operation.Operator.MUL, c1, v4));
		Operation o12 = new Operation(Operation.Operator.AND, o1, o2);
		Operation o34 = new Operation(Operation.Operator.AND, o3, o4);
		checkSatisfiability(o12, o34, true);
	}

	@Test
	public void testChoco13() {
		IntVariable v1 = new IntVariable("aa", 1, 9);
		IntVariable v2 = new IntVariable("bb", 0, 9);
		IntVariable v3 = new IntVariable("cc", 0, 9);
		IntVariable v4 = new IntVariable("dd", 0, 9);
		IntVariable v5 = new IntVariable("ee", 0, 9);
		IntConstant c1 = new IntConstant(2);
		Operation o1 = new Operation(Operation.Operator.EQ, v2, new Operation(Operation.Operator.MUL, c1, v1));
		Operation o2 = new Operation(Operation.Operator.EQ, v3, new Operation(Operation.Operator.MUL, c1, v2));
		Operation o3 = new Operation(Operation.Operator.EQ, v4, new Operation(Operation.Operator.MUL, c1, v3));
		Operation o4 = new Operation(Operation.Operator.EQ, v5, new Operation(Operation.Operator.MUL, c1, v4));
		Operation o12 = new Operation(Operation.Operator.AND, o1, o2);
		Operation o34 = new Operation(Operation.Operator.AND, o3, o4);
		checkSatisfiability(o12, o34, false);
	}

	@Test
	public void testChoco14() {
		IntVariable v1 = new IntVariable("i1", 0, 2048);
		IntVariable v2 = new IntVariable("j2", 0, 2048);
		IntVariable v3 = new IntVariable("k3", 0, 2048);
		IntConstant c1 = new IntConstant(0);
		Operation o1 = new Operation(Operation.Operator.NE, v2, v3);
		Operation o2 = new Operation(Operation.Operator.EQ, v1, v3);
		Operation o3 = new Operation(Operation.Operator.EQ, v1, v2);
		Operation o4 = new Operation(Operation.Operator.GT, v1, c1);
		Operation o5 = new Operation(Operation.Operator.GT, v2, c1);
		Operation o6 = new Operation(Operation.Operator.GT, v3, c1);
		Operation o34 = new Operation(Operation.Operator.AND, o3, o4);
		Operation o56 = new Operation(Operation.Operator.AND, o5, o6);
		Operation o234 = new Operation(Operation.Operator.AND, o2, o34);
		Operation o23456 = new Operation(Operation.Operator.AND, o234, o56);
		checkSatisfiability(o1, o23456, false);
	}

}
