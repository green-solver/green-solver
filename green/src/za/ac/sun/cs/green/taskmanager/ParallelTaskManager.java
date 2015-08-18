package za.ac.sun.cs.green.taskmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Service;
import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.util.Reporter;

public class ParallelTaskManager implements TaskManager {

	private final Green solver;

	private final Logger log;

	private final ExecutorService executor;

	private int processedCount = 0;

	private int threadsCreated = 0;
	
	private int maxSimultaneousThreads = 0;
	
	public ParallelTaskManager(final Green solver) {
		this.solver = solver;
		log = solver.getLog();
		executor = Executors.newCachedThreadPool();
	}

	public Object execute(Service parent, Instance parentInstance, Set<Service> services, Set<Instance> instances) throws InterruptedException, ExecutionException {
		CompletionService<Object> cs = new ExecutorCompletionService<Object>(executor);
		int n = services.size() * instances.size();
		if (n > maxSimultaneousThreads) {
			maxSimultaneousThreads = n;
		}
		List<Future<Object>> futures = new ArrayList<Future<Object>>(n);
		Object result = null;
		try {
			for (Service service : services) {
				for (Instance instance : instances) {
					futures.add(cs.submit(new Task(parent, parentInstance, service, instance)));
					threadsCreated++;
				}
			}
			while ((result == null) && (n-- > 0)) {
				result = cs.take().get();
			}
		} finally {
			for (Future<Object> f : futures) {
				f.cancel(true);
			}
		}
		if (parent != null) {
			result = parent.allChildrenDone(parentInstance, result);
			if (result == null)
				log.severe("Should never happen! Got AllChildrenDone in PTM with NULL result");
		}
		return result;
	}

	@Override
	public Object process(final String serviceName, final Instance instance) {
		processedCount++;
		log.info("processing serviceName=\"" + serviceName + "\"");
		final Set<Service> services = solver.getService(serviceName);
		try {
			return execute(null, null, services, Collections.singleton(instance));
		} catch (InterruptedException x) {
			log.log(Level.SEVERE, "interrupted", x);
		} catch (ExecutionException x) {
			log.log(Level.SEVERE, "thread execution error", x);
		}
		return null;
	}

	@Override
	public void report(Reporter reporter) {
		reporter.report(getClass().getSimpleName(), "processedCount = " + processedCount);
		reporter.report(getClass().getSimpleName(), "threadsCreated = " + threadsCreated);
		reporter.report(getClass().getSimpleName(), "maxSimultaneousThreads = " + maxSimultaneousThreads);
	}

	@Override
	public void shutdown() {
		executor.shutdown();
	}

	private class Task implements Callable<Object> {

		private final Service parent;

		private final Instance parentInstance;
		
		private final Service service;
		
		private final Instance instance;

		public Task(final Service parent, final Instance parentInstance, final Service service, final Instance instance) {
			this.parent = parent;
			this.parentInstance = parentInstance;
			this.service = service;
			this.instance = instance;
		}

		@Override
		public Object call() throws Exception {
			Object result = null;
			Set<Instance> subinstances = service.processRequest(instance);
			if ((subinstances != null) && (subinstances.size() > 0)) {
				Set<Service> subservices = solver.getService(service);
				if ((subservices != null) && (subservices.size() > 0)) {
					result = execute(service, instance, subservices, subinstances);
				} else {
					result = service.allChildrenDone(instance, result);
				}
			} else {
				result = service.allChildrenDone(instance, result);
			}
			if (parent != null) {
				result = parent.childDone(parentInstance, service, instance, result); 
			}
			return result;
		}
			
	}

}
