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

public class CountLattEWithBounderTest {

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
		props.setProperty("green.service.count", "(bounder (canonize latte))");
		props.setProperty("green.service.count.latte", "za.ac.sun.cs.green.service.latte.CountLattEService");
		props.setProperty("green.service.count.bounder", "za.ac.sun.cs.green.service.bounder.BounderService");
		props.setProperty("green.service.count.canonize", "za.ac.sun.cs.green.service.canonizer.SATCanonizerService");
		props.setProperty("green.latte.path", EntireSuite.LATTE_PATH);
		Configuration config = new Configuration(solver, props);
		config.configure();
	}

	private static boolean checkLattEPresence() {
		final String DIRNAME = System.getProperty("java.io.tmpdir");
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
	
	/**
	 * Problem:
	 *   aa == 0
	 * Count:
	 *   1
	 */
	@Test
	public void test01() {
		IntVariable v = new IntVariable("aa", 0, 99);
		IntConstant c = new IntConstant(0);
		Operation o = new Operation(Operation.Operator.EQ, v, c);
		check(o, new Apint(1));
	}

	/**
	 * Problem:
	 *   aa > 0
	 *   aa < 10
	 * Count:
	 *   9
	 */
	@Test
	public void test02() {
		IntVariable vv = new IntVariable("aa", 0, 99);
		Operation ao = new Operation(Operation.Operator.GT, vv, new IntConstant(0));
		Operation bo = new Operation(Operation.Operator.LT, vv, new IntConstant(10));
		Operation o = new Operation(Operation.Operator.AND, ao, bo);
		check(o, new Apint(9));
	}
	
	/**
	 * Problem:
	 *   3 * aa > 6
	 *   aa < 10
	 * Count:
	 *   7
	 */
	@Test
	public void test03() {
		IntVariable vv = new IntVariable("aa", 0, 99);
		Operation ww = new Operation(Operation.Operator.MUL, new IntConstant(3), vv);
		Operation ao = new Operation(Operation.Operator.GT, ww, new IntConstant(6));
		Operation bo = new Operation(Operation.Operator.LT, vv, new IntConstant(10));		
		Operation o = new Operation(Operation.Operator.AND, ao, bo);
		check(o, new Apint(7));
	}

	/**
	 * Problem:
	 *   3 * aa > 6
	 * Count:
	 *   7
	 */
	@Test
	public void test04() {
		IntVariable vv = new IntVariable("aa", 0, 9);
		Operation ww = new Operation(Operation.Operator.MUL, new IntConstant(3), vv);
		Operation ao = new Operation(Operation.Operator.GT, ww, new IntConstant(6));
		Operation bo = new Operation(Operation.Operator.LT, vv, new IntConstant(10));		
		Operation o = new Operation(Operation.Operator.AND, ao, bo);
		check(o, new Apint(7));
	}
	
	/**
	 * Problem:
	 *   aa < bb
	 * Count:
	 *   45
	 */
	@Test
	public void test05() {
		IntVariable aa = new IntVariable("aa", 0, 9);
		IntVariable bb = new IntVariable("bb", 0, 9);
		Operation o = new Operation(Operation.Operator.LT, aa, bb);
		check(o, new Apint(45));
	}
	
}
