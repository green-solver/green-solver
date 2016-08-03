package za.ac.sun.cs.green.service.z3;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.IntVariable;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.expr.RealConstant;
import za.ac.sun.cs.green.expr.RealVariable;
import za.ac.sun.cs.green.expr.Variable;
import za.ac.sun.cs.green.expr.Visitor;
import za.ac.sun.cs.green.expr.VisitorException;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;

class Z3JavaTranslator extends Visitor {
	
	private Context context = null;

	private Stack<Expr> stack = null;
	
	private List<BoolExpr> domains = null;

	private Map<Variable, Expr> v2e = null;

	public Z3JavaTranslator(Context c) {
		this.context = c;
		stack = new Stack<Expr>();
		v2e = new HashMap<Variable, Expr>();
		domains = new LinkedList<BoolExpr>();
	}

	public BoolExpr getTranslation() {
		BoolExpr result = (BoolExpr)stack.pop();
		/* not required due to Bounder being used */
		/* not sure why this was commented out, it is clearly wrong, with or without bounder */
		for (BoolExpr expr : domains) {
			try {
				result = context.mkAnd(result,expr);
			} catch (Z3Exception e) {
				e.printStackTrace();
			}
		}
		/* was end of old comment */
		return result;
	}
	
	public Map<Variable, Expr> getVariableMap() {
		return v2e;
	}
	

	@Override
	public void postVisit(IntConstant constant) {			
		try {
			stack.push(context.mkInt(constant.getValue()));
		} catch (Z3Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void postVisit(RealConstant constant) {
		try {
			stack.push(context.mkReal(Double.toString(constant.getValue())));
		} catch (Z3Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void postVisit(IntVariable variable) {
		Expr v = v2e.get(variable);
		if (v == null) {
			Integer lower = variable.getLowerBound();
			Integer upper = variable.getUpperBound();
			try {
				v = context.mkIntConst(variable.getName());
				// now add bounds
				BoolExpr low  = context.mkGe((ArithExpr)v,(ArithExpr)context.mkInt(lower));
				BoolExpr high = context.mkLe((ArithExpr)v,(ArithExpr)context.mkInt(upper));
				domains.add(context.mkAnd(low,high));
			} catch (Z3Exception e) {
				e.printStackTrace();
			}
			v2e.put(variable, v);
		}
		stack.push(v);
	}

	@Override
	public void postVisit(RealVariable variable) {
		Expr v = v2e.get(variable);
		if (v == null) {
			int lower = (int) (double) variable.getLowerBound();
			int upper = (int) (double) variable.getUpperBound();
			try {
				v = context.mkRealConst(variable.getName());
				// now add bounds
				BoolExpr low  = context.mkGe((ArithExpr)v,(ArithExpr)context.mkReal(lower));
				BoolExpr high = context.mkLe((ArithExpr)v,(ArithExpr)context.mkReal(upper));
				domains.add(context.mkAnd(low,high));
			} catch (Z3Exception e) {
				e.printStackTrace();
			}
			v2e.put(variable, v);
		}
		stack.push(v);
	}

	@Override
	public void postVisit(Operation operation) throws VisitorException {
		Expr l = null;
		Expr r = null;
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
		try {
			switch (operation.getOperator()) {
			case EQ:
				stack.push(context.mkEq(l, r));
				break;
			case NE:
				stack.push(context.mkNot(context.mkEq(l, r)));
				break;
			case LT:
				stack.push(context.mkLt((ArithExpr) l, (ArithExpr) r));
				break;
			case LE:
				stack.push(context.mkLe((ArithExpr) l, (ArithExpr) r));
				break;
			case GT:
				stack.push(context.mkGt((ArithExpr) l, (ArithExpr) r));
				break;
			case GE:
				stack.push(context.mkGe((ArithExpr) l, (ArithExpr) r));
				break;
			case AND:
				stack.push(context.mkAnd((BoolExpr) l, (BoolExpr) r));
				break;
			case OR:
				stack.push(context.mkOr((BoolExpr) l, (BoolExpr) r));
				break;
			case IMPLIES:
				stack.push(context.mkImplies((BoolExpr) l, (BoolExpr) r));
				break;
			case ADD:
				stack.push(context.mkAdd((ArithExpr) l, (ArithExpr) r));
				break;
			case SUB:
				stack.push(context.mkSub((ArithExpr) l, (ArithExpr) r));
				break;
			case MUL:
				stack.push(context.mkMul((ArithExpr) l, (ArithExpr) r));
				break;
			case DIV:
				stack.push(context.mkDiv((ArithExpr) l, (ArithExpr) r));
				break;
			default:
				throw new TranslatorUnsupportedOperation(
						"unsupported operation " + operation.getOperator());
			}
		} catch (Z3Exception e) {
			e.printStackTrace();
		}
	}
}