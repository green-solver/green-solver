package za.ac.sun.cs.green.parser.smtlib2;

import static org.junit.Assert.*;

import org.junit.Test;

public class SMTLIB2Scanner0Test {

	@Test
	public void recognizeBasicTokens() throws ParseException {
		Scanner0 s = new Scanner0(":all-statistics as assert :authors check-sat"
				+ " declare-fun declare-sort define-fun define-sort"
				+ " :diagnostic-output-channel :error-behavior exists exit"
				+ " :expand-definitions false forall get-assertions"
				+ " get-assignment get-info get-option get-proof"
				+ " get-unsat-core get-value :interactive-mode let :name pop"
				+ " :print-success :produce-assignments :produce-models"
				+ " :produce-proofs :produce-unsat-cores push :random-seed"
				+ " :reason-unknown :regular-output-channel set-info set-logic"
				+ " set-option :status true :verbosity :version ( ! ) _");
		assertEquals(Keyword0.ALL_STATISTICS, s.expectKeyword());
		assertTrue(s.eat(Token0.AS));
		assertTrue(s.eat(Token0.ASSERT));
		assertEquals(Keyword0.AUTHORS, s.expectKeyword());
		assertTrue(s.eat(Token0.CHECK_SAT));
		assertTrue(s.eat(Token0.DECLARE_FUN));
		assertTrue(s.eat(Token0.DECLARE_SORT));
		assertTrue(s.eat(Token0.DEFINE_FUN));
		assertTrue(s.eat(Token0.DEFINE_SORT));
		assertEquals(Keyword0.DIAGNOSTIC_OUTPUT_CHANNEL, s.expectKeyword());
		assertEquals(Keyword0.ERROR_BEHAVIOR, s.expectKeyword());
		assertTrue(s.eat(Token0.EXISTS));
		assertTrue(s.eat(Token0.EXIT));
		assertEquals(Keyword0.EXPAND_DEFINITIONS, s.expectKeyword());
		assertTrue(s.eat(Token0.FALSE));
		assertTrue(s.eat(Token0.FORALL));
		assertTrue(s.eat(Token0.GET_ASSERTIONS));
		assertTrue(s.eat(Token0.GET_ASSIGNMENT));
		assertTrue(s.eat(Token0.GET_INFO));
		assertTrue(s.eat(Token0.GET_OPTION));
		assertTrue(s.eat(Token0.GET_PROOF));
		assertTrue(s.eat(Token0.GET_UNSAT_CORE));
		assertTrue(s.eat(Token0.GET_VALUE));
		assertEquals(Keyword0.INTERACTIVE_MODE, s.expectKeyword());
		assertTrue(s.eat(Token0.LET));
		assertEquals(Keyword0.NAME, s.expectKeyword());
		assertTrue(s.eat(Token0.POP));
		assertEquals(Keyword0.PRINT_SUCCESS, s.expectKeyword());
		assertEquals(Keyword0.PRODUCE_ASSIGNMENTS, s.expectKeyword());
		assertEquals(Keyword0.PRODUCE_MODELS, s.expectKeyword());
		assertEquals(Keyword0.PRODUCE_PROOFS, s.expectKeyword());
		assertEquals(Keyword0.PRODUCE_UNSAT_CORES, s.expectKeyword());
		assertTrue(s.eat(Token0.PUSH));
		assertEquals(Keyword0.RANDOM_SEED, s.expectKeyword());
		assertEquals(Keyword0.REASON_UNKNOWN, s.expectKeyword());
		assertEquals(Keyword0.REGULAR_OUTPUT_CHANNEL, s.expectKeyword());
		assertTrue(s.eat(Token0.SET_INFO));
		assertTrue(s.eat(Token0.SET_LOGIC));
		assertTrue(s.eat(Token0.SET_OPTION));
		assertEquals(Keyword0.STATUS, s.expectKeyword());
		assertTrue(s.eat(Token0.TRUE));
		assertEquals(Keyword0.VERBOSITY, s.expectKeyword());
		assertEquals(Keyword0.VERSION, s.expectKeyword());
		assertTrue(s.eat(Token0.LPAREN));
		assertTrue(s.eat(Token0.NOT));
		assertTrue(s.eat(Token0.RPAREN));
		assertTrue(s.eat(Token0.UNDERSCORE));
	}

	@Test
	public void recognizeNumbers() throws ParseException {
		Scanner0 s = new Scanner0("0 123 #b0 #b100100 #x0 #xabcd 0.12 0.999");
		assertEquals(0, s.expectNumeral());
		assertEquals(123, s.expectNumeral());
		assertEquals(0, s.expectBinary());
		assertEquals(36, s.expectBinary());
		assertEquals(0, s.expectHexadecimal());
		assertEquals(43981, s.expectHexadecimal());
		assertEquals(0.12, s.expectDecimal(), 0.00001);
		assertEquals(0.999, s.expectDecimal(), 0.00001);
	}

	/*
	NUMERAL("a numeral"),
	DECIMAL("a decimal numeral"),
	HEXADECIMAL("a hexadecimal numeral"),
	BINARY("a binary numeral"),
	STRING("a string"),
	KEYWORD("a keyword"),
	SYMBOL("a symbol"),
	*/
}
