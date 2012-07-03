package za.ac.sun.cs.green;

import java.util.logging.Level;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import za.ac.sun.cs.green.expr.ExprTest;
import za.ac.sun.cs.green.logger.GreenHandler;
import za.ac.sun.cs.green.service.ChocoTest;
import za.ac.sun.cs.green.service.SlicerTest;

@RunWith(Suite.class)

@Suite.SuiteClasses({
	ExprTest.class,
	SlicerTest.class,
//	SolverTest.class,
//	DefaultCanonizerTest.class,
//	CVC3Test1.class,
//	CVC3Test2.class,
	ChocoTest.class,
//	LattETest1.class
})

public class EntireSuite {

	public static final boolean DETAILED_LOG = true;

	public static GreenHandler greenHandler = null;

	public static void setDetailedLogger(Solver solver) {
		if (greenHandler == null) {
			greenHandler = new GreenHandler(Level.ALL);
			solver.logger.addHandler(greenHandler);
		}
	}
	
}
