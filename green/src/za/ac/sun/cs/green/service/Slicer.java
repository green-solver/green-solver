package za.ac.sun.cs.green.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;

import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Solver;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.expr.Variable;
import za.ac.sun.cs.green.expr.Visitor;
import za.ac.sun.cs.green.expr.VisitorException;

public class Slicer extends AbstractService {

	/**
	 * Number of times the slicer has been invoked.
	 */
	private int invocationCount = 0;

	/**
	 * Total number of conjuncts processed.
	 */
	private int totalConjunctCount = 0;

	/**
	 * Number of minimal conjuncts returned.
	 */
	private int minimalConjunctCount = 0;

	/**
	 * Total number of variables processed.
	 */
	private int totalVariableCount = 0;

	/**
	 * Number of minimal variables returned.
	 */
	private int minimalVariableCount = 0;

	public Slicer(Solver solver) {
		super(solver);
	}

	@Override
	public Serializable handle(Object request, Instance instance) {
		@SuppressWarnings("unchecked")
		Map<String, Object> data = (Map<String, Object>) instance
				.getServiceData(getClass());
		if (data == null) {
			data = new HashMap<String, Object>();
			instance.setServiceData(getClass(), data);
			slice(data, instance);
		}
		return Solver.UNSOLVED;
	}

	@Override
	public String getStoreKey(Object request, Instance instance) {
		return null;
	}

	private void slice(Map<String, Object> data, Instance instance) {
		// First update our statistics
		invocationCount++;
		// Store the fresh conjuncts and the rest in local variables
		Expression fresh = instance.getExpression();
		Expression rest = null;
		Expression full = fresh;
		Instance parent = instance.getParent();
		if (parent != null) {
			handle(null, parent);
			@SuppressWarnings("unchecked")
			Map<String, Object> parentData = (Map<String, Object>) parent
					.getServiceData(getClass());
			rest = (Expression) parentData.get("oldFullExpression");
			full = new Operation(Operation.Operator.AND, fresh, rest);
		}
		// Store the existing expression
		data.put("oldFullExpression", full);

		// Prepare to build the conjunct <-> variable mappings
		Map<Expression, Set<Variable>> conjunct2Vars = new HashMap<Expression, Set<Variable>>();
		Map<Variable, Set<Expression>> var2Conjuncts = new HashMap<Variable, Set<Expression>>();
		Collector collector = new Collector(conjunct2Vars, var2Conjuncts);
		// First collect the conjunct <-> variable information from the fresh
		// conjunct
		try {
			collector.explore(fresh);
		} catch (VisitorException x) {
			solver.logger.log(Level.SEVERE, "encountered an exception -- this should not be happening!", x);
		}
		// If there are no variables in the fresh conjunct, we do not modify the
		// instance any further: its conjunct forms a trivial slice
		if (conjunct2Vars.size() == 0) {
			return;
		}
		// Otherwise, complete the mappings
		if (rest != null) {
			try {
				collector.explore(rest);
			} catch (VisitorException x) {
				solver.logger.log(Level.SEVERE, "encountered an exception -- this should not be happening!", x);
			}
		}
		// Update our statistics
		totalConjunctCount += conjunct2Vars.size();
		totalVariableCount += var2Conjuncts.size();

		// Prepare to collect the minimal conjuncts
		Set<Expression> minimalConjuncts = new HashSet<Expression>();
		Queue<Expression> workset = new LinkedList<Expression>();
		Set<Variable> varSet = new HashSet<Variable>();
		// The following
		try {
			fresh.accept(new Enqueuer(minimalConjuncts, workset));
		} catch (VisitorException x) {
			solver.logger.log(Level.SEVERE, "encountered an exception -- this should not be happening!", x);
		}
		while (!workset.isEmpty()) {
			Expression e = workset.remove();
			Set<Variable> vs = conjunct2Vars.get(e);
			if (vs != null) {
				for (Variable v : vs) {
					varSet.add(v);
					Set<Expression> fs = var2Conjuncts.get(v);
					if (fs != null) {
						for (Expression f : fs) {
							if (!minimalConjuncts.contains(f)) {
								workset.add(f);
								minimalConjuncts.add(f);
							}
						}
					}
				}
			}
		}
		// Update statistics once again
		minimalConjunctCount += minimalConjuncts.size();
		minimalVariableCount += varSet.size();

		// Finally, combine the minimal conjuncts into one conjunction
		Expression minimal = null;
		for (Expression e : minimalConjuncts) {
			if (minimal == null) {
				minimal = e;
			} else {
				minimal = new Operation(Operation.Operator.AND, minimal, e);
			}
		}
		instance.setFullExpression(minimal);
	}

	@Override
	public void report() {
		solver.logger.info("invocations = " + invocationCount);
		solver.logger.info("totalConjuncts = " + totalConjunctCount);
		solver.logger.info("minimalConjuncts = " + minimalConjunctCount);
		solver.logger
				.info("conjunctReduction = "
						+ ((totalConjunctCount - minimalConjunctCount) * 100.0D / totalConjunctCount));
		solver.logger.info("totalVariables = " + totalVariableCount);
		solver.logger.info("minimalVariables = " + minimalVariableCount);
		solver.logger
				.info("variableReduction = "
						+ ((totalVariableCount - minimalVariableCount) * 100.0D / totalVariableCount));
	}

	/**
	 * Visitor that builds the maps from conjuncts to variables and from
	 * variables to conjuncts.
	 * 
	 * @author Jaco Geldenhuys <jaco@cs.sun.ac.za>
	 */
	private class Collector extends Visitor {

		/**
		 * The map from conjuncts to the variables it contains.
		 */
		private Map<Expression, Set<Variable>> conjunct2Vars = null;

		/**
		 * The map from variables to the conjuncts in which they appear.
		 */
		private Map<Variable, Set<Expression>> var2Conjuncts = null;

		/**
		 * The currentConjunct being visited.
		 */
		private Expression currentConjunct = null;

		/**
		 * Constructor that sets the two mappings that the collector builds.
		 * 
		 * @param conjunct2Vars
		 *            a map from conjuncts to the variables they contain
		 * @param var2Conjuncts
		 *            a map from the variables to the conjuncts in which they
		 *            appear
		 */
		public Collector(Map<Expression, Set<Variable>> conjunct2Vars,
				Map<Variable, Set<Expression>> var2Conjuncts) {
			this.conjunct2Vars = conjunct2Vars;
			this.var2Conjuncts = var2Conjuncts;
		}

		/**
		 * Explores the expression by setting the default conjunct and then
		 * visiting the expression.
		 * 
		 * @param expression
		 *            the expression to explore
		 * @throws VisitorException
		 *             should never happen
		 */
		public void explore(Expression expression) throws VisitorException {
			currentConjunct = expression;
			expression.accept(this);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * za.ac.sun.cs.solver.expr.Visitor#postVisit(za.ac.sun.cs.solver.expr
		 * .Variable)
		 */
		@Override
		public void postVisit(Variable variable) {
			if (currentConjunct != null) {
				// add mapping c -> v
				Set<Variable> c2v = conjunct2Vars.get(currentConjunct);
				if (c2v == null) {
					c2v = new HashSet<Variable>();
				}
				c2v.add(variable);
				conjunct2Vars.put(currentConjunct, c2v);
				// add mapping v -> c
				Set<Expression> v2c = var2Conjuncts.get(variable);
				if (v2c == null) {
					v2c = new HashSet<Expression>();
				}
				v2c.add(currentConjunct);
				var2Conjuncts.put(variable, v2c);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * za.ac.sun.cs.solver.expr.Visitor#preVisit(za.ac.sun.cs.solver.expr
		 * .Expression)
		 */
		@Override
		public void preVisit(Expression expression) {
			if (expression instanceof Operation) {
				Operation.Operator op = ((Operation) expression).getOperator();
				if ((op == Operation.Operator.EQ)
						|| (op == Operation.Operator.NE)
						|| (op == Operation.Operator.LT)
						|| (op == Operation.Operator.LE)
						|| (op == Operation.Operator.GT)
						|| (op == Operation.Operator.GE)) {
					currentConjunct = expression;
				}
			}
		}

	}

	/**
	 * @author Jaco Geldenhuys <jaco@cs.sun.ac.za>
	 */
	private static class Enqueuer extends Visitor {

		/**
		 * The set of minimal conjuncts found in an expression.
		 */
		private Set<Expression> minimalConjuncts = null;

		/**
		 * The set of minimal conjuncts to start the transitive closure
		 * computation from.
		 */
		private Queue<Expression> workset = null;

		/**
		 * Constructor for the visitor that builds the set of minimal conjuncts
		 * and the initial working set.
		 * 
		 * @param minimalConjuncts
		 *            a set of minimal conjuncts that appear in the expression
		 * @param workset
		 *            the initial working set of fresh conjuncts
		 */
		public Enqueuer(Set<Expression> minimalConjuncts,
				Queue<Expression> workset) {
			this.minimalConjuncts = minimalConjuncts;
			this.workset = workset;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * za.ac.sun.cs.solver.expr.Visitor#preVisit(za.ac.sun.cs.solver.expr
		 * .Expression)
		 */
		@Override
		public void preVisit(Expression expression) {
			if (expression instanceof Operation) {
				Operation.Operator op = ((Operation) expression).getOperator();
				if ((op == Operation.Operator.EQ)
						|| (op == Operation.Operator.NE)
						|| (op == Operation.Operator.LT)
						|| (op == Operation.Operator.LE)
						|| (op == Operation.Operator.GT)
						|| (op == Operation.Operator.GE)) {
					minimalConjuncts.add(expression);
					workset.add(expression);
				}
			}
		}

	}

}
