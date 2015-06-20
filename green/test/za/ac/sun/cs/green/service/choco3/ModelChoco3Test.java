package za.ac.sun.cs.green.service.choco3;

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

public class ModelChoco3Test {

	public static Green solver;

	@BeforeClass
	public static void initialize() {
		solver = new Green();
		Properties props = new Properties();
		//green.store = za.ac.sun.cs.green.store.redis.RedisStore
		props.setProperty("green.services", "model");
		props.setProperty("green.service.model", "choco3");
		props.setProperty("green.service.model.choco3",
				"za.ac.sun.cs.green.service.choco3.ModelChoco3Service");
		//props.setProperty("green.store", "za.ac.sun.cs.green.store.redis.RedisStore");
		Configuration config = new Configuration(solver, props);
		config.configure();
	}

	@AfterClass
	public static void report() {
		solver.report();
	}

	private void checkVal(Expression expression, Expression parentExpression, IntVariable var, int expected) {
		Instance p = (parentExpression == null) ? null : new Instance(solver, null, parentExpression);
		Instance i = new Instance(solver, p, expression);
		Object result = i.request("model");
		assertNotNull(result);
		@SuppressWarnings("unchecked")
		Map<IntVariable,Object> res = (Map<IntVariable,Object>)result; 
		System.out.println(" variable " + var + " = " + res.get(var) + " -> " + expected);
		assertEquals(res.get(var),expected);
	}
	
	private void checkModel(Expression expression, IntVariable v, int value) {
		checkVal(expression, null, v, value);
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
		checkModel(o3,v1,10);
	}
	
}
