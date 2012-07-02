package za.ac.sun.cs.green;

import java.util.HashMap;
import java.util.Map;

import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.service.Service;

/**
 * An {@code Instance} instance describes a constraint about which different
 * question can be asked. For example, for a symbolic execution system this
 * might represent a path condition. Instances are organised hierarchically and
 * each instance has a parent that usually contains the greater part of the
 * constraint. The "fresh" part of the constraint is stored in the instance
 * itself, and the two parts are implicitly conjuncted.
 */
public abstract class Instance {

	/**
	 * The solver to which this instance belongs. This is used to access the
	 * available filters and services.
	 */
	protected Solver solver;

	/**
	 * The parent instance that contains the non-fresh part of the path
	 * condition.
	 */
	protected Instance parent;

	/**
	 * The fresh part of the constraint.
	 */
	protected Expression expression;

	/**
	 * The full constraint.
	 */
	protected Expression fullExpression;

	/**
	 * A store where services are allowed to store data that are relevant only to
	 * this instance. The keys of the mapping are the service classes themselves,
	 * and it is up to the services to manage their values.
	 */
	private Map<Class<? extends Service>, Object> serviceData;

	public Instance(Solver solver, Instance parent, Expression expression) {
		this.solver = solver;
		this.parent = parent;
		this.expression = expression;
		serviceData = new HashMap<Class<? extends Service>, Object>();
	}

	/**
	 * Returns the parent instance.
	 * 
	 * @return the parent instance
	 */
	public Instance getParent() {
		return parent;
	}

	/**
	 * Returns the expression (= the fresh part of the constraint represented by
	 * the instance).
	 * 
	 * @return the expression
	 */
	public Expression getExpression() {
		return expression;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
	}

	public Expression getFullExpression() {
		if (fullExpression == null) {
			Instance p = getParent();
			Expression e = (p == null) ? null : p.getFullExpression();
			fullExpression = (e == null) ? expression : new Operation(Operation.Operator.AND, expression, e);
		}
		return fullExpression;
	}

	public void setFullExpression(Expression expression) {
		fullExpression = expression;
	}

	/**
	 * Returns the data associated with the given service class.
	 * 
	 * @param serviceClass
	 *            the class of the service
	 * @return the data associated with the service class (may be null)
	 */
	public Object getServiceData(Class<? extends Service> serviceClass) {
		return serviceData.get(serviceClass);
	}

	/**
	 * Sets the data associated with the given service class.
	 * 
	 * @param serviceClass
	 *            the class of the service
	 * @param data
	 *            the new data associated with the service class
	 */
	public void setServiceData(Class<? extends Service> serviceClass, Object data) {
		serviceData.put(serviceClass, data);
	}

	/**
	 * 
	 */
	public Object issueRequest(Request request) {
		return solver.issueRequest(request, this);
	}

}
