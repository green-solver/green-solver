package za.ac.sun.cs.green;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import za.ac.sun.cs.green.service.canonizer.SATCanonizerTest;
import za.ac.sun.cs.green.service.choco.SATChocoTest;
import za.ac.sun.cs.green.service.cvc3.SATCVC3Test;
import za.ac.sun.cs.green.service.slicer.ParallelSATSlicerTest;
import za.ac.sun.cs.green.service.slicer.SATSlicerTest;
import za.ac.sun.cs.green.service.z3.SATZ3Test;
import za.ac.sun.cs.green.util.ParallelSATTest;
import za.ac.sun.cs.green.util.SetServiceTest;
import za.ac.sun.cs.green.util.SetTaskManagerTest;

@RunWith(Suite.class)

@Suite.SuiteClasses({
	SetTaskManagerTest.class,
	SetServiceTest.class,
	SATSlicerTest.class,
	SATCanonizerTest.class,
	SATChocoTest.class,
	SATCVC3Test.class,
	ParallelSATSlicerTest.class,
	ParallelSATTest.class,
	SATZ3Test.class
})

public class EntireSuite {

}
