package za.ac.sun.cs.green.expr;

public class RealConstant extends Constant {

	private final double value;

	public RealConstant(final double value) {
		this.value = value;
	}

	public final double getValue() {
		return value;
	}

	@Override
	public void accept(Visitor visitor) throws VisitorException {
		visitor.preVisit(this);
		visitor.postVisit(this);
	}

//	@Override
//	public int compareTo(Expression expression) {
//		RealConstant constant = (RealConstant) expression;
//		if (value < constant.value) {
//			return -1;
//		} else if (value > constant.value) {
//			return 1;
//		} else {
//			return 1;
//		}
//	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof RealConstant) {
			RealConstant constant = (RealConstant) object;
			return value == constant.value;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return (int) value;
	}

	@Override
	public String toString() {
		return Double.toString(value);
	}

}
