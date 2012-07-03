package za.ac.sun.cs.green.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;

import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Solver;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.IntVariable;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.expr.RealConstant;
import za.ac.sun.cs.green.expr.RealVariable;
import za.ac.sun.cs.green.expr.Variable;
import za.ac.sun.cs.green.expr.Visitor;
import za.ac.sun.cs.green.expr.VisitorException;

import cvc3.Expr;
import cvc3.FlagsMut;
import cvc3.Rational;
import cvc3.SatResult;
import cvc3.Type;
import cvc3.ValidityChecker;

public class CVC3 extends AbstractService {

	private static final int BASE = 10; // used in creating real variables

	private Expression expression = null;

	private Boolean satisfiable = null;

	private HashMap<Variable, Object> model = null;

	private int isSatInvocationCount = 0;

	private long dpTimeConsumption = 0;

	private long csTimeConsumption = 0;

	public CVC3(Solver solver) {
		super(solver);
		expression = null;
		satisfiable = null;
		model = null;
	}

	private void solve(Expression expression) {
		FlagsMut flags = ValidityChecker.createFlags(null);
		flags.setFlag("dagify-exprs", false);
		ValidityChecker validityChecker = ValidityChecker.create(flags);
		this.expression = expression;
		Translator translator = new Translator(validityChecker);
		try {
			expression.accept(translator);
		} catch (TranslatorUnsupportedOperation x) {
			solver.logger.log(Level.WARNING, x.getMessage(), x);
			return;
		} catch (VisitorException x) {
			solver.logger.log(Level.SEVERE, "encountered an exception -- this should not be happening!", x);
			return;
		}
		long startTime = System.currentTimeMillis();
		validityChecker.push();
		SatResult result = validityChecker.checkUnsat(translator
				.getTranslation());
		dpTimeConsumption += System.currentTimeMillis() - startTime;
		if (result == SatResult.SATISFIABLE) {
			startTime = System.currentTimeMillis();
			@SuppressWarnings("rawtypes")
			HashMap m = null;
			try {
				m = validityChecker.getConcreteModel();
			} catch (Exception x) {
				// Sometimes CVC3 produces an exception here. Why?
				System.out.println("############ CVC3 Exception ###########");
				System.out.println("############ PC: " + expression);
				x.printStackTrace();
			}
			validityChecker.pop();
			csTimeConsumption += System.currentTimeMillis() - startTime;
			if (m != null) {
				model = new HashMap<Variable, Object>();
				for (Map.Entry<Variable, Expr> e : translator.getMap()
						.entrySet()) {
					if (e.getKey() instanceof IntVariable) {
						Expr x = (Expr) m.get((Expr) e.getValue());
						model.put(e.getKey(), x.getRational().getInteger());
					} else {
						Expr x = (Expr) m.get((Expr) e.getValue());
						Rational r = x.getRational();
						double num = r.getNumerator().getInteger();
						double den = r.getDenominator().getInteger();
						model.put(e.getKey(), num / den);
					}
				}
			}
			satisfiable = true;
		} else {
			validityChecker.pop();
			model = null;
			satisfiable = false;
		}
		if (validityChecker != null) {
			validityChecker.delete();
		}
		if (flags != null) {
			flags.delete();
		}
		validityChecker = null;
		flags = null;
	}

	@Override
	public Serializable handle(Object request, Instance instance) {
		isSatInvocationCount++;
		if (satisfiable == null || !this.expression.equals(expression)) {
			solve(expression);
		}
		return satisfiable;
	}

	@SuppressWarnings("serial")
	private static class TranslatorUnsupportedOperation extends VisitorException {

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

		public Map<Variable, Expr> getMap() {
			return v2e;
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
				throw new TranslatorUnsupportedOperation("unsupported operation " + operation.getOperator());
			}
		}
	}

	@Override
	public void report() {
		solver.logger.info("isSatInvocationCount = " + isSatInvocationCount);
		solver.logger.info("csTimeConsumption = " + csTimeConsumption);
		solver.logger.info("dpTimeConsumption = " + dpTimeConsumption);
	}

	@Override
	public void shutdown() {
	}

}
