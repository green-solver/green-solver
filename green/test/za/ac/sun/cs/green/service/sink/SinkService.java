package za.ac.sun.cs.green.service.sink;

import java.util.Set;

import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.service.BasicService;

public class SinkService extends BasicService {

	public SinkService(Green solver) {
		super(solver);
	}

	@Override
	public Set<Instance> processRequest(Instance instance) {
		return null;
	}

	@Override
	public Object allChildrenDone(Instance instance, Object result) {
		return instance;
	}
	
}
