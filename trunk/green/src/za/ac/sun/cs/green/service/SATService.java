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

	public SATService(Green solver) {
		super(solver);
	}

	@Override
	public void report(Reporter reporter) {
		reporter.report(getClass().getName(), "invocationCount = " + invocationCount);
		reporter.report(getClass().getName(), "cacheHitCount = " + cacheHitCount);
		reporter.report(getClass().getName(), "cacheMissCount = " + cacheMissCount);
		reporter.report(getClass().getName(), "timeConsumption = " + timeConsumption);
	}

	@Override
	public Object processResponse(Instance instance, Object result) {
		return (Boolean) instance.getData(getClass());
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
