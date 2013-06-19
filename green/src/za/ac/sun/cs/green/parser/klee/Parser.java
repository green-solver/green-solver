package za.ac.sun.cs.green.parser.klee;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.expr.Operation.Operator;

public class Parser {

	private final Scanner scanner;

	private final Map<String, KArray> arrays;

	private final Map<String, KExpression> labels;

	private final List<KQuery> queries;

	public Parser(Scanner scanner) {
		this.scanner = scanner;
		arrays = new HashMap<String, KArray>();
		labels = new HashMap<String, KExpression>();
		queries = new LinkedList<KQuery>();
	}

	public Expression getExpression() {
		if (queries.size() == 0) {
			return null;
		}
		KQuery query = queries.iterator().next();
		List<Expression> exprs = new LinkedList<Expression>();
		for (KExpression expr : query.getConstraintList()) {
			exprs.add(getExpression(expr));
		}
		exprs.add(getExpression(query.getQueryExpression()));
		if (exprs.size() == 0) {
			return Operation.TRUE;
		}
		Expression result = null;
		for (Expression e : exprs) {
			if (result == null) {
				result = e;
			} else {
				result = new Operation(Operator.AND, result, e);
			}
		}
		return result;
	}

	private Expression getExpression(KExpression expr) {
		if (expr instanceof KNumber) {
			return new IntConstant((int) ((KNumber) expr).getValue());
		} else {
			KOperation op = (KOperation) expr;
			Token oper = op.getOperator();
			Expression l = null, r = null;
			switch (oper) {
			case ADD:
				l = getExpression(op.getLeft());
				r = getExpression(op.getRight());
				return new Operation(Operator.ADD, l, r);
			case AND:
				l = getExpression(op.getLeft());
				r = getExpression(op.getRight());
				return new Operation(Operator.BIT_AND, l, r);
			case ASHR:
				l = getExpression(op.getLeft());
				r = getExpression(op.getRight());
				return new Operation(Operator.SHIFTUR, l, r);
			case CONCAT:
				l = getExpression(op.getLeft());
				r = getExpression(op.getRight());
				return new Operation(Operator.BIT_CONCAT, l, r);
			case EQ:
				l = getExpression(op.getLeft());
				r = getExpression(op.getRight());
				return new Operation(Operator.EQ, l, r);
			case EXTRACT:
				// TODO
			case LSHR:
				l = getExpression(op.getLeft());
				r = getExpression(op.getRight());
				return new Operation(Operator.SHIFTR, l, r);
			case MUL:
				l = getExpression(op.getLeft());
				r = getExpression(op.getRight());
				return new Operation(Operator.MUL, l, r);
			case NE:
				l = getExpression(op.getLeft());
				r = getExpression(op.getRight());
				return new Operation(Operator.NE, l, r);
			case NEG:
				l = getExpression(op.getLeft());
				return new Operation(Operator.NEG, l);
			case NOT:
				l = getExpression(op.getLeft());
				return new Operation(Operator.NOT, l);
			case OR:
				l = getExpression(op.getLeft());
				r = getExpression(op.getRight());
				return new Operation(Operator.BIT_OR, l, r);
			case READ:
			case READLSB:
			case READMSB:
				// TODO
			case SDIV:
			case UDIV:
				l = getExpression(op.getLeft());
				r = getExpression(op.getRight());
				return new Operation(Operator.DIV, l, r);
			case SELECT:
			case SEXT:
				// equate to new symbolic variable
				// add
			case SGE:
			case UGE:
				l = getExpression(op.getLeft());
				r = getExpression(op.getRight());
				return new Operation(Operator.GE, l, r);
			case SGT:
			case UGT:
				l = getExpression(op.getLeft());
				r = getExpression(op.getRight());
				return new Operation(Operator.GT, l, r);
			case SLE:
			case ULE:
				l = getExpression(op.getLeft());
				r = getExpression(op.getRight());
				return new Operation(Operator.LE, l, r);
			case SLT:
			case ULT:
				l = getExpression(op.getLeft());
				r = getExpression(op.getRight());
				return new Operation(Operator.LT, l, r);
			case SHL:
				l = getExpression(op.getLeft());
				r = getExpression(op.getRight());
				return new Operation(Operator.SHIFTL, l, r);
			case SREM:
			case UREM:
				l = getExpression(op.getLeft());
				r = getExpression(op.getRight());
				return new Operation(Operator.MOD, l, r);
			case SUB:
				l = getExpression(op.getLeft());
				r = getExpression(op.getRight());
				return new Operation(Operator.SUB, l, r);
			case XOR:
				l = getExpression(op.getLeft());
				r = getExpression(op.getRight());
				return new Operation(Operator.BIT_XOR, l, r);
			case ZEXT:
				// TODO
			default:
				return null;
			}
		}
	}

	public void parse() throws ParseException {
		while (true) {
			if (scanner.next() == Token.ARRAY) {
				parseArray();
			} else if (scanner.next() == Token.LPAREN) {
				queries.add(parseQuery());
			} else {
				break;
			}
		}
	}

	private KNumber parseNumber() throws ParseException {
		if (scanner.eat(Token.TRUE)) {
			return new KNumber(1, 1);
		} else if (scanner.eat(Token.FALSE)) {
			return new KNumber(1, 0);
		} else {
			int sign = 1;
			if (scanner.eat(Token.PLUS)) {
				sign = 1;
			} else if (scanner.eat(Token.MINUS)) {
				sign = -1;
			}
			return new KNumber(-1, sign * scanner.expectInt());
		}
	}

	private void parseArray() throws ParseException {
		scanner.expect(Token.ARRAY);
		String name = scanner.expectId();
		int size = -1;
		scanner.expect(Token.LBRACKET);
		if (scanner.next() != Token.RBRACKET) {
			size = (int) parseNumber().getValue();
		}
		scanner.expect(Token.RBRACKET);
		scanner.expect(Token.COLON);
		int domain = scanner.expectType();
		scanner.expect(Token.ARROW);
		int range = scanner.expectType();
		scanner.expect(Token.EQUALS);
		if (scanner.eat(Token.SYMBOLIC)) {
			arrays.put(name, new KSymbolicArray(size, domain, range));
		} else {
			KConcreteArray a = new KConcreteArray(size, domain, range);
			arrays.put(name, a);
			scanner.expect(Token.LBRACKET);
			a.addValue(parseNumber());
			while (scanner.eat(Token.COMMA)) {
				a.addValue(parseNumber());
			}
			scanner.expect(Token.RBRACKET);
		}
	}

	private KQuery parseQuery() throws ParseException {
		KQuery q = new KQuery();
		scanner.expect(Token.LPAREN);
		scanner.expect(Token.QUERY);
		// constraint-list
		scanner.expect(Token.LBRACKET);
		while (scanner.next() != Token.RBRACKET) {
			q.addConstraint(parseExpression());
		}
		scanner.expect(Token.RBRACKET);
		// query-expression
		q.setQueryExpression(parseExpression());
		// eval-expr-list
		scanner.expect(Token.LBRACKET);
		while (scanner.next() != Token.RBRACKET) {
			q.addEvalExpr(parseExpression());
		}
		scanner.expect(Token.RBRACKET);
		// eval-array-list
		scanner.expect(Token.LBRACKET);
		while (scanner.next() != Token.RBRACKET) {
			q.addEvalArray(scanner.expectId());
		}
		scanner.expect(Token.RBRACKET);
		scanner.expect(Token.RPAREN);
		return q;
	}

	private KExpression parseExpression() throws ParseException {
		KExpression e = null;
		if (scanner.next() == Token.ID) {
			String id = scanner.expectId();
			if (labels.containsKey(id)) {
				e = labels.get(id);
			} else {
				scanner.expect(Token.COLON);
				e = parseExpression();
				if (labels.containsKey(id)) {
					throw new ParseException("redefinition of \"" + id + "\"");
				}
				labels.put(id, e);
			}
		} else if (scanner.next() == Token.PLUS) {
			e = parseNumber();
		} else if (scanner.next() == Token.MINUS) {
			e = parseNumber();
		} else if (scanner.next() == Token.INT) {
			e = parseNumber();
		} else {
			scanner.expect(Token.LPAREN);
			int tp;
			KExpression f = null, g = null, h = null;
			KNumber n = null;
			KVersion v = null;
			Token t = scanner.next();
			switch (t) {
			case TYPE:
				e = new KNumber(scanner.expectType(), parseNumber().getValue());
				break;
			case ZEXT:
			case SEXT:
				e = new KOperation(scanner.expectType(), t, parseExpression());
				break;
			case ADD:
			case SUB:
			case MUL:
			case UDIV:
			case UREM:
			case SDIV:
			case SREM:
			case AND:
			case OR:
			case XOR:
			case SHL:
			case LSHR:
			case ASHR:
				e = new KOperation(scanner.expectType(), t, parseExpression(), parseExpression());
				break;
			case NOT:
			case NEG:
				if (scanner.next() == Token.TYPE) {
					e = new KOperation(scanner.expectType(), t, parseExpression());
				} else {
					f = parseExpression();
					e = new KOperation(f.getType(), t, f);
				}
				break;
			case EQ:
			case NE:
			case ULT:
			case ULE:
			case UGT:
			case UGE:
			case SLT:
			case SLE:
			case SGT:
			case SGE:
			case CONCAT:
				if (scanner.next() == Token.TYPE) {
					tp = scanner.expectType();
					f = parseExpression();
					g = parseExpression();
					// TODO check that the operations have a compatible type
					e = new KOperation(tp, t, f, g);
				} else {
					f = parseExpression();
					g = parseExpression();
					if (f.getType() != g.getType()) {
						throw new ParseException("uncompatiable types");
					}
					e = new KOperation(f.getType(), t, f, g);
				}
				break;
			case EXTRACT:
				tp = scanner.expectType();
				n = parseNumber();
				f = parseExpression();
				// TODO Any check?
				e = new KOperation(tp, t, n, f);
				break;
			case READ:
			case READLSB:
			case READMSB:
				tp = scanner.expectType();
				f = parseExpression();
				v = parseVersion();
				e = new KOperation(tp, t, f, v);
				break;
			case SELECT:
				tp = scanner.expectType();
				f = parseExpression();
				g = parseExpression();
				h = parseExpression();
				// TODO check that all three operations have a compatible type
				e = new KOperation(tp, t, f, g, h);
				break;
			default:
				throw new ParseException("unexpected token " + t);
			}
			scanner.expect(Token.RPAREN);
		}
		return e;
	}

	private KVersion parseVersion() throws ParseException {
		if (scanner.next() == Token.ID) {
			String n = scanner.expectId();
			KArray a = arrays.get(n);
			if (a == null) {
				throw new ParseException("unknown array \"" + n + "\"");
			}
			return new KArrayVersion(a);
		} else {
			scanner.expect(Token.LBRACKET);
			return parseRecursiveVersion();
		}
	}

	private KVersion parseRecursiveVersion() throws ParseException {
		KExpression lhs = parseExpression();
		scanner.expect(Token.EQUALS);
		KExpression rhs = parseExpression();
		if (scanner.eat(Token.COMMA)) {
			return new KUpdateVersion(lhs, rhs, parseRecursiveVersion());
		} else {
			scanner.expect(Token.RBRACKET);
			scanner.expect(Token.AT);
			return new KUpdateVersion(lhs, rhs, parseVersion());
		}
	}

	public static class KArray {

		protected final int size;

		protected final int domain;

		protected final int range;

		public KArray(final int size, final int domain, final int range) {
			this.size = size;
			this.domain = domain;
			this.range = range;
		}

	}

	public static class KConcreteArray extends KArray {

		private final List<KNumber> values;

		public KConcreteArray(final int size, final int domain, final int range) {
			super(size, domain, range);
			values = new ArrayList<KNumber>();
		}

		public void addValue(KNumber value) {
			if ((size == -1) || (values.size() < size)) {
				values.add(value);
			}
		}
	}

	public static class KSymbolicArray extends KArray {

		public KSymbolicArray(final int size, final int domain, final int range) {
			super(size, domain, range);
		}

	}

	public static class KExpression {

		protected final int type;

		public KExpression(final int type) {
			this.type = type;
		}

		public int getType() {
			return type;
		}

	}

	public static class KNumber extends KExpression {

		protected final long value;

		public KNumber(final int type, final long value) {
			super(type);
			this.value = value;
		}

		public long getValue() {
			return value;
		}

	}

	public static class KOperation extends KExpression {

		protected final Token operator;

		protected final KExpression left;

		protected final KExpression right;

		protected final KExpression extra; // stores number for EXTRACT

		protected final KVersion version;

		public KOperation(final int type, final Token operator, final KExpression left) {
			super(type);
			this.operator = operator;
			this.left = left;
			this.right = null;
			this.extra = null;
			this.version = null;
		}

		public Token getOperator() {
			return operator;
		}

		public KExpression getLeft() {
			return left;
		}

		public KExpression getRight() {
			return right;
		}

		public KExpression getExtra() {
			return extra;
		}

		public KOperation(final int type, final Token operator, final KExpression left, final KExpression right) {
			super(type);
			this.operator = operator;
			this.left = left;
			this.right = right;
			this.extra = null;
			this.version = null;
		}

		public KOperation(final int type, final Token operator, final KExpression left, final KExpression right, final KExpression extra) {
			super(type);
			this.operator = operator;
			this.left = left;
			this.right = right;
			this.extra = extra;
			this.version = null;
		}

		public KOperation(final int type, final Token operator, final KNumber number, final KExpression left) {
			super(type);
			this.operator = operator;
			this.left = left;
			this.right = null;
			this.extra = number;
			this.version = null;
		}

		public KOperation(final int type, final Token operator, final KExpression left, final KVersion version) {
			super(type);
			this.operator = operator;
			this.left = left;
			this.right = null;
			this.extra = null;
			this.version = version;
		}

	}

	public static class KVersion {

	}

	public static class KArrayVersion extends KVersion {

		protected final KArray array;

		public KArrayVersion(final KArray array) {
			this.array = array;
		}

	}

	public static class KUpdateVersion extends KVersion {

		protected final KExpression lhs;

		protected final KExpression rhs;

		protected final KVersion version;

		public KUpdateVersion(final KExpression lhs, final KExpression rhs, final KVersion version) {
			this.lhs = lhs;
			this.rhs = rhs;
			this.version = version;
		}

	}

	public static class KQuery {

		protected KExpression queryExpression;

		protected final List<KExpression> constraintList;

		protected final List<KExpression> evalExprList;

		protected final List<String> evalArrayList;

		public KQuery() {
			this.queryExpression = null;
			this.constraintList = new LinkedList<KExpression>();
			this.evalExprList = new LinkedList<KExpression>();
			this.evalArrayList = new LinkedList<String>();
		}

		public KExpression getQueryExpression() {
			return queryExpression;
		}

		public void setQueryExpression(final KExpression queryExpression) {
			this.queryExpression = queryExpression;
		}

		public Iterable<KExpression> getConstraintList() {
			return new Iterable<KExpression>() {
				@Override
				public Iterator<KExpression> iterator() {
					return new Iterator<KExpression>() {
						private final Iterator<KExpression> iter = constraintList.iterator();

						@Override
						public boolean hasNext() {
							return iter.hasNext();
						}

						@Override
						public KExpression next() {
							return iter.next();
						}

						@Override
						public void remove() {
							throw new UnsupportedOperationException();
						}
					};
				}
			};
		}

		public void addConstraint(final KExpression constraint) {
			constraintList.add(constraint);
		}

		public void addEvalExpr(final KExpression evalExpr) {
			evalExprList.add(evalExpr);
		}

		public void addEvalArray(final String evalArray) {
			evalArrayList.add(evalArray);
		}

	}

}
