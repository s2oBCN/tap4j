package org.tap4j.tokens;

import org.tap4j.error.Mark;

public class CommentToken extends Token {

	private final String comment;
	
	public CommentToken(String comment, Mark startMark, Mark endMark) {
		super(startMark, endMark);
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}
	
	@Override
	public ID getTokenId() {
		return ID.Comment;
	}

}
