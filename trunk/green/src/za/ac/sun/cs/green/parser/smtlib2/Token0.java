package za.ac.sun.cs.green.parser.smtlib2;

public enum Token0 {

	UNKNOWN("an unknown token"),
	EOF("the end of file"),
	NUMERAL("a numeral"),
	DECIMAL("a decimal numeral"),
	HEXADECIMAL("a hexadecimal numeral"),
	BINARY("a binary numeral"),
	STRING("a string"),
	KEYWORD("a keyword"),
	SYMBOL("a symbol"),

	AS("\"as\""),
	ASSERT("\"assert\""),
	CHECK_SAT("\"check-sat\""),
	DECLARE_FUN("\"declare-fun\""),
	DECLARE_SORT("\"declare-sort\""),
	DEFINE_FUN("\"define-fun\""),
	DEFINE_SORT("\"define-sort\""),
	EXISTS("\"exists\""),
	EXIT("\"exit\""),
	FALSE("\"false\""),
	FORALL("\"forall\""),
	GET_ASSERTIONS("\"get-assertions\""),
	GET_ASSIGNMENT("\"get-assignment\""),
	GET_INFO("\"get-info\""),
	GET_OPTION("\"get-option\""),
	GET_PROOF("\"get-proof\""),
	GET_UNSAT_CORE("\"get-unsat-core\""),
	GET_VALUE("\"get-value\""),
	LET("\"let\""),
	POP("\"pop\""),
	PUSH("\"push\""),
	SET_INFO("\"set-info\""),
	SET_LOGIC("\"set-logic\""),
	SET_OPTION("\"set-option\""),
	TRUE("\"true\""),
	
	LPAREN("\"(\""),
	NOT("\"!\""),
	RPAREN("\")\""),
	UNDERSCORE("\"_\"");

	/**
	 * A description string for the token.
	 */
	private final String string;

	/**
	 * Constructs a token.
	 * 
	 * @param string the description string
	 */
	private Token0(String string) {
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
