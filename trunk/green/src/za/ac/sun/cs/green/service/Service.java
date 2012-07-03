package za.ac.sun.cs.green.service;

import java.io.Serializable;

import za.ac.sun.cs.green.Solver;
import za.ac.sun.cs.green.Instance;

public interface Service {

	public Solver getSolver();

	public Serializable handle(Object request, Instance instance);

	public String getStoreKey(Object request, Instance instance);
	
	public String getName();

	public void report();

	public void shutdown();

}
