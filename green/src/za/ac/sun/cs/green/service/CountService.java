package za.ac.sun.cs.green.service;

import java.util.Set;

import org.apfloat.Apint;

import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.util.Reporter;

public abstract class CountService extends BasicService {

	private static final String SERVICE_KEY = "COUNT:";

	private int invocationCount = 0;

	private int cacheHitCount = 0;
	
	private int cacheMissCount = 0;
	
	private long timeConsumption = 0;

	public CountService(Green solver) {
		super(solver);
	}

	@Override
	public void report(Reporter reporter) {
		reporter.report(getClass().getSimpleName(), "invocationCount = " + invocationCount);
		reporter.report(getClass().getSimpleName(), "cacheHitCount = " + cacheHitCount);
		reporter.report(getClass().getSimpleName(), "cacheMissCount = " + cacheMissCount);
		reporter.report(getClass().getSimpleName(), "timeConsumption = " + timeConsumption);
	}

	@Override
	public Object allChildrenDone(Instance instance, Object result) {
		return instance.getData(getClass());
	}
	
	@Override
	public Set<Instance> processRequest(Instance instance) {
		Apint result = (Apint) instance.getData(getClass());
		if (result == null) {
			result = solve0(instance);
			if (result != null) {
				instance.setData(getClass(), result);
			}
		}
		return null;
	}

	private Apint solve0(Instance instance) {
		invocationCount++;
		String key = SERVICE_KEY + instance.getFullExpression().toString();
		Apint result = store.getApfloatInteger(key);
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

	private Apint solve1(Instance instance) {
		long startTime = System.currentTimeMillis();
		Apint result = solve(instance); 
		timeConsumption += System.currentTimeMillis() - startTime;
		return result;
	}

	protected abstract Apint solve(Instance instance);

}
