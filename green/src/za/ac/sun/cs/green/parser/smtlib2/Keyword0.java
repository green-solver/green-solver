package za.ac.sun.cs.green.parser.smtlib2;

public enum Keyword0 {

	UNPREDEFINED("\"a user-predefined keyword\""),
	ALL_STATISTICS("\":all-statistics\""),
	AUTHORS("\":authors\""),
	DIAGNOSTIC_OUTPUT_CHANNEL("\":diagnostic-output-channel\""),
	ERROR_BEHAVIOR("\":error-behavior\""),
	EXPAND_DEFINITIONS("\":expand-definitions\""),
	INTERACTIVE_MODE("\":interactive-mode\""),
	NAME("\":name\""),
	PRINT_SUCCESS("\":print-success\""),
	PRODUCE_ASSIGNMENTS("\":produce-assignments\""),
	PRODUCE_MODELS("\":produce-models\""),
	PRODUCE_PROOFS("\":produce-proofs\""),
	PRODUCE_UNSAT_CORES("\":produce-unsat-cores\""),
	RANDOM_SEED("\":random-seed\""),
	REASON_UNKNOWN("\":reason-unknown\""),
	REGULAR_OUTPUT_CHANNEL("\":regular-output-channel\""),
	STATUS("\":status\""),
	VERBOSITY("\":verbosity\""),
	VERSION("\":version\"");
	
	/**
	 * A description string for the information flag.
	 */
	private final String string;

	/**
	 * Constructs an information flag.
	 * 
	 * @param string the description string
	 */
	private Keyword0(String string) {
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
