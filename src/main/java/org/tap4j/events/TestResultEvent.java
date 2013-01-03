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

package org.tap4j.events;

import org.tap4j.error.Mark;
import org.tap4j.tokens.Skip;
import org.tap4j.tokens.TestResultToken;
import org.tap4j.tokens.TestResultToken.Status;
import org.tap4j.tokens.Todo;

public class TestResultEvent extends Event {

    private final Status status;
    private final int number;
    private final String description;
    private final String comment;
    private final Skip skip;
    private final Todo todo;

    public TestResultEvent(Status status, int number, String description,
            String comment, Mark startMark, Mark endMark) {
        this(status, number, description, comment, null, null, startMark,
                endMark);
    }

    public TestResultEvent(Status status, int number, String description,
            String comment, Skip skip, Todo todo, Mark startMark, Mark endMark) {
        super(startMark, endMark);
        this.status = status;
        this.number = number;
        this.description = description;
        this.comment = comment;
        this.skip = skip;
        this.todo = todo;
    }

    /**
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * @return the number
     */
    public int getNumber() {
        return number;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }
    
    /**
     * @return the skip
     */
    public Skip getSkip() {
        return skip;
    }
    
    /**
     * @return the todo
     */
    public Todo getTodo() {
        return todo;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.tap4j.events.Event#is(org.tap4j.events.Event.ID)
     */
    @Override
    public boolean is(ID id) {
        return ID.TestResult == id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tap4j.events.Event#getArguments()
     */
    @Override
    protected String getArguments() {
        StringBuilder args = new StringBuilder();
        args.append("status=");
        args.append((this.status == TestResultToken.Status.OK ? "ok" : "not ok"));
        args.append(", number=" + number);
        args.append(", description=" + description);
        args.append(", comment=" + comment);
        args.append(", skip=" + skip);
        args.append(", todo=" + todo);
        return args.toString();
    }

}
