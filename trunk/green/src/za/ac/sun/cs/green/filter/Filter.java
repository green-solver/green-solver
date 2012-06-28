package za.ac.sun.cs.green.filter;

import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Request;
import za.ac.sun.cs.green.Solver;

public interface Filter {

	public Solver getSolver();

	public Object handle(Request request, Instance instance);

	public String getName();

	public void report();

	public void shutdown();

}
