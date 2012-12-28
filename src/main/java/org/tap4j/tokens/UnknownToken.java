package org.tap4j.tokens;

import org.tap4j.error.Mark;

public class UnknownToken extends Token {
	
	private final String line;

	public UnknownToken(String line, Mark startMark, Mark endMark) {
		super(startMark, endMark);
		this.line = line;
	}
	
	public String getLine() {
		return line;
	}
	
	@Override
	public ID getTokenId() {
		return ID.Unknown;
	}
	
}
