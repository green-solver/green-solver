package za.ac.sun.cs.green.parser.klee;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.logging.Logger;

import za.ac.sun.cs.green.util.NullLogger;

public class Scanner {

	/**
	 * The next token in the token stream.
	 */
	private Token token = Token.UNKNOWN;

	/**
	 * If the next token is an integer literal, this field contains its value.
	 */
	private int intValue = -1;

	/**
	 * If the next token is a type, this field contains its size.
	 */
	private int typeSize = -1;

	/**
	 * If the next token is an identifier, this field contains its value.
	 */
	private String idValue = null;

	/**
	 * The character to process.
	 */
	private int nextCh = ' ';

	/**
	 * The reader where characters are read from.
	 */
	private final Reader reader;

	/**
	 * For logging debugging messages.
	 */
	private final Logger log;

	public Scanner(Reader reader, Logger log) throws ParseException {
		this.log = log;
		this.reader = reader;
		token = Token.UNKNOWN;
		intValue = -1;
		idValue = null;
		nextCh = ' ';
		scanNextToken();
	}

	public Scanner(Reader reader) throws ParseException {
		this(reader, new NullLogger());
	}
	
	public Scanner(String query, Logger log) throws ParseException {
		this.log = log;
		reader = new StringReader(query);
		token = Token.UNKNOWN;
		intValue = -1;
		idValue = null;
		nextCh = ' ';
		scanNextToken();
	}

	public Scanner(String query) throws ParseException {
		this(query, new NullLogger());
	}
	
	/**
	 * Returns the next token in the token stream.
	 * 
	 * @return the next token
	 */
	public Token next() {
		return token;
	}

	/**
	 * Returns the value of the (last or current) identifier.
	 * 
	 * @return the identifier
	 */
	public String nextId() {
		return idValue;
	}

	/**
	 * Returns the value of the (last or current) integer literal.
	 * 
	 * @return the integer literal value
	 */
	public int nextInt() {
		return intValue;
	}

	public void expect(Token token) throws ParseException {
		if (next() != token) {
			throw new ParseException("Expected " + token + " but found " + next());
		}
		scanNextToken();
	}

	public String expectId() throws ParseException {
		String id = idValue;
		expect(Token.ID);
		return id;
	}

	public int expectInt() throws ParseException {
		int x = intValue;
		expect(Token.INT);
		return x;
	}

	public int expectType() throws ParseException {
		int x = typeSize;
		expect(Token.TYPE);
		return x;
	}

	public boolean eat(Token token) throws ParseException {
		if (next() != token) {
			return false;
		}
		scanNextToken();
		return true;
	}

	private void scanNextToken() throws ParseException {
		token = Token.UNKNOWN;
		while (token == Token.UNKNOWN) {
			if (nextCh == -1) {
				token = Token.EOF;
			} else if (Character.isWhitespace(nextCh)) {
				readNextCh();
			} else if (nextCh == '@') {
				token = Token.AT;
				readNextCh();
			} else if (nextCh == ':') {
				token = Token.COLON;
				readNextCh();
			} else if (nextCh == ',') {
				token = Token.COMMA;
				readNextCh();
			} else if (nextCh == '=') {
				token = Token.EQUALS;
				readNextCh();
			} else if (nextCh == '[') {
				token = Token.LBRACKET;
				readNextCh();
			} else if (nextCh == ']') {
				token = Token.RBRACKET;
				readNextCh();
			} else if (nextCh == '(') {
				token = Token.LPAREN;
				readNextCh();
			} else if (nextCh == ')') {
				token = Token.RPAREN;
				readNextCh();
			} else if (nextCh == '+') {
				token = Token.PLUS;
				readNextCh();
			} else if (nextCh == '-') {
				token = Token.MINUS;
				readNextCh();
				if (nextCh == '>') {
					token = Token.ARROW;
					readNextCh();
				}
			} else if (Character.isDigit(nextCh)) {
				int x = nextCh;
				token = Token.INT;
				intValue = 0;
				readNextCh();
				if (nextCh == 'b') { // binary
					readNextCh();
					while (((nextCh >= '0') && (nextCh <= '1')) || (nextCh == '_')) {
						if ((nextCh >= '0') && (nextCh <= '1')) {
							intValue = intValue * 2 + nextCh - '0';
						}
						readNextCh();
					}
				} else if (nextCh == 'o') { // octal
					readNextCh();
					while (((nextCh >= '0') && (nextCh <= '7')) || (nextCh == '_')) {
						if ((nextCh >= '0') && (nextCh <= '7')) {
							intValue = intValue * 8 + nextCh - '0';
						}
						readNextCh();
					}
				} else if (nextCh == 'x') { // kexadecimal
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
				} else { // decimal
					if (x != '_') {
						intValue = x - '0';
					}
					while (Character.isDigit(nextCh) || (nextCh == '_')) {
						if (nextCh != '_') {
							intValue = intValue * 10 + nextCh - '0';
						}
						readNextCh();
					}
				}
			} else if (Character.isLetter(nextCh) || (nextCh == '_')) {
				StringBuffer b = new StringBuffer();
				while (Character.isLetterOrDigit(nextCh) || (nextCh == '_') || (nextCh == '.')) {
					b.append((char) nextCh);
					readNextCh();
				}
				String s = b.toString();
				if (s.matches("w[0-9]+")) {
					token = Token.TYPE;
					typeSize = Integer.parseInt(s.substring(1));
				} else if (s.equals("Add")) {
					token = Token.ADD;
				} else if (s.equals("And")) {
					token = Token.AND;
				} else if (s.equals("array")) {
					token = Token.ARRAY;
				} else if (s.equals("AShl")) {
					token = Token.ASHR;
				} else if (s.equals("Concat")) {
					token = Token.CONCAT;
				} else if (s.equals("Eq")) {
					token = Token.EQ;
				} else if (s.equals("Extract")) {
					token = Token.EXTRACT;
				} else if (s.equals("false")) {
					token = Token.FALSE;
				} else if (s.equals("LShl")) {
					token = Token.LSHR;
				} else if (s.equals("Mul")) {
					token = Token.MUL;
				} else if (s.equals("Ne")) {
					token = Token.NE;
				} else if (s.equals("Neg")) {
					token = Token.NEG;
				} else if (s.equals("Not")) {
					token = Token.NOT;
				} else if (s.equals("Or")) {
					token = Token.OR;
				} else if (s.equals("query")) {
					token = Token.QUERY;
				} else if (s.equals("Read")) {
					token = Token.READ;
				} else if (s.equals("ReadLSB")) {
					token = Token.READLSB;
				} else if (s.equals("ReadMSB")) {
					token = Token.READMSB;
				} else if (s.equals("SDiv")) {
					token = Token.SDIV;
				} else if (s.equals("Select")) {
					token = Token.SELECT;
				} else if (s.equals("SExt")) {
					token = Token.SEXT;
				} else if (s.equals("Sge")) {
					token = Token.SGE;
				} else if (s.equals("Sgt")) {
					token = Token.SGT;
				} else if (s.equals("Shl")) {
					token = Token.SHL;
				} else if (s.equals("Sle")) {
					token = Token.SLE;
				} else if (s.equals("Slt")) {
					token = Token.SLT;
				} else if (s.equals("SRem")) {
					token = Token.SREM;
				} else if (s.equals("Sub")) {
					token = Token.SUB;
				} else if (s.equals("symbolic")) {
					token = Token.SYMBOLIC;
				} else if (s.equals("true")) {
					token = Token.TRUE;
				} else if (s.equals("UDiv")) {
					token = Token.UDIV;
				} else if (s.equals("Uge")) {
					token = Token.UGE;
				} else if (s.equals("Ugt")) {
					token = Token.UGT;
				} else if (s.equals("Ule")) {
					token = Token.ULE;
				} else if (s.equals("Ult")) {
					token = Token.ULT;
				} else if (s.equals("URem")) {
					token = Token.UREM;
				} else if (s.equals("Xor")) {
					token = Token.XOR;
				} else if (s.equals("ZExt")) {
					token = Token.ZEXT;
				} else {
					token = Token.ID;
					idValue = s;
				}
			} else {
				throw new ParseException("unrecognized character \"" + ((char) nextCh) + "\"");
			}
		}
		log.finest("token== " + token);
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
