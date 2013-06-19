package za.ac.sun.cs.green.parser.klee;

public enum Token {

	UNKNOWN("an unknown token"),
	EOF("the end of file"),
	ID("an identifier"),
	INT("an integer literal"),
	TYPE("a type"),

	ADD("\"Add\""),
	AND("\"And\""),
	ARRAY("\"array\""),
	ASHR("\"AShr\""),
	CONCAT("\"Concat\""),
	EQ("\"Eq\""),
	EXTRACT("\"Extract\""),
	FALSE("\"false\""),
	LSHR("\"LShr\""),
	MUL("\"Mul\""),
	NE("\"Ne\""),
	NEG("\"Neg\""),
	NOT("\"Not\""),
	OR("\"Or\""),
	QUERY("\"query\""),
	READ("\"Read\""),
	READLSB("\"ReadLSB\""),
	READMSB("\"ReadMSB\""),
	SDIV("\"SDiv\""),
	SELECT("\"Select\""),
	SEXT("\"SExt\""),
	SGE("\"Sge\""),
	SGT("\"Sgt\""),
	SHL("\"Shl\""),
	SLE("\"Sle\""),
	SLT("\"Slt\""),
	SREM("\"SRem\""),
	SUB("\"Sub\""),
	SYMBOLIC("\"symbolic\""),
	TRUE("\"true\""),
	UDIV("\"UDiv\""),
	UGE("\"Uge\""),
	UGT("\"Ugt\""),
	ULE("\"Ule\""),
	ULT("\"Ult\""),
	UREM("\"URem\""),
	XOR("\"Xor\""),
	ZEXT("\"ZExt\""),
	
	ARROW("\"->\""),
	AT("\"@\""),
	COLON("\":\""),
	COMMA("\",\""),
	EQUALS("\"=\""),
	LBRACKET("\"[\""),
	LPAREN("\"(\""),
	MINUS("\"-\""),
	PLUS("\"+\""),
	RBRACKET("\"]\""),
	RPAREN("\")\"");

	/**
	 * A description string for the token.
	 */
	private final String string;

	/**
	 * Constructs a token.
	 * 
	 * @param string the description string
	 */
	private Token(String string) {
		this.string = string;
	}

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return string;
	}

}
