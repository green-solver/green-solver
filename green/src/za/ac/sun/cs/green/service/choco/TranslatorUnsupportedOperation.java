package za.ac.sun.cs.green.service.choco;

import za.ac.sun.cs.green.expr.VisitorException;

@SuppressWarnings("serial") class TranslatorUnsupportedOperation extends VisitorException {

	public TranslatorUnsupportedOperation(String message) {
		super(message);
	}

}