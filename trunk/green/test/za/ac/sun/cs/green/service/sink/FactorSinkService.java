package za.ac.sun.cs.green.service.sink;

import java.util.HashSet;
import java.util.Set;

import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.service.BasicService;

public class FactorSinkService extends BasicService {

	public FactorSinkService(Green solver) {
		super(solver);
	}

	@Override
	public Set<Instance> processRequest(Instance instance) {
		Instance original = instance.getSource();
		@SuppressWarnings("unchecked")
		Set<Instance> factors = (Set<Instance>) original.getData(getClass());
		if (factors == null) {
			factors = new HashSet<Instance>();
		}
		factors.add(instance);
		original.setData(getClass(), factors);
		return null;
	}

}
