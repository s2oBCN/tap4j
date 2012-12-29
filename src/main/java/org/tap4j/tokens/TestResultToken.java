/*
 * The MIT License
 * 
 * Copyright (c) 2010 tap4j team (see AUTHORS)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.tap4j.tokens;

import org.tap4j.error.Mark;

public class TestResultToken extends Token {
	public enum Status {
		OK, NOT_OK
	}

	private final Status status;
	private final int number;
	private final String description;
	private final CommentToken comment;
	
	public TestResultToken(Status status, int number, String description, CommentToken comment, Mark startMark, Mark endMark) {
		super(startMark, endMark);
		this.status = status;
		this.number = number;
		this.description = description;
		this.comment = comment;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public int getNumber() {
		return number;
	}
	
	public String getDescription() {
		return description;
	}
	
	public CommentToken getComment() {
		return comment;
	}

	@Override
	public ID getTokenId() {
		return ID.TestResult;
	}

}
