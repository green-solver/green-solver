package za.ac.sun.cs.green.expr;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Operation extends Expression {

	public static enum Operator {
		EQ, NE, LT, LE, GT, GE,
		AND, OR, IMPLIES, NOT,
		ADD, SUB, MUL, DIV, MOD, NEG,
		BIT_AND, BIT_OR, BIT_XOR, BIT_NOT, SHIFTL, SHIFTR, SHIFTUR,
		SIN, COS, TAN, ASIN, ACOS, ATAN, ATAN2,
		ROUND, LOG, EXP, POWER, SQRT;

		@Override
		public String toString() {
			switch (this) {
			case EQ:
				return "==";
			case NE:
				return "!=";
			case LT:
				return "<";
			case LE:
				return "<=";
			case GT:
				return ">";
			case GE:
				return ">=";
			case AND:
				return "&&";
			case OR:
				return "||";
			case IMPLIES:
				return "=>";
			case NOT:
				return "!";
			case ADD:
				return "+";
			case SUB:
				return "-";
			case MUL:
				return "*";
			case DIV:
				return "/";
			case MOD:
				return "%";
			case NEG:
				return "-";
			case BIT_AND:
				return "&";
			case BIT_OR:
				return "|";
			case BIT_XOR:
				return "^";
			case BIT_NOT:
				return "~";
			case SHIFTL:
				return "<<";
			case SHIFTR:
				return ">>";
			case SHIFTUR:
				return ">>>";
			case SIN:
				return "SIN";
			case COS:
				return "COS";
			case TAN:
				return "TAN";
			case ASIN:
				return "ASIN";
			case ACOS:
				return "ACOS";
			case ATAN:
				return "ATAN";
			case ATAN2:
				return "ATAN2";
			case ROUND:
				return "ROUND";
			case LOG:
				return "LOG";
			case EXP:
				return "EXP";
			case POWER:
				return "POWER";
			case SQRT:
				return "SQRT";
			default:
				return "??";
			}
		}

		public int getArity() {
			switch (this) {
			case EQ:
			case NE:
			case LT:
			case LE:
			case GT:
			case GE:
			case AND:
			case OR:
			case IMPLIES:
			case ADD:
			case SUB:
			case MUL:
			case DIV:
			case MOD:
			case BIT_AND:
			case BIT_OR:
			case BIT_XOR:
			case SHIFTL:
			case SHIFTR:
			case SHIFTUR:
			case ATAN2:
				return 2;
			case NOT:
			case NEG:
			case BIT_NOT:
			case SIN:
			case COS:
			case TAN:
			case ASIN:
			case ACOS:
			case ATAN:
			case ROUND:
			case LOG:
			case EXP:
			case POWER:
			case SQRT:
				return 1;
			default:
				return 0;
			}
		}

	}

	public static final IntConstant ZERO = new IntConstant(0);

	public static final IntConstant ONE = new IntConstant(1);

	public static final Expression FALSE = new Operation(Operation.Operator.EQ, ZERO, ONE);

	public static final Expression TRUE = new Operation(Operation.Operator.EQ, ZERO, ZERO);

	private Operator operator = null;

	private Expression[] operands = null;

	public Operation(Operator operator, Expression... operands) {
		this.operator = operator;
		this.operands = operands;
	}

	public Operator getOperator() {
		return operator;
	}

	public Iterable<Expression> getOperands() {
		return new Iterable<Expression>() {
			@Override
			public Iterator<Expression> iterator() {
				return new Iterator<Expression>() {
					private int index = 0;

					@Override
					public boolean hasNext() {
						return index < operands.length;
					}

					@Override
					public Expression next() {
						if (index < operands.length) {
							return operands[index++];
						} else {
							throw new NoSuchElementException();
						}
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}

	public Expression getOperand(int index) {
		if ((index < 0) || (index >= operands.length)) {
			return null;
		} else {
			return operands[index];
		}
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.preVisit(this);
		for (Expression operand : operands) {
			operand.accept(visitor);
		}
		visitor.postVisit(this);
	}

//	@Override
//	public int compareTo(Expression expression) {
//		Operation operation = (Operation) expression;
//		int result = operator.compareTo(operation.operator);
//		if (result != 0) {
//			return result;
//		}
//		if (operands.length < operation.operands.length) {
//			return -1;
//		} else if (operands.length > operation.operands.length) {
//			return 1;
//		}
//		for (int i = 0; i < operands.length; i++) {
//			result = operands[i].compareTo(operation.operands[i]);
//			if (result != 0) {
//				return result;
//			}
//		}
//		return 0;
//	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Operation) {
			Operation operation = (Operation) object;
			if (operator != operation.operator) {
				return false;
			}
			if (operands.length != operation.operands.length) {
				return false;
			}
			for (int i = 0; i < operands.length; i++) {
				if (!operands[i].equals(operation.operands[i])) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int h = operator.hashCode();
		for (Expression o : operands) {
			h ^= o.hashCode();
		}
		return h;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int arity = operator.getArity();
		if (operator == Operator.ATAN2) {
			sb.append(operator.toString());
			sb.append('(');
			sb.append(operands[0].toString());
			sb.append(',');
			sb.append(operands[1].toString());
			sb.append(')');
		} else if (arity == 2) {
			if ((operands[0] instanceof Constant) || (operands[0] instanceof Variable)) {
				sb.append(operands[0].toString());
			} else {
				sb.append('(');
				sb.append(operands[0].toString());
				sb.append(')');
			}
			sb.append(operator.toString());
			if ((operands[1] instanceof Constant) || (operands[1] instanceof Variable)) {
				sb.append(operands[1].toString());
			} else {
				sb.append('(');
				sb.append(operands[1].toString());
				sb.append(')');
			}
		} else if (arity == 1) {
			sb.append(operator.toString());
			sb.append('(');
			sb.append(operands[0].toString());
			sb.append(')');
		} else {
			sb.append(operator.toString());
		}
		return sb.toString();
	}

}
