package za.ac.sun.cs.green.service.factorizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.IntVariable;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.util.Configuration;

public class ComplexSATFactorizerTest {

	public static Green solver;

	@BeforeClass
	public static void initialize() {
		solver = new Green();
		Properties props = new Properties();
		props.setProperty("green.services", "sat");
		props.setProperty("green.service.sat", "(factor (canonize z3))");
		props.setProperty("green.service.sat.factor", "za.ac.sun.cs.green.service.factorizer.SATFactorizerService");
		props.setProperty("green.service.sat.canonize", "za.ac.sun.cs.green.service.canonizer.SATCanonizerService");
		props.setProperty("green.service.sat.z3", "za.ac.sun.cs.green.service.z3.SATZ3Service");
		props.setProperty("green.z3.path", "lib/macosx/z3");
		Configuration config = new Configuration(solver, props);
		config.configure();
	}

	private void check(Expression expression, Expression parentExpression,
			boolean expected) {
		Instance p = (parentExpression == null) ? null : new Instance(solver,
				null, parentExpression);
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
	
	/**
	 * Check the following system of equations:
	 * 
	 * (v0 <= v1) && (v1 <= v2) && ... && (vN-1 <= v0) && (vN < 10)
	 * 
	 * vi = 0..99
	 * 
	 * Should be satisfiable.
	 */
	@Test
	public void test01() {
		final int N = 10;
		IntVariable[] v = new IntVariable[N + 1];
		for (int i = 0; i < N + 1; i++) {
			v[i] = new IntVariable("v" + i, 0, 99);
		}
		Operation[] o = new Operation[N + 1];
		for (int i = 0; i < N; i++) {
			o[i] = new Operation(Operation.Operator.LE, v[i], v[(i + 1) % N]);
		}
		IntConstant c10 = new IntConstant(10);
		o[N] = new Operation(Operation.Operator.LT, v[N], c10);
		Operation oo = o[0];
		for (int i = 1; i <= N; i++) {
			oo = new Operation(Operation.Operator.AND, oo, o[i]);
		}
		checkSat(o[N], oo);
	}

	/**
	 * Check the following system of equations:
	 * 
	 * (v0 < v1) && (v1 < v2) && ... && (vN-1 < v0) && (vN < 10)
	 * 
	 * vi = 0..99
	 * 
	 * Should be unsatisfiable due to cycle in first N conjuncts.
	 */
	@Test
	public void test02() {
		final int N = 10;
		IntVariable[] v = new IntVariable[N + 1];
		for (int i = 0; i < N + 1; i++) {
			v[i] = new IntVariable("v" + i, 0, 99);
		}
		Operation[] o = new Operation[N + 1];
		for (int i = 0; i < N; i++) {
			o[i] = new Operation(Operation.Operator.LT, v[i], v[(i + 1) % N]);
		}
		IntConstant c10 = new IntConstant(10);
		o[N] = new Operation(Operation.Operator.LT, v[N], c10);
		Operation oo = o[0];
		for (int i = 1; i <= N; i++) {
			oo = new Operation(Operation.Operator.AND, oo, o[i]);
		}
		checkUnsat(o[N], oo);
	}
	
	/**
	 * Check the following system of equations:
	 * 
	 * (v0 < v1) && (v1 < v2) && ... && (vN-1 < vN) && (vN < N)
	 * 
	 * vi = 0..10
	 * 
	 * Should be unsatisfiable because the only possible values are:
	 *   v0 = 0
	 *   v1 = 1
	 *   v2 = 2
	 *   ...
	 *   vN = N
	 *   
	 * but last conjunct claims vN < N.
	 */
	@Test
	public void test03() {
		final int N = 10;
		IntVariable[] v = new IntVariable[N + 1];
		for (int i = 0; i < N + 1; i++) {
			v[i] = new IntVariable("v" + i, 0, N);
		}
		Operation[] o = new Operation[N + 1];
		for (int i = 0; i < N; i++) {
			o[i] = new Operation(Operation.Operator.LT, v[i], v[i + 1]);
		}
		IntConstant cN = new IntConstant(N);
		o[N] = new Operation(Operation.Operator.LT, v[N], cN);
		Operation oo = o[0];
		for (int i = 1; i <= N; i++) {
			oo = new Operation(Operation.Operator.AND, oo, o[i]);
		}
		checkUnsat(o[N], oo);
	}
	
	/**
	 * Check the following system of equations:
	 * 
	 *    (v0 <= w0) && (w0 <= v0)
	 * && (v1 <= w1) && (w1 <= v1)
	 * && ...
	 * && (vN-1 <= wN-1) && (wN-1 <= vN-1)
	 * 
	 * vi = 0..99
	 * wi = 0..99
	 * 
	 * Should be satisfiable.
	 */
	@Test
	public void test04() {
		final int N = 10;
		IntVariable[] v = new IntVariable[N];
		IntVariable[] w = new IntVariable[N];
		for (int i = 0; i < N; i++) {
			v[i] = new IntVariable("v" + i, 0, 99);
			w[i] = new IntVariable("w" + i, 0, 99);
		}
		Operation[] o = new Operation[N + 1];
		for (int i = 0; i < N; i++) {
			Operation o0 = new Operation(Operation.Operator.LE, v[i], w[i]);
			Operation o1 = new Operation(Operation.Operator.LE, w[i], v[i]);
			o[i] = new Operation(Operation.Operator.AND, o0, o1);
		}
		Operation oo = o[0];
		for (int i = 1; i < N; i++) {
			oo = new Operation(Operation.Operator.AND, oo, o[i]);
		}
		checkSat(oo);
	}
	
	/**
	 * Check the following system of equations:
	 * 
	 *    (v0 < w0) && (w0 < v0)
	 * && (v1 < w1) && (w1 < v1)
	 * && ...
	 * && (vN-1 < wN-1) && (wN-1 < vN-1)
	 * 
	 * vi = 0..99
	 * wi = 0..99
	 * 
	 * Should be unsatisfiable.
	 */
	@Test
	public void test05() {
		final int N = 10;
		IntVariable[] v = new IntVariable[N];
		IntVariable[] w = new IntVariable[N];
		for (int i = 0; i < N; i++) {
			v[i] = new IntVariable("v" + i, 0, 99);
			w[i] = new IntVariable("w" + i, 0, 99);
		}
		Operation[] o = new Operation[N + 1];
		for (int i = 0; i < N; i++) {
			Operation o0 = new Operation(Operation.Operator.LT, v[i], w[i]);
			Operation o1 = new Operation(Operation.Operator.LT, w[i], v[i]);
			o[i] = new Operation(Operation.Operator.AND, o0, o1);
		}
		Operation oo = o[0];
		for (int i = 1; i < N; i++) {
			oo = new Operation(Operation.Operator.AND, oo, o[i]);
		}
		checkUnsat(oo);
	}
	
}
