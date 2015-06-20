package za.ac.sun.cs.green.service.choco3;

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

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

public class ModelChoco3Service extends ModelService {

	public ModelChoco3Service(Green solver) {
		super(solver);
	}

	@Override
	protected HashMap<Variable,Object> model(Instance instance) {
		Solver choco3Solver = new Solver();
		Map<Variable, IntVar> variableMap = new HashMap<Variable, IntVar>();
		HashMap<Variable,Object> results = new HashMap<Variable, Object>();
		
		try {
			new Choco3Translator(choco3Solver, variableMap).translate(instance.getExpression());
			
			if (!choco3Solver.findSolution()) {
				log.log(Level.WARNING,"constraint has no model, it is infeasible");
				return null;
			}
			for(Map.Entry<Variable,IntVar> entry : variableMap.entrySet()) {
				Variable greenVar = entry.getKey();
				IntVar chocoVar = entry.getValue();
				Object val = chocoVar.getValue();
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
