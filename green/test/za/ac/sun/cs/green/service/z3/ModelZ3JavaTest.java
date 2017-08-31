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
	
	//x3!=1&&&&x4<=x5&&((x4!=x5)&&((x2==1)&&((x1==1)&&(0==0))))))
	
	@Test
	public void test03() {
		IntVariable x1 = new IntVariable("x1", 0, 99);
		IntVariable x2 = new IntVariable("x2", 0, 99);
		IntVariable x3 = new IntVariable("x3", 0, 99);
		IntVariable x4 = new IntVariable("x4", 0, 99);
		IntVariable x5 = new IntVariable("x5", 0, 99);
		
		IntConstant c1 = new IntConstant(1);
		Operation o1 = new Operation(Operation.Operator.NE, x3, c1);
		Operation o2 = new Operation(Operation.Operator.LE, x4, x5);
		Operation o3 = new Operation(Operation.Operator.NE, x4, x5);
		Operation o4 = new Operation(Operation.Operator.AND, o2, o3);
		Operation o5 = new Operation(Operation.Operator.AND, o1, o4);
		checkModelRange(o5,x4,0,99);
	}
	
	//(!(x3==1))&&((x5==x5)&&((x4<=x5)&&((x4!=x5)&&((x2==1)&&((x1==1)&&(0==0))))))
	@Test
	public void test04() {
		IntVariable x1 = new IntVariable("x1", 0, 99);
		IntVariable x2 = new IntVariable("x2", 0, 99);
		IntVariable x3 = new IntVariable("x3", 0, 99);
		IntVariable x4 = new IntVariable("x4", 0, 99);
		IntVariable x5 = new IntVariable("x5", 0, 99);
		
		IntConstant c1 = new IntConstant(1);
		IntConstant c0 = new IntConstant(0);
		
		Operation x2eq1 = new Operation(Operation.Operator.EQ, x2, c1);
		Operation x1eq1 = new Operation(Operation.Operator.EQ, x1, c1);
		Operation c0eqc0 = new Operation(Operation.Operator.EQ, c0, c0);
		Operation o4a = new Operation(Operation.Operator.AND, x1eq1, c0eqc0);
		Operation o4b = new Operation(Operation.Operator.AND, x2eq1, o4a);
		
		
		Operation o1a = new Operation(Operation.Operator.EQ, x3, c1);
		Operation o1 = new Operation(Operation.Operator.NOT, o1a);
		
		Operation o2a = new Operation(Operation.Operator.EQ, x5, x5);
		Operation o2 = new Operation(Operation.Operator.LE, x4, x5);
		Operation o3 = new Operation(Operation.Operator.NE, x4, x5);
		Operation o3a = new Operation(Operation.Operator.AND, o3, o4b);
		
		Operation o4 = new Operation(Operation.Operator.AND, o2, o3a);
		
		Operation o5 = new Operation(Operation.Operator.AND, o2a, o4);
		Operation o6 = new Operation(Operation.Operator.AND, o1, o5);
		checkModelRange(o6,x4,0,99);
	}
	
	
}
