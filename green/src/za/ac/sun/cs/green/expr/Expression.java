package za.ac.sun.cs.green.expr;

public abstract class Expression implements Comparable<Expression> {

	public abstract void accept(Visitor visitor);

	@Override
	public final int compareTo(Expression expression) {
		return toString().compareTo(expression.toString());
	}

	@Override
	public abstract boolean equals(Object object);

	@Override
	public abstract String toString();

}
