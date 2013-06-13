package za.ac.sun.cs.green.util;

import java.util.Properties;

import org.junit.Test;

import za.ac.sun.cs.green.Green;

/* TODO incomplete */

public class SetServiceTest {

	@Test
	public void test() {
		Green solver = new Green();
		Properties props = new Properties();
		props.setProperty("green.services", "sat");
//		props.setProperty("green.service.sat", "(slice (canonize (z3 cvc3 choco)))");
		props.setProperty("green.service.sat", "slice");
		props.setProperty("green.service.sat.slice", "za.ac.sun.cs.green.service.slicer.SATSlicerService");
		Configuration config = new Configuration(solver, props);
		config.configure();
	}

}
