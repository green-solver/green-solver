package za.ac.sun.cs.green;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import cvc3.ValidityChecker;
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

	public static final boolean HAS_CVC3;

	public static final boolean HAS_Z3;
	
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
		HAS_CVC3 = checkCVC3Presence();
		HAS_Z3 = checkZ3Presence();
	}

	private static boolean checkCVC3Presence() {
		try {
			ValidityChecker.create();
		} catch (SecurityException x) {
			return false;
		} catch (UnsatisfiedLinkError x) {
			return false;
		}
		return true;
	}

	private static boolean checkZ3Presence() {
		final String DIRNAME = System.getProperty("java.io.tmpdir");
		String result = "";
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			DefaultExecutor executor = new DefaultExecutor();
			executor.setStreamHandler(new PumpStreamHandler(outputStream));
			executor.setWorkingDirectory(new File(DIRNAME));
			executor.execute(CommandLine.parse(EntireSuite.Z3_PATH + " -h"));
			result = outputStream.toString();
		} catch (IOException e) {
			return false;
		}
		return result.startsWith("Z3 [version ");
	}

}
