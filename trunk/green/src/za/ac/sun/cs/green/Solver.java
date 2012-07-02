package za.ac.sun.cs.green;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import za.ac.sun.cs.green.service.Service;
import za.ac.sun.cs.green.store.NullStore;
import za.ac.sun.cs.green.store.Store;

public class Solver {

	public static final Object UNSOLVED = new Object();

	public static final Object ERROR = new Object();

	public Logger logger;

	/**
	 * The data store where results are cached.  This may be null.
	 */
	private Store store;

	private List<Service> services;

	public Solver() {
		logger = Logger.getLogger("za.ac.sun.cs.green");
		logger.setUseParentHandlers(false);
		new NullStore(this);
		services = new LinkedList<Service>();
	}

	public Store getStore() {
		return store;
	}

	public void setStore(Store store) {
		logger.info("setting store " + store.getClass().getCanonicalName());
		this.store = store;
	}

	public void addService(Service service) {
		logger.info("adding service" + service.getClass().getCanonicalName());
		services.add(service);
	}

	public void report() {
		store.report();
		for (Service s : services) {
			s.report();
		}
	}

	public void shutdown() {
		store.shutdown();
		for (Service s : services) {
			s.shutdown();
		}
		logger.info("shutdown");
	}

	public Object issueRequest(Request request, Instance instance) {
		logger.fine("request issued: " + instance);
		for (Service s : services) {
			logger.fine("checking service " + s.getName());
			Object r = s.handle(request, instance);
			if (r != UNSOLVED) {
				logger.fine("service returned result " + r);
				return r;
			}
		}
		logger.fine("no service returned a result");
		return UNSOLVED;
	}

}
