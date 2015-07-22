package za.ac.sun.cs.green.util;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import za.ac.sun.cs.green.EntireSuite;
import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.IntVariable;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.taskmanager.ParallelTaskManager;
import za.ac.sun.cs.green.util.Configuration;

public class ParallelSATTest {

	public static Green solver = null;

	@BeforeClass
	public static void initialize() {
		if (!EntireSuite.HAS_CVC3) {
			Assume.assumeTrue(false);
			return;
		}
		
		solver = new Green();
		Properties props = new Properties();
		props.setProperty("green.taskmanager", ParallelTaskManager.class.getCanonicalName());
		props.setProperty("green.services", "sat");
		props.setProperty("green.service.sat", "(slice (canonize choco cvc3))");
		props.setProperty("green.service.sat.slice",
				"za.ac.sun.cs.green.service.slicer.SATSlicerService");
		props.setProperty("green.service.sat.canonize",
				"za.ac.sun.cs.green.service.canonizer.SATCanonizerService");
		props.setProperty("green.service.sat.choco",
				"za.ac.sun.cs.green.service.choco.SATChocoService");
		props.setProperty("green.service.sat.cvc3",
				"za.ac.sun.cs.green.service.cvc3.SATCVC3Service");
		Configuration config = new Configuration(solver, props);
		config.configure();
	}

	@AfterClass
	public static void report() {
		if (solver != null) {
			solver.report();
		}
	}

	private void check(Expression expression, Expression parentExpression, boolean expected) {
		Instance p = (parentExpression == null) ? null : new Instance(solver, null, parentExpression);
		Instance i = new Instance(solver, p, expression);
		Object result = i.request("sat");
		assertNotNull(result);
		assertEquals(Boolean.class, result.getClass());
		assertEquals(expected, result);
	}

	private void checkSat(Expression expression) {
		check(expression, null, true);
	}

	private void checkUnsat(Expression expression) {
		check(expression, null, false);
	}
	
	private void checkSat(Expression expression, Expression parentExpression) {
		check(expression, parentExpression, true);
	}
	
	private void checkUnsat(Expression expression, Expression parentExpression) {
		check(expression, parentExpression, false);
	}
	
	@Test
	public void test01() {
		IntVariable v = new IntVariable("aa", 0, 99);
		IntConstant c = new IntConstant(0);
		Operation o = new Operation(Operation.Operator.EQ, v, c);
		checkSat(o);
	}

	@Test
	public void test02() {
		IntVariable v = new IntVariable("aa", 0, 99);
		IntConstant c = new IntConstant(100);
		Operation o = new Operation(Operation.Operator.EQ, v, c);
		checkUnsat(o);
	}

	@Test
	public void test03() {
		IntVariable v = new IntVariable("aa", 0, 99);
		IntConstant c1 = new IntConstant(10);
		IntConstant c2 = new IntConstant(10);
		Operation o1 = new Operation(Operation.Operator.EQ, v, c1);
		Operation o2 = new Operation(Operation.Operator.EQ, v, c2);
		Operation o3 = new Operation(Operation.Operator.AND, o1, o2);
		checkSat(o3);
	}

	@Test
	public void test04() {
		IntVariable v = new IntVariable("aa", 0, 99);
		IntConstant c1 = new IntConstant(10);
		IntConstant c2 = new IntConstant(20);
		Operation o1 = new Operation(Operation.Operator.EQ, v, c1);
		Operation o2 = new Operation(Operation.Operator.EQ, v, c2);
		Operation o3 = new Operation(Operation.Operator.AND, o1, o2);
		checkUnsat(o3);
	}

	@Test
	public void test05() {
		IntVariable v = new IntVariable("aa", 0, 99);
		IntConstant c1 = new IntConstant(10);
		IntConstant c2 = new IntConstant(20);
		Operation o1 = new Operation(Operation.Operator.GE, v, c1);
		Operation o2 = new Operation(Operation.Operator.LT, v, c2);
		Operation o3 = new Operation(Operation.Operator.AND, o1, o2);
		checkSat(o3);
	}

	@Test
	public void test06() {
		IntVariable v = new IntVariable("aa", 0, 99);
		IntConstant c1 = new IntConstant(10);
		IntConstant c2 = new IntConstant(20);
		Operation o1 = new Operation(Operation.Operator.GE, v, c1);
		Operation o2 = new Operation(Operation.Operator.LT, v, c2);
		checkSat(o1, o2);
	}

	@Test
	public void test07() {
		IntVariable v1 = new IntVariable("aa", 0, 99);
		IntVariable v2 = new IntVariable("bb", 0, 99);
		IntConstant c1 = new IntConstant(10);
		IntConstant c2 = new IntConstant(2012);
		Operation o1 = new Operation(Operation.Operator.GE, v1, c1);
		Operation o2 = new Operation(Operation.Operator.EQ, v2, c2);
		checkSat(o1, o2);
	}

	@Test
	public void test08() {
		IntVariable v1 = new IntVariable("aa", 0, 99);
		IntVariable v2 = new IntVariable("bb", 0, 99);
		IntConstant c1 = new IntConstant(10);
		IntConstant c2 = new IntConstant(2012);
		Operation o1 = new Operation(Operation.Operator.GE, v1, c1);
		Operation o2 = new Operation(Operation.Operator.EQ, v2, c2);
		checkUnsat(o2, o1);
	}

	@Test
	public void test09() {
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
		checkUnsat(o12, o345);
	}

	@Test
	public void test10() {
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
		checkSat(o12, o345);
	}

	@Test
	public void test11() {
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
		checkSat(o12, o34);
	}

	@Test
	public void test12() {
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
		checkSat(o12, o34);
	}

	@Test
	public void test13() {
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
		checkUnsat(o12, o34);
	}

	@Test
	public void test14() {
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
		checkUnsat(o1, o23456);
	}

}
