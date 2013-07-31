package za.ac.sun.cs.green.service.slicer;

import java.util.Collections;
import java.util.Set;

import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.service.BasicService;
import za.ac.sun.cs.green.service.factorizer.FactorExpression;
import za.ac.sun.cs.green.util.Reporter;

public class SATFactorSlicerService extends BasicService {

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

	public SATFactorSlicerService(Green solver) {
		super(solver);
	}

	@Override
	public Set<Instance> processRequest(Instance instance) {
		@SuppressWarnings("unchecked")
		Set<Instance> result = (Set<Instance>) instance.getData(getClass());
		if (result == null) {
			final Instance p = instance.getParent();
			
			// Handle two initial conditions: no parent and no FactoredConstraint for the parent
			FactorExpression fc0 = null;
			if (p!=null) {
				fc0 = (FactorExpression) p.getData(FactorExpression.class);
				if (fc0==null) {
					// Construct the parent's factor and store it 
					fc0 = new FactorExpression(null, p.getFullExpression());
					p.setData(FactorExpression.class, fc0);
				}
			}
			
			final FactorExpression fc = new FactorExpression(fc0, instance.getExpression());
			instance.setData(FactorExpression.class, fc);

			final Expression e = fc.getDependentFactor(instance.getExpression());

			final Instance i = new Instance(getSolver(), instance.getSource(), null, e);
			result = Collections.singleton(i);
			instance.setData(getClass(), result);
			
			// First update our statistics
			invocationCount++;
			minimalVariableCount += fc.getDependentVariableCount(instance.getExpression());
			minimalConjunctCount += fc.getDependentConjunctCount(instance.getExpression());
			totalConjunctCount += fc.getConjunctCount();
			totalVariableCount += fc.getVariableCount();
		}
		return result;
	}

	@Override
	public void report(Reporter reporter) {
		reporter.report(getClass().getSimpleName(), "invocations = " + invocationCount);
		reporter.report(getClass().getSimpleName(), "totalConjuncts = " + totalConjunctCount);
		reporter.report(getClass().getSimpleName(), "minimalConjuncts = " + minimalConjunctCount);
		reporter.report(getClass().getSimpleName(), "conjunctReduction = " + ((totalConjunctCount - minimalConjunctCount) * 100.0D / totalConjunctCount));
		reporter.report(getClass().getSimpleName(), "totalVariables = " + totalVariableCount);
		reporter.report(getClass().getSimpleName(), "minimalVariables = " + minimalVariableCount);
		reporter.report(getClass().getSimpleName(), "variableReduction = " + ((totalVariableCount - minimalVariableCount) * 100.0D / totalVariableCount));
	}

}
