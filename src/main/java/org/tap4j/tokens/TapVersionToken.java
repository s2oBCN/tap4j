package org.tap4j.tokens;

import org.tap4j.error.Mark;

public class TapVersionToken extends Token {

	private final int version;
	
	public TapVersionToken(int version, Mark startMark, Mark endMark) {
		super(startMark, endMark);
		this.version = version;
	}

	@Override
	public ID getTokenId() {
		return ID.TAPVersion;
	}

	public int getVersion() {
		return version;
	}

}
