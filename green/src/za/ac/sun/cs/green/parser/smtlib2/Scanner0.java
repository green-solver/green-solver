package za.ac.sun.cs.green.parser.smtlib2;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.logging.Logger;

import za.ac.sun.cs.green.util.NullLogger;

public class Scanner0 {

	/**
	 * The next token in the token stream.
	 */
	private Token0 token = Token0.UNKNOWN;

	/**
	 * The character to process.
	 */
	private int nextCh = ' ';

	/**
	 * If the next token is an integer literal, this field contains its value.
	 */
	private int intValue = -1;

	/**
	 * If the next token is a real literal, this field contains its value.
	 */
	private double realValue = -1;
	
	/**
	 * If the next token is a string literal or a keyword, this field contains its value.
	 */
	private String stringValue = null;
	
	/**
	 * If the next token is a predefined keyword, this fields contains its value.
	 */
	private Keyword0 keyword = Keyword0.UNPREDEFINED;
	
	/**
	 * The reader where characters are read from.
	 */
	private Reader reader = null;

	/**
	 * For logging debugging messages.
	 */
	private final Logger log;

	public Scanner0(Reader reader, Logger log) throws ParseException {
		this.log = log;
		this.reader = reader;
		token = Token0.UNKNOWN;
		nextCh = ' ';
		scanNextToken();
	}

	public Scanner0(Reader reader) throws ParseException {
		this(reader, new NullLogger());
	}
	
	public Scanner0(String query, Logger log) throws ParseException {
		this.log = log;
		reader = new StringReader(query);
		token = Token0.UNKNOWN;
		nextCh = ' ';
		scanNextToken();
	}
	
	public Scanner0(String query) throws ParseException {
		this(query, new NullLogger());
	}

	/**
	 * Returns the next token in the token stream.
	 * 
	 * @return the next token
	 */
	public Token0 next() {
		return token;
	}

	public void expect(Token0 token) throws ParseException {
		if (next() != token) {
			throw new ParseException("Expected " + token + " but found " + next());
		}
		scanNextToken();
	}

	public int expectNumeral() throws ParseException {
		int value = intValue;
		expect(Token0.NUMERAL);
		return value;
	}

	public int expectHexadecimal() throws ParseException {
		int value = intValue;
		expect(Token0.HEXADECIMAL);
		return value;
	}
	
	public int expectBinary() throws ParseException {
		int value = intValue;
		expect(Token0.BINARY);
		return value;
	}
	
	public double expectDecimal() throws ParseException {
		double value = realValue;
		expect(Token0.DECIMAL);
		return value;
	}
	
	public Keyword0 expectKeyword() throws ParseException {
		Keyword0 value = keyword;
		expect(Token0.KEYWORD);
		return value;
	}
	
	public Keyword0 nextKeyword() throws ParseException {
		return keyword;
	}
	
	public boolean eat(Token0 token) throws ParseException {
		if (next() != token) {
			return false;
		}
		scanNextToken();
		return true;
	}

	private void scanNextToken() throws ParseException {
		token = Token0.UNKNOWN;
		while (token == Token0.UNKNOWN) {
			if (nextCh == -1) {
				token = Token0.EOF;
			} else if (Character.isWhitespace(nextCh)) {
				readNextCh();
			} else if (nextCh == '(') {
				token = Token0.LPAREN;
				readNextCh();
			} else if (nextCh == ')') {
				token = Token0.RPAREN;
				readNextCh();
			} else if (nextCh == '_') {
				token = Token0.UNDERSCORE;
				readNextCh();
			} else if (nextCh == '#') {
				readNextCh();
				if (nextCh == 'x') {
					intValue = 0;
					token = Token0.HEXADECIMAL;
					readNextCh();
					while (Character.isDigit(nextCh) || ((nextCh >= 'a') && (nextCh <= 'f')) || ((nextCh >= 'A') && (nextCh <= 'F')) || (nextCh == '_')) {
						if (Character.isDigit(nextCh)) {
							intValue = intValue * 16 + nextCh - '0';
						} else if ((nextCh >= 'a') && (nextCh <= 'f')) {
							intValue = intValue * 16 + 10 + nextCh - 'a';
						} else if ((nextCh >= 'A') && (nextCh <= 'F')) {
							intValue = intValue * 16 + 10 + nextCh - 'A';
						}
						readNextCh();
					}
				} else if (nextCh == 'b') {
					intValue = 0;
					token = Token0.BINARY;
					readNextCh();
					while (((nextCh >= '0') && (nextCh <= '1')) || (nextCh == '_')) {
						if ((nextCh >= '0') && (nextCh <= '1')) {
							intValue = intValue * 2 + nextCh - '0';
						}
						readNextCh();
					}
				} else {
					throw new ParseException("expected a hexadecimal or binary constant");
				}
			} else if (Character.isDigit(nextCh)) {
				intValue = 0;
				token = Token0.NUMERAL;
				while (Character.isDigit(nextCh)) {
					intValue = intValue * 10 + nextCh - '0';
					readNextCh();
				}
				if (nextCh == '.') {
					realValue = intValue;
					token = Token0.DECIMAL;
					readNextCh();
					double scale = 0.1;
					while (Character.isDigit(nextCh)) {
						realValue += scale * (nextCh - '0');
						scale *= 0.1;
						readNextCh();
					}
				}
			} else if (nextCh == '"') {
				StringBuilder b = new StringBuilder();
				token = Token0.STRING;
				readNextCh();
				while (nextCh != '"') {
					if (nextCh == '\\') {
						readNextCh();
						if (nextCh == '"') {
							b.append('"');
						} else if (nextCh == '\\') {
							b.append('\\');
						} else {
							throw new ParseException("illegal string escape sequence '\\" + ((char) nextCh) + "'");
						}
					} else if (nextCh == -1) {
						throw new ParseException("unterminated string");
					} else {
						b.append((char) nextCh);
					}
					readNextCh();
				}
				stringValue = b.toString();
				readNextCh();
			} else if (nextCh == ':') {
				StringBuilder k = new StringBuilder();
				k.append(':');
				readNextCh();
				while (isIdentifierChar(nextCh)) {
					k.append((char) nextCh);
					readNextCh();
				}
				stringValue = k.toString();
				token = Token0.KEYWORD;
				if (stringValue.equals(":all-statistics")) {
					keyword = Keyword0.ALL_STATISTICS;
				} else if (stringValue.equals(":authors")) {
					keyword = Keyword0.AUTHORS;
				} else if (stringValue.equals(":diagnostic-output-channel")) {
					keyword = Keyword0.DIAGNOSTIC_OUTPUT_CHANNEL;
				} else if (stringValue.equals(":error-behavior")) {
					keyword = Keyword0.ERROR_BEHAVIOR;
				} else if (stringValue.equals(":expand-definitions")) {
					keyword = Keyword0.EXPAND_DEFINITIONS;
				} else if (stringValue.equals(":interactive-mode")) {
					keyword = Keyword0.INTERACTIVE_MODE;
				} else if (stringValue.equals(":name")) {
					keyword = Keyword0.NAME;
				} else if (stringValue.equals(":print-success")) {
					keyword = Keyword0.PRINT_SUCCESS;
				} else if (stringValue.equals(":produce-assignments")) {
					keyword = Keyword0.PRODUCE_ASSIGNMENTS;
				} else if (stringValue.equals(":produce-models")) {
					keyword = Keyword0.PRODUCE_MODELS;
				} else if (stringValue.equals(":produce-proofs")) {
					keyword = Keyword0.PRODUCE_PROOFS;
				} else if (stringValue.equals(":produce-unsat-cores")) {
					keyword = Keyword0.PRODUCE_UNSAT_CORES;
				} else if (stringValue.equals(":random-seed")) {
					keyword = Keyword0.RANDOM_SEED;
				} else if (stringValue.equals(":reason-unknown")) {
					keyword = Keyword0.REASON_UNKNOWN;
				} else if (stringValue.equals(":regular-output-channel")) {
					keyword = Keyword0.REGULAR_OUTPUT_CHANNEL;
				} else if (stringValue.equals(":status")) {
					keyword = Keyword0.STATUS;
				} else if (stringValue.equals(":verbosity")) {
					keyword = Keyword0.VERBOSITY;
				} else if (stringValue.equals(":version")) {
					keyword = Keyword0.VERSION;
				} else {
					keyword = Keyword0.UNPREDEFINED;
				}
			} else if (nextCh == '|') {
				StringBuilder s = new StringBuilder();
				readNextCh();
				while (nextCh != '|') {
					if (nextCh == -1) {
						throw new ParseException("unterminated symbol name \"" + s.toString() + "\"");
					}
					s.append((char) nextCh);
					readNextCh();
				}
				token = Token0.SYMBOL; 
				readNextCh();
			} else if (isIdentifierChar(nextCh)) {
				StringBuilder s = new StringBuilder();
				while (isIdentifierChar(nextCh)) {
					s.append((char) nextCh);
					readNextCh();
				}
				stringValue = s.toString();
				if (stringValue.equals("!")) {
					token = Token0.NOT;
				} else if (stringValue.equals("as")) {
					token = Token0.AS;
				} else if (stringValue.equals("assert")) {
					token = Token0.ASSERT;
				} else if (stringValue.equals("check-sat")) {
					token = Token0.CHECK_SAT;
				} else if (stringValue.equals("declare-fun")) {
					token = Token0.DECLARE_FUN;
				} else if (stringValue.equals("declare-sort")) {
					token = Token0.DECLARE_SORT;
				} else if (stringValue.equals("define-fun")) {
					token = Token0.DEFINE_FUN;
				} else if (stringValue.equals("define-sort")) {
					token = Token0.DEFINE_SORT;
				} else if (stringValue.equals("exists")) {
					token = Token0.EXISTS;
				} else if (stringValue.equals("exit")) {
					token = Token0.EXIT;
				} else if (stringValue.equals("false")) {
					token = Token0.FALSE;
				} else if (stringValue.equals("forall")) {
					token = Token0.FORALL;
				} else if (stringValue.equals("get-assertions")) {
					token = Token0.GET_ASSERTIONS;
				} else if (stringValue.equals("get-assignment")) {
					token = Token0.GET_ASSIGNMENT;
				} else if (stringValue.equals("get-info")) {
					token = Token0.GET_INFO;
				} else if (stringValue.equals("get-option")) {
					token = Token0.GET_OPTION;
				} else if (stringValue.equals("get-proof")) {
					token = Token0.GET_PROOF;
				} else if (stringValue.equals("get-unsat-core")) {
					token = Token0.GET_UNSAT_CORE;
				} else if (stringValue.equals("get-value")) {
					token = Token0.GET_VALUE;
				} else if (stringValue.equals("let")) {
					token = Token0.LET;
				} else if (stringValue.equals("pop")) {
					token = Token0.POP;
				} else if (stringValue.equals("push")) {
					token = Token0.PUSH;
				} else if (stringValue.equals("set-info")) {
					token = Token0.SET_INFO;
				} else if (stringValue.equals("set-logic")) {
					token = Token0.SET_LOGIC;
				} else if (stringValue.equals("set-option")) {
					token = Token0.SET_OPTION;
				} else if (stringValue.equals("true")) {
					token = Token0.TRUE;
				} else {
					token = Token0.SYMBOL;
				}
			} else {
				throw new ParseException("unrecognized character \"" + ((char) nextCh) + "\"");
			}
		}
		log.finest("token==[" + token + "] nextCh=='" + ((char) nextCh) + "' intValue==" + intValue + " realValue==" + realValue + " stringValue==\"" + stringValue + "\" keyword==[" + keyword + "]");
	}

	private boolean isIdentifierChar(int ch) {
		return Character.isLetterOrDigit(ch) || (ch == '+') || (ch == '-') || (ch == '/') || (ch == '*') || (ch == '=') || (ch == '%')
				 || (ch == '?') || (ch == '!') || (ch == '.') || (ch == '$') || (ch == '_') || (ch == '~') || (ch == '&') || (ch == '^')
				  || (ch == '<') || (ch == '>') || (ch == '@');
	}

	private void readNextCh() throws ParseException {
		if (nextCh == -1) {
			return;
		}
		try {
			nextCh = reader.read();
		} catch (IOException e) {
			throw new ParseException("IO problem: " + e.getMessage());
		}
		if (nextCh == 10) {
			// chLoc.advanceLine();
		} else {
			// chLoc.advancePos();
		}
	}

}

