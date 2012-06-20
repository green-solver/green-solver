package za.ac.sun.cs.green.expr;

public abstract class Variable extends Expression {

	private String name;

	public Variable(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
