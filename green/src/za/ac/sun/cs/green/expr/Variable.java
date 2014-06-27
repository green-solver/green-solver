package za.ac.sun.cs.green.expr;

import java.io.Serializable;

public abstract class Variable extends Expression implements Serializable {

	private static final long serialVersionUID = -1712398155778326862L;

	private final String name;

	private final Object original;

	public Variable(final String name) {
		this.name = name;
		this.original = null;
	}
	
	public Variable(final String name, final Object original) {
		this.name = name;
		this.original = original;
	}

	public final String getName() {
		return name;
	}

	public final Object getOriginal() {
		return original;
	}

}
