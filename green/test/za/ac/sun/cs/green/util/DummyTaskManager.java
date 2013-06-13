package za.ac.sun.cs.green.util;

import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.taskmanager.TaskManager;

public class DummyTaskManager implements TaskManager {

	public DummyTaskManager(Green solver) {
	}

	@Override
	public Object process(String serviceName, Instance instance) {
		return null;
	}

	@Override
	public void report(Reporter reporter) {
	}

	@Override
	public void shutdown() {
	}
	
}
