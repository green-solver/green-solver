package za.ac.sun.cs.green.service;

import java.util.logging.Logger;

import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Service;
import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.store.Store;
import za.ac.sun.cs.green.util.Reporter;

public abstract class BasicService implements Service {

	protected final Green solver;

	protected final Logger log;

	protected final Store store;

	public BasicService(final Green solver) {
		this.solver = solver;
		log = solver.getLog();
		store = solver.getStore();
	}

	@Override
	public Green getSolver() {
		return solver;
	}

	@Override
	public Object processResponse(Instance instance, Object result) {
		return result;
	}

	@Override
	public void shutdown() {
	}
	
	@Override
	public void report(Reporter reporter) {
	}

}
