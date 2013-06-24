package za.ac.sun.cs.green.service.factorizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.IntVariable;
import za.ac.sun.cs.green.expr.Operation;

public class FactoredConstraintTest {
	private void checkForSubstrings(String wholeString, String... partialStrings) {
		for (String s : partialStrings) {
			//System.out.println("Looking for "+s+" in "+factorString);
			int p = wholeString.indexOf(s);
			assertTrue(p >= 0);
			if (p == 0) {
				wholeString = wholeString.substring(p + s.length());
			} else if (p > 0) {
				wholeString = wholeString.substring(0, p - 1) + wholeString.substring(p + s.length());
			}
		}
		wholeString = wholeString.replaceAll("[()&]", "");
		assertEquals("", wholeString);
	}
	
	@Test
	public void test01() {
		IntVariable v1 = new IntVariable("v1", 0, 100);
		IntConstant c1 = new IntConstant(5);
		Operation o1 = new Operation(Operation.Operator.EQ, v1, c1);
		FactorExpression fc1 = new FactorExpression(null, o1);	
		assertTrue(fc1.getNumFactors() == 1);
		checkForSubstrings(fc1.getDependentFactor(o1).toString(), "v1==5");
		assertTrue(fc1.getDependentVariableCount(o1) == 1);
		assertTrue(fc1.getDependentConjunctCount(o1) == 1);
	}
		
	@Test
	public void test02() {
		
		IntVariable v1 = new IntVariable("v1", 0, 100);		
		IntConstant c1 = new IntConstant(5);
		Operation o1 = new Operation(Operation.Operator.EQ, v1, c1);
		FactorExpression fc1 = new FactorExpression(null, o1);	
		assertTrue(fc1.getNumFactors() == 1);
		checkForSubstrings(fc1.getDependentFactor(o1).toString(), "v1==5");
		
		IntVariable v2 = new IntVariable("v2", 0, 100);
		IntConstant c2 = new IntConstant(4);
		Operation o2 = new Operation(Operation.Operator.EQ, v2, c2);
		FactorExpression fc2 = new FactorExpression(fc1, o2);	
		assertTrue(fc2.getNumFactors() == 2);
		checkForSubstrings(fc1.getDependentFactor(o1).toString(), "v1==5");
		checkForSubstrings(fc2.getDependentFactor(o2).toString(), "v2==4");
		assertTrue(fc2.getDependentVariableCount(o2) == 1);
		assertTrue(fc2.getDependentConjunctCount(o2) == 1);
	}
	
	@Test
	public void test03() {		
		IntVariable v1 = new IntVariable("v1", 0, 100);	
		IntConstant c1 = new IntConstant(5);
		Operation o1 = new Operation(Operation.Operator.EQ, c1, v1);
		FactorExpression fc1 = new FactorExpression(null, o1);	
		assertTrue(fc1.getNumFactors() == 1);
		checkForSubstrings(fc1.getDependentFactor(o1).toString(), "5==v1");
		
		IntVariable v2 = new IntVariable("v2", 0, 100);
		IntConstant c2 = new IntConstant(4);
		Operation o2 = new Operation(Operation.Operator.EQ, v2, c2);
		FactorExpression fc2 = new FactorExpression(fc1, o2);	
		assertTrue(fc2.getNumFactors() == 2);
		checkForSubstrings(fc1.getDependentFactor(v1).toString(), "5==v1");
		checkForSubstrings(fc2.getDependentFactor(v2).toString(), "v2==4");
		
		Operation o3 = new Operation(Operation.Operator.EQ, v2, v1);
		FactorExpression fc3 = new FactorExpression(fc2, o3);	
		assertTrue(fc3.getNumFactors() == 1);
		checkForSubstrings(fc3.getDependentFactor(o1).toString(), "5==v1", "v2==4", "v2==v1");
		checkForSubstrings(fc3.getDependentFactor(o2).toString(), "5==v1", "v2==4", "v2==v1");
		checkForSubstrings(fc3.getDependentFactor(o3).toString(), "5==v1", "v2==4", "v2==v1");	
		assertTrue(fc3.getDependentVariableCount(o2) == 2);
		assertTrue(fc3.getDependentConjunctCount(o2) == 3);
	}
	
	@Test
	public void test04() {		
		IntVariable v1 = new IntVariable("v1", 0, 100);	
		IntConstant c1 = new IntConstant(5);
		Operation o1 = new Operation(Operation.Operator.EQ, c1, v1);
		FactorExpression fc1 = new FactorExpression(null, o1);	
		assertTrue(fc1.getNumFactors() == 1);
		checkForSubstrings(fc1.getDependentFactor(o1).toString(), "5==v1");
		
		IntVariable v2 = new IntVariable("v2", 0, 100);
		IntConstant c2 = new IntConstant(4);
		Operation o2 = new Operation(Operation.Operator.EQ, v2, c2);
		FactorExpression fc2 = new FactorExpression(fc1, o2);	
		assertTrue(fc2.getNumFactors() == 2);
		checkForSubstrings(fc1.getDependentFactor(o1).toString(), "5==v1");
		checkForSubstrings(fc2.getDependentFactor(o2).toString(), "v2==4");
		
		Operation o3 = new Operation(Operation.Operator.EQ, v2, c1);
		FactorExpression fc3 = new FactorExpression(fc2, o3);	
		assertTrue(fc3.getNumFactors() == 2);
		checkForSubstrings(fc3.getDependentFactor(v1).toString(), "5==v1");
		checkForSubstrings(fc3.getDependentFactor(v2).toString(), "v2==4", "v2==5");			
		checkForSubstrings(fc3.getDependentFactor(o2).toString(), "v2==4", "v2==5");
		
		System.out.println("Factors of "+fc3+" are:");
		for (Expression e : fc3.getFactors()) {
			System.out.println("   "+e);
		}
	}
	
	@Test
	public void test05() {
		IntVariable v1 = new IntVariable("v1", 0, 100);
		IntConstant c1 = new IntConstant(5);
		Operation o1 = new Operation(Operation.Operator.EQ, v1, c1);
		IntVariable v2 = new IntVariable("v2", 0, 100);
		IntConstant c2 = new IntConstant(4);
		Operation o2 = new Operation(Operation.Operator.EQ, v2, c2);
		Operation o3 = new Operation(Operation.Operator.AND, o1, o2);
		Operation o4 = new Operation(Operation.Operator.AND, v1, v2);

		FactorExpression fc1 = new FactorExpression(null, o3);	
		assertTrue(fc1.getNumFactors() == 2);
		checkForSubstrings(fc1.getDependentFactor(v1).toString(), "v1==5");
		checkForSubstrings(fc1.getDependentFactor(v2).toString(), "v2==4");
		checkForSubstrings(fc1.getDependentFactor(o4).toString(), "v1==5", "v2==4");
		
		System.out.println("Factors of "+fc1+" are:");
		for (Expression e : fc1.getFactors()) {
			System.out.println("   "+e);
		}
	}
	
	@Test
	public void test06() {
		IntVariable v1 = new IntVariable("v1", 0, 99);
		IntConstant c1 = new IntConstant(0);
		Operation o1 = new Operation(Operation.Operator.EQ, v1, c1);
		IntConstant c2 = new IntConstant(1);
		Operation o2 = new Operation(Operation.Operator.NE, v1, c2);
		
		FactorExpression fc1 = new FactorExpression(null, o1);	
		FactorExpression fc2 = new FactorExpression(fc1, o2);	

		checkForSubstrings(fc2.getDependentFactor(o2).toString(), "v1==0", "v1!=1");
		
		System.out.println("Factors of "+fc2+" are:");
		for (Expression e : fc2.getFactors()) {
			System.out.println("   "+e);
		}
	}
	
	@Test
	public void test07() {
		IntVariable v1 = new IntVariable("v1", 0, 99);
		IntVariable v2 = new IntVariable("v2", 0, 99);
		Operation o1 = new Operation(Operation.Operator.EQ, v1, v2);
		IntVariable v3 = new IntVariable("v3", 0, 99);
		Operation o2 = new Operation(Operation.Operator.EQ, v2, v3);
		IntVariable v4 = new IntVariable("v4", 0, 99);
		Operation o3 = new Operation(Operation.Operator.EQ, v3, v4);
		IntVariable v5 = new IntVariable("v5", 0, 99);
		Operation o4 = new Operation(Operation.Operator.EQ, v4, v5);
		Operation o34 = new Operation(Operation.Operator.AND, o3, o4);
		Operation o234 = new Operation(Operation.Operator.AND, o2, o34);
		
		FactorExpression fc1 = new FactorExpression(null, o1);	
		FactorExpression fc2 = new FactorExpression(fc1, o234);	

		checkForSubstrings(fc2.getDependentFactor(o234).toString(), "v1==v2", "v2==v3", "v3==v4", "v4==v5");
		
		System.out.println("Factors of "+fc2+" are:");
		for (Expression e : fc2.getFactors()) {
			System.out.println("   "+e);
		}
	}

}
