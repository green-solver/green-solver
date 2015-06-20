package za.ac.sun.cs.green.service.choco3;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.IntVariable;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.expr.Variable;
import za.ac.sun.cs.green.expr.Visitor;
import za.ac.sun.cs.green.expr.VisitorException;
import choco.cp.model.CPModel;

//import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;


import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Arithmetic;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;


class Choco3Translator extends Visitor {

	private CPModel chocoModel = null;
	private Solver choco3Solver = null;

	private Stack<Object> stack = null;

	private List<Constraint> constraints = null;

	private Map<Variable, IntVar> variableMap = null;

	public Choco3Translator(Solver choco3Solver, Map<Variable, IntVar> variableMap) {
		this.choco3Solver = choco3Solver;
		stack = new Stack<Object>();
		constraints = new LinkedList<Constraint>();
		this.variableMap = variableMap;
	}

	public void translate(Expression expression) throws VisitorException {
		expression.accept(this);
		for (Constraint c : constraints) {
			//chocoModel.addConstraint(c);
			choco3Solver.post(c);
		}
		if (!stack.isEmpty()) {
			//chocoModel.addConstraint((Constraint) stack.pop());
			choco3Solver.post((Constraint) stack.pop());
		}
		assert stack.isEmpty();
	}

	@Override
	public void postVisit(IntConstant constant) {
		stack.push(constant.getValue());
	}

	@Override
	public void postVisit(IntVariable variable) {
		//IntegerVariable v = variableMap.get(variable);
		IntVar v = variableMap.get(variable);
		if (v == null) {
			Integer lower = variable.getLowerBound();
			Integer upper = variable.getUpperBound();
			//v = choco.Choco.makeIntVar(variable.getName(), lower, upper);
			v = VariableFactory.bounded(variable.getName(), lower, upper, choco3Solver);
			//chocoModel.addVariable(v);
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
				//stack.push(choco.Choco.eq((Integer) l, (IntegerExpressionVariable) r));
				stack.push(IntConstraintFactory.arithm((IntVar) r, "=", (Integer) l));
			} else if (r instanceof Integer) {
				//stack.push(choco.Choco.eq((IntegerExpressionVariable) l, (Integer) r));
				stack.push(IntConstraintFactory.arithm((IntVar) l, "=", (Integer) r));
			} else {
				//stack.push(choco.Choco.eq((IntegerExpressionVariable) l, (IntegerExpressionVariable) r));
				stack.push(IntConstraintFactory.arithm((IntVar) l, "=", (IntVar) r));
			}
			break;
		case NE:
			if (l instanceof Integer) {
				//stack.push(choco.Choco.neq((Integer) l, (IntegerExpressionVariable) r));
				stack.push(IntConstraintFactory.arithm((IntVar) r, "!=", (Integer) l));
			} else if (r instanceof Integer) {
				//stack.push(choco.Choco.neq((IntegerExpressionVariable) l, (Integer) r));
				stack.push(IntConstraintFactory.arithm((IntVar) l, "!=", (Integer) r));
			} else {
				//stack.push(choco.Choco.neq((IntegerExpressionVariable) l, (IntegerExpressionVariable) r));
				stack.push(IntConstraintFactory.arithm((IntVar) l, "!=", (IntVar) r));
			}
			break;
		case LT:
			if (l instanceof Integer) {
				//stack.push(choco.Choco.lt((Integer) l, (IntegerExpressionVariable) r));
				stack.push(IntConstraintFactory.arithm((IntVar) r, ">=", (Integer) l));
			} else if (r instanceof Integer) {
				//stack.push(choco.Choco.lt((IntegerExpressionVariable) l, (Integer) r));
				stack.push(IntConstraintFactory.arithm((IntVar) l, "<", (Integer) r));
			} else {
				//stack.push(choco.Choco.lt((IntegerExpressionVariable) l, (IntegerExpressionVariable) r));
				stack.push(IntConstraintFactory.arithm((IntVar) l, "<", (IntVar) r));
			}
			break;
		case LE:
			if (l instanceof Integer) {
				//stack.push(choco.Choco.leq((Integer) l, (IntegerExpressionVariable) r));
				stack.push(IntConstraintFactory.arithm((IntVar) r, ">", (Integer) l));
			} else if (r instanceof Integer) {
				//stack.push(choco.Choco.leq((IntegerExpressionVariable) l, (Integer) r));
				stack.push(IntConstraintFactory.arithm((IntVar) l, "<=", (Integer) r));
			} else {
				//stack.push(choco.Choco.leq((IntegerExpressionVariable) l, (IntegerExpressionVariable) r));
				stack.push(IntConstraintFactory.arithm((IntVar) l, "<=", (IntVar) r));
			}
			break;
		case GT:
			if (l instanceof Integer) {
				//stack.push(choco.Choco.gt((Integer) l, (IntegerExpressionVariable) r));
				stack.push(IntConstraintFactory.arithm((IntVar) r, "<=", (Integer) l));
			} else if (r instanceof Integer) {
				//stack.push(choco.Choco.gt((IntegerExpressionVariable) l, (Integer) r));
				stack.push(IntConstraintFactory.arithm((IntVar) l, ">", (Integer) r));
			} else {
				//stack.push(choco.Choco.gt((IntegerExpressionVariable) l, (IntegerExpressionVariable) r));
				stack.push(IntConstraintFactory.arithm((IntVar) l, ">", (IntVar) r));
			}
			break;
		case GE:
			if (l instanceof Integer) {
				//stack.push(choco.Choco.geq((Integer) l, (IntegerExpressionVariable) r));
				stack.push(IntConstraintFactory.arithm((IntVar) r, "<", (Integer) l));
			} else if (r instanceof Integer) {
				//stack.push(choco.Choco.geq((IntegerExpressionVariable) l, (Integer) r));
				stack.push(IntConstraintFactory.arithm((IntVar) l, ">=", (Integer) r));
			} else {
				//stack.push(choco.Choco.geq((IntegerExpressionVariable) l, (IntegerExpressionVariable) r));
				stack.push(IntConstraintFactory.arithm((IntVar) l, ">=", (IntVar) r));
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
				//stack.push(choco.Choco.plus((Integer) l, (IntegerExpressionVariable) r));
				stack.push(IntConstraintFactory.arithm((IntVar) r, "+", (Integer) l));
			} else if (r instanceof Integer) {
				//stack.push(choco.Choco.plus((IntegerExpressionVariable) l, (Integer) r));
				stack.push(IntConstraintFactory.arithm((IntVar) l, "+", (Integer) r));
			} else {
				//stack.push(choco.Choco.plus((IntegerExpressionVariable) l, (IntegerExpressionVariable) r));
				stack.push(IntConstraintFactory.arithm((IntVar) l, "+", (IntVar) r));
			}
			break;
		case SUB:
			if (l instanceof Integer) {
				//stack.push(choco.Choco.minus((Integer) l, (IntegerExpressionVariable) r));
				stack.push(IntConstraintFactory.arithm(VariableFactory.minus((IntVar) r), "+", (Integer) l));
			} else if (r instanceof Integer) {
				//stack.push(choco.Choco.minus((IntegerExpressionVariable) l, (Integer) r));
				stack.push(IntConstraintFactory.arithm((IntVar) l, "-", (Integer) r));
			} else {
				//stack.push(choco.Choco.minus((IntegerExpressionVariable) l, (IntegerExpressionVariable) r));
				stack.push(IntConstraintFactory.arithm((IntVar) l, "-", (IntVar) r));
			}
			break;
		case MUL:
			//stack.push(choco.Choco.mult((Integer) l, (IntegerExpressionVariable) r));
			stack.push(VariableFactory.scale((IntVar) r, (Integer) l));
			break;
		default:
			throw new TranslatorUnsupportedOperation("unsupported operation " + operation.getOperator());
		}
	}
}