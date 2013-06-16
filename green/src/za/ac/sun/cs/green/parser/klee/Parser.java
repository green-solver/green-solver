package za.ac.sun.cs.green.parser.klee;

public class Parser {

	private final Scanner scanner;

	// private final Map<String, Expression> labelMap;

	public Parser(Scanner scanner) {
		this.scanner = scanner;
		// labelMap = new HashMap<String, Expression>();
	}

	public void parse() throws ParseException {
		while (true) {
			if (scanner.next() == Token.ARRAY) {
				parseArray();
			} else if (scanner.next() == Token.LPAREN) {
				parseQuery();
			} else {
				break;
			}
		}
	}

	private void parseNumber() throws ParseException {
		if (scanner.eat(Token.TRUE)) {
			// do something with it
		} else if (scanner.eat(Token.FALSE)) {
			// do something with it
		} else {
			// int sign = 1;
			if (scanner.eat(Token.PLUS)) {
				// sign = 1;
			} else if (scanner.eat(Token.MINUS)) {
				// sign = -1;
			}
			scanner.expectInt();
		}
	}

	private void parseArray() throws ParseException {
		scanner.expect(Token.ARRAY);
		scanner.expectId(); // String name = scanner.expectId();
		scanner.expect(Token.LBRACKET);
		if (scanner.next() != Token.RBRACKET) {
			parseNumber();
		}
		scanner.expect(Token.RBRACKET);
		scanner.expect(Token.COLON);
		scanner.expectType(); // int domain = scanner.expectType();
		scanner.expect(Token.ARROW);
		scanner.expectType(); // int range = scanner.expectType();
		scanner.expect(Token.EQUALS);
		if (scanner.eat(Token.SYMBOLIC)) {
			// record it
		} else {
			scanner.expect(Token.LBRACKET);
			parseNumber();
			while (scanner.eat(Token.COMMA)) {
				parseNumber();
			}
			scanner.expect(Token.RBRACKET);
		}
	}

	private void parseQuery() throws ParseException {
		scanner.expect(Token.LPAREN);
		scanner.expect(Token.QUERY);
		// constraint-list
		scanner.expect(Token.LBRACKET);
		while (scanner.next() != Token.RBRACKET) {
			parseExpression();
		}
		scanner.expect(Token.RBRACKET);
		// query-expression
		parseExpression();
		// eval-expr-list
		scanner.expect(Token.LBRACKET);
		while (scanner.next() != Token.RBRACKET) {
			parseExpression();
		}
		scanner.expect(Token.RBRACKET);
		// eval-array-list
		scanner.expect(Token.LBRACKET);
		while (scanner.next() != Token.RBRACKET) {
			scanner.expectId();
		}
		scanner.expect(Token.RBRACKET);
		scanner.expect(Token.RPAREN);
	}

	private void parseExpression() throws ParseException {
		if (scanner.next() == Token.ID) {
			scanner.expectId();
		} else if (scanner.next() == Token.PLUS) {
			parseNumber();
		} else if (scanner.next() == Token.MINUS) {
			parseNumber();
		} else if (scanner.next() == Token.INT) {
			parseNumber();
		} else {
			scanner.expect(Token.LPAREN);
			Token t = scanner.next();
			switch (t) {
			case TYPE:
				scanner.expectType();
				parseNumber();
				break;
			case ZEXT:
			case SEXT:
				scanner.expectType();
				parseExpression();
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
			case LSHL:
			case ASHL:
				scanner.expectType();
				parseExpression();
				parseExpression();
				break;
			case NOT:
			case NEG:
				if (scanner.next() == Token.TYPE) {
					scanner.expectType();
				}
				parseExpression();
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
					scanner.expectType();
				}
				parseExpression();
				parseExpression();
				break;
			case EXTRACT:
				if (scanner.next() == Token.TYPE) {
					scanner.expectType();
				}
				parseNumber();
				parseExpression();
				break;
			case READ:
			case READLSB:
			case READMSB:
				scanner.expectType();
				parseExpression();
				parseVersion();
				break;
			case SELECT:
				scanner.expectType();
				parseExpression();
				parseExpression();
				parseExpression();
				break;
			default:
			}
			scanner.expect(Token.RPAREN);
		}
	}

	private void parseVersion() throws ParseException {
		if (scanner.next() == Token.ID) {
			scanner.expectId();
		} else {
			scanner.expect(Token.LBRACKET);
			parseExpression();
			scanner.expect(Token.EQUALS);
			parseExpression();
			while (scanner.eat(Token.COMMA)) {
				parseExpression();
				scanner.expect(Token.EQUALS);
				parseExpression();
			}
			scanner.expect(Token.RBRACKET);
			scanner.expect(Token.AT);
			parseVersion();
		}
	}
}
