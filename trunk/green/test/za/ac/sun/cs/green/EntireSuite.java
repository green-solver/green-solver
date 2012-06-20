package za.ac.sun.cs.green;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import za.ac.sun.cs.green.expr.ExprTest;

@RunWith(Suite.class)

@Suite.SuiteClasses({
	ExprTest.class,
//	SolverTest.class,
//	DefaultSlicerTest.class,
//	DefaultCanonizerTest.class,
//	CVC3Test1.class,
//	CVC3Test2.class,
//	ChocoTest1.class,
//	LattETest1.class
})

public class EntireSuite { }
