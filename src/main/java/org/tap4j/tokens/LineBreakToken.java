package org.tap4j.tokens;

import org.tap4j.error.Mark;

public class LineBreakToken extends Token {

	public LineBreakToken(Mark startMark, Mark endMark) {
        super(startMark, endMark);
    }
	
	@Override
	public ID getTokenId() {
		return ID.LineBreak;
	}

}
