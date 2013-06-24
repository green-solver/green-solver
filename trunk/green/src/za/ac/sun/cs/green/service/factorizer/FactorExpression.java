package za.ac.sun.cs.green.service.factorizer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.expr.Variable;
import za.ac.sun.cs.green.expr.Visitor;
import za.ac.sun.cs.green.expr.VisitorException;

/**
 * This class records the factors for a given constraint. It supports
 * incrementally updating, i.e., given an extension of a constraint the factors
 * are computed incrementally based on that extension.
 */
public class FactorExpression {

	/**
	 * Factors are sets of mutually dependent expressions. Each such set is
	 * grouped into a map entry whose key is the set of variables involved in
	 * expressions in the set.
	 * 
	 * An invariant for this map is that the keys are disjoint sets of
	 * variables.
	 */
	private Map<Set<Variable>, Set<Expression>> var2Factor;

	/**
	 * For convenience we maintain the union of the image of var2Factor as a
	 * set.
	 */
	private Set<Variable> variables;

	/**
	 * The number of conjuncts in the entire constraint
	 */
	private int conjunctCount;

	/**
	 * Statistics caches: these help avoid having to recompute conjunct and
	 * variable counts which can be accumulated when the actual dependent factor
	 * calculations are performed.
	 * 
	 * The first shadows var2Factor and records the number of conjuncts in each
	 * factor. The last two cache counts from the dependent factor calculations.
	 */
	private Map<Set<Variable>, Integer> var2ConjunctCounts;
	private Map<Expression, Integer> dependentVarCounts;
	private Map<Expression, Integer> dependentConjunctCounts;

	/**
	 * Create a new empty FactoredConstraint
	 */
	public FactorExpression() {
		var2Factor = new HashMap<Set<Variable>, Set<Expression>>();
		variables = new HashSet<Variable>();
		conjunctCount = 0;
		var2ConjunctCounts = new HashMap<Set<Variable>, Integer>();
		dependentVarCounts = new HashMap<Expression, Integer>();
		dependentConjunctCounts = new HashMap<Expression, Integer>();
	}

	/**
	 * Create a new FactoredConstraint incrementally from a given
	 * FactoredConstraint, base, and a new Expression, fresh.
	 * 
	 * A deep copy of the base is created to ensure changes here do not affect
	 * other users of the base. The copy only goes as deep as the structures
	 * used in this class. Specifically, things like Expressions and Variables
	 * do not need to be copied.
	 */
	public FactorExpression(FactorExpression base, Expression fresh) {
		if (base == null) {
			base = new FactorExpression();
		}

		// System.out.println("In FactorMap with baseMap "+base);

		// Make a deep copy of the base map. The underlying Variables and
		// Expressions can be shared
		var2Factor = new HashMap<Set<Variable>, Set<Expression>>();
		var2ConjunctCounts = new HashMap<Set<Variable>, Integer>();

		for (Set<Variable> keys : base.var2Factor.keySet()) {
			Set<Expression> copyExprs = new LinkedHashSet<Expression>(base.var2Factor.get(keys));
			Set<Variable> copyVars = new HashSet<Variable>(keys);
			var2Factor.put(copyVars, copyExprs);

			// Copy the statistics structure
			var2ConjunctCounts.put(copyVars, base.var2ConjunctCounts.get(keys));
		}

		// Make a copy of the base variables.
		variables = new HashSet<Variable>(base.variables);

		// Initialize statistics structures
		conjunctCount = base.conjunctCount;
		dependentVarCounts = new HashMap<Expression, Integer>();
		dependentConjunctCounts = new HashMap<Expression, Integer>();

		/**
		 * Extract both the constraints and the associated variables within
		 * those expressions from the fresh expression.
		 */
		Map<Expression, Set<Variable>> conjunct2Vars = new HashMap<Expression, Set<Variable>>();
		Collector collector = new Collector(conjunct2Vars);
		collector.explore(fresh);

		/**
		 * Add each of the fresh constraints to the FactorizedFormule
		 */
		for (Expression expr : conjunct2Vars.keySet()) {
			assert expr instanceof Operation;
			conjunctCount++;
			addConstraint(expr, conjunct2Vars);
		}

		// System.out.println("Updated map is "+this);
	}

	/**
	 * The new expression (and its associated vars) are added to the conjunct
	 * map.
	 * 
	 * There are two cases: 1) The new expression is independent of the existing
	 * map, in which case it is a new factor. 2) The new expression is dependent
	 * on some existing factors, in which case all dependent factors must be
	 * merged.
	 * 
	 * @param vars
	 *            The set of variables involved in the new conjunct
	 * @param exprs
	 *            The logical expression being conjoined
	 */
	private void addConstraint(Expression expr, Map<Expression, Set<Variable>> conjunct2Vars) {
		Set<Variable> vars = conjunct2Vars.get(expr);

		// Compute intersection of variables with existing factor's vars
		Set<Variable> intersection = new HashSet<Variable>(vars);
		intersection.retainAll(variables);
		if (intersection.isEmpty()) {
			// System.out.println("New independent factor relative to keys "+var2Factor.keySet()+" with vars "+vars);
			// Expression is a new independent factor so add it to map and
			// factors
			Set<Expression> newFactor = new LinkedHashSet<Expression>();
			newFactor.add(expr);
			var2Factor.put(vars, newFactor);
			variables.addAll(vars);

			// Update statistics
			var2ConjunctCounts.put(vars, new Integer(1));

			// System.out.println("   Updated keys "+var2Factor.keySet());

		} else {
			// Merge the factors that are related to vars: find them, combine
			// them, update map and factors structures
			Set<Expression> mergedFactor = new LinkedHashSet<Expression>();

			// Add the potentially new conjunct to the merged factor
			mergedFactor.add(expr);

			Set<Variable> mergedVars = new HashSet<Variable>(vars);

			// System.out.println("Merging map with keys "+var2Factor.keySet()+" with new factor for vars "+vars);

			// Compute the new merged factor and old factors to be removed
			Set<Set<Variable>> oldKeys = new HashSet<Set<Variable>>();
			for (Set<Variable> key : var2Factor.keySet()) {
				// does new factor's vars intersect this one's?
				Set<Variable> keysCopy = new HashSet<Variable>(key);
				keysCopy.retainAll(vars);
				if (!keysCopy.isEmpty()) {
					// System.out.println("   Merging entry with key "+key);
					mergedFactor.addAll(var2Factor.get(key));
					mergedVars.addAll(key);

					// remove the now defunct map entry for these key vars
					oldKeys.add(key);
				}
			}

			// Remove the old keys and update with new key
			for (Set<Variable> key : oldKeys) {
				var2Factor.remove(key);
			}
			var2Factor.put(mergedVars, mergedFactor);
			variables.addAll(mergedVars);

			// Update statistics
			var2ConjunctCounts.put(mergedVars, new Integer(mergedFactor.size()));

			// System.out.println("   Updated keys "+var2Factor.keySet());

		}
	}

	public int getVariableCount() {
		return variables.size();
	}

	public int getConjunctCount() {
		return conjunctCount;
	}

	public Expression getDependentFactor(Expression expr) {
		/**
		 * Rather cavalier reuse of the local collector class. Should really
		 * have a common variable name collector that is reused.
		 * 
		 * In the end vars is the set of variables in the given constraint
		 */
		Map<Expression, Set<Variable>> conjunct2Vars = new HashMap<Expression, Set<Variable>>();
		Collector collector = new Collector(conjunct2Vars);
		collector.explore(expr);

		// Initialize tatistics
		int numVars = 0;
		int numConjuncts = 0;

		Expression dependentExpr = null;

		// If there are no variables in the fresh conjunct, we do not modify the
		// instance any further: its conjunct forms a trivial slice
		if (conjunct2Vars.size() == 0) {
			dependentExpr = expr;
		} else {

			Set<Variable> exprVars = new HashSet<Variable>();
			for (Expression e : conjunct2Vars.keySet()) {
				exprVars.addAll(conjunct2Vars.get(e));
			}

			// System.out.println("Computing slice for map with vars "+var2Factor.keySet()+" for fresh "+exprVars);
			for (Set<Variable> vars : var2Factor.keySet()) {
				// System.out.println("  considering these vars "+vars);
				Set<Variable> intersect = new HashSet<Variable>(vars);
				intersect.retainAll(exprVars);
				if (!intersect.isEmpty()) {
					// System.out.println("     found a match with conjuncts "+var2Factor.get(vars));
					for (Expression e : var2Factor.get(vars)) {
						dependentExpr = (dependentExpr == null) ? e : new Operation(Operation.Operator.AND, dependentExpr, e);

					}

					// Update local statistics
					numVars += vars.size();
					numConjuncts += var2ConjunctCounts.get(vars);
				}
			}
		}

		// Update statistics
		dependentVarCounts.put(expr, new Integer(numVars));
		dependentConjunctCounts.put(expr, new Integer(numConjuncts));

		return dependentExpr;
	}

	public int getDependentVariableCount(Expression expr) {
		Integer varCount = dependentVarCounts.get(expr);
		if (varCount == null) {
			getDependentFactor(expr);
			return dependentVarCounts.get(expr).intValue();
		} else {
			return varCount.intValue();
		}
	}

	public int getDependentConjunctCount(Expression expr) {
		Integer varCount = dependentConjunctCounts.get(expr);
		if (varCount == null) {
			getDependentFactor(expr);
			return dependentConjunctCounts.get(expr).intValue();
		} else {
			return varCount.intValue();
		}
	}

	public Set<Expression> getFactors() {
		Set<Expression> factors = new LinkedHashSet<Expression>();
		for (Set<Variable> vars : var2Factor.keySet()) {
			Expression curFactor = null;
			for (Expression e : var2Factor.get(vars)) {
				curFactor = (curFactor == null) ? e : new Operation(Operation.Operator.AND, curFactor, e);
			}
			factors.add(curFactor);
		}
		return factors;
	}

	public int getNumFactors() {
		return var2Factor.size();
	}

	public String toString() {
		String string = "";
		for (Set<Variable> vars : var2Factor.keySet()) {
			string = string + vars.toString() + "->" + var2Factor.get(vars).toString() + ", ";
		}
		if (string.length() < 2) {
			return string;
		} else {
			return string.substring(0, string.length() - 2);
		}
	}

	/**
	 * Visitor that builds the maps from conjuncts to variables. Should probably
	 * reuse this, but for now it is adapted from the code in the default
	 * slicer.
	 * 
	 * @author Jaco Geldenhuys <jaco@cs.sun.ac.za>
	 */
	private class Collector extends Visitor {

		/**
		 * The map from conjuncts to the variables it contains.
		 */
		private Map<Expression, Set<Variable>> conjunct2Vars = null;

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
		public Collector(Map<Expression, Set<Variable>> conjunct2Vars) {
			this.conjunct2Vars = conjunct2Vars;
		}

		/**
		 * Explores the expression by setting the default conjunct and then
		 * visiting the expression.
		 * 
		 * @param expression
		 *            the expression to explore
		 */
		public void explore(Expression expression) {
			currentConjunct = expression;
			try {
				expression.accept(this);
			} catch (VisitorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
				if ((op == Operation.Operator.EQ) || (op == Operation.Operator.NE) || (op == Operation.Operator.LT) || (op == Operation.Operator.LE) || (op == Operation.Operator.GT) || (op == Operation.Operator.GE)) {
					currentConjunct = expression;
				}
			}
		}

	}

}
