package za.ac.sun.cs.green.service.choco;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.variables.integer.IntegerVariable;
import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.expr.Variable;
import za.ac.sun.cs.green.expr.VisitorException;
import za.ac.sun.cs.green.service.ModelService;

public class ModelChocoService extends ModelService {

	public ModelChocoService(Green solver) {
		super(solver);
	}

	@Override
	protected HashMap<Variable,Object> model(Instance instance) {
		CPModel chocoModel = new CPModel();
		Map<Variable, IntegerVariable> variableMap = new HashMap<Variable, IntegerVariable>();
		HashMap<Variable,Object> results = new HashMap<Variable, Object>();
		
		try {
			new ChocoTranslator(chocoModel, variableMap).translate(instance.getExpression());
			CPSolver chocoSolver = new CPSolver();
			chocoSolver.read(chocoModel);
			chocoSolver.solve();
			if (!chocoSolver.isFeasible()) {
				log.log(Level.WARNING,"constraint has no model, it is infeasible");
				return null;
			}
			for(Map.Entry<Variable,IntegerVariable> entry : variableMap.entrySet()) {
				Variable greenVar = entry.getKey();
				IntegerVariable chocoVar = entry.getValue();
				Object val = chocoSolver.getVar(chocoVar).getVal();
				results.put(greenVar, val);
				String logMessage = "" + greenVar + " has value " + val;
				log.log(Level.INFO,logMessage);
			}
			return results; 
		} catch (TranslatorUnsupportedOperation x) {
			log.log(Level.WARNING, x.getMessage(), x);
		} catch (VisitorException x) {
			log.log(Level.SEVERE, "encountered an exception -- this should not be happening!", x);
		}
		return null;
	}

}
