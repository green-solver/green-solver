package za.ac.sun.cs.green.filter;

import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Request;
import za.ac.sun.cs.green.Solver;

public abstract class AbstractFilter implements Filter {

	protected String name;
	
	protected Solver solver;

	public AbstractFilter(Solver solver) {
		this.solver = solver;
		solver.addFilter(this);
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
	public abstract Object handle(Request request, Instance instance);

	@Override
	public void report() {
	}

	@Override
	public void shutdown() {
	}

}
