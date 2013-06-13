package za.ac.sun.cs.green.taskmanager;

import java.util.Collections;
import java.util.Set;
import java.util.logging.Logger;

import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Service;
import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.util.Reporter;

public class SerialTaskManager implements TaskManager {

	private final Green solver;

	private final Logger log;

	private int processedCount = 0;

	public SerialTaskManager(final Green solver) {
		this.solver = solver;
		log = solver.getLog();
	}

	public Object execute(Service parent, Set<Service> services, Set<Instance> instances) {
		for (Service service : services) {
			for (Instance instance : instances) {
				Set<Instance> subproblems = service.processRequest(instance);
				Object result = null;
				if ((subproblems != null) && (subproblems.size() > 0)) {
					Set<Service> subservices = solver.getService(service);
					if ((subservices != null) && (subservices.size() > 0)) {
						result = execute(service, subservices, subproblems);
					} else {
						result = service.processResponse(instance, result);
					}
				} else {
					result = service.processResponse(instance, result);
				}
				if (parent != null) {
					result = parent.processResponse(instance, result);
				}
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	@Override
	public Object process(final String serviceName, final Instance instance) {
		log.info("processing serviceName=\"" + serviceName + "\"");
		processedCount++;
		final Set<Service> service = solver.getService(serviceName);
		return execute(null, service, Collections.singleton(instance));
	}

	@Override
	public void report(Reporter reporter) {
		reporter.report(getClass().getName(), "processedCount = " + processedCount);
	}

	@Override
	public void shutdown() {
	}

}
