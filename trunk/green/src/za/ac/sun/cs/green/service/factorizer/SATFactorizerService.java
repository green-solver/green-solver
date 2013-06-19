package za.ac.sun.cs.green.service.factorizer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Service;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.service.BasicService;
import za.ac.sun.cs.green.util.Reporter;

public class SATFactorizerService extends BasicService {

	private static final String FACTORS = "FACTORS";

	private static final String FACTORS_UNSOLVED = "FACTORS_UNSOLVED";
	
	/**
	 * Number of times the slicer has been invoked.
	 */
	private int invocationCount = 0;

	/**
	 * Total number of constraints processed.
	 */
	private int constraintCount = 0;

	/**
	 * Number of factored constraints returned.
	 */
	private int factorCount = 0;

	public SATFactorizerService(Green solver) {
		super(solver);
	}

	@Override
	public Set<Instance> processRequest(Instance instance) {
		@SuppressWarnings("unchecked")
		Set<Instance> result = (Set<Instance>) instance.getData(FACTORS);
		if (result == null) {
			final Instance p = instance.getParent();

			FactoredConstraint fc0 = null;
			if (p != null) {
				fc0 = (FactoredConstraint) p.getData(FactoredConstraint.class);
				if (fc0 == null) {
					// Construct the parent's factor and store it
					fc0 = new FactoredConstraint(null, p.getFullExpression());
					p.setData(FactoredConstraint.class, fc0);
				}
			}

			final FactoredConstraint fc = new FactoredConstraint(fc0, instance.getExpression());
			instance.setData(FactoredConstraint.class, fc);

			result = new HashSet<Instance>();
			for (Expression e : fc.getFactors()) {
				System.out.println("Factorizer computes instance for :" + e);
				final Instance i = new Instance(getSolver(), instance.getSource(), null, e);
				result.add(i);
			}
			result = Collections.unmodifiableSet(result);
			instance.setData(FACTORS, result);
			instance.setData(FACTORS_UNSOLVED, result);

			System.out.println("Factorize exiting with " + result.size() + " results");

			constraintCount += 1;
			factorCount += fc.getNumFactors();
		}
		return result;
	}

	@Override
	public Object childDone(Instance instance, Service subservice, Instance subinstance, Object result) {
		Boolean issat = (Boolean) result;
		if ((issat != null) && !issat) {
			return false;
		}
		@SuppressWarnings("unchecked")
		HashSet<Instance> unsolved = (HashSet<Instance>) instance.getData(FACTORS_UNSOLVED);
		if (unsolved.contains(subinstance)) {
			// Remove the subinstance now that it is solved 
			unsolved.remove(subinstance);
			instance.setData(FACTORS_UNSOLVED, unsolved);
			// Return true of no more unsolved factors; else return null to carry on the computation
			return (unsolved.isEmpty()) ? result : true; 
		} else {
			// We have already solved this subinstance; return null to carry on the computation
			return null;
		}
	}

	@Override
	public void report(Reporter reporter) {
		reporter.report(getClass().getSimpleName(), "invocations = " + invocationCount);
		reporter.report(getClass().getSimpleName(), "totalConstraints = " + constraintCount);
		reporter.report(getClass().getSimpleName(), "factoredConstraints = " + factorCount);
	}

}
