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

public class SATCanonizerTest {

	public static Green solver;

	@BeforeClass
	public static void initialize() {
		solver = new Green();
		Properties props = new Properties();
		props.setProperty("green.services", "sat");
		props.setProperty("green.service.sat", "(slice (canonize sink))");
		props.setProperty("green.service.sat.slice",
				"za.ac.sun.cs.green.service.slicer.SATSlicerService");
		props.setProperty("green.service.sat.canonize",
				"za.ac.sun.cs.green.service.canonizer.SATCanonizerService");
		props.setProperty("green.service.sat.sink",
				"za.ac.sun.cs.green.service.sink.SinkService");
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

	private void check(Expression expression, String full,
			String... expected) {
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
		IntVariable v = new IntVariable("aa", 0, 99);
		IntConstant c = new IntConstant(0);
		Operation o = new Operation(Operation.Operator.EQ, v, c);
		check(o, "aa==0", "1*v==0");
	}

	@Test
	public void test02() {
		IntVariable v1 = new IntVariable("aa", 0, 99);
		IntConstant c1 = new IntConstant(0);
		Operation o1 = new Operation(Operation.Operator.EQ, v1, c1);
		IntVariable v2 = new IntVariable("bb", 0, 99);
		IntConstant c2 = new IntConstant(1);
		Operation o2 = new Operation(Operation.Operator.NE, v2, c2);
		Operation o3 = new Operation(Operation.Operator.AND, o1, o2);
		check(o3, "(aa==0)&&(bb!=1)", "1*v==0", "1*v+-1!=0");
	}

	@Test
	public void test03() {
		IntVariable v1 = new IntVariable("aa", 0, 99);
		IntConstant c1 = new IntConstant(0);
		Operation o1 = new Operation(Operation.Operator.EQ, v1, c1);
		IntVariable v2 = new IntVariable("bb", 0, 99);
		IntConstant c2 = new IntConstant(1);
		Operation o2 = new Operation(Operation.Operator.NE, v2, c2);
		check(o1, o2, "(aa==0)&&(bb!=1)", "1*v==0");
	}

	@Test
	public void test04() {
		IntVariable v1 = new IntVariable("aa", 0, 99);
		IntConstant c1 = new IntConstant(0);
		Operation o1 = new Operation(Operation.Operator.EQ, v1, c1);
		IntConstant c2 = new IntConstant(1);
		Operation o2 = new Operation(Operation.Operator.NE, v1, c2);
		check(o1, o2, "(aa==0)&&(aa!=1)", "1*v==0", "1*v+-1!=0");
	}

	@Test
	public void test05() {
		IntVariable v1 = new IntVariable("aa", 0, 99);
		IntVariable v2 = new IntVariable("bb", 0, 99);
		Operation o1 = new Operation(Operation.Operator.EQ, v1, v2);
		IntVariable v3 = new IntVariable("cc", 0, 99);
		Operation o2 = new Operation(Operation.Operator.EQ, v2, v3);
		IntVariable v4 = new IntVariable("dd", 0, 99);
		Operation o3 = new Operation(Operation.Operator.EQ, v3, v4);
		IntVariable v5 = new IntVariable("ee", 0, 99);
		Operation o4 = new Operation(Operation.Operator.EQ, v4, v5);
		Operation o34 = new Operation(Operation.Operator.AND, o3, o4);
		Operation o234 = new Operation(Operation.Operator.AND, o2, o34);
		check(o1, o234, "(aa==bb)&&((bb==cc)&&((cc==dd)&&(dd==ee)))", "1*v+-1*v==0", "1*v+-1*v==0", "1*v+-1*v==0", "1*v+-1*v==0");
	}

	@Test
	public void test06() {
		IntVariable v1 = new IntVariable("aa", 0, 99);
		IntVariable v2 = new IntVariable("bb", 0, 99);
		Operation o1 = new Operation(Operation.Operator.EQ, v1, v2);
		IntVariable v3 = new IntVariable("cc", 0, 99);
		Operation o2 = new Operation(Operation.Operator.EQ, v2, v3);
		IntVariable v4 = new IntVariable("dd", 0, 99);
		Operation o3 = new Operation(Operation.Operator.EQ, v3, v4);
		IntVariable v5 = new IntVariable("ee", 0, 99);
		IntVariable v6 = new IntVariable("ff", 0, 99);
		Operation o4 = new Operation(Operation.Operator.EQ, v5, v6);
		Operation o34 = new Operation(Operation.Operator.AND, o3, o4);
		Operation o234 = new Operation(Operation.Operator.AND, o2, o34);
		check(o1, o234, "(aa==bb)&&((bb==cc)&&((cc==dd)&&(ee==ff)))", "1*v+-1*v==0", "1*v+-1*v==0", "1*v+-1*v==0");
	}

	@Test
	public void test07() {
		IntVariable v1 = new IntVariable("aa", 0, 99);
		IntVariable v2 = new IntVariable("bb", 0, 99);
		IntVariable v3 = new IntVariable("cc", 0, 99);
		IntVariable v4 = new IntVariable("dd", 0, 99);
		IntVariable v5 = new IntVariable("ee", 0, 99);
		IntVariable v6 = new IntVariable("ff", 0, 99);
		IntVariable v7 = new IntVariable("gg", 0, 99);
		Operation o1 = new Operation(Operation.Operator.LT, v1, new Operation(Operation.Operator.ADD, v2, v3));
		Operation o2 = new Operation(Operation.Operator.LT, v2, new Operation(Operation.Operator.ADD, v4, v5));
		Operation o3 = new Operation(Operation.Operator.LT, v3, new Operation(Operation.Operator.ADD, v6, v7));
		Operation o23 = new Operation(Operation.Operator.AND, o2, o3);
		check(o1, o23, "(aa<(bb+cc))&&((bb<(dd+ee))&&(cc<(ff+gg)))", "1*v+-1*v+-1*v+1<=0", "1*v+-1*v+-1*v+1<=0", "1*v+-1*v+-1*v+1<=0");
	}

	@Test
	public void test08() {
		IntVariable v1 = new IntVariable("aa", 0, 99);
		IntVariable v2 = new IntVariable("bb", 0, 99);
		IntVariable v3 = new IntVariable("cc", 0, 99);
		IntVariable v4 = new IntVariable("dd", 0, 99);
		IntVariable v5 = new IntVariable("ee", 0, 99);
		IntVariable v6 = new IntVariable("ff", 0, 99);
		IntVariable v7 = new IntVariable("gg", 0, 99);
		IntVariable v8 = new IntVariable("hh", 0, 99);
		Operation o1 = new Operation(Operation.Operator.LT, v1, new Operation(Operation.Operator.ADD, v2, v3));
		Operation o2 = new Operation(Operation.Operator.LT, v2, new Operation(Operation.Operator.ADD, v4, v5));
		Operation o3 = new Operation(Operation.Operator.LT, v6, new Operation(Operation.Operator.ADD, v7, v8));
		Operation o23 = new Operation(Operation.Operator.AND, o2, o3);
		check(o1, o23, "(aa<(bb+cc))&&((bb<(dd+ee))&&(ff<(gg+hh)))", "1*v+-1*v+-1*v+1<=0", "1*v+-1*v+-1*v+1<=0");
	}

	@Test
	public void test09() {
		IntVariable v1 = new IntVariable("aa", 0, 99);
		IntVariable v2 = new IntVariable("bb", 0, 99);
		IntVariable v3 = new IntVariable("cc", 0, 99);
		Operation o1 = new Operation(Operation.Operator.ADD, v1, v2);
		Operation o2 = new Operation(Operation.Operator.ADD, v1, v3);
		Operation o3 = new Operation(Operation.Operator.LT, o1, o2);
		check(o3, "(aa+bb)<(aa+cc)", "1*v+-1*v+1<=0");
	}

	@Test
	public void test10() {
		IntVariable v1 = new IntVariable("aa", 0, 99);
		IntVariable v2 = new IntVariable("bb", 0, 99);
		IntVariable v3 = new IntVariable("cc", 0, 99);
		Operation o1 = new Operation(Operation.Operator.ADD, v1, v2);
		Operation o2 = new Operation(Operation.Operator.SUB, v1, v3);
		Operation o3 = new Operation(Operation.Operator.LT, o1, o2);
		check(o3, "(aa+bb)<(aa-cc)", "1*v+1*v+1<=0");
	}

	@Test
	public void test11() {
		IntVariable v1 = new IntVariable("aa", 0, 99);
		IntVariable v2 = new IntVariable("bb", 0, 99);
		Operation o1 = new Operation(Operation.Operator.ADD, v1, v2);
		Operation o2 = new Operation(Operation.Operator.SUB, v2, v1);
		Operation o3 = new Operation(Operation.Operator.LT, o1, o2);
		check(o3, "(aa+bb)<(bb-aa)", "2*v+1<=0");
	}

	@Test
	public void test12() {
		IntConstant c1 = new IntConstant(2);
		IntConstant c2 = new IntConstant(3);
		IntVariable v1 = new IntVariable("aa", 0, 99);
		IntVariable v2 = new IntVariable("bb", 0, 99);
		Operation o1 = new Operation(Operation.Operator.ADD, c1, c2);
		Operation o2 = new Operation(Operation.Operator.MUL, o1, v1);
		Operation o3 = new Operation(Operation.Operator.LT, o2, v2);
		check(o3, "((2+3)*aa)<bb", "5*v+-1*v+1<=0");
	}

	@Test
	public void test13() {
		IntConstant c1 = new IntConstant(2);
		IntConstant c2 = new IntConstant(3);
		IntVariable v1 = new IntVariable("aa", 0, 99);
		IntVariable v2 = new IntVariable("bb", 0, 99);
		Operation o1 = new Operation(Operation.Operator.ADD, c1, c2);
		Operation o2 = new Operation(Operation.Operator.MUL, v1, o1);
		Operation o3 = new Operation(Operation.Operator.LT, o2, v2);
		check(o3, "(aa*(2+3))<bb", "5*v+-1*v+1<=0");
	}

	@Test
	public void test14() {
		IntConstant c1 = new IntConstant(2);
		IntVariable v1 = new IntVariable("aa", 0, 99);
		IntVariable v2 = new IntVariable("bb", 0, 99);
		Operation o1 = new Operation(Operation.Operator.SUB, v1, v2);
		Operation o2 = new Operation(Operation.Operator.MUL, o1, c1);
		Operation o3 = new Operation(Operation.Operator.LT, o2, v1);
		check(o3, "((aa-bb)*2)<aa", "1*v+-2*v+1<=0");
	}

	@Test
	public void test15() {
		IntConstant c1 = new IntConstant(2);
		IntVariable v1 = new IntVariable("aa", 0, 99);
		IntVariable v2 = new IntVariable("bb", 0, 99);
		Operation o1 = new Operation(Operation.Operator.SUB, v1, v2);
		Operation o2 = new Operation(Operation.Operator.MUL, c1, o1);
		Operation o3 = new Operation(Operation.Operator.LT, o2, v1);
		check(o3, "(2*(aa-bb))<aa", "1*v+-2*v+1<=0");
	}

	@Test
	public void test16() {
		IntConstant c1 = new IntConstant(2);
		IntConstant c2 = new IntConstant(4);
		IntVariable v1 = new IntVariable("aa", 0, 99);
		IntVariable v2 = new IntVariable("bb", 0, 99);
		Operation o1 = new Operation(Operation.Operator.MUL, v1, c1);
		Operation o2 = new Operation(Operation.Operator.MUL, c2, v1);
		Operation o3 = new Operation(Operation.Operator.ADD, o1, o2);
		Operation o4 = new Operation(Operation.Operator.LT, o3, v2);
		check(o4, "((aa*2)+(4*aa))<bb", "6*v+-1*v+1<=0");
	}

	@Test
	public void test17() {
		IntConstant c1 = new IntConstant(2);
		Operation o1 = new Operation(Operation.Operator.LT, c1, c1);
		check(o1, "2<2", "0==1");
	}

	@Test
	public void test18() {
		IntConstant c1 = new IntConstant(2);
		IntVariable v1 = new IntVariable("aa", 0, 99);
		Operation o1 = new Operation(Operation.Operator.LT, c1, c1);
		Operation o2 = new Operation(Operation.Operator.LT, v1, c1);
		Operation o3 = new Operation(Operation.Operator.AND, o1, o2);
		check(o3, "(2<2)&&(aa<2)", "0==1");
	}

	@Test
	public void test19() {
		IntConstant c1 = new IntConstant(2);
		Operation o1 = new Operation(Operation.Operator.LE, c1, c1);
		check(o1, "2<=2", "0==0");
	}

	@Test
	public void test20() {
		IntConstant c1 = new IntConstant(2);
		IntVariable v1 = new IntVariable("aa", 0, 99);
		Operation o1 = new Operation(Operation.Operator.LE, c1, c1);
		Operation o2 = new Operation(Operation.Operator.LT, v1, c1);
		Operation o3 = new Operation(Operation.Operator.AND, o1, o2);
		check(o3, "(2<=2)&&(aa<2)", "1*v+-1<=0");
	}

}
