package za.ac.sun.cs.green;

import java.util.Set;

import za.ac.sun.cs.green.util.Reporter;

/**
 * The basis interface for all services.
 * 
 * @author Jaco Geldenhuys <jaco@cs.sun.ac.za>
 */
public interface Service {

	/**
	 * Return the instance of the Green solver associated with this service.
	 * 
	 * @return the instance of the Green solver associated with this service
	 */
	public Green getSolver();

	/**
	 * Process an instance by (possibly) transforming it and (possibly) breaking
	 * it into one or more sub-instances.
	 * 
	 * For example, the factorizer partitions the formula associated with the
	 * instance into subformulas, creates a new sub-instance for each
	 * subformula, and returns this set. On the other hand, a SAT service would
	 * pass the formula to an external solver and return an empty set of
	 * sub-instances.
	 * 
	 * @param instance
	 *            the instance to solve
	 * @return a set of sub-instances passed to sub-services
	 */
	public Set<Instance> processRequest(Instance instance);

	/**
	 * Perform an update computation based on a new subresult computed by a
	 * child service of the current service.
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
	 */
	public Object childDone(Instance instance, Service subService,
			Instance subInstance, Object result);

	/**
	 * Perform a final computation on the results computed by the children of
	 * the current service.
	 * 
	 * @param instance
	 *            the input instance
	 * @param result
	 *            the result computed so far by this service
	 * @return the final result
	 */
	public Object allChildrenDone(Instance instance, Object result);

	/**
	 * Shut down the service.
	 */
	public void shutdown();

	/**
	 * Report on the performance or state of the service.
	 * 
	 * @param reporter
	 *            the mechanism through which reporting is done
	 */
	public void report(Reporter reporter);

}
