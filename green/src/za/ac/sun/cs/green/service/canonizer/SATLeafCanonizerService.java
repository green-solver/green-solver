package za.ac.sun.cs.green.service.canonizer;

import za.ac.sun.cs.green.Instance;

import java.util.Set;

import za.ac.sun.cs.green.Green;

public class SATLeafCanonizerService extends SATCanonizerService {

	public SATLeafCanonizerService(Green solver) {
		super(solver);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object allChildrenDone(Instance instance, Object result) {
		Object canonized = instance.getData(getClass());
		if (canonized instanceof Set) {
			return ((Set<Instance>) canonized).iterator().next().getExpression();
		} else {
			return null;
		}
	}
	

}
