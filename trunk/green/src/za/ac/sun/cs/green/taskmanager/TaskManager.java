package za.ac.sun.cs.green.taskmanager;

import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.util.Reporter;

public interface TaskManager {

	public Object process(String serviceName, Instance instance);

	public void report(Reporter reporter);

	public void shutdown();
	
}
