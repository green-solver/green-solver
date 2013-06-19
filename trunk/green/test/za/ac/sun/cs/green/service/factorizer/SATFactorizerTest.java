package za.ac.sun.cs.green.service.factorizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.BeforeClass;
import org.junit.Test;

import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.IntVariable;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.service.sink.FactorSinkService;
import za.ac.sun.cs.green.util.Configuration;

public class SATFactorizerTest {

	public static Green solver;

	@BeforeClass
	public static void initialize() {
		solver = new Green();
		Properties props = new Properties();
		props.setProperty("green.services", "sat");
		props.setProperty("green.service.sat", "(factor sink)");
		props.setProperty("green.service.sat.factor", "za.ac.sun.cs.green.service.factorizer.SATFactorizerService");
		props.setProperty("green.service.sat.sink", "za.ac.sun.cs.green.service.sink.FactorSinkService");
		Configuration config = new Configuration(solver, props);
		config.configure();
	}

	private boolean finalCheck(String[] expected, Instance factor) {
		String s0 = factor.getExpression().toString().replaceAll("[()]", "");
		String s1 = s0.replaceAll("v[0-9]", "v");
		SortedSet<String> s2 = new TreeSet<String>(Arrays.asList(s1.split("&&")));
		SortedSet<String> s3 = new TreeSet<String>(Arrays.asList(expected));
		return s2.equals(s3);
	}
	
	private void finalCheck(String[][] expected, Set<Instance> factors) {
		assertEquals(expected.length, factors.size());
		for (Instance i : factors) {
			boolean found = false;
			for (String[] e : expected) {
				if (finalCheck(e, i)) {
					found = true;
					break;
				}
			}
			if (!found) {
				System.out.println("Not found: " + i.getExpression());
			}
			assertTrue(found);
		}
	}

	private void check(Expression expression, String[]... expected) {
		Instance i = new Instance(solver, null, expression);
		Expression e = i.getExpression();
		assertTrue(e.equals(expression));
		assertEquals(expression.toString(), e.toString());
		Object result = i.request("sat");
		assertEquals(null, result);
		Object f0 = i.getData(FactorSinkService.class);
		assertTrue(f0 instanceof Set<?>);
		@SuppressWarnings("unchecked")
		Set<Instance> f = (Set<Instance>) f0;
		System.out.println("Original expression:");
		System.out.println("  " + expression);
		System.out.println("Expected:");
		for (String[] ex : expected) {
			System.out.print(" ");
			for (String ey : ex) {
				System.out.println(" " + ey);
			}
			System.out.println();
		}
		System.out.println("Factors:");
		for (Instance in : f) {
			System.out.println("  " + in.getExpression());
		}
		finalCheck(expected, f);
	}

	private void check(Expression expression, Expression parentExpression, String... expected) {
		Instance i1 = new Instance(solver, null, parentExpression);
		Instance i2 = new Instance(solver, i1, expression);
		Expression e = i2.getExpression();
		assertTrue(e.equals(expression));
		assertEquals(expression.toString(), e.toString());
		Object result = i2.request("sat");
		assertNotNull(result);
		assertEquals(Instance.class, result.getClass());
		Instance j = (Instance) result;
		System.out.println("Factor found :" + j.getExpression().toString());
	}

	@Test
	public void test01() {
		IntVariable v = new IntVariable("v", 0, 99);
		IntConstant c = new IntConstant(0);
		Operation o = new Operation(Operation.Operator.EQ, v, c);
		check(o, new String[] { "v==0" });
	}

	@Test
	public void test02() {
		IntVariable v1 = new IntVariable("v1", 0, 99);
		IntConstant c1 = new IntConstant(42);
		Operation o1 = new Operation(Operation.Operator.EQ, v1, c1);
		IntVariable v2 = new IntVariable("v2", 0, 99);
		IntConstant c2 = new IntConstant(1);
		Operation o2 = new Operation(Operation.Operator.NE, v2, c2);
		Operation o3 = new Operation(Operation.Operator.AND, o2, o1);
		check(o3, new String[] { "v!=1" }, new String[] { "v==42" });
	}

}
