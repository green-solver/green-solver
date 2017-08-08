package za.ac.sun.cs.green.service.choco3;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.IntVariable;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.util.Configuration;

/**
 * Test case for the alleged bug #5 (on github).
 * 
 * @author Jaco Geldenhuys (geld@sun.ac.za)
 */
public class ModelChoco3Test2 {

	public static Green solver;

	/**
	 * Sets up the Green configuration for our test.
	 */
	@BeforeClass
	public static void initialize() {
		solver = new Green();
		Properties props = new Properties();
		props.setProperty("green.services", "model");
		props.setProperty("green.service.model", "(bounder (canonizer choco3))");
		props.setProperty("green.service.model.bounder", "za.ac.sun.cs.green.service.bounder.BounderService");
		props.setProperty("green.service.model.canonizer", "za.ac.sun.cs.green.service.canonizer.ModelCanonizerService");
		props.setProperty("green.service.model.choco3", "za.ac.sun.cs.green.service.choco3.ModelChoco3Service");
		Configuration config = new Configuration(solver, props);
		config.configure();
	}

	/**
	 * Asks the Green instance to issue a report.
	 */
	@AfterClass
	public static void report() {
		solver.report();
	}

	/**
	 * Performs a test for model finding. The query is
	 * 
	 * <pre>
	 * (!(Y <= 5)) && ((!(X >= Y)) && (0 == 0))
	 * </pre>
	 * 
	 * The last conjunct is obviously redundant but it is still encoded. In an
	 * actual application this returned the model {X=99,Y=0} which does not seem
	 * correct.
	 */
	@Test
	public void test01() {
		IntVariable v1 = new IntVariable("X", 0, 99);
		IntVariable v2 = new IntVariable("Y", 0, 99);
		IntConstant c0 = new IntConstant(0);
		IntConstant c1 = new IntConstant(5);
		Operation o1 = new Operation(Operation.Operator.EQ, c0, c0);
		Operation o2 = new Operation(Operation.Operator.LE, v2, c1);
		Operation o3 = new Operation(Operation.Operator.NOT, o2);
		Operation o4 = new Operation(Operation.Operator.GE, v1, v2);
		Operation o5 = new Operation(Operation.Operator.NOT, o4);
		Operation o6 = new Operation(Operation.Operator.AND, o5, o1);
		Operation o7 = new Operation(Operation.Operator.AND, o3, o6);
		// Obtain the model
		Instance i = new Instance(solver, null, o7);
		Object result = i.request("model");
		assertNotNull(result);
		@SuppressWarnings("unchecked")
		Map<IntVariable, Object> res = (Map<IntVariable, Object>) result;
		// Check the values satisfy the constraints
		int v1Value = (Integer) res.get(v1);
		int v2Value = (Integer) res.get(v2);
		assertTrue((v1Value >= 0) && (v1Value <= 99) && (v1Value < v2Value));
		assertTrue((v2Value >= 0) && (v2Value <= 99) && (v2Value > 5));
	}

	/**
	 * Performs a test for model finding. This test is identical to
	 * {@link #test01()} exact that the last conjunct has been omitted.
	 */
	@Test
	public void test02() {
		IntVariable v1 = new IntVariable("X", 0, 99);
		IntVariable v2 = new IntVariable("Y", 0, 99);
		IntConstant c1 = new IntConstant(5);
		Operation o2 = new Operation(Operation.Operator.LE, v2, c1);
		Operation o3 = new Operation(Operation.Operator.NOT, o2);
		Operation o4 = new Operation(Operation.Operator.GE, v1, v2);
		Operation o5 = new Operation(Operation.Operator.NOT, o4);
		Operation o7 = new Operation(Operation.Operator.AND, o3, o5);
		// Obtain the model
		Instance i = new Instance(solver, null, o7);
		Object result = i.request("model");
		assertNotNull(result);
		@SuppressWarnings("unchecked")
		Map<IntVariable, Object> res = (Map<IntVariable, Object>) result;
		// Check the values satisfy the constraints
		int v1Value = (Integer) res.get(v1);
		int v2Value = (Integer) res.get(v2);
		assertTrue((v1Value >= 0) && (v1Value <= 99) && (v1Value < v2Value));
		assertTrue((v2Value >= 0) && (v2Value <= 99) && (v2Value > 5));
	}

}
