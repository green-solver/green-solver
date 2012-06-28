package za.ac.sun.cs.green;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import za.ac.sun.cs.green.filter.Filter;
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

	private List<Filter> filters;

	private List<Service> services;

	public Solver() {
		logger = Logger.getLogger("za.ac.sun.cs.green");
		logger.setUseParentHandlers(false);
		new NullStore(this);
		filters = new LinkedList<Filter>();
		services = new LinkedList<Service>();
	}

	public Store getStore() {
		return store;
	}

	public void setStore(Store store) {
		logger.info("setting store " + store.getClass().getCanonicalName());
		this.store = store;
	}

	public void addFilter(Filter filter) {
		logger.info("adding filter " + filter.getClass().getCanonicalName());
		filters.add(filter);
	}

	public void addService(Service service) {
		logger.info("adding service" + service.getClass().getCanonicalName());
		services.add(service);
	}

	public void report() {
		store.report();
		for (Filter f : filters) {
			f.report();
		}
		for (Service s : services) {
			s.report();
		}
	}

	public void shutdown() {
		store.shutdown();
		for (Filter f : filters) {
			f.shutdown();
		}
		for (Service s : services) {
			s.shutdown();
		}
		logger.info("shutdown");
	}

	public Object filter(Request request, Instance instance) {
		logger.fine("filter: " + instance);
		for (Filter f : filters) {
			logger.fine("filtering through " + f.getName());
			Object r = f.handle(request, instance);
			if (r != UNSOLVED) {
				logger.fine("filter returned result " + r);
				return r;
			}
		}
		logger.fine("no filter returned a result");
		return UNSOLVED;
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
