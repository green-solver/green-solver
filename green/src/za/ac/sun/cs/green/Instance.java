package za.ac.sun.cs.green;

import java.util.Hashtable;
import java.util.Map;

import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.Operation;

public class Instance {

	private final Green solver;

	private Instance source;
	
	private final Instance parent;

	private final Expression expression;

	private Expression fullExpression;

	private final Map<Object, Object> data;

	public Instance(final Green solver, final Instance parent, final Expression expression) {
		this.solver = solver;
		this.source = (parent == null) ? null : parent.source;
		this.parent = parent;
		this.expression = expression;
		fullExpression = null;
		data = new Hashtable<Object, Object>();
	}

	public Instance(final Green solver, final Instance source, final Instance parent, final Expression expression) {
		this.solver = solver;
		this.source = source;
		this.parent = parent;
		this.expression = expression;
		fullExpression = null;
		data = new Hashtable<Object, Object>();
	}
	
	public Instance getSource() {
		return source;
	}

	public Instance getParent() {
		return parent;
	}
	
	public Expression getExpression() {
		return expression;
	}

	public Expression getFullExpression() {
		if (fullExpression == null) {
			Instance p = getParent();
			Expression e = (p == null) ? null : p.getFullExpression();
			fullExpression = (e == null) ? expression : new Operation(Operation.Operator.AND, expression, e);
		}
		return fullExpression;
	}
	
	public Object request(String serviceName) {
		source = this;
		return solver.handleRequest(serviceName, this);
	}

	public void setData(Object key, Object value) {
		data.put(key, value);
	}

	public Object getData(Object key) {
		return data.get(key);
	}
	
}
