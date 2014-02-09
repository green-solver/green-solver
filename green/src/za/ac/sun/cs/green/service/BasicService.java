package za.ac.sun.cs.green.service;

import java.util.Set;
import java.util.logging.Logger;

import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Service;
import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.store.Store;
import za.ac.sun.cs.green.util.Reporter;

/**
 * An partial implementation of a service that provides some common
 * functionality. In practice, most services extend this class.
 * 
 * The class provides default implementations of the
 * {@link #processRequest(Instance)},
 * {@link #childDone(Instance, Service, Instance, Object)}, and
 * {@link #allChildrenDone(Instance, Object)} routines. Usually these would be
 * left abstract, but many services may want the fallback behaviour, especially
 * for the latter two routines.
 * 
 * As implemented, {@link #processRequest(Instance)} returns {@code null}, which
 * the standard task managers interpret to mean that the service has "handled"
 * the request, processed the input instance in some way, and is ready to
 * deliver a result. No child service is invoked, since there is no sub-instance
 * to pass along to them.
 * 
 * As implemented here, {@link #childDone(Instance, Service, Instance, Object)}
 * returns whatever result it receives as input. The standard task managers
 * interpret this to mean that the child has successfully "handled" its
 * sub-instance and that the result it computed also qualifies as a result for
 * the current instance of the current service. The task manager may decide not
 * to invoke any further children for the current service since this (possibly
 * first) result already serves as an answer. In the case of a parallel task
 * manager, it may decide to terminate other children that are already
 * processing their sub-instances.
 * 
 * Finally, {@link #allChildrenDone(Instance, Object)} gets a last opportunity
 * to "finish" the result received by the current service from its child
 * services. As implemented here, the routine simply once again returns the
 * first result it received from any potential children.
 * 
 * @author Jaco Geldenhuys <jaco@cs.sun.ac.za>
 */
public abstract class BasicService implements Service {

	/**
	 * The {@link Green} solver to which this service "belongs".
	 */
	protected final Green solver;

	/**
	 * The Java {@link Logger} associated with the {@link Green} solver.
	 */
	protected final Logger log;

	/**
	 * The {@link Store} associated with the {@link Green} solver.
	 */
	protected final Store store;

	/**
	 * Constructor for the basic service. It simply initializes its three
	 * attributes.
	 * 
	 * @param solver
	 *            the {@link Green} solver this service will be added to
	 */
	public BasicService(final Green solver) {
		this.solver = solver;
		log = solver.getLog();
		store = solver.getStore();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see za.ac.sun.cs.green.Service#getSolver()
	 */
	@Override
	public Green getSolver() {
		return solver;
	}

	/**
	 * This fallback implementation of {@link #processRequest(Instance)} return
	 * {@code null}. See the class comment for a description of what effect this
	 * may have.
	 * 
	 * @param instance
	 *            the instance to solve
	 * @return a set of sub-instances passed to sub-services
	 * 
	 * @see za.ac.sun.cs.green.Service#processRequest(za.ac.sun.cs.green.Instance)
	 */
	@Override
	public Set<Instance> processRequest(Instance instance) {
		return null;
	}

	/**
	 * This fallback implementation of
	 * {@link #childDone(Instance, Service, Instance, Object)} simply returns
	 * the result computed by the child service. See the class comment for a
	 * description of what effect this may have.
	 * 
	 * @param instance
	 *            the input instance
	 * @param subService
	 *            the sub-service (i.e., child service) that computed a result
	 * @param subInstance
	 *            the sub-instance which this service passed to the sub-service
	 * @param result
	 *            the result return by the sub-service
	 * @return a new (intermediary) result
	 * 
	 * @see za.ac.sun.cs.green.Service#childDone(za.ac.sun.cs.green.Instance,
	 *      za.ac.sun.cs.green.Service, za.ac.sun.cs.green.Instance,
	 *      java.lang.Object)
	 */
	@Override
	public Object childDone(Instance instance, Service subService,
			Instance subInstance, Object result) {
		return result;
	}

	/**
	 * This fallback implementation of
	 * {@link #allChildrenDone(Instance, Object)} simply returns the result
	 * without modification.
	 * 
	 * @param instance
	 *            the input instance
	 * @param result
	 *            the result computed so far by this service
	 * @return the final result
	 * 
	 * @see za.ac.sun.cs.green.Service#allChildrenDone(za.ac.sun.cs.green.Instance,
	 *      java.lang.Object)
	 */
	@Override
	public Object allChildrenDone(Instance instance, Object result) {
		return result;
	}

	/**
	 * This fallback implementation of {@link #shutdown()} does nothing.
	 * 
	 * @see za.ac.sun.cs.green.Service#shutdown()
	 */
	@Override
	public void shutdown() {
	}

	/**
	 * This fallback implementation of {@link #report(Reporter)} ignores its
	 * parameter and does nothing.
	 * 
	 * @param reporter
	 *            the mechanism through which reporting is done
	 * 
	 * @see za.ac.sun.cs.green.Service#report(za.ac.sun.cs.green.util.Reporter)
	 */
	@Override
	public void report(Reporter reporter) {
	}

}
