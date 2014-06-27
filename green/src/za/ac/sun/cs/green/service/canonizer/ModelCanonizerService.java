package za.ac.sun.cs.green.service.canonizer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.Service;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.Variable;

public class ModelCanonizerService extends SATCanonizerService {

	private static final String RENAME = "RENAME";
	
	public ModelCanonizerService(Green solver) {
		super(solver);
	}

	private Map<Variable, Variable> reverseMap(Map<Variable, Variable> map) {
		Map<Variable, Variable> revMap = new HashMap<Variable, Variable>();
		for (Map.Entry<Variable,Variable> m : map.entrySet()) {
			revMap.put(m.getValue(), m.getKey());
		}
		return revMap;
	}
	
	@Override
	public Set<Instance> processRequest(Instance instance) {
		@SuppressWarnings("unchecked")
		Set<Instance> result = (Set<Instance>) instance.getData(getClass());
		if (result == null) {
			final Map<Variable, Variable> map = new HashMap<Variable, Variable>();
			final Expression e = canonize(instance.getFullExpression(), map);
			Map<Variable, Variable> reverseMap = reverseMap(map);
			final Instance i = new Instance(getSolver(), instance.getSource(), null, e);
			result = Collections.singleton(i);
			instance.setData(getClass(), result);
			instance.setData(RENAME, reverseMap);
		}
		return result;
	}

	@Override
	public Object childDone(Instance instance, Service subService,
			Instance subInstance, Object result) {
		@SuppressWarnings("unchecked")
		HashMap<Variable,Object> r = (HashMap<Variable,Object>)result;
		
		@SuppressWarnings("unchecked")
		HashMap<Variable, Variable> reverseMap = (HashMap<Variable, Variable>)instance.getData(RENAME);
		
		HashMap<Variable,Object> newResult = new HashMap<Variable,Object>();
		for (Map.Entry<Variable,Object> m : r.entrySet()) {
			newResult.put(reverseMap.get(m.getKey()), m.getValue());
		}
		return newResult;
	}
	
}
