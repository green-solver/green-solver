package za.ac.sun.cs.green.service.cvc3;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;

import cvc3.Expr;
import cvc3.FlagsMut;
import cvc3.SatResult;
import cvc3.Type;
import cvc3.ValidityChecker;

import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.IntVariable;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.expr.RealConstant;
import za.ac.sun.cs.green.expr.RealVariable;
import za.ac.sun.cs.green.expr.Variable;
import za.ac.sun.cs.green.expr.Visitor;
import za.ac.sun.cs.green.expr.VisitorException;
import za.ac.sun.cs.green.service.SATService;

public class SATCVC3Service extends SATService {

	private static final int BASE = 10; // used in creating real variables

	public SATCVC3Service(Green solver) {
		super(solver);
	}
	
	@Override
	protected Boolean solve(Instance instance) {
		Boolean result = null;
		FlagsMut flags = ValidityChecker.createFlags(null);
		flags.setFlag("dagify-exprs", false);
		ValidityChecker validityChecker = ValidityChecker.create(flags);
		Translator translator = new Translator(validityChecker);
		try {
			instance.getExpression().accept(translator);
			validityChecker.push();
			SatResult issat = validityChecker.checkUnsat(translator
					.getTranslation());
			if (issat == SatResult.SATISFIABLE) {
				result = true;
			} else {
				validityChecker.pop();
				result = false;
			}
			if (validityChecker != null) {
				validityChecker.delete();
			}
			if (flags != null) {
				flags.delete();
			}
		} catch (TranslatorUnsupportedOperation x) {
			log.log(Level.SEVERE, x.getMessage(), x);
		} catch (VisitorException x) {
			log.log(Level.SEVERE, x.getMessage(), x);
		}
		return result;
	}

	@SuppressWarnings("serial")
	private static class TranslatorUnsupportedOperation extends
			VisitorException {

		public TranslatorUnsupportedOperation(String message) {
			super(message);
		}

	}

	private static class Translator extends Visitor {

		private ValidityChecker validityChecker = null;

		private Stack<Expr> stack = null;

		private Map<Variable, Expr> v2e = null;

		public Translator(ValidityChecker validityChecker) {
			this.validityChecker = validityChecker;
			stack = new Stack<Expr>();
			v2e = new HashMap<Variable, Expr>();
		}

		public Expr getTranslation() {
			return stack.pop();
		}

		@Override
		public void postVisit(IntConstant constant) {
			stack.push(validityChecker.ratExpr(constant.getValue()));
		}

		@Override
		public void postVisit(RealConstant constant) {
			stack.push(validityChecker.ratExpr(
					Double.toString(constant.getValue()), BASE));
		}

		@Override
		public void postVisit(IntVariable variable) {
			Expr v = v2e.get(variable);
			if (v == null) {
				Integer lower = variable.getLowerBound();
				Integer upper = variable.getUpperBound();
				Type type = validityChecker.subrangeType(
						validityChecker.ratExpr(lower),
						validityChecker.ratExpr(upper));
				v = validityChecker.varExpr(variable.getName(), type);
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
				Type type = validityChecker.subrangeType(
						validityChecker.ratExpr(lower),
						validityChecker.ratExpr(upper));
				v = validityChecker.varExpr(variable.getName(), type);
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
			switch (operation.getOperator()) {
			case EQ:
				stack.push(validityChecker.eqExpr(l, r));
				break;
			case NE:
				stack.push(validityChecker.notExpr(validityChecker.eqExpr(l, r)));
				break;
			case LT:
				stack.push(validityChecker.ltExpr(l, r));
				break;
			case LE:
				stack.push(validityChecker.leExpr(l, r));
				break;
			case GT:
				stack.push(validityChecker.gtExpr(l, r));
				break;
			case GE:
				stack.push(validityChecker.geExpr(l, r));
				break;
			case AND:
				stack.push(validityChecker.andExpr(l, r));
				break;
			case OR:
				stack.push(validityChecker.orExpr(l, r));
				break;
			case IMPLIES:
				stack.push(validityChecker.impliesExpr(l, r));
				break;
			case ADD:
				stack.push(validityChecker.plusExpr(l, r));
				break;
			case SUB:
				stack.push(validityChecker.minusExpr(l, r));
				break;
			case MUL:
				stack.push(validityChecker.multExpr(l, r));
				break;
			case DIV:
				stack.push(validityChecker.divideExpr(l, r));
				break;
			default:
				throw new TranslatorUnsupportedOperation(
						"unsupported operation " + operation.getOperator());
			}
		}
	}
}
