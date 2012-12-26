package org.tap4j.error;

public class TAPException extends RuntimeException {
	private static final long serialVersionUID = -8169829254199892762L;
	
	public TAPException(String message) {
		super(message);
	}
	
	public TAPException(Throwable cause) {
		super(cause);
	}
	
	public TAPException(String message, Throwable cause) {
		super(message, cause);
	}
}
