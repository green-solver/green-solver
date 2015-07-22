package za.ac.sun.cs.green.service.z3;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.IntVariable;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.util.Configuration;

public class ModelZ3JavaTest {

	public static Green solver;

	@BeforeClass
	public static void initialize() {
		solver = new Green();
		Properties props = new Properties();
		props.setProperty("green.services", "model");
		props.setProperty("green.service.model", "(bounder z3java)");
		props.setProperty("green.service.model.bounder", "za.ac.sun.cs.green.service.bounder.BounderService");
				
		props.setProperty("green.service.model.z3java",
				"za.ac.sun.cs.green.service.z3.ModelZ3JavaService");
		Configuration config = new Configuration(solver, props);
		config.configure();
	}

	@AfterClass
	public static void report() {
		solver.report();
	}

	private void checkVal(Expression expression, Expression parentExpression, IntVariable var, int low, int high) {
		Instance p = (parentExpression == null) ? null : new Instance(solver, null, parentExpression);
		Instance i = new Instance(solver, p, expression);
		Object result = i.request("model");
		assertNotNull(result);
		@SuppressWarnings("unchecked")
		Map<IntVariable,Object> res = (Map<IntVariable,Object>)result; 
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
	
	@Test
	public void test01() {
		IntVariable v = new IntVariable("aa", 0, 99);
		IntConstant c = new IntConstant(0);
		Operation o = new Operation(Operation.Operator.EQ, v, c);
		checkModel(o,v,0);
	}


	@Test
	public void test02() {
		IntVariable v1 = new IntVariable("aa", 0, 99);
		IntVariable v2 = new IntVariable("bb", 0, 99);
		IntConstant c1 = new IntConstant(10);
		IntConstant c2 = new IntConstant(20);
		Operation o1 = new Operation(Operation.Operator.GE, v1, c1);
		Operation o2 = new Operation(Operation.Operator.EQ, v2, c2);
		Operation o3 = new Operation(Operation.Operator.AND, o1, o2);
		checkModelRange(o3,v1,10,99);
	}
	
}
