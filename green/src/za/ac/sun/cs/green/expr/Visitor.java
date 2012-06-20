package za.ac.sun.cs.green.expr;

public abstract class Visitor {

	public void preVisit(Constant constant) {
		preVisit((Expression) constant);
	}

	public void preVisit(Expression expression) {
	}

	public void preVisit(IntConstant intConstant) {
		preVisit((Constant) intConstant);
	}

	public void preVisit(IntVariable intVariable) {
		preVisit((Variable) intVariable);
	}

	public void preVisit(Operation operation) {
		preVisit((Expression) operation);
	}

	public void preVisit(RealConstant realConstant) {
		preVisit((Constant) realConstant);
	}

	public void preVisit(RealVariable realVariable) {
		preVisit((Variable) realVariable);
	}

	public void preVisit(Variable variable) {
		preVisit((Expression) variable);
	}

	public void postVisit(Constant constant) {
		postVisit((Expression) constant);
	}

	public void postVisit(Expression expression) {
	}

	public void postVisit(IntConstant intConstant) {
		postVisit((Constant) intConstant);
	}

	public void postVisit(IntVariable intVariable) {
		postVisit((Variable) intVariable);
	}

	public void postVisit(Operation operation) {
		postVisit((Expression) operation);
	}

	public void postVisit(RealConstant realConstant) {
		postVisit((Constant) realConstant);
	}

	public void postVisit(RealVariable realVariable) {
		postVisit((Variable) realVariable);
	}

	public void postVisit(Variable variable) {
		postVisit((Expression) variable);
	}

}
