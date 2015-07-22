package za.ac.sun.cs.green;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import za.ac.sun.cs.green.parser.smtlib2.SMTLIB2Parser0Test;
import za.ac.sun.cs.green.parser.smtlib2.SMTLIB2Scanner0Test;
import za.ac.sun.cs.green.service.bounder.BounderTest;
import za.ac.sun.cs.green.service.canonizer.SATCanonizerTest;
import za.ac.sun.cs.green.service.choco.SATChocoTest;
import za.ac.sun.cs.green.service.cvc3.SATCVC3Test;
import za.ac.sun.cs.green.service.factorizer.SATFactorizerTest;
import za.ac.sun.cs.green.service.latte.CountLattETest;
import za.ac.sun.cs.green.service.latte.CountLattEWithBounderTest;
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
	SATZ3Test.class,
	SATFactorizerTest.class,
	CountLattETest.class,
	CountLattEWithBounderTest.class,
	BounderTest.class,
	SMTLIB2Scanner0Test.class,
	SMTLIB2Parser0Test.class
})

public class EntireSuite {

	public static final String Z3_PATH;

	public static final String LATTE_PATH;

	static {
		String z3 = null, latte = null;
		InputStream is = EntireSuite.class.getClassLoader().getResourceAsStream("build.properties");
		if (is != null) {
			Properties p = new Properties();
			try {
				p.load(is);
				z3 = p.getProperty("z3path");
				latte = p.getProperty("lattepath");
			} catch (IOException e) {
				// do nothing
			}
		}
		Z3_PATH = z3;
		LATTE_PATH = latte;
	}

}
