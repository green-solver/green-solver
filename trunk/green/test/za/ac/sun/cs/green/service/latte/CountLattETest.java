package za.ac.sun.cs.green.service.latte;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apfloat.Apint;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import za.ac.sun.cs.green.EntireSuite;
import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.IntVariable;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.util.Configuration;

public class CountLattETest {

	public static Green solver = null;

	@BeforeClass
	public static void initialize() {
		if (!checkLattEPresence()) {
			Assume.assumeTrue(false);
			return;
		}		
		solver = new Green();
		Properties props = new Properties();
		props.setProperty("green.services", "count");
		props.setProperty("green.service.count", "latte");
		props.setProperty("green.service.count.latte",
				"za.ac.sun.cs.green.service.latte.CountLattEService");
		props.setProperty("green.latte.path", EntireSuite.LATTE_PATH);
		Configuration config = new Configuration(solver, props);
		config.configure();
	}

	private static boolean checkLattEPresence() {
		final String DIRNAME = System.getProperty("java.io.tmpdir");
		// System.out.println(">>> LATTE DIRNAME: " + DIRNAME);
		String result = "";
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			DefaultExecutor executor = new DefaultExecutor();
			executor.setStreamHandler(new PumpStreamHandler(outputStream));
			executor.setWorkingDirectory(new File(DIRNAME));
			executor.execute(CommandLine.parse(EntireSuite.LATTE_PATH));
			result = outputStream.toString();
		} catch (IOException e) {
			return false;
		}
		return result.startsWith("This is LattE integrale");
	}

	@AfterClass
	public static void report() {
		if (solver != null) {
			solver.report();
		}
	}

	private void check(Expression expression, Expression parentExpression, Apint expected) {
		Instance p = (parentExpression == null) ? null : new Instance(solver, null, parentExpression);
		Instance i = new Instance(solver, p, expression);
		Object result = i.request("count");
		assertNotNull(result);
		assertEquals(Apint.class, result.getClass());
		assertEquals(expected, result);
	}

	private void check(Expression expression, Apint expected) {
		check(expression, null, expected);
	}
	
	@Test
	public void test01() {
		IntConstant a = new IntConstant(1);
		IntVariable v = new IntVariable("aa", 0, 99);
		Operation t = new Operation(Operation.Operator.MUL, a, v);
		IntConstant c = new IntConstant(0);
		Operation o = new Operation(Operation.Operator.EQ, t, c);
		check(o, new Apint(1));
	}

	@Test
	public void test02() {
		IntConstant zz = new IntConstant(0);
		IntConstant oo = new IntConstant(1);
		IntVariable vv = new IntVariable("aa", 0, 99);
		
		Operation at = new Operation(Operation.Operator.MUL, oo, vv);
		Operation ao = new Operation(Operation.Operator.GT, at, zz);
		
		Operation bt1 = new Operation(Operation.Operator.MUL, oo, vv);
		Operation bt2 = new Operation(Operation.Operator.ADD, bt1, new IntConstant(-10));
		Operation bo = new Operation(Operation.Operator.LT, bt2, zz);
		
		Operation o = new Operation(Operation.Operator.AND, ao, bo);
		check(o, new Apint(9));
	}
	
	@Test
	public void test03() {
		IntConstant zz = new IntConstant(0);
		IntConstant oo = new IntConstant(1);
		IntConstant tt = new IntConstant(3);
		IntVariable vv = new IntVariable("aa", 0, 99);
		
		Operation at1 = new Operation(Operation.Operator.MUL, tt, vv);
		Operation at2 = new Operation(Operation.Operator.ADD, at1, new IntConstant(-6));
		Operation ao = new Operation(Operation.Operator.GT, at2, zz);
		
		Operation bt1 = new Operation(Operation.Operator.MUL, oo, vv);
		Operation bt2 = new Operation(Operation.Operator.ADD, bt1, new IntConstant(-10));
		Operation bo = new Operation(Operation.Operator.LT, bt2, zz);
		
		Operation o = new Operation(Operation.Operator.AND, ao, bo);
		check(o, new Apint(7));
	}

	@Test
	public void test04() {
		IntConstant zero = new IntConstant(0);
		IntConstant one = new IntConstant(1);
		IntConstant minone = new IntConstant(-1);
		IntVariable aa = new IntVariable("aa", 0, 9);
		IntVariable bb = new IntVariable("bb", 0, 9);

		Operation taa = new Operation(Operation.Operator.MUL, one, aa);
		Operation tbb = new Operation(Operation.Operator.MUL, minone, bb);

		Operation o1 = new Operation(Operation.Operator.ADD, taa, tbb);
		Operation o = new Operation(Operation.Operator.LT, o1, zero);
		check(o, new Apint(45));
	}
	
}
