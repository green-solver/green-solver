package za.ac.sun.cs.green;

import java.util.Set;

import za.ac.sun.cs.green.util.Reporter;

public interface Service {

	public Green getSolver();

	public Set<Instance> processRequest(Instance instance);
	
	public Object processResponse(Instance instance, Object result);

	public void shutdown();
	
	public void report(Reporter reporter);

}
