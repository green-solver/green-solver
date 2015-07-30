package za.ac.sun.cs.green.service;

import java.util.Set;

import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.util.Reporter;

public abstract class SATService extends BasicService {

	private static final String SERVICE_KEY = "SAT:";

	private int invocationCount = 0;

	private int cacheHitCount = 0;
	
	private int cacheMissCount = 0;
	
	private long timeConsumption = 0;
	
	private int satCount = 0;
	private int unsatCount = 0;
	

	public SATService(Green solver) {
		super(solver);
	}

	@Override
	public void report(Reporter reporter) {
		reporter.report(getClass().getSimpleName(), "invocationCount = " + invocationCount);
		reporter.report(getClass().getSimpleName(), "cacheHitCount = " + cacheHitCount);
		reporter.report(getClass().getSimpleName(), "cacheMissCount = " + cacheMissCount);
		reporter.report(getClass().getSimpleName(), "timeConsumption = " + timeConsumption);
		reporter.report(getClass().getSimpleName(), "SAT queries = " + satCount);
		reporter.report(getClass().getSimpleName(), "UNSAT queries = " + unsatCount);
	}

	@Override
	public Object allChildrenDone(Instance instance, Object result) {
		return instance.getData(getClass());
	}
	
	@Override
	public Set<Instance> processRequest(Instance instance) {
		Boolean result = (Boolean) instance.getData(getClass());
		if (result == null) {
			result = solve0(instance);
			if (result != null) {
				instance.setData(getClass(), result);
			}
		}
		if (result.booleanValue())
			satCount++; 
		else 	
			unsatCount++;
		return null;
	}

	private Boolean solve0(Instance instance) {
		invocationCount++;
		String key = SERVICE_KEY + instance.getFullExpression().toString();
		Boolean result = store.getBoolean(key);
		if (result == null) {
			cacheMissCount++;
			result = solve1(instance);
			if (result != null) {
				store.put(key, result);
			}
		} else {
			cacheHitCount++;
		}
		return result;
	}

	private Boolean solve1(Instance instance) {
		long startTime = System.currentTimeMillis();
		Boolean result = solve(instance); 
		timeConsumption += System.currentTimeMillis() - startTime;
		return result;
	}

	protected abstract Boolean solve(Instance instance);

}
