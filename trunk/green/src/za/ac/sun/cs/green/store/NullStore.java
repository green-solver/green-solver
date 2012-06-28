package za.ac.sun.cs.green.store;

import java.io.Serializable;

import za.ac.sun.cs.green.Solver;

public class NullStore extends AbstractStore {

	public NullStore(Solver solver) {
		super(solver);
	}

	private int getCount = 0;

	private int putCount = 0;

	@Override
	public void report() {
		solver.logger.info("number of get requests = " + getCount);
		solver.logger.info("number of put requests = " + putCount);
	}

	@Override
	public void put(String key, String value) {
		putCount++;
	}

	@Override
	public void put(String key, Serializable value) {
		putCount++;
	}

}
