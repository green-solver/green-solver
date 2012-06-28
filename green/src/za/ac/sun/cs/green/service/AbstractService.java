package za.ac.sun.cs.green.service;

import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Request;
import za.ac.sun.cs.green.Solver;

public abstract class AbstractService implements Service {

	protected String name;

	protected Solver solver;

	public AbstractService(Solver solver) {
		this.solver = solver;
		solver.addService(this);
		name = getClass().getCanonicalName();
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Solver getSolver() {
		return solver;
	}

	@Override
	public Object handle(Request request, Instance instance) {
		return Solver.UNSOLVED;
	}

	@Override
	public void report() {
	}

	@Override
	public void shutdown() {
	}

}
