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
import za.ac.sun.cs.green.service.SATService;

public class SATChocoService extends SATService {

	public SATChocoService(Green solver) {
		super(solver);
	}

	@Override
	protected Boolean solve(Instance instance) {
		CPModel chocoModel = new CPModel();
		Map<Variable, IntegerVariable> variableMap = new HashMap<Variable, IntegerVariable>();
		try {
			new ChocoTranslator(chocoModel, variableMap).translate(instance.getExpression());
			CPSolver chocoSolver = new CPSolver();
			chocoSolver.read(chocoModel);
			return chocoSolver.solve();
		} catch (TranslatorUnsupportedOperation x) {
			log.log(Level.WARNING, x.getMessage(), x);
		} catch (VisitorException x) {
			log.log(Level.SEVERE, "encountered an exception -- this should not be happening!", x);
		}
		return null;
	}
}
