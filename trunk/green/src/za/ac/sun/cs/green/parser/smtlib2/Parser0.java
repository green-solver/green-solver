package za.ac.sun.cs.green.parser.smtlib2;

import java.util.logging.Logger;

import za.ac.sun.cs.green.util.NullLogger;

public class Parser0 {

	private final Scanner0 scanner;

	private final Logger log;

	public Parser0(Scanner0 scanner, Logger log) {
		this.log = log;
		this.scanner = scanner;
	}

	public Parser0(Scanner0 scanner) {
		this(scanner, new NullLogger());
	}
	
	public void parse() throws ParseException {
		log.entering("", "parse");
		while (true) {
			if (scanner.next() == Token0.LPAREN) {
				parseCommand();
			} else {
				break;
			}
		}
		log.exiting("", "parse");
	}

	private void parseCommand() throws ParseException, ParseException {
		log.entering("", "parseCommand");
		scanner.expect(Token0.LPAREN);
		if (scanner.next() == Token0.SET_LOGIC) {
			parseSetLogic();
		} else if (scanner.next() == Token0.SET_OPTION) {
			parseSetOption();
		} else if (scanner.next() == Token0.SET_INFO) {
			parseSetInfo();
		} else if (scanner.next() == Token0.DECLARE_SORT) {
			parseDeclareSort();
		} else if (scanner.next() == Token0.DEFINE_SORT) {
			parseDefineSort();
		} else if (scanner.next() == Token0.DECLARE_FUN) {
			parseDeclareFun();
		} else if (scanner.next() == Token0.DEFINE_FUN) {
			parseDefineFun();
		} else if (scanner.next() == Token0.PUSH) {
			parsePush();
		} else if (scanner.next() == Token0.POP) {
			parsePop();
		} else if (scanner.next() == Token0.ASSERT) {
			parseAssert();
		} else if (scanner.next() == Token0.CHECK_SAT) {
			parseCheckSat();
		} else if (scanner.next() == Token0.GET_ASSERTIONS) {
			parseGetAssertions();
		} else if (scanner.next() == Token0.GET_PROOF) {
			parseGetProof();
		} else if (scanner.next() == Token0.GET_UNSAT_CORE) {
			parseGetUnsatCore();
		} else if (scanner.next() == Token0.GET_VALUE) {
			parseGetValue();
		} else if (scanner.next() == Token0.GET_ASSIGNMENT) {
			parseGetAssignment();
		} else if (scanner.next() == Token0.GET_OPTION) {
			parseGetOption();
		} else if (scanner.next() == Token0.GET_INFO) {
			parseGetInfo();
		} else if (scanner.next() == Token0.EXIT) {
			parseExit();
		} else {
			throw new ParseException("unexpected token: " + scanner.next());
		}
		scanner.expect(Token0.RPAREN);
		log.exiting("", "parseCommand");
	}

	/**
	 * Parse a "set-logic" command.
	 * 
	 * <pre>
	 * set_logic ::= "set-logic" symbol.
	 * </pre>
	 * 
	 * @throws ParseException
	 */
	private void parseSetLogic() throws ParseException {
		log.entering("", "parseSetLogic");
		scanner.expect(Token0.SET_LOGIC);
		scanner.expect(Token0.SYMBOL);
		log.exiting("", "parseSetLogic");
	}

	/**
	 * Parse a "set-option" command.
	 * 
	 * <pre>
	 * set_option ::= "set-option" option.
	 * </pre>
	 * 
	 * @throws ParseException
	 */
	private void parseSetOption() throws ParseException {
		log.entering("", "parseSetOption");
		scanner.expect(Token0.SET_OPTION);
		parseOption();
		log.exiting("", "parseSetOption");
	}
	
	/**
	 * Parse "set-info <attribute>".
	 * 
	 * @throws ParseException
	 */
	private void parseSetInfo() throws ParseException {
		log.entering("", "parseSetInfo");
		scanner.expect(Token0.SET_INFO);
		parseAttribute();
		log.exiting("", "parseSetInfo");
	}

	/**
	 * Parse "declare-sort <symbol> <numeral>".
	 * 
	 * @throws ParseException
	 */
	private void parseDeclareSort() throws ParseException {
		log.entering("", "parseDeclareSort");
		scanner.expect(Token0.DECLARE_SORT);
		scanner.expect(Token0.SYMBOL);
		scanner.expect(Token0.NUMERAL);
		log.exiting("", "parseDeclareSort");
	}

	/**
	 * Parse "define-sort <symbol> ( <symbol>* ) <sort>".
	 * 
	 * @throws ParseException
	 */
	private void parseDefineSort() throws ParseException {
		log.entering("", "parseDefineSort");
		scanner.expect(Token0.DEFINE_SORT);
		scanner.expect(Token0.SYMBOL);
		scanner.expect(Token0.LPAREN);
		while (scanner.next() != Token0.RPAREN) {
			scanner.expect(Token0.SYMBOL);
		}
		scanner.expect(Token0.RPAREN);
		parseSort();
		log.exiting("", "parseDefineSort");
	}

	/**
	 * Parse "declare-fun <symbol> ( <sort>* ) <sort>".
	 * 
	 * @throws ParseException
	 */
	private void parseDeclareFun() throws ParseException {
		log.entering("", "parseDeclareFun");
		scanner.expect(Token0.DECLARE_FUN);
		scanner.expect(Token0.SYMBOL);
		scanner.expect(Token0.LPAREN);
		while (scanner.next() != Token0.RPAREN) {
			parseSort();
		}
		scanner.expect(Token0.RPAREN);
		parseSort();
		log.exiting("", "parseDeclareFun");
	}

	/**
	 * Parse "define-fun <symbol> ( <sorted var>* ) <sort> <term>".
	 * 
	 * @throws ParseException
	 */
	private void parseDefineFun() throws ParseException {
		log.entering("", "parseDefineFun");
		scanner.expect(Token0.DEFINE_FUN);
		scanner.expect(Token0.SYMBOL);
		scanner.expect(Token0.LPAREN);
		while (scanner.next() != Token0.RPAREN) {
			parseSortedVar();
		}
		scanner.expect(Token0.RPAREN);
		parseSort();
		parseTerm();
		log.exiting("", "parseDefineFun");
	}

	/**
	 * Parse "push <numeral>".
	 * 
	 * @throws ParseException
	 */
	private void parsePush() throws ParseException {
		log.entering("", "parsePush");
		scanner.expect(Token0.PUSH);
		scanner.expect(Token0.NUMERAL);
		log.exiting("", "parsePush");
	}

	/**
	 * Parse "pop <numeral>".
	 * 
	 * @throws ParseException
	 */
	private void parsePop() throws ParseException {
		log.entering("", "parsePop");
		scanner.expect(Token0.POP);
		scanner.expect(Token0.NUMERAL);
		log.exiting("", "parsePop");
	}

	/**
	 * Parse "assert <term>".
	 * 
	 * @throws ParseException
	 */
	private void parseAssert() throws ParseException {
		log.entering("", "parseAssert");
		scanner.expect(Token0.ASSERT);
		parseTerm();
		log.exiting("", "parseAssert");
	}

	/**
	 * Parse "check-sat".
	 * 
	 * @throws ParseException
	 */
	private void parseCheckSat() throws ParseException {
		log.entering("", "parseCheckSat");
		scanner.expect(Token0.CHECK_SAT);
		log.exiting("", "parseCheckSat");
	}

	/**
	 * Parse "get-assertions".
	 * 
	 * @throws ParseException
	 */
	private void parseGetAssertions() throws ParseException {
		log.entering("", "parseGetAssertions");
		scanner.expect(Token0.GET_ASSERTIONS);
		log.exiting("", "parseGetAssertions");
	}

	/**
	 * Parse "get-proof".
	 * 
	 * @throws ParseException
	 */
	private void parseGetProof() throws ParseException {
		log.entering("", "parseGetProof");
		scanner.expect(Token0.GET_PROOF);
		log.exiting("", "parseGetProof");
	}

	/**
	 * Parse "get-unsat-core".
	 * 
	 * @throws ParseException
	 */
	private void parseGetUnsatCore() throws ParseException {
		log.entering("", "parseGetUnsatCore");
		scanner.expect(Token0.GET_UNSAT_CORE);
		log.exiting("", "parseGetUnsatCore");
	}

	/**
	 * Parse "get-value ( <term>+ )".
	 * 
	 * @throws ParseException
	 */
	private void parseGetValue() throws ParseException {
		log.entering("", "parseGetValue");
		scanner.expect(Token0.GET_VALUE);
		scanner.expect(Token0.LPAREN);
		parseTerm();
		while (scanner.next() != Token0.RPAREN) {
			parseTerm();
		}
		scanner.expect(Token0.RPAREN);
		log.exiting("", "parseGetValue");
	}

	/**
	 * Parse "get-assignment".
	 * 
	 * @throws ParseException
	 */
	private void parseGetAssignment() throws ParseException {
		log.entering("", "parseGetAssignment");
		scanner.expect(Token0.GET_ASSIGNMENT);
		log.exiting("", "parseGetAssignment");
	}

	/**
	 * Parse "get-option <keyword>".
	 * 
	 * @throws ParseException
	 */
	private void parseGetOption() throws ParseException {
		log.entering("", "parseGetOption");
		scanner.expect(Token0.GET_OPTION);
		scanner.expect(Token0.KEYWORD);
		log.exiting("", "parseGetOption");
	}

	/**
	 * Parse "get-info <infoflag>".
	 * 
	 * @throws ParseException
	 */
	private void parseGetInfo() throws ParseException {
		log.entering("", "parseGetInfo");
		scanner.expect(Token0.GET_INFO);
		parseInfoFlag();
		log.exiting("", "parseGetInfo");
	}
	
	/**
	 * Parse "exit".
	 * 
	 * @throws ParseException
	 */
	private void parseExit() throws ParseException {
		log.entering("", "parseExit");
		scanner.expect(Token0.EXIT);
		log.exiting("", "parseExit");
	}

	/**
	 * Parse an option.
	 * 
	 * <pre>
	 * option ::= ":print-success" b_value
	 *          | ":expand-definitions" b_value
	 *          | ":interactive-mode" b_value
	 *          | ":produce_proofs" b_value
	 *          | ":produce_unsat_cores" b_value
	 *          | ":produce_models" b_value
	 *          | ":produce_assignments" b_value
	 *          | ":regular-output-channel" string
	 *          | ":diagnostic-output-channel" string
	 *          | ":random-seed" numeral
	 *          | ":verbosity" numeral
	 *          | attribute.
	 * </pre>
	 * 
	 * @throws ParseException
	 */
	private void parseOption() throws ParseException {
		log.entering("", "parseOption");
		if (scanner.next() == Token0.KEYWORD) {
			Keyword0 k = scanner.nextKeyword();
			if (k == Keyword0.PRINT_SUCCESS) {
				scanner.expect(Token0.KEYWORD);
				parseBValue();
			} else if (k == Keyword0.EXPAND_DEFINITIONS) {
				scanner.expect(Token0.KEYWORD);
				parseBValue();
			} else if (k == Keyword0.INTERACTIVE_MODE) {
				scanner.expect(Token0.KEYWORD);
				parseBValue();
			} else if (k == Keyword0.PRODUCE_PROOFS) {
				scanner.expect(Token0.KEYWORD);
				parseBValue();
			} else if (k == Keyword0.PRODUCE_UNSAT_CORES) {
				scanner.expect(Token0.KEYWORD);
				parseBValue();
			} else if (k == Keyword0.PRODUCE_MODELS) {
				scanner.expect(Token0.KEYWORD);
				parseBValue();
			} else if (k == Keyword0.PRODUCE_ASSIGNMENTS) {
				scanner.expect(Token0.KEYWORD);
				parseBValue();
			} else if (k == Keyword0.REGULAR_OUTPUT_CHANNEL) {
				scanner.expect(Token0.KEYWORD);
				scanner.expect(Token0.STRING);
			} else if (k == Keyword0.DIAGNOSTIC_OUTPUT_CHANNEL) {
				scanner.expect(Token0.KEYWORD);
				scanner.expect(Token0.STRING);
			} else if (k == Keyword0.RANDOM_SEED) {
				scanner.expect(Token0.KEYWORD);
				scanner.expect(Token0.NUMERAL);
			} else if (k == Keyword0.VERBOSITY) {
				scanner.expect(Token0.KEYWORD);
				scanner.expect(Token0.NUMERAL);
			} else {
				parseAttribute();
			}
		} else {
			scanner.expect(Token0.KEYWORD);
		}
		log.exiting("", "parseOption");
	}
	
	/**
	 * Parse an attribute.
	 * 
	 * <pre>
	 * attribute ::= keyword
	 *             | keyword attribute_value.
	 * attribute_value ::= spec_constant
	 *                   | symbol
	 *                   | "(" { s_expr } ")".
	 * </pre>
	 * 
	 * @throws ParseException
	 */
	private void parseAttribute() throws ParseException {
		log.entering("", "parseAttribute");
		scanner.expect(Token0.KEYWORD);
		Token0 t = scanner.next();
		if ((t == Token0.NUMERAL) || (t == Token0.DECIMAL) || (t == Token0.HEXADECIMAL) || (t == Token0.BINARY) || (t == Token0.STRING)) {
			parseSpecConstant();
		} else if (t == Token0.SYMBOL) {
			scanner.expect(Token0.SYMBOL);
		} else {
			scanner.expect(Token0.LPAREN);
			while (scanner.next() != Token0.RPAREN) {
				parseSExpr();
			}
			scanner.expect(Token0.RPAREN);
		}
		log.exiting("", "parseAttribute");
	}
	
	/**
	 * Parse an s-expression.
	 * 
	 * <pre>
	 * s_expr ::= spec_constant
	 *          | symbol
	 *          | keyword
	 *          | "(" { s_expr } ")".
	 * </pre>
	 * 
	 * @throws ParseException
	 */
	private void parseSExpr() throws ParseException {
		log.entering("", "parseSExpr");
		Token0 t = scanner.next();
		if ((t == Token0.NUMERAL) || (t == Token0.DECIMAL) || (t == Token0.HEXADECIMAL) || (t == Token0.BINARY) || (t == Token0.STRING)) {
			parseSpecConstant();
		} else if (t == Token0.SYMBOL) {
			scanner.expect(Token0.SYMBOL);
		} else if (t == Token0.KEYWORD) {
			scanner.expect(Token0.KEYWORD);
		} else {
			scanner.expect(Token0.LPAREN);
			while (scanner.next() != Token0.RPAREN) {
				parseSExpr();
			}
			scanner.expect(Token0.RPAREN);
		}		
		log.exiting("", "parseSExpr");
	}
	
	/**
	 * Parse a boolean value.
	 * 
	 * <pre>
	 * b_value ::= "true"
	 *           | "false".
	 * </pre>
	 * 
	 * @throws ParseException
	 */
	private void parseBValue() throws ParseException {
		log.entering("", "parseBValue");
		if (scanner.eat(Token0.TRUE)) {
		} else if (scanner.eat(Token0.FALSE)) {
		} else {
			throw new ParseException("unexpected token " + scanner.next());
		}
		log.exiting("", "parseBValue");
	}
	
	/**
	 * Parse an identifier.
	 * 
	 * <pre>
	 * identifier ::= symbol
	 *              | "(" "_" symbol numeral { numeral } ")".
	 * </pre>
	 * 
	 * @throws ParseException
	 */
	private void parseIdentifier() throws ParseException {
		log.entering("", "parseIdentifier");
		if (scanner.next() == Token0.SYMBOL) {
			scanner.expect(Token0.SYMBOL);
		} else {
			scanner.expect(Token0.LPAREN);
			scanner.expect(Token0.UNDERSCORE);
			scanner.expect(Token0.SYMBOL);
			scanner.expect(Token0.NUMERAL);
			while (scanner.next() == Token0.NUMERAL) {
				scanner.expect(Token0.NUMERAL);
			}
			scanner.expect(Token0.RPAREN);
		}
		log.exiting("", "parseIdentifier");
	}
	
	/**
	 * Parse a sort.
	 * 
	 * <pre>
	 * sort ::= identifier
	 *        | "(" identifier sort { sort } ")".
	 * identifier ::= symbol
	 *              | "(" "_" symbol numeral { numeral } ")".
	 * </pre>
	 *
	 * @throws ParseException
	 */
	private void parseSort() throws ParseException {
		log.entering("", "parseSort");
		if (scanner.next() == Token0.SYMBOL) {
			scanner.expect(Token0.SYMBOL);
		} else {
			scanner.expect(Token0.LPAREN);
			if (scanner.eat(Token0.UNDERSCORE)) {
				scanner.expect(Token0.SYMBOL);
				scanner.expect(Token0.NUMERAL);
				while (scanner.next() == Token0.NUMERAL) {
					scanner.expect(Token0.NUMERAL);
				}
			} else {
				parseIdentifier();
				parseSort();
				while ((scanner.next() == Token0.LPAREN) || (scanner.next() == Token0.SYMBOL)) {
					parseSort();
				}
			}
			scanner.expect(Token0.RPAREN);
		}
		log.exiting("", "parseSort");
	}
	
	/**
	 * Parse a sorted variable.
	 * 
	 * <pre>
	 * sorted_var ::= "(" symbol sort ")".
	 * </pre>
	 * 
	 * @throws ParseException
	 */
	private void parseSortedVar() throws ParseException {
		log.entering("", "parseSortedVar");
		scanner.expect(Token0.LPAREN);
		scanner.expect(Token0.SYMBOL);
		parseSort();
		scanner.expect(Token0.RPAREN);
		log.exiting("", "parseSortedVar");
	}
	
	/**
	 * Parse a term.
	 * 
	 * <pre>
	 * term ::= spec_constant
	 *        | qual_identifier
	 *        | "(" qual_identifier term { term } ")"
	 *        | "(" "let" "(" var_binding { var_binding } ")" term ")"
	 *        | "(" "forall" "(" sorted_var { sorted_var } ")" term ")"
	 *        | "(" "exists" "(" sorted_var { sorted_var } ")" term ")"
	 *        | "(" "!" term attribute { attribute } ")".
	 * </pre>
	 * 
	 * We have to inline the definition of <code>qual_identifier</code> because
	 * it may start with a "(".
	 * 
	 * <pre>
	 * qual_identifier ::= identifier
	 *                   | "(" "as" identifier sort ")".
	 * identifier ::= symbol
	 *              | "(" "_" symbol numeral { numeral } ")".
	 * </pre>
	 * 
	 * @throws ParseException
	 */
	private void parseTerm() throws ParseException {
		log.entering("", "parseTerm");
		if (scanner.next() == Token0.LPAREN) {
			scanner.expect(Token0.LPAREN);
			if (scanner.eat(Token0.AS)) {
				// one part of qual_identifier
				parseIdentifier();
				parseSort();
			} else if (scanner.eat(Token0.LET)) {
				scanner.expect(Token0.LPAREN);
				parseVarBinding();
				while (scanner.next() == Token0.LPAREN) {
					parseVarBinding();
				}
				scanner.expect(Token0.RPAREN);
				parseTerm();
			} else if (scanner.eat(Token0.FORALL)) {
				scanner.expect(Token0.LPAREN);
				parseSortedVar();
				while (scanner.next() == Token0.LPAREN) {
					parseSortedVar();
				}
				scanner.expect(Token0.RPAREN);
				parseTerm();
			} else if (scanner.eat(Token0.EXISTS)) {
				scanner.expect(Token0.LPAREN);
				parseSortedVar();
				while (scanner.next() == Token0.LPAREN) {
					parseSortedVar();
				}
				scanner.expect(Token0.RPAREN);
				parseTerm();
			} else if (scanner.eat(Token0.NOT)) {
				parseTerm();
				parseAttribute();
				while (scanner.next() == Token0.KEYWORD) {
					parseAttribute();
				}
			} else if (scanner.eat(Token0.UNDERSCORE)) {
				scanner.expect(Token0.SYMBOL);
				scanner.expect(Token0.NUMERAL);
				while (scanner.next() == Token0.NUMERAL) {
					scanner.expect(Token0.NUMERAL);
				}
			} else {
				parseQualIdentifier();
				parseTerm();
				while (scanner.next() != Token0.RPAREN) {
					parseTerm();
				}
			}
			scanner.expect(Token0.RPAREN);
		} else if (scanner.next() == Token0.SYMBOL) {
			// one part of qual_identifier
			scanner.expect(Token0.SYMBOL);
		} else {
			parseSpecConstant();
		}
		log.exiting("", "parseTerm");
	}

	/**
	 * Parse a qualified identifier.
	 * 
	 * <pre>
	 * qual_identifier ::= identifier
	 *                   | "(" "as" identifier sort ")".
	 * identifier ::= symbol
	 *              | "(" "_" symbol numeral { numeral } ")".
	 * </pre>
	 * 
	 * @throws ParseException
	 */
	private void parseQualIdentifier() throws ParseException {
		log.entering("", "parseQualIdentifier");
		if (scanner.next() == Token0.SYMBOL) {
			scanner.expect(Token0.SYMBOL);
		} else {
			scanner.expect(Token0.LPAREN);
			if (scanner.eat(Token0.UNDERSCORE)) {
				scanner.expect(Token0.SYMBOL);
				scanner.expect(Token0.NUMERAL);
				while (scanner.next() == Token0.NUMERAL) {
					scanner.expect(Token0.NUMERAL);
				}
			} else {
				scanner.expect(Token0.AS);
				scanner.expect(Token0.SYMBOL);
				parseSort();
			}
			scanner.expect(Token0.RPAREN);
		}
		log.exiting("", "parseQualIdentifier");
	}
	
	/**
	 * Parse a variable binding.
	 * 
	 * <pre>
	 * var_binding ::= "(" symbol term ")".
	 * </pre>
	 * 
	 * @throws ParseException
	 */
	private void parseVarBinding() throws ParseException {
		log.entering("", "parseVarBinding");
		scanner.expect(Token0.LPAREN);
		scanner.expect(Token0.SYMBOL);
		parseTerm();
		scanner.expect(Token0.RPAREN);
		log.exiting("", "parseVarBinding");
	}
	
	/**
	 * Parse a special constant.
	 * 
	 * <pre>
	 * spec_constant ::= numeral
	 *                 | decimal
	 *                 | hexadecimal
	 *                 | binary
	 *                 | string
	 *                 | b_value.
	 * </pre>
	 * 
	 * @throws ParseException
	 */
	private void parseSpecConstant() throws ParseException {
		log.entering("", "parseSpecConstant");
		if (scanner.next() == Token0.NUMERAL) {
			scanner.expect(Token0.NUMERAL);
		} else if (scanner.next() == Token0.DECIMAL) {
			scanner.expect(Token0.DECIMAL);
		} else if (scanner.next() == Token0.HEXADECIMAL) {
			scanner.expect(Token0.HEXADECIMAL);
		} else if (scanner.next() == Token0.BINARY) {
			scanner.expect(Token0.BINARY);
		} else if (scanner.next() == Token0.STRING) {
			scanner.expect(Token0.STRING);
		} else if (scanner.next() == Token0.TRUE) {
			scanner.expect(Token0.TRUE);
		} else if (scanner.next() == Token0.FALSE) {
			scanner.expect(Token0.FALSE);
		} else {
			throw new ParseException("expected a special constant");
		}
		log.exiting("", "parseSpecConstant");
	}
	
	/**
	 * Parse an information flag.
	 * 
	 * <pre>
	 * info_flag ::= ":error-behavior"
	 *             | ":name"
	 *             | ":authors"
	 *             | ":version"
	 *             | ":status"
	 *             | ":reason-unknown"
	 *             | ":all-statistics"
	 *             | keyword.
	 * </pre>
	 * 
	 * @throws ParseException
	 */
	private void parseInfoFlag() throws ParseException {
		log.entering("", "parseInfoFlag");
		Keyword0 k = scanner.expectKeyword();
		if (k == Keyword0.ERROR_BEHAVIOR) {
			// ???
		} else if (k == Keyword0.NAME) {
			// ???
		} else if (k == Keyword0.AUTHORS) {
			// ???
		} else if (k == Keyword0.VERSION) {
			// ???
		} else if (k == Keyword0.STATUS) {
			// ???
		} else if (k == Keyword0.REASON_UNKNOWN) {
			// ???
		} else if (k == Keyword0.ALL_STATISTICS) {
			// ???
		} else {
			scanner.expect(Token0.KEYWORD);
		}
		log.exiting("", "parseInfoFlag");
	}
	
}
