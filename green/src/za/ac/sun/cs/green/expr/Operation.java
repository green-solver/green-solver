package za.ac.sun.cs.green.expr;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Operation extends Expression {

	public static enum Fix {
		PREFIX, INFIX, POSTFIX;
	}

	public static enum Operator {
		EQ("==", 2, Fix.INFIX),
		NE("!=", 2, Fix.INFIX),
		LT("<", 2, Fix.INFIX),
		LE("<=", 2, Fix.INFIX),
		GT(">", 2, Fix.INFIX),
		GE(">=", 2, Fix.INFIX),
		AND("&&", 2, Fix.INFIX),
		OR("||", 2, Fix.INFIX),
		IMPLIES("=>", 2, Fix.INFIX),
		NOT("!", 1, Fix.INFIX),
		ADD("+", 2, Fix.INFIX),
		SUB("-", 2, Fix.INFIX),
		MUL("*", 2, Fix.INFIX),
		DIV("/", 2, Fix.INFIX),
		MOD("%", 2, Fix.INFIX),
		NEG("-", 1, Fix.INFIX),
		BIT_AND("&", 2, Fix.INFIX),
		BIT_OR("|", 2, Fix.INFIX),
		BIT_XOR("^", 2, Fix.INFIX),
		BIT_NOT("~", 1, Fix.INFIX),
		SHIFTL("<<", 2, Fix.INFIX),
		SHIFTR(">>", 2, Fix.INFIX),
		SHIFTUR(">>>", 2, Fix.INFIX),
		BIT_CONCAT("BIT_CONCAT", 2, Fix.PREFIX),
		SIN("SIN", 1),
		COS("COS", 1),
		TAN("TAN", 1),
		ASIN("ASIN", 1),
		ACOS("ACOS", 1),
		ATAN("ATAN", 1),
		ATAN2("ATAN2", 2),
		ROUND("ROUND", 1),
		LOG("LOG", 1),
		EXP("EXP", 1),
		POWER("POWER", 1),
		SQRT("SQRT", 1),
		// String Operations
		SUBSTRING("SUBSTRING", 3, Fix.POSTFIX),
		CONCAT("CONCAT", 2, Fix.POSTFIX),
		TRIM("TRIM", 1, Fix.POSTFIX), 
		REPLACE("REPLACE", 3, Fix.POSTFIX),
		REPLACEFIRST("REPLACEFIRST", 3, Fix.POSTFIX),  
		TOLOWERCASE("TOLOWERCASE", 2, Fix.POSTFIX),
		TOUPPERCASE("TOUPPERCASE", 2, Fix.POSTFIX), 
		VALUEOF("VALUEOF", 2, Fix.POSTFIX),
		// String Comparators
		NOTCONTAINS("NOTCONTAINS", 2, Fix.POSTFIX),
		CONTAINS("CONTAINS", 2, Fix.POSTFIX),
		LASTINDEXOFCHAR("LASTINDEXOFCHAR", 3, Fix.POSTFIX),
		LASTINDEXOFSTRING("LASTINDEXOFSTRING", 3, Fix.POSTFIX),
		STARTSWITH("STARTSWITH", 3, Fix.POSTFIX),
		NOTSTARTSWITH("NOTSTARTSWITH", 3, Fix.POSTFIX),
		ENDSWITH("ENDSWITH", 2, Fix.POSTFIX),
		NOTENDSWITH("NOTENDSWITH", 2, Fix.POSTFIX),
		EQUALS("EQUALS", 2, Fix.POSTFIX),
		NOTEQUALS("NOTEQUALS", 2, Fix.POSTFIX),
		EQUALSIGNORECASE("EQUALSIGNORECASE", 2, Fix.POSTFIX),
		NOTEQUALSIGNORECASE("NOTEQUALSIGNORECASE", 2, Fix.POSTFIX),
		EMPTY("EMPTY", 1, Fix.POSTFIX),
		NOTEMPTY("NOTEMPTY", 1, Fix.POSTFIX),
		ISINTEGER("ISINTEGER", 1, Fix.POSTFIX),
		NOTINTEGER("NOTINTEGER", 1, Fix.POSTFIX),
		ISFLOAT("ISFLOAT", 1, Fix.POSTFIX),
		NOTFLOAT("NOTFLOAT", 1, Fix.POSTFIX),
		ISLONG("ISLONG", 1, Fix.POSTFIX),
		NOTLONG("NOTLONG", 1, Fix.POSTFIX),
		ISDOUBLE("ISDOUBLE", 1, Fix.POSTFIX),
		NOTDOUBLE("NOTDOUBLE", 1, Fix.POSTFIX),
		ISBOOLEAN("ISBOOLEAN", 1, Fix.POSTFIX),
		NOTBOOLEAN("NOTBOOLEAN", 1, Fix.POSTFIX),
		REGIONMATCHES("REGIONMATCHES", 6, Fix.POSTFIX),
		NOTREGIONMATCHES("NOTREGIONMATCHES", 6, Fix.POSTFIX);

		private final String string;

		private final int maxArity;

		private final Fix fix;

		Operator(String string, int maxArity) {
			this.string = string;
			this.maxArity = maxArity;
			fix = Fix.PREFIX;
		}

		Operator(String string, int maxArity, Fix fix) {
			this.string = string;
			this.maxArity = maxArity;
			this.fix = fix;
		}

		@Override
		public String toString() {
			return string;
		}

		public int getArity() {
			return maxArity;
		}

		public Fix getFix() {
			return fix;
		}

	}

	public static final IntConstant ZERO = new IntConstant(0);

	public static final IntConstant ONE = new IntConstant(1);

	public static final Expression FALSE = new Operation(Operation.Operator.EQ, ZERO, ONE);

	public static final Expression TRUE = new Operation(Operation.Operator.EQ, ZERO, ZERO);

	private final Operator operator;

	private final Expression[] operands;

	public Operation(final Operator operator, Expression... operands) {
		this.operator = operator;
		this.operands = operands;
	}

	public static Expression apply(Operator operator, Expression... operands) {
		switch (operator) {
		case ADD:
			int result = 0;
			for (Expression operand : operands) {
				if (!(operand instanceof IntConstant)) {
					return new Operation(operator, operands);
				} else {
					result += ((IntConstant) operand).getValue();
				}
			}
			return new IntConstant(result);
		default:
			return new Operation(operator, operands);
		}
	}

	public Operator getOperator() {
		return operator;
	}

	public int getOperatandCount() {
		return operands.length;
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
	public void accept(Visitor visitor) throws VisitorException {
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
		Fix fix = operator.getFix();
		if (arity == 2 && fix == Fix.INFIX) {
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
		} else if (arity == 1 && fix == Fix.INFIX) {
			sb.append(operator.toString());
			if ((operands[0] instanceof Constant) || (operands[0] instanceof Variable)) {
				sb.append(operands[0].toString());
			} else {
				sb.append('(');
				sb.append(operands[0].toString());
				sb.append(')');
			}
		} else if (fix == Fix.POSTFIX) {
			sb.append(operands[0].toString());
			sb.append('.');
			sb.append(operator.toString());
			sb.append('(');
			if (operands.length > 1) {
				sb.append(operands[1].toString());
				for (int i = 2; i < operands.length; i++) {
					sb.append(',');
					sb.append(operands[i].toString());
				}
			}
			sb.append(')');
		} else if (operands.length > 0) {
			sb.append(operator.toString());
			sb.append('(');
			sb.append(operands[0].toString());
			for (int i = 1; i < operands.length; i++) {
				sb.append(',');
				sb.append(operands[i].toString());
			}
			sb.append(')');
		} else {
			sb.append(operator.toString());
		}
		return sb.toString();
	}

}
