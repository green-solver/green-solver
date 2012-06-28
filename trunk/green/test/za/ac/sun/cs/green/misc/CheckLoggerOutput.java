package za.ac.sun.cs.green.misc;

import java.util.logging.Level;

import za.ac.sun.cs.green.Solver;
import za.ac.sun.cs.green.logger.GreenHandler;
import za.ac.sun.cs.green.store.NullStore;

public class CheckLoggerOutput {

	public static void main(String args[]) {
		Solver solver = new Solver();
		solver.logger.addHandler(new GreenHandler(Level.FINE));
		new NullStore(solver);
		solver.report();
		solver.shutdown();
	}

}
