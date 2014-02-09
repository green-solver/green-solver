package za.ac.sun.cs.green.service.bounder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.IntVariable;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.expr.Variable;
import za.ac.sun.cs.green.expr.Visitor;
import za.ac.sun.cs.green.expr.VisitorException;
import za.ac.sun.cs.green.service.BasicService;
import za.ac.sun.cs.green.util.Reporter;

/**
 * It is often desirable to explicitly add variable bounds to an expression. For
 * example, "x==y" could become "(x==y)&(x>0)&(x<10)&(y>0)&(y<10)". This is
 * necessary in many cases where variables with different bounds occur within a
 * program, or where the user wants to count the number of solutions.
 * 
 * This service collects the bounds for all the variables that occur within the
 * expression associated with a given instance. The bounds are expressed as a
 * conjunction and added (in a new instance) to the parent instance of the input
 * instance. It is added to the parent because we do not want to influence any
 * slicing tools. And it is added to a new instance, so that the original
 * instance is not distorted in any way.
 * 
 * At the moment, the service operates only on integer variables.
 * 
 * @author Jaco Geldenhuys <jaco@cs.sun.ac.za>
 */
public class BounderService extends BasicService {

	/**
	 * Number of times the bounder has been invoked.
	 */
	private int invocationCount = 0;

	/**
	 * Total number of variables processed.
	 */
	private int totalVariableCount = 0;

	public BounderService(Green solver) {
		super(solver);
	}

	@Override
	public Set<Instance> processRequest(Instance instance) {
		@SuppressWarnings("unchecked")
		Set<Instance> result = (Set<Instance>) instance.getData(getClass());
		if (result == null) {
			Expression e = bound(instance.getFullExpression());
			final Instance p = instance.getParent();
			if (p != null) {
				e = new Operation(Operation.Operator.AND,
						p.getFullExpression(), e);
			}
			final Instance q = new Instance(getSolver(), instance.getSource(),
					null, e);
			final Instance i = new Instance(getSolver(), instance.getSource(),
					q, instance.getExpression());
			result = Collections.singleton(i);
			instance.setData(getClass(), result);
		}
		return result;
	}

	@Override
	public void report(Reporter reporter) {
		reporter.report(getClass().getSimpleName(), "invocations = "
				+ invocationCount);
		reporter.report(getClass().getSimpleName(), "totalVariables = "
				+ totalVariableCount);
	}

	/**
	 * Collect all of the variables that appear in an expression and construct
	 * conjuncts that encode the minimum and maximum bounds on the variables.
	 * Then return a new expression which consists of the conjunction of the
	 * bounds.
	 * 
	 * @param expression
	 *            the input expression
	 * @return bound conjuncts for all of the variables in the input
	 */
	private Expression bound(Expression expression) {
		invocationCount++;
		Expression e = null;
		try {
			Set<Variable> variables = new VariableCollector()
					.getVariables(expression);
			totalVariableCount += variables.size();
			for (Variable v : variables) {
				if (v instanceof IntVariable) {
					IntVariable iv = (IntVariable) v;
					Operation lower = new Operation(Operation.Operator.GE, iv,
							new IntConstant(iv.getLowerBound()));
					Operation upper = new Operation(Operation.Operator.LE, iv,
							new IntConstant(iv.getUpperBound()));
					Operation bound = new Operation(Operation.Operator.AND,
							lower, upper);
					if (e == null) {
						e = bound;
					} else {
						e = new Operation(Operation.Operator.AND, e, bound);
					}
				} else {
					log.log(Level.WARNING,
							"NOT adding bounds for non-integer variable \""
									+ v.getName() + "\"");
				}
			}
		} catch (VisitorException x) {
			log.log(Level.SEVERE,
					"encountered an exception -- this should not be happening!",
					x);
		}
		return (e == null) ? Operation.TRUE : e;
	}

	private static class VariableCollector extends Visitor {

		private final Set<Variable> variables;

		public VariableCollector() {
			variables = new HashSet<Variable>();
		}

		public Set<Variable> getVariables(Expression expression)
				throws VisitorException {
			variables.clear();
			expression.accept(this);
			return variables;
		}

		@Override
		public void postVisit(Variable variable) {
			variables.add(variable);
		}

	}

}
