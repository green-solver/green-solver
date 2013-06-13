package za.ac.sun.cs.green;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import za.ac.sun.cs.green.log.GreenHandler;
import za.ac.sun.cs.green.store.NullStore;
import za.ac.sun.cs.green.store.Store;
import za.ac.sun.cs.green.taskmanager.SerialTaskManager;
import za.ac.sun.cs.green.taskmanager.TaskManager;
import za.ac.sun.cs.green.util.Reporter;

public class Green {

	private static int solverCounter = 0;

	/**
	 * The name of this solver.  This is used mainly for naming the logger.
	 */
	private final String greenName;

	/**
	 * The logger for this solver.
	 */
	private final Logger log;

	/**
	 * The task manager that handles how requests are processed.
	 */
	private TaskManager taskManager;

	/**
	 * A mapping from service names to services.
	 */
	private final Map<String, Set<Service>> services0;
	
	/**
	 * A mapping from services to sub-services.
	 */
	private final Map<Service, Set<Service>> services1;

	private Store store;

	public Green(String solverName) {
		this.greenName = solverName;
		log = Logger.getLogger("za.ac.sun.cs.green[" + solverName + "]");
		log.setUseParentHandlers(false);
		log.setLevel(Level.ALL);
		log.addHandler(new GreenHandler(Level.ALL));
		taskManager = new SerialTaskManager(this);
		store = new NullStore(this);
		services0 = new HashMap<String, Set<Service>>();
		services1 = new HashMap<Service, Set<Service>>();
		log.finest("Solver(\"" + solverName + "\") created");
	}

	public Green() {
		greenName = "anonymous" + ++solverCounter;
		log = Logger.getLogger("za.ac.sun.cs.green[" + greenName + "]");
		log.setUseParentHandlers(false);
		log.setLevel(Level.ALL);
		log.addHandler(new GreenHandler(Level.ALL));
		taskManager = new SerialTaskManager(this);
		store = new NullStore(this);
		services0 = new HashMap<String, Set<Service>>();
		services1 = new HashMap<Service, Set<Service>>();
		log.finest("Solver(\"" + greenName + "\") created");
	}

	public String getSolverName() {
		return greenName;
	}

	public Logger getLog() {
		return log;
	}

	public void setTaskManager(final TaskManager taskManager) {
		this.taskManager = taskManager;
	}

	public TaskManager getTaskManager() {
		return taskManager;
	}

	public void setStore(final Store store) {
		this.store = store;
	}
	
	public Store getStore() {
		return store;
	}
	
	public Set<Service> getService(String serviceName) {
		return services0.get(serviceName);
	}
	
	public Set<Service> getService(Service service) {
		return services1.get(service);
	}
	
	public void registerService(String serviceName, Service subService) {
		log.info("register service: name=\"" + serviceName + "\", subservice="
				+ subService.getClass().getName());
		Set<Service> serviceSet = services0.get(serviceName);
		if (serviceSet == null) {
			serviceSet = new HashSet<Service>();
			services0.put(serviceName, serviceSet);
		}
		serviceSet.add(subService);
	}

	public void registerService(Service service, Service subService) {
		log.info("register service: name=\"" + service.getClass().getName() + "\", subservice="
				+ subService.getClass().getName());
		Set<Service> serviceSet = services1.get(service);
		if (serviceSet == null) {
			serviceSet = new HashSet<Service>();
			services1.put(service, serviceSet);
		}
		serviceSet.add(subService);
	}
	
	public Object handleRequest(String serviceName, Instance instance) {
		return taskManager.process(serviceName, instance);
	}

	public void report() {
		report(new Reporter() {
			@Override
			public void report(String context, String message) {
				log.info(message);
			}
		});
	}
	
	public void report(Reporter reporter) {
		taskManager.report(reporter);
		store.report(reporter);
		for (Set<Service> s : services0.values()) {
			for (Service ss : s) {
				ss.report(reporter);
			}
		}
		for (Set<Service> s : services1.values()) {
			for (Service ss : s) {
				ss.report(reporter);
			}
		}
	}

	public void shutdown() {
		for (Set<Service> s : services0.values()) {
			for (Service ss : s) {
				ss.shutdown();
			}
		}
		for (Set<Service> s : services1.values()) {
			for (Service ss : s) {
				ss.shutdown();
			}
		}
		taskManager.shutdown();
		store.shutdown();
	}
	
}
