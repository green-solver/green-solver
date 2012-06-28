package za.ac.sun.cs.green.service;

import za.ac.sun.cs.green.Request;
import za.ac.sun.cs.green.Solver;
import za.ac.sun.cs.green.Instance;

public interface Service {

	public Solver getSolver();

	public Object handle(Request request, Instance instance);

	public String getName();

	public void report();

	public void shutdown();

}
