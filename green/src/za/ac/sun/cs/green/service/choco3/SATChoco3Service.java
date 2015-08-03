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
import za.ac.sun.cs.green.service.SATService;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

public class SATChoco3Service extends SATService {

	public SATChoco3Service(Green solver) {
		super(solver);
	}

	@Override
	protected Boolean solve(Instance instance) {
		//CPModel chocoModel = new CPModel();
		Solver choco3Solver = new Solver();
		Map<Variable, IntVar> variableMap = new HashMap<Variable, IntVar>();
		try {
			new Choco3Translator(choco3Solver, variableMap).translate(instance.getExpression());
			//CPSolver chocoSolver = new CPSolver();
			//chocoSolver.read(chocoModel);
			System.out.println(choco3Solver);
			return choco3Solver.findSolution();
		} catch (TranslatorUnsupportedOperation x) {
			log.log(Level.WARNING, x.getMessage(), x);
		} catch (VisitorException x) {
			log.log(Level.SEVERE, "encountered an exception -- this should not be happening!", x);
		}
		return null;
	}
}
