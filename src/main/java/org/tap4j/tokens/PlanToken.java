package org.tap4j.tokens;

import org.tap4j.error.Mark;

public class PlanToken extends Token {

	private final int begin;
	private final int end;
	
	public PlanToken(int begin, int end, Mark startMark, Mark endMark) {
		super(startMark, endMark);
		this.begin = begin;
		this.end = end;
	}
	
	public int getBegin() {
		return begin;
	}
	
	public int getEnd() {
		return end;
	}

	@Override
	public ID getTokenId() {
		return ID.Plan;
	}

}
