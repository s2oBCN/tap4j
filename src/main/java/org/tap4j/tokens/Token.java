package org.tap4j.tokens;

import org.tap4j.error.Mark;
import org.tap4j.error.TAPException;

public abstract class Token {
	public enum ID {
		TAPVersion, Plan, TestResult
	}
	
	private final Mark startMark;
	private final Mark endMark;
	
	public Token(Mark startMark, Mark endMark) {
		if (startMark == null || endMark == null) {
			throw new TAPException("Token requires marks.");
		}
		this.startMark = startMark;
		this.endMark = endMark;
	}
	
	public Mark getStartMark() {
		return startMark;
	}
	
	public Mark getEndMark() {
		return endMark;
	}
	
	protected String getArguments() {
		return "";
	}
	
	public abstract Token.ID getTokenId();
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Token) {
			return toString().equals(obj.toString());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public String toString() {
		return "<" + this.getClass().getName() + "(" + getArguments() + ")>";
	}
}
