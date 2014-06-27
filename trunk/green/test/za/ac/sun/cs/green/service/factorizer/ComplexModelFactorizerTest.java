package za.ac.sun.cs.green.service.factorizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.IntVariable;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.util.Configuration;

public class ComplexModelFactorizerTest {

	public static Green solver;

	
	private static Properties setupNoCanon(Properties props) {
		props.setProperty("green.service.model", "(bounder (factor z3))");
		props.setProperty("green.service.model.factor", "za.ac.sun.cs.green.service.factorizer.ModelFactorizerService");
		return props;
	}
	
	private static Properties setupWithCanon(Properties props) {
		props.setProperty("green.service.model", "(bounder (factor (canonize z3)))");
		props.setProperty("green.service.model.factor", "za.ac.sun.cs.green.service.factorizer.ModelFactorizerService");
		props.setProperty("green.service.model.canonize", "za.ac.sun.cs.green.service.canonizer.ModelCanonizerService");
		return props;
	}
	
	
	@BeforeClass
	public static void initialize() {
		solver = new Green();
		Properties props = new Properties();
		props.setProperty("green.services", "model");
		props = setupWithCanon(props);
		props.setProperty("green.service.model.bounder", "za.ac.sun.cs.green.service.bounder.BounderService");
		props.setProperty("green.service.model.z3", "za.ac.sun.cs.green.service.z3.ModelZ3JavaService");
		props.setProperty("green.z3.path", "/Users/willemvisser/Documents/tools/z3/bin/z3");
		props.setProperty("green.store", "za.ac.sun.cs.green.store.redis.RedisStore");
		Configuration config = new Configuration(solver, props);
		config.configure();
	}

	@AfterClass
	public static void report() {
		solver.report();
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

	private void checkVal(Expression expression, Expression parentExpression, IntVariable var, int low, int high) {
		Instance p = (parentExpression == null) ? null : new Instance(solver, null, parentExpression);
		Instance i = new Instance(solver, p, expression);
		Object result = i.request("model");
		assertNotNull(result);
		@SuppressWarnings("unchecked")
		Map<IntVariable,Object> res = (Map<IntVariable,Object>)result; 
		for (Map.Entry<IntVariable, Object> entry : res.entrySet()) {
			System.out.println(" variable " + entry.getKey() + " = " + entry.getValue());
		}
		int value = (Integer) res.get(var);
		System.out.println(" variable " + var + " = " + value + " -> [" + low + "," + high + "]");
		assertTrue(value >= low && value <= high);
	}
	
	private void checkModel(Expression expression, IntVariable v, int value) {
		checkVal(expression, null, v, value, value);
	}
	
	private void checkModelRange(Expression expression, IntVariable v, int low, int high) {
		checkVal(expression, null, v, low, high);
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
		//checkSat(o[N], oo);
		checkModelRange(oo, v[0], 0, 99);
	}

	/**
	 * Check the following system of equations:
	 * 
	 * (v0 <= v1) && (v1 <= v2) && ... && (vN-1 <= v0) && (vN < 10) && (vN > 5)
	 * 
	 * vi = 0..99
	 * 
	 * Should be satisfiable.
	 */
	@Test
	public void test01b() {
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
		IntConstant c5 = new IntConstant(5);		
		Operation ooExtra = new Operation(Operation.Operator.GT, v[N], c5);
		oo = new Operation(Operation.Operator.AND, oo, ooExtra);
		checkModelRange(oo, v[N], 5, 10);
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
		checkModelRange(oo, v[0], 0, 99);
	}
		
}
