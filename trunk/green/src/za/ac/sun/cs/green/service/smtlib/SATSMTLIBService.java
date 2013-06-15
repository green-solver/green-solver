package za.ac.sun.cs.green.service.smtlib;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;

import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.IntVariable;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.expr.Operation.Operator;
import za.ac.sun.cs.green.expr.RealConstant;
import za.ac.sun.cs.green.expr.RealVariable;
import za.ac.sun.cs.green.expr.Variable;
import za.ac.sun.cs.green.expr.Visitor;
import za.ac.sun.cs.green.expr.VisitorException;
import za.ac.sun.cs.green.service.SATService;
import za.ac.sun.cs.green.util.Misc;

public abstract class SATSMTLIBService extends SATService {

	public SATSMTLIBService(Green solver) {
		super(solver);
	}

	@Override
	protected Boolean solve(Instance instance) {
		try {
			Translator t = new Translator();
			instance.getExpression().accept(t);
			StringBuilder b = new StringBuilder();
			b.append("(set-option :produce-models false)");
			b.append("(set-logic AUFLIRA)"); // AUFLIA ???
			b.append(Misc.join(t.getVariables(), " "));
			b.append("(assert ").append(t.getTranslation()).append(')');
			b.append("(check-sat)");
			return solve0(b.toString());
		} catch (TranslatorUnsupportedOperation x) {
			log.log(Level.WARNING, x.getMessage(), x);
		} catch (VisitorException x) {
			log.log(Level.SEVERE, x.getMessage(), x);
		}
		return null;
	}

	protected abstract Boolean solve0(String smtQuery);

	@SuppressWarnings("serial")
	private static class TranslatorUnsupportedOperation extends
			VisitorException {

		public TranslatorUnsupportedOperation(String message) {
			super(message);
		}

	}

	private static class TranslatorPair {
		
		private final String string;
		
		private final Class<? extends Variable> type;
		
		public TranslatorPair(final String string, final Class<? extends Variable> type) {
			this.string = string;
			this.type = type;
		}

		public String getString() {
			return string;
		}
		
		public Class<? extends Variable> getType() {
			return type;
		}

	}
	
	private static class Translator extends Visitor {

		private final Stack<TranslatorPair> stack;

		private Map<Variable, String> varMap;

		private final List<String> defs;

		private final List<String> domains;

		public Translator() {
			stack = new Stack<SATSMTLIBService.TranslatorPair>();
			varMap = new HashMap<Variable, String>();
			defs = new LinkedList<String>();
			domains = new LinkedList<String>();
		}

		public List<String> getVariables() {
			return defs;
		}

		/*
		 * public Map<Variable, String> getVarMapping() { return varMap; }
		 */

		public String getTranslation() {
			StringBuilder b = new StringBuilder();
			b.append("(and");
			for (String domain : domains) {
				b.append(' ').append(domain);
			}
			TranslatorPair p = stack.pop();
			b.append(' ').append(p.getString()).append(')');
			assert stack.isEmpty();
			return b.toString();
		}

		private String transformNegative(int v) {
			if (v < 0) {
				StringBuilder b = new StringBuilder();
				b.append("(- ").append(-v).append(')');
				return b.toString();
			} else {
				return Integer.toString(v);
			}
		}

		private String transformNegative(double v) {
			if (v < 0) {
				StringBuilder b = new StringBuilder();
				b.append("(- ").append(-v).append(')');
				return b.toString();
			} else {
				return Double.toString(v);
			}
		}

		@Override
		public void postVisit(IntConstant constant) {
			int val = constant.getValue();
			stack.push(new TranslatorPair(transformNegative(val), IntVariable.class));
		}

		@Override
		public void postVisit(RealConstant constant) {
			double val = constant.getValue();
			stack.push(new TranslatorPair(transformNegative(val), RealVariable.class));
		}

		@Override
		public void postVisit(IntVariable variable) {
			String v = varMap.get(variable);
			String n = variable.getName();
			if (v == null) {
				StringBuilder b = new StringBuilder();
				b.append("(declare-fun ").append(n).append(" () Int)");
				defs.add(b.toString());
				b.setLength(0);
				// lower bound
				b.append("(and (>= ").append(n).append(' ');
				b.append(transformNegative(variable.getLowerBound()));
				// upper bound
				b.append(") (<= ").append(n).append(' ');
				b.append(transformNegative(variable.getUpperBound()));
				b.append("))");
				domains.add(b.toString());
				varMap.put(variable, n);
			}
			stack.push(new TranslatorPair(n, IntVariable.class));
		}

		@Override
		public void postVisit(RealVariable variable) {
			String v = varMap.get(variable);
			String n = variable.getName();
			if (v == null) {
				StringBuilder b = new StringBuilder();
				b.append("(declare-fun ").append(n).append(" () Real)");
				defs.add(b.toString());
				b.setLength(0);
				// lower bound
				b.append("(and (>= ").append(n).append(' ');
				b.append(transformNegative(variable.getLowerBound()));
				// upper bound
				b.append(") (<= ").append(n).append(' ');
				b.append(transformNegative(variable.getUpperBound()));
				b.append("))");
				domains.add(b.toString());
				varMap.put(variable, n);
			}
			stack.push(new TranslatorPair(n, RealVariable.class));
		}

		private Class<? extends Variable> superType(TranslatorPair left, TranslatorPair right) {
			if ((left.getType() == RealVariable.class) || (right.getType() == RealVariable.class)) {
				return RealVariable.class;
			} else {
				return IntVariable.class;
			}
		}

		private String adjust(TranslatorPair term, Class<? extends Variable> type) {
			String s = term.getString();
			Class<? extends Variable> t = term.getType();
			if (t == type) {
				return s;
			} else {
				StringBuilder b = new StringBuilder();
				b.append("(to_real ").append(s).append(')');
				return b.toString();
			}
		}
		
		private String setOperator(Operator op)
				throws TranslatorUnsupportedOperation {
			switch (op) {
			case EQ:
				return "=";
			case LT:
				return "<";
			case LE:
				return "<=";
			case GT:
				return ">";
			case GE:
				return ">=";
			case AND:
				return "and";
			case OR:
				return "or";
			case IMPLIES:
				return "=>"; // not sure about this one?
			case ADD:
				return "+";
			case SUB:
				return "-";
			case MUL:
				return "*";
			case DIV:
				return "div";
			case MOD:
				return "mod";
			case BIT_AND:
			case BIT_OR:
			case BIT_XOR:
			case SHIFTL:
			case SHIFTR:
			case SHIFTUR:
			case SIN:
			case COS:
			case TAN:
			case ASIN:
			case ACOS:
			case ATAN:
			case ATAN2:
			case ROUND:
			case LOG:
			case EXP:
			case POWER:
			case SQRT:
			default:
				throw new TranslatorUnsupportedOperation(
						"unsupported operation " + op);
			}
		}

		public void postVisit(Operation operation)
				throws TranslatorUnsupportedOperation {
			TranslatorPair l = null;
			TranslatorPair r = null;
			Operator op = operation.getOperator();
			int arity = op.getArity();
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
			if (op.equals(Operator.NE)) {
				Class<? extends Variable> v = superType(l, r);
				StringBuilder b = new StringBuilder();
				b.append("(not (= ");
				b.append(adjust(l, v)).append(' ');
				b.append(adjust(r, v)).append("))");
				stack.push(new TranslatorPair(b.toString(), v));
			} else {
				Class<? extends Variable> v = superType(l, r);
				StringBuilder b = new StringBuilder();
				b.append('(').append(setOperator(op)).append(' ');
				b.append(adjust(l, v)).append(' ');
				b.append(adjust(r, v)).append(')');
				stack.push(new TranslatorPair(b.toString(), v));
			}
		}
	}

}
