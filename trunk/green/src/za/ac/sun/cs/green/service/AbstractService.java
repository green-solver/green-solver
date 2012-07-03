package za.ac.sun.cs.green.service;

import java.io.Serializable;

import za.ac.sun.cs.green.Instance;
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
	public Serializable handle(Object request, Instance instance) {
		return Solver.UNSOLVED;
	}

	@Override
	public String getStoreKey(Object request, Instance instance) {
		return request.toString() + ':' + instance.toString();
	}

	@Override
	public void report() {
	}

	@Override
	public void shutdown() {
	}

}
