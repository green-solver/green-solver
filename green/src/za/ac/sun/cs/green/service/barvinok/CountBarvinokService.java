package za.ac.sun.cs.green.service.barvinok;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.apfloat.Apint;

import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.IntVariable;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.service.CountService;

public class CountBarvinokService extends CountService {

	/**
	 * Directory where the LattE output file numOFLatticePoints is stored.
	 */
	private static final String DIRECTORY = System.getProperty("java.io.tmpdir");

	private static final String DATE = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date());
	
	private static final int RANDOM = new Random().nextInt(9);

	private static final String DIRNAME = String.format("%s/%s%s", DIRECTORY, DATE, RANDOM);

	private static String directory = null;

	static {
		File d = new File(DIRNAME);
		if (!d.exists()) {
			if (d.mkdir()) {
				directory = DIRNAME;
			} else {
				directory = DIRECTORY;
			}
		}
	}

	/**
	 * File where the LattE input is stored.
	 */
	private static final String FILENAME = directory + "/probsymbc-barvinok.in";

	/**
	 * Pattern that identifies the answer from Barvinok.
	 */
	private static final String ANSWER_PATTERN = "";

	/**
	 * The location of the LattE executable file.
	 */
	private final String DEFAULT_BARVINOK_PATH = "barvinok_count";

	/**
	 * Options passed to the Barvinok executable.
	 */
	private final String DEFAULT_BARVINOK_ARGS = " ";
	
	/**
	 * Combination of the Barvinok executable, options, and the filename, all
	 * separated by spaces.
	 */
	private final String barvinokCommand;
	
	/**
	 * Pearl script to transform latte to barvinok
	 * This will be removed later just a hack to get barvinok to work quickly
	 */
	private final String barvinokTransformScript;
	
	/**
	 * Logger.
	 */
	private Logger log;

	public CountBarvinokService(Green solver, Properties properties) {
		super(solver);
		log = solver.getLog();
		String p = properties.getProperty("green.barvinok.path", DEFAULT_BARVINOK_PATH);
		String a = properties.getProperty("green.barvinok.args", DEFAULT_BARVINOK_ARGS);
		barvinokCommand = p + ' ' + a + FILENAME;
		String script = p.substring(0, p.lastIndexOf('/'));
		barvinokTransformScript = script + "/latte2polylib.pl " + FILENAME + " > " + FILENAME+"2";
		log.finer("barvinokCommand=" + barvinokCommand);
		log.finer("barvinokScript=" + barvinokTransformScript);
		
		log.finer("directory=" + directory);
	}

	@Override
	protected Apint solve(Instance instance) {
		return new HMatrix().count(instance.getExpression());
	}

	/**
	 * A row that may appear in a matrix. It stores a mapping from variables to
	 * coefficients (and an additional, implicit mapping from the "
	 * <code>null</code>" variable to the constant coefficient that must appear
	 * in each row).
	 * 
	 * Each row has a type, which is a integer comparison (equal-to,
	 * not-equal-to, less-than, less-than-or-equal-to, greater-than, or
	 * greater-then-or-equal-to). Once all the coefficients have been entered,
	 * it is "fixed". This means that no new coefficients may be added, and some
	 * internal flags are set.
	 * 
	 */
	private static class HRow {

		/**
		 * Flag to indicate that the row has been fixed.
		 */
		private boolean fixed;

		/**
		 * Factor used to turn less-than and less-than-or-equal-to around.
		 */
		private int flip;

		/**
		 * The type of the row.
		 */
		private Operation.Operator type;

		/**
		 * The constant (i.e., variable-less) coefficient for the row.
		 */
		private int constantCoefficient;

		/**
		 * A mapping of variables to coefficients.
		 */
		private Map<IntVariable, Integer> coefficients;

		/**
		 * Constructor for the row.
		 * 
		 * @param type
		 *            the type of the row
		 */
		public HRow(Operation.Operator type) {
			assert (type == Operation.Operator.EQ)
					|| (type == Operation.Operator.NE)
					|| (type == Operation.Operator.LT)
					|| (type == Operation.Operator.LE)
					|| (type == Operation.Operator.GT)
					|| (type == Operation.Operator.GE);
			fixed = false;
			flip = 1;
			this.type = type;
			constantCoefficient = 0;
			coefficients = new HashMap<IntVariable, Integer>();
		}

		/**
		 * Adds a coefficient for the given variable. This overwrites any
		 * previous coefficient that might have been associated with a variable.
		 * If the given variable is <code>null</code>, the coefficient is taken
		 * to be the constant coefficient.
		 * 
		 * @param variable
		 *            the variable
		 * @param coefficient
		 *            the coefficient for the variable
		 */
		public void put(IntVariable variable, int coefficient) {
			assert !fixed;
			if (variable == null) {
				constantCoefficient = coefficient;
			} else {
				coefficients.put(variable, coefficient);
			}
		}

		/**
		 * Adds a coefficient for the given variable. This overwrites any
		 * previous coefficient that might have been associated with a variable.
		 * If the given variable is <code>null</code>, the coefficient is taken
		 * to be the constant coefficient.
		 * 
		 * @param variable
		 *            the variable
		 * @param coefficient
		 *            the coefficient for the variable
		 */
		public void put(IntVariable variable, IntConstant coefficient) {
			put(variable, coefficient.getValue());
		}

		/**
		 * Adds a value to the coefficient for a variable. If the variable has
		 * not yet been assigned a coefficient, the given value is taken to be
		 * the new coefficient. If the given variable is <code>null</code>, the
		 * coefficient is taken to be the constant coefficient.
		 * 
		 * @param variable
		 *            the variable
		 * @param delta
		 *            the value to add to the variable's coefficient
		 */
		public void add(IntVariable variable, int delta) {
			assert !fixed;
			if (variable == null) {
				constantCoefficient += delta;
			} else {
				Integer k = coefficients.get(variable);
				if (k == null) {
					coefficients.put(variable, delta);
				} else {
					coefficients.put(variable, k + delta);
				}
			}
		}

		/**
		 * Adds a value to the coefficient for a variable. If the variable has
		 * not yet been assigned a coefficient, the given value is taken to be
		 * the new coefficient. If the given variable is <code>null</code>, the
		 * coefficient is taken to be the constant coefficient.
		 * 
		 * @param variable
		 *            the variable
		 * @param delta
		 *            the value to add to the variable's coefficient
		 */
		@SuppressWarnings("unused")
		// Not used at the moment
		public void add(IntVariable variable, IntConstant delta) {
			add(variable, delta.getValue());
		}

		/**
		 * Finds the coefficient of the given variable. If the variable is
		 * <code>null</code>, the constant coefficient is returned. If the
		 * variable has no associated coefficient, the value 0 is returned.
		 * 
		 * @param variable
		 *            the given variable
		 * @return the coefficient associated with the variable (or 0)
		 */
		public int get(IntVariable variable) {
			if (variable == null) {
				return flip * constantCoefficient;
			} else {
				Integer k = coefficients.get(variable);
				return (k != null) ? flip * k : 0;
			}
		}

		/**
		 * Fixes the row by adjusting the coefficients for rows of type
		 * greater-than, greater-than-or-equal, and less-than-or-equal, and
		 * changing their types to less-than.
		 */
		public void fix() {
			if (!fixed) {
				if (type == Operation.Operator.EQ) {
					flip = -1;
				} else if (type == Operation.Operator.NE) {
					flip = -1;
				} else if (type == Operation.Operator.LT) {
					flip = -1;
					add(null, 1);
					type = Operation.Operator.LE;
				} else if (type == Operation.Operator.LE) {
					flip = -1;
				} else if (type == Operation.Operator.GT) {
					add(null, -1);
					type = Operation.Operator.LE;
				} else if (type == Operation.Operator.GE) {
					type = Operation.Operator.LE;
				}
				fixed = true;
			}
		}

		/**
		 * Returns the set of variables that appear with non-zero coefficients
		 * in the row. The "<code>null</code>" constant coefficient variable is
		 * not returned.
		 * 
		 * @return the set of variables with non-zero coefficients
		 */
		public Set<IntVariable> getVariables() {
			assert fixed;
			return coefficients.keySet();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object object) {
			assert fixed;
			HRow row = (HRow) object;
			return (type == row.type) && (flip == row.flip)
					&& (constantCoefficient == row.constantCoefficient)
					&& (coefficients.equals(row.coefficients));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return type.hashCode() ^ constantCoefficient
					^ coefficients.hashCode();
		}

	}

	/**
	 * A representation of a conjunction of constraints. Each constraint is
	 * represented by an instance of an {@link HRow}. The rows are placed into
	 * sets based on their type. Once the sets have been populated, further
	 * internal calculation is used to take care of the inequality constraints
	 * which LattE cannot handle directly.
	 */
	private class HMatrix {

		/**
		 * The set of rows of the equal-to type.
		 */
		private Set<HRow> eqRows;

		/**
		 * The set of rows of the not-equal-to type.
		 */
		private Set<HRow> neRows;

		/**
		 * The set of rows of the less-than type.
		 */
		private Set<HRow> ltRows;

		/**
		 * The set of variables that are present in all calls to LattE.
		 */
		private Set<IntVariable> allVariables;

		/**
		 * The set of variables that are present in every call to LattE.
		 */
		private Set<IntVariable> commonVariables;

		/**
		 * The number of times each variable occurs in the constraints that are
		 * present in every call to LattE.
		 */
		private Map<IntVariable, Integer> commonOccurences;

		private Map<IntVariable, Apint> variableRange;

		/**
		 * The correction that is applied to subset results.
		 */
		private Apint correction;

		/**
		 * Constructor for an H-matrix.
		 */
		public HMatrix() {
			eqRows = new HashSet<HRow>();
			neRows = new HashSet<HRow>();
			ltRows = new HashSet<HRow>();
			allVariables = new HashSet<IntVariable>();
			commonVariables = new HashSet<IntVariable>();
			commonOccurences = new HashMap<IntVariable, Integer>();
			variableRange = new HashMap<IntVariable, Apint>();
		}

		/**
		 * Constructs the rows of the matrix by recursively exploring the
		 * expression. It is assumed that the expression has a very specific
		 * form.
		 * 
		 * <pre>
		 * expr ::= constraint | expr && expr
		 * constraint ::= cexpr ( = | != | < | <= | > | >= ) 0
		 * cexpr ::= term | cexpr + term
		 * term ::= integer_constant | integer_constant * variable
		 * </pre>
		 * 
		 * @param operation
		 *            the expression to explore
		 */
		private void explore(Operation operation) {
			Operation.Operator op = operation.getOperator();
			if (op == Operation.Operator.AND) {
				explore((Operation) operation.getOperand(0));
				explore((Operation) operation.getOperand(1));
			} else {
				assert (op == Operation.Operator.EQ)
						|| (op == Operation.Operator.NE)
						|| (op == Operation.Operator.LT)
						|| (op == Operation.Operator.LE)
						|| (op == Operation.Operator.GT)
						|| (op == Operation.Operator.GE);
				HRow row = new HRow(op);
				int c = ((IntConstant) operation.getOperand(1)).getValue();
				assert (c == 0);
				Expression e = operation.getOperand(0);
				while ((e instanceof Operation)
						&& (((Operation) e).getOperator() == Operation.Operator.ADD)) {
					explore0(row, ((Operation) e).getOperand(1));
					e = ((Operation) e).getOperand(0);
				}
				explore0(row, e);
				register(row);
			}
		}

		/**
		 * Processes one term of an expression and adding it to the given row.
		 * The term is assumed to have a very specific form.
		 * 
		 * <pre>
		 * term ::= integer_constant | integer_constant * variable
		 * </pre>
		 * 
		 * @param row
		 *            the row to which the term information is added
		 * @param expression
		 *            the term to process
		 */
		private void explore0(HRow row, Expression expression) {
			if (expression instanceof IntConstant) {
				int c = ((IntConstant) expression).getValue();
				row.put(null, c);
			} else {
				Operation o = (Operation) expression;
				assert o.getOperator() == Operation.Operator.MUL;
				row.put((IntVariable) o.getOperand(1),
						(IntConstant) o.getOperand(0));
			}
		}

		/**
		 * Register a row with the matrix by "fixing" it (by calling
		 * {@link HRow#fix()}) and then adding it to the appropriate set, based
		 * on its type. The variables of the row is added to the set of
		 * variables.
		 * 
		 * @param row
		 *            the row to add to the matrix
		 */
		private void register(HRow row) {
			row.fix();
			Operation.Operator type = row.type;
			allVariables.addAll(row.getVariables());
			if (type == Operation.Operator.EQ) {
				eqRows.add(row);
				for (IntVariable v : row.getVariables()) {
					Integer o = commonOccurences.get(v);
					commonOccurences.put(v, o != null ? o + 1 : 1);
					commonVariables.add(v);
				}
			} else if (type == Operation.Operator.NE) {
				neRows.add(row);
			} else if (type == Operation.Operator.LE) {
				ltRows.add(row);
				for (IntVariable v : row.getVariables()) {
					Integer o = commonOccurences.get(v);
					commonOccurences.put(v, o != null ? o + 1 : 1);
					commonVariables.add(v);
				}
			} else {
				assert false;
			}
		}

		/**
		 * Counts the number solutions that satisfy the expression.
		 * 
		 * @param expression
		 *            the expression to satisfy
		 * @return the number of satisfying solutions
		 */
		public Apint count(Expression expression) {
			explore((Operation) expression);
			for (IntVariable v : allVariables) {
				Apint x = new Apint(v.getUpperBound());
				variableRange.put(v, x.subtract(new Apint(v.getLowerBound())));
			}
			Apint n = Apint.ZERO;
			Subsetter<HRow> s = new Subsetter<HRow>(neRows);
			for (Set<HRow> ne = new HashSet<HRow>(); ne != null; ne = s
					.advance()) {
				if (ne.size() % 2 == 0) {
					n = n.add(processInput(generateConstraints(ne)));
				} else {
					n = n.subtract(processInput(generateConstraints(ne)));
				}
			}
			return n;
		}

		private String generateConstraints(Set<HRow> neRows) {
			// Now generate the constraints
			SortedSet<String> constraints = new TreeSet<String>();
			Set<String> eqConstraints = new HashSet<String>();
			// Construct the set of variables and list of columns
			Set<IntVariable> variables = new HashSet<IntVariable>(
					commonVariables);
			List<IntVariable> columns = new LinkedList<IntVariable>(variables);
			final Map<IntVariable, Integer> occurences = new HashMap<IntVariable, Integer>(
					commonOccurences);
			for (HRow r : neRows) {
				for (IntVariable v : r.getVariables()) {
					Integer o = occurences.get(v);
					occurences.put(v, o != null ? o + 1 : 1);
					variables.add(v);
				}
			}
			Collections.sort(columns, new Comparator<IntVariable>() {
				@Override
				public int compare(IntVariable v1, IntVariable v2) {
					int k1 = occurences.get(v1);
					int k2 = occurences.get(v2);
					if (k1 < k2) {
						return -1;
					} else if (k1 > k2) {
						return 1;
					} else {
						return v1.compareTo(v2);
					}
				}
			});
			// Calculate the correction factor
			correction = Apint.ONE;
			for (IntVariable v : allVariables) {
				if (!variables.contains(v)) {
					correction = correction.multiply(variableRange.get(v));
				}
			}
			// Now we are ready to construct the constraints string, starting
			// with the less-than constraints
			for (HRow r : ltRows) {
				StringBuilder c = new StringBuilder();
				c.append(r.get(null));
				for (IntVariable v : columns) {
					c.append('\t').append(r.get(v));
				}
				constraints.add(c.toString());
			}
			// Emit the equal-to constraints
			for (HRow r : eqRows) {
				StringBuilder c = new StringBuilder();
				c.append(r.get(null));
				for (IntVariable v : columns) {
					c.append('\t').append(r.get(v));
				}
				String s = c.toString();
				constraints.add(s);
				eqConstraints.add(s);
			}
			// Emit the not-equal-to constraints
			for (HRow r : neRows) {
				StringBuilder c = new StringBuilder();
				c.append(r.get(null));
				for (IntVariable v : columns) {
					c.append('\t').append(r.get(v));
				}
				String s = c.toString();
				constraints.add(s);
				eqConstraints.add(s);
			}
			// Construct the final string version
			int numColumns = columns.size() + 1;
			int numRows = constraints.size();
			int numEqRows = eqConstraints.size();
			StringBuilder c = new StringBuilder();
			c.append(numRows).append(' ');
			c.append(numColumns).append('\n');
			StringBuilder e = new StringBuilder();
			e.append("linearity ").append(numEqRows);
			int n = 0;
			for (String s : constraints) {
				n++;
				//c.append('1').append('\t');
				c.append(s).append('\n');
				if (eqConstraints.contains(s)) {
					e.append(' ').append(n);
				}
			}
			if (numEqRows > 0) {
				e.append('\n');
				c.append(e);
			}
			return c.toString();
		}

		/**
		 * Processes the input to produce the number of satisfying solutions. If
		 * present, the store is checked first. If the answer is not already
		 * present, it is calculated and added to the store.
		 * 
		 * @param input
		 *            the LattE input as an H-matrix
		 * @return the number of satisfying solutions as an {@link Apint}
		 */
		private Apint processInput(String input) {
			if (store == null) {
				return new Apint(invokeBarvinok(input)).multiply(correction);
			} else {
				String count = store.getString(input);
				if (count == null) {
					count = invokeBarvinok(input);
					store.put(input, count);
				}
				return new Apint(count).multiply(correction);
			}
		}

		
		
		/**
		 * Stores the input in a file, invokes LattE on the file, captures and
		 * processes the output, and returns the number of satisfying solutions
		 * as a string.
		 * 
		 * @param input
		 *            the LattE input as an H-matrix
		 * @return the number of satisfying solutions as a string
		 */
		private String invokeBarvinok(String input) {
			//System.out.println(">>> INVOKING Barvinok Latte:");
			// System.out.println(input);
			String result = "";
			try {
				// First store the input in a file
				File file = new File(FILENAME);
				if (file.exists()) {
					file.delete();
				}
				file.createNewFile();
				FileWriter writer = new FileWriter(file);
				writer.write(input);
				writer.close();
				// Now invoke Barvinok
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				DefaultExecutor executor = new DefaultExecutor();
				executor.setStreamHandler(new PumpStreamHandler(outputStream));
				executor.setWorkingDirectory(new File(directory));
				executor.setExitValues(null); 
				executor.execute(CommandLine.parse(barvinokCommand));
				result = outputStream.toString();
			} catch (ExecuteException e) {
				System.out.println("LattECounter : caught " + e.getClass()
						+ " while executing " + FILENAME);
				e.printStackTrace();
				throw new RuntimeException();
			} catch (IOException e) {
				System.out.println("LattECounter : caught " + e.getClass()
						+ " while executing " + FILENAME);
				e.printStackTrace();
				throw new RuntimeException();
			}
			// Process Barvinok's output
			//System.out.println(result);
			if (!result.startsWith("POLYHEDRON", 0)) {
				System.out.println("Barvinok Failed! ");
				throw new RuntimeException();
			}
			int lastSpace = result.lastIndexOf(' ');
			int secondLastSpace = result.substring(0, lastSpace).lastIndexOf(' ');
			result = result.substring(secondLastSpace+1, lastSpace);
			int newlineIndex = result.indexOf("\n");
			if (newlineIndex != -1) {
				result = result.substring(newlineIndex+1);
				//System.out.println(result);
			}
			return result;
		}

	}

	/**
	 * Generic class to iterate over all subsets of a given set. Note that the
	 * empty set is never returned. The correct way to use this class is as
	 * follows:
	 * 
	 * <pre>
	 * Subsetter<X> z = new Subsetter<X>(...some set of X elements...);
	 * for (Set<X> s = new HashSet<X>(); s != null; s = z.advance()) {
	 *   ...do something with subset s...
	 * }
	 * </pre>
	 * 
	 * @param <T>
	 *            the base type of element
	 */
	private static class Subsetter<T> {

		/**
		 * A list version of the whole set. This is needed to access the
		 * individual elements by number.
		 */
		private List<T> list;

		/**
		 * The result set.
		 */
		private Set<T> set;

		/**
		 * The number of elements in the whole set.
		 */
		private int size;

		/**
		 * A bitset to record the elements of the whole set ({@link #list}) that
		 * are currently included in the result set ({@link #set}).
		 */
		private BitSet elements;

		/**
		 * Constructor for the subsetter. Class fields are initialized but no
		 * heavy computation is performed.
		 * 
		 * @param wholeSet
		 *            the whole set over which subsets are taken
		 */
		public Subsetter(Set<T> wholeSet) {
			list = new LinkedList<T>(wholeSet);
			set = new HashSet<T>();
			size = list.size();
			elements = new BitSet(size);
		}

		/**
		 * Calculates and returns the next subset. Once there are no more
		 * subsets, the method returns <code>null</code>.
		 * 
		 * @return the next subset of the whole set, or <code>null</code> if all
		 *         subsets have been returned
		 */
		public final Set<T> advance() {
			int i = 0;
			while ((i < size) && elements.get(i)) {
				set.remove(list.get(i));
				elements.clear(i++);
			}
			if (i == size) {
				return null;
			} else {
				set.add(list.get(i));
				elements.set(i);
				return set;
			}
		}

	}

}
