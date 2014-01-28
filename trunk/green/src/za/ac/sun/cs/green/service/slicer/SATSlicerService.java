package za.ac.sun.cs.green.service.slicer;

import java.util.Collections;
import java.util.Set;

import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.service.BasicService;
import za.ac.sun.cs.green.util.Reporter;

public class SATSlicerService extends BasicService {

	private final Slicer slicer;

	public SATSlicerService(Green solver) {
		super(solver);
		slicer = new Slicer(log);
	}

	@Override
	public Set<Instance> processRequest(Instance instance) {
		@SuppressWarnings("unchecked")
		Set<Instance> result = (Set<Instance>) instance.getData(getClass());
		if (result == null) {
			final Instance p = instance.getParent();
			final Expression f = (p == null) ? null : p.getFullExpression();
			final Expression e = slicer.slice(instance.getExpression(), f);
			final Instance i = new Instance(getSolver(), instance.getSource(), null, e);
			result = Collections.singleton(i);
			instance.setData(getClass(), result);
		}
		return result;
	}

	@Override
	public void report(Reporter reporter) {
		int ic = slicer.getInvocationCount();
		int tc = slicer.getTotalConjunctCount();
		int mc = slicer.getMinimalConjunctCount();
		int tv = slicer.getTotalVariableCount();
		int mv = slicer.getMinimalVariableCount();
		reporter.report(getClass().getSimpleName(), "invocations = " + ic);
		reporter.report(getClass().getSimpleName(), "totalConjuncts = " + tc);
		reporter.report(getClass().getSimpleName(), "minimalConjuncts = " + mc);
		reporter.report(getClass().getSimpleName(), "conjunctReduction = " + ((tc - mc) * 100.0D / tc));
		reporter.report(getClass().getSimpleName(), "totalVariables = " + tv);
		reporter.report(getClass().getSimpleName(), "minimalVariables = " + mv);
		reporter.report(getClass().getSimpleName(), "variableReduction = " + ((tv - mv) * 100.0D / tv));
	}

}
