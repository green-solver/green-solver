package za.ac.sun.cs.green.misc;

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
import za.ac.sun.cs.green.util.Configuration;

public class SATZ3JavaCNFTest {

	public static Green solver;

	@BeforeClass
	public static void initialize() {
		if (!EntireSuite.HAS_Z3JAVA) {
			Assume.assumeTrue(false);
			return;
		}
		solver = new Green();
		Properties props = new Properties();
		props.setProperty("green.services", "sat");
		props.setProperty("green.service.sat","(factor z3)");
		props.setProperty("green.service.sat.factor",
				"za.ac.sun.cs.green.service.factorizer.SATFactorizerService");
		props.setProperty("green.service.sat.z3",
				"za.ac.sun.cs.green.service.z3.SATZ3JavaService");
		Configuration config = new Configuration(solver, props);
		config.configure();
	}

	@AfterClass
	public static void report() {
		if (solver != null) {
			solver.report();
		}
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

	private Operation BOOL(IntVariable v) {
		return new Operation(Operation.Operator.NE, v, Operation.ZERO);
	}

	/* (v1 or v2) and (v3 or v4) */
	
	@Test
	public void test1() {
		Operation v1 = BOOL(new IntVariable("a1", 0, 1));
		Operation v2 = BOOL(new IntVariable("a2", 0, 1));
		Operation v3 = BOOL(new IntVariable("a3", 0, 1));
		Operation v4 = BOOL(new IntVariable("a4", 0, 1));

		
		Operation c1 = new Operation(Operation.Operator.OR, v1, v2);
		Operation c2 = new Operation(Operation.Operator.OR, v3, v4);
		
		Operation all = new Operation(Operation.Operator.AND, c1, c2);
		checkSat(all);
	}	

	@Test
	public void test01() {
		Operation v1 = BOOL(new IntVariable("a1", 0, 1));
		Operation v2 = BOOL(new IntVariable("a2", 0, 1));
		Operation v3 = BOOL(new IntVariable("a3", 0, 1));
		Operation v4 = BOOL(new IntVariable("a4", 0, 1));

		
		Operation c1 = new Operation(Operation.Operator.OR, v1, v2);
		Operation c2 = new Operation(Operation.Operator.OR, c1, v3);
		
		Operation all = new Operation(Operation.Operator.AND, c2, v4);
		checkSat(all);
	}	

	
	@Test
	public void test2() {
		Operation v1 = BOOL(new IntVariable("a1", 0, 1));
		Operation v2 = BOOL(new IntVariable("a2", 0, 1));
		Operation v3 = BOOL(new IntVariable("a3", 0, 1));
		Operation v4 = BOOL(new IntVariable("a4", 0, 1));

		
		Operation c1 = new Operation(Operation.Operator.OR, v1, v2);
		Operation c2 = new Operation(Operation.Operator.OR, v2, v4);
		
		Operation all = new Operation(Operation.Operator.AND, c1, c2);
		checkSat(all);
	}	

	@Test
	public void test3() {
		Operation v1 = BOOL(new IntVariable("a1", 0, 1));
		Operation v2 = BOOL(new IntVariable("a2", 0, 1));
		Operation v3 = BOOL(new IntVariable("a3", 0, 1));
		Operation v4 = BOOL(new IntVariable("a4", 0, 1));
		Operation v5 = BOOL(new IntVariable("a5", 0, 1));
		Operation v6 = BOOL(new IntVariable("a6", 0, 1));
		
		Operation c1 = new Operation(Operation.Operator.OR, v1, v2);
		Operation c2 = new Operation(Operation.Operator.OR, v3, v4);
		Operation c3 = new Operation(Operation.Operator.OR, v5, v6);
		
		Operation all1 = new Operation(Operation.Operator.AND, c1, c2);
		
		Operation all = new Operation(Operation.Operator.AND, all1, c3);
		checkSat(all);
	}	
	
	// (!a5 || a1 || a4)  &&  (!a1 || a5 || a3 || a4) && (!a3 || !a4) 
	@Test
	public void test4() {
		Operation v1 = BOOL(new IntVariable("a1", 0, 1));
		Operation v3 = BOOL(new IntVariable("a3", 0, 1));
		Operation v4 = BOOL(new IntVariable("a4", 0, 1));
		Operation v5 = BOOL(new IntVariable("a5", 0, 1));

		Operation notv5 = new Operation(Operation.Operator.NOT, v5);

		Operation c1 = new Operation(Operation.Operator.OR, v1, notv5);
		Operation c11 = new Operation(Operation.Operator.OR, c1, v4);

		Operation notv1 = new Operation(Operation.Operator.NOT, v1);

		Operation c2 = new Operation(Operation.Operator.OR, notv1, v5);
		Operation c22 = new Operation(Operation.Operator.OR, c2, v3);
		Operation c222 = new Operation(Operation.Operator.OR, c22, v4);

		Operation notv3 = new Operation(Operation.Operator.NOT, v3);
		Operation notv4 = new Operation(Operation.Operator.NOT, v4);

		Operation c3 = new Operation(Operation.Operator.OR, notv3, notv4);

		Operation all1 = new Operation(Operation.Operator.AND, c11, c222);
		Operation all2 = new Operation(Operation.Operator.AND, all1, c3);
		checkSat(all2);

	}	
	
}
