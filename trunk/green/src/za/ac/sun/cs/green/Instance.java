package za.ac.sun.cs.green;

import java.util.HashMap;
import java.util.Map;

import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.filter.Filter;

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
	 * A store where filters are allowed to store data that are relevant only to
	 * this instance. The keys of the mapping are the filter classes themselves,
	 * and it is up to the filters to manage their values.
	 */
	private Map<Class<? extends Filter>, Object> filterData;

	public Instance(Solver solver, Instance parent, Expression expression) {
		this.solver = solver;
		this.parent = parent;
		this.expression = expression;
		filterData = new HashMap<Class<? extends Filter>, Object>();
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
	 * Returns the data associated with the given filter class.
	 * 
	 * @param filterClass
	 *            the class of the filter
	 * @return the data associated with the filter class (may be null)
	 */
	public Object getFilterData(Class<? extends Filter> filterClass) {
		return filterData.get(filterClass);
	}

	/**
	 * Sets the data associated with the given filter class.
	 * 
	 * @param filterClass
	 *            the class of the filter
	 * @param data
	 *            the new data associated with the filter class
	 */
	public void setFilterData(Class<? extends Filter> filterClass, Object data) {
		filterData.put(filterClass, data);
	}

	/**
	 * 
	 */
	public Object filter(Request request) {
		return solver.filter(request, this);
	}

	/**
	 * 
	 */
	public Object issueRequest(Request request) {
		return solver.issueRequest(request, this);
	}

}
