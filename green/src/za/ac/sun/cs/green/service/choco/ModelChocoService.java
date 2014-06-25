package za.ac.sun.cs.green.service.choco;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;

import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;

import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.IntVariable;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.expr.Variable;
import za.ac.sun.cs.green.expr.Visitor;
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
			new Translator(chocoModel, variableMap).translate(instance.getExpression());
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
	
	@SuppressWarnings("serial")
	private static class TranslatorUnsupportedOperation extends VisitorException {

		public TranslatorUnsupportedOperation(String message) {
			super(message);
		}

	}

	private static class Translator extends Visitor {

		private CPModel chocoModel = null;

		private Stack<Object> stack = null;

		private List<Constraint> constraints = null;

		private Map<Variable, IntegerVariable> variableMap = null;

		public Translator(CPModel chocoModel, Map<Variable, IntegerVariable> variableMap) {
			this.chocoModel = chocoModel;
			stack = new Stack<Object>();
			constraints = new LinkedList<Constraint>();
			this.variableMap = variableMap;
		}

		public void translate(Expression expression) throws VisitorException {
			expression.accept(this);
			for (Constraint c : constraints) {
				chocoModel.addConstraint(c);
			}
			if (!stack.isEmpty()) {
				chocoModel.addConstraint((Constraint) stack.pop());
			}
			assert stack.isEmpty();
		}

		@Override
		public void postVisit(IntConstant constant) {
			stack.push(constant.getValue());
		}

		@Override
		public void postVisit(IntVariable variable) {
			IntegerVariable v = variableMap.get(variable);
			if (v == null) {
				Integer lower = variable.getLowerBound();
				Integer upper = variable.getUpperBound();
				v = choco.Choco.makeIntVar(variable.getName(), lower, upper);
				chocoModel.addVariable(v);
				variableMap.put(variable, v);
			}
			stack.push(v);
		}

		@Override
		public void postVisit(Operation operation) throws TranslatorUnsupportedOperation {
			Object l = null;
			Object r = null;
			int arity = operation.getOperator().getArity();
			if (arity == 2) {
				if (!stack.isEmpty()) {
					r = stack.pop();
				}
				if (!stack.isEmpty()) {
					l = stack.pop();
				}
			} else if (arity == 1) {
				if (!stack.isEmpty()) {
					l = stack.pop();
				}
			}
			switch (operation.getOperator()) {
			case EQ:
				if (l instanceof Integer) {
					stack.push(choco.Choco.eq((Integer) l, (IntegerExpressionVariable) r));
				} else if (r instanceof Integer) {
					stack.push(choco.Choco.eq((IntegerExpressionVariable) l, (Integer) r));
				} else {
					stack.push(choco.Choco.eq((IntegerExpressionVariable) l, (IntegerExpressionVariable) r));
				}
				break;
			case NE:
				if (l instanceof Integer) {
					stack.push(choco.Choco.neq((Integer) l, (IntegerExpressionVariable) r));
				} else if (r instanceof Integer) {
					stack.push(choco.Choco.neq((IntegerExpressionVariable) l, (Integer) r));
				} else {
					stack.push(choco.Choco.neq((IntegerExpressionVariable) l, (IntegerExpressionVariable) r));
				}
				break;
			case LT:
				if (l instanceof Integer) {
					stack.push(choco.Choco.lt((Integer) l, (IntegerExpressionVariable) r));
				} else if (r instanceof Integer) {
					stack.push(choco.Choco.lt((IntegerExpressionVariable) l, (Integer) r));
				} else {
					stack.push(choco.Choco.lt((IntegerExpressionVariable) l, (IntegerExpressionVariable) r));
				}
				break;
			case LE:
				if (l instanceof Integer) {
					stack.push(choco.Choco.leq((Integer) l, (IntegerExpressionVariable) r));
				} else if (r instanceof Integer) {
					stack.push(choco.Choco.leq((IntegerExpressionVariable) l, (Integer) r));
				} else {
					stack.push(choco.Choco.leq((IntegerExpressionVariable) l, (IntegerExpressionVariable) r));
				}
				break;
			case GT:
				if (l instanceof Integer) {
					stack.push(choco.Choco.gt((Integer) l, (IntegerExpressionVariable) r));
				} else if (r instanceof Integer) {
					stack.push(choco.Choco.gt((IntegerExpressionVariable) l, (Integer) r));
				} else {
					stack.push(choco.Choco.gt((IntegerExpressionVariable) l, (IntegerExpressionVariable) r));
				}
				break;
			case GE:
				if (l instanceof Integer) {
					stack.push(choco.Choco.geq((Integer) l, (IntegerExpressionVariable) r));
				} else if (r instanceof Integer) {
					stack.push(choco.Choco.geq((IntegerExpressionVariable) l, (Integer) r));
				} else {
					stack.push(choco.Choco.geq((IntegerExpressionVariable) l, (IntegerExpressionVariable) r));
				}
				break;
			case AND:
				if (l != null) {
					constraints.add((Constraint) l);
				}
				if (r != null) {
					constraints.add((Constraint) r);
				}
				break;
			case ADD:
				if (l instanceof Integer) {
					stack.push(choco.Choco.plus((Integer) l, (IntegerExpressionVariable) r));
				} else if (r instanceof Integer) {
					stack.push(choco.Choco.plus((IntegerExpressionVariable) l, (Integer) r));
				} else {
					stack.push(choco.Choco.plus((IntegerExpressionVariable) l, (IntegerExpressionVariable) r));
				}
				break;
			case SUB:
				if (l instanceof Integer) {
					stack.push(choco.Choco.minus((Integer) l, (IntegerExpressionVariable) r));
				} else if (r instanceof Integer) {
					stack.push(choco.Choco.minus((IntegerExpressionVariable) l, (Integer) r));
				} else {
					stack.push(choco.Choco.minus((IntegerExpressionVariable) l, (IntegerExpressionVariable) r));
				}
				break;
			case MUL:
				stack.push(choco.Choco.mult((Integer) l, (IntegerExpressionVariable) r));
				break;
			default:
				throw new TranslatorUnsupportedOperation("unsupported operation " + operation.getOperator());
			}
		}
	}

}
