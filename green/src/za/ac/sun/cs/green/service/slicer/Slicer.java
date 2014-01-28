package za.ac.sun.cs.green.service.slicer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.expr.Variable;
import za.ac.sun.cs.green.expr.Visitor;
import za.ac.sun.cs.green.expr.VisitorException;

public class Slicer {

	protected final Logger log;

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

	public Slicer(final Logger log) {
		this.log = log;
	}

	public int getInvocationCount() {
		return invocationCount;
	}
	
	public int getTotalConjunctCount() {
		return totalConjunctCount;
	}
	
	public int getMinimalConjunctCount() {
		return minimalConjunctCount;
	}
	
	public int getTotalVariableCount() {
		return totalVariableCount;
	}
	
	public int getMinimalVariableCount() {
		return minimalVariableCount;
	}
	
	public Expression slice(Expression fresh, Expression rest) {
		// First update our statistics
		invocationCount++;

		// Prepare to build the conjunct <-> variable mappings
		Map<Expression, Set<Variable>> conjunct2Vars = new HashMap<Expression, Set<Variable>>();
		Map<Variable, Set<Expression>> var2Conjuncts = new HashMap<Variable, Set<Expression>>();
		Collector collector = new Collector(conjunct2Vars, var2Conjuncts);

		// Collect the conjunct <-> variable information from the fresh conjunct
		try {
			collector.explore(fresh);
		} catch (VisitorException x) {
			log.log(Level.SEVERE,
					"encountered an exception -- this should not be happening!",
					x);
		}

		// If there are no variables in the fresh conjunct, we do not modify the
		// instance any further: its conjunct forms a trivial slice
		if (conjunct2Vars.size() == 0) {
			return fresh;
		}

		// Otherwise, complete the mappings
		if (rest != null) {
			try {
				collector.explore(rest);
			} catch (VisitorException x) {
				log.log(Level.SEVERE,
						"encountered an exception -- this should not be happening!",
						x);
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
			log.log(Level.SEVERE,
					"encountered an exception -- this should not be happening!",
					x);
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
		return minimal;
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
