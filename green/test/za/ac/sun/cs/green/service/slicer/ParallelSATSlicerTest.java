package za.ac.sun.cs.green.service.slicer;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.IntVariable;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.taskmanager.ParallelTaskManager;
import za.ac.sun.cs.green.util.Configuration;

public class ParallelSATSlicerTest {

	public static Green solver;

	@BeforeClass
	public static void initialize() {
		solver = new Green();
		Properties props = new Properties();
		props.setProperty("green.taskmanager", ParallelTaskManager.class.getCanonicalName());
		props.setProperty("green.services", "sat");
		props.setProperty("green.service.sat", "(slice sink)");
		props.setProperty("green.service.sat.slice",
				"za.ac.sun.cs.green.service.slicer.SATSlicerService");
		props.setProperty("green.service.sat.sink",
				"za.ac.sun.cs.green.service.sink.SinkService");
		Configuration config = new Configuration(solver, props);
		config.configure();
	}

	private void finalCheck(String observed, String[] expected) {
		for (String s : expected) {
			int p = observed.indexOf(s);
			assertTrue(p >= 0);
			if (p == 0) {
				observed = observed.substring(p + s.length());
			} else if (p > 0) {
				observed = observed.substring(0, p - 1)
						+ observed.substring(p + s.length());
			}
		}
		observed = observed.replaceAll("[()&]", "");
		assertEquals("", observed);
	}

	private void check(Expression expression, String full,
			String... expected) {
		Instance i = new Instance(solver, null, expression);
		Expression e = i.getExpression();
		assertTrue(e.equals(expression));
		assertEquals(expression.toString(), e.toString());
		assertEquals(full, i.getFullExpression().toString());
		Object result = i.request("sat");
		assertNotNull(result);
		assertEquals(Instance.class, result.getClass());
		Instance j = (Instance) result;
		finalCheck(j.getExpression().toString(), expected);
	}

	private void check(Expression expression, Expression parentExpression,
			String full, String... expected) {
		Instance i1 = new Instance(solver, null, parentExpression);
		Instance i2 = new Instance(solver, i1, expression);
		Expression e = i2.getExpression();
		assertTrue(e.equals(expression));
		assertEquals(expression.toString(), e.toString());
		assertEquals(full, i2.getFullExpression().toString());
		Object result = i2.request("sat");
		assertNotNull(result);
		assertEquals(Instance.class, result.getClass());
		Instance j = (Instance) result;
		finalCheck(j.getExpression().toString(), expected);
	}

	@Test
	public void test01() {
		IntVariable v = new IntVariable("v", 0, 99);
		IntConstant c = new IntConstant(0);
		Operation o = new Operation(Operation.Operator.EQ, v, c);
		check(o, "v==0", "v==0");
	}

	@Test
	public void test02a() {
		IntConstant c1 = new IntConstant(2);
		IntConstant c2 = new IntConstant(2);
		Operation o = new Operation(Operation.Operator.EQ, c1, c2);
		check(o, "2==2", "2==2");
	}
	
	@Test
	public void test02b() {
		IntConstant c1 = new IntConstant(2);
		IntConstant c2 = new IntConstant(2);
		Operation o = new Operation(Operation.Operator.LT, c1, c2);
		check(o, "2<2", "2<2");
	}
	
	@Test
	public void test03() {
		IntVariable v1 = new IntVariable("v1", 0, 99);
		IntConstant c1 = new IntConstant(0);
		Operation o1 = new Operation(Operation.Operator.EQ, v1, c1);
		IntVariable v2 = new IntVariable("v2", 0, 99);
		IntConstant c2 = new IntConstant(1);
		Operation o2 = new Operation(Operation.Operator.NE, v2, c2);
		check(o1, o2, "(v1==0)&&(v2!=1)", "v1==0");
	}

	@Test
	public void test04() {
		IntVariable v1 = new IntVariable("v1", 0, 99);
		IntConstant c1 = new IntConstant(0);
		Operation o1 = new Operation(Operation.Operator.EQ, v1, c1);
		IntVariable v2 = new IntVariable("v2", 0, 99);
		IntConstant c2 = new IntConstant(1);
		Operation o2 = new Operation(Operation.Operator.NE, v2, c2);
		check(o1, o2, "(v1==0)&&(v2!=1)", "v1==0");
	}
	
	@Test
	public void test05() {
		IntVariable v1 = new IntVariable("v1", 0, 99);
		IntConstant c1 = new IntConstant(0);
		Operation o1 = new Operation(Operation.Operator.EQ, v1, c1);
		IntConstant c2 = new IntConstant(1);
		Operation o2 = new Operation(Operation.Operator.NE, v1, c2);
		check(o1, o2, "(v1==0)&&(v1!=1)", "v1==0", "v1!=1");
	}
	
	@Test
	public void test06() {
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
		check(o1, o234, "(v1==v2)&&((v2==v3)&&((v3==v4)&&(v4==v5)))", "v1==v2", "v2==v3", "v3==v4", "v4==v5");
	}
	
	@Test
	public void test07() {
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
		check(o1, o234, "(v1==v2)&&((v2==v3)&&((v3==v4)&&(v5==v6)))", "v2==v3", "v3==v4", "v1==v2");
	}
	
	@Test
	public void test08() {
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
		check(o1, o23, "(v1<(v2+v3))&&((v2<(v4+v5))&&(v3<(v6+v7)))", "v1<(v2+v3)", "v3<(v6+v7)", "v2<(v4+v5)");
	}
	
	@Test
	public void test09() {
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
		check(o1, o23, "(v1<(v2+v3))&&((v2<(v4+v5))&&(v6<(v7+v8)))", "v1<(v2+v3)", "v2<(v4+v5)");
	}
	
}
