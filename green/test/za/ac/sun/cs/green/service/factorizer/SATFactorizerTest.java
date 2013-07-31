package za.ac.sun.cs.green.service.factorizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
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
		String[] expectedReplaced = new String[expected.length];
		for (int i=0; i<expected.length; i++) {
			expectedReplaced[i] = expected[i].replaceAll("[()]", "");
		}
		String s0 = factor.getExpression().toString().replaceAll("[()]", "");
		SortedSet<String> s2 = new TreeSet<String>(Arrays.asList(s0.split("&&")));
		SortedSet<String> s3 = new TreeSet<String>(Arrays.asList(expectedReplaced));
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
		finalCheck(expected, f);
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
		check(o3, new String[] { "v2!=1" }, new String[] { "v1==42" });
	}
		
	@Test
	public void test03() {
		IntVariable v1 = new IntVariable("v1", 0, 99);
		IntConstant c1 = new IntConstant(0);
		Operation o1 = new Operation(Operation.Operator.EQ, v1, c1);
		IntConstant c2 = new IntConstant(1);
		Operation o2 = new Operation(Operation.Operator.NE, v1, c2);
		Operation o3 = new Operation(Operation.Operator.AND, o2, o1);
		check(o3, new String[] { "v1!=1", "v1==0" });
	}
	
	@Test
	public void test04() {
//		IntVariable v1 = new IntVariable("v1", 0, 99);
		IntVariable v2 = new IntVariable("v2", 0, 99);
//		Operation o1 = new Operation(Operation.Operator.EQ, v1, v2);
		IntVariable v3 = new IntVariable("v3", 0, 99);
		Operation o2 = new Operation(Operation.Operator.EQ, v2, v3);
		IntVariable v4 = new IntVariable("v4", 0, 99);
		Operation o3 = new Operation(Operation.Operator.EQ, v3, v4);
		IntVariable v5 = new IntVariable("v5", 0, 99);
		Operation o4 = new Operation(Operation.Operator.EQ, v4, v5);
		Operation o34 = new Operation(Operation.Operator.AND, o3, o4);
		Operation o234 = new Operation(Operation.Operator.AND, o2, o34);
		check(o234, new String[] { "v2==v3", "v3==v4", "v4==v5"});
	}
	
	@Test
	public void test05() {
		IntVariable v1 = new IntVariable("v1", 0, 99);
		IntVariable v2 = new IntVariable("v2", 0, 99);
		IntVariable v3 = new IntVariable("v3", 0, 99);
		Operation o1 = new Operation(Operation.Operator.EQ, v1, v2);
		Operation o2 = new Operation(Operation.Operator.EQ, v2, v3);
		IntVariable v4 = new IntVariable("v4", 0, 99);
		IntVariable v5 = new IntVariable("v5", 0, 99);
		IntVariable v6 = new IntVariable("v6", 0, 99);
		Operation o3 = new Operation(Operation.Operator.EQ, v6, v4);
		Operation o4 = new Operation(Operation.Operator.EQ, v5, v6);
		Operation o34 = new Operation(Operation.Operator.AND, o4, o2);
		Operation o234 = new Operation(Operation.Operator.AND, o3, o34);
		Operation o1234 = new Operation(Operation.Operator.AND, o1, o234);
		check(o1234, new String[] {"v1==v2", "v2==v3"}, new String[] {"v6==v4", "v5==v6"});
	}
	
	@Test
	public void test06() {
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
		Operation o12 = new Operation(Operation.Operator.AND, o1, o2);		
		Operation o123 = new Operation(Operation.Operator.AND, o12, o3);


		check(o123, new String[] {"v1<(v2+v3)", "v2<(v4+v5)", "v3<(v6+v7)"});
	}
	
	@Test
	public void test07() {
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
		Operation o12 = new Operation(Operation.Operator.AND, o1, o2);		
		Operation o123 = new Operation(Operation.Operator.AND, o12, o3);
		check(o123, new String[] {"v1<(v2+v3)", "v2<(v4+v5)"}, new String[] {"v6<(v7+v8)"});
	}

}
