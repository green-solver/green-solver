package za.ac.sun.cs.green.service.canonizer;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.BeforeClass;
import org.junit.Test;

import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.IntVariable;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.util.Configuration;

public class SATCanonizerTest2 {

	public static Green solver;

	@BeforeClass
	public static void initialize() {
		solver = new Green();
		Properties props = new Properties();
		props.setProperty("green.services", "sat");
		props.setProperty("green.service.sat", "(canonize sink)");
		props.setProperty("green.service.sat.canonize", "za.ac.sun.cs.green.service.canonizer.SATCanonizerService");
		props.setProperty("green.service.sat.sink", "za.ac.sun.cs.green.service.sink.SinkService");
		Configuration config = new Configuration(solver, props);
		config.configure();
	}

	private void finalCheck(String observed, String[] expected) {
		String s0 = observed.replaceAll("[()]", "");
		String s1 = s0.replaceAll("v[0-9]", "v");
		SortedSet<String> s2 = new TreeSet<String>(Arrays.asList(s1.split("&&")));
		SortedSet<String> s3 = new TreeSet<String>(Arrays.asList(expected));
		assertEquals(s2, s3);
	}

	private void check(Expression expression, String full, String... expected) {
		Instance i = new Instance(solver, null, null, expression);
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

	private void check(Expression expression, Expression parentExpression, String full, String... expected) {
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
	public void test23() {
		IntVariable x5 = new IntVariable("x5", 0, 99);
		IntConstant c1 = new IntConstant(1);
		IntConstant c0 = new IntConstant(0);
		Operation o2b = new Operation(Operation.Operator.NE, x5, x5);
		Operation o2a = new Operation(Operation.Operator.NOT, o2b);
		check(o2a, "!(x5!=x5)", "0==0");
	}

}
