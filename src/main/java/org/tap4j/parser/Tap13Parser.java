/*
 * The MIT License
 *
 * Copyright (c) <2012> <Bruno P. Kinoshita>
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
package org.tap4j.parser;


import org.tap4j.error.Mark;
import org.tap4j.event.Event;
import org.tap4j.event.StreamStartEvent;
import org.tap4j.reader.StreamReader;
import org.tap4j.scanner.Scanner;
import org.tap4j.scanner.ScannerImpl;
import org.tap4j.token.StreamStartToken;
import org.tap4j.util.ArrayStack;

/**
 * 
 * @author Bruno P. Kinoshita - http://www.kinoshita.eti.br
 * @since 0.1
 */
public class Tap13Parser implements Parser {

	private final Scanner scanner;
	private Event currentEvent;
	private String tapVersion;
	private final ArrayStack<Production> states;
	private final ArrayStack<Mark> marks;
	private Production state;
	
	public Tap13Parser(StreamReader reader) {
		this.scanner = new ScannerImpl(reader);
		currentEvent = null;
		tapVersion = "13"; // TBD: check if that's really right
		states = new ArrayStack<Production>(100);
		marks = new ArrayStack<Mark>(10);
		state = new ParseStreamStart();
	}
	
	/* (non-Javadoc)
	 * @see org.tap4j.parser.Parser#checkEvent()
	 */
	public boolean checkEvent(Event.ID choices) {
		peekEvent();
        if (currentEvent != null) {
            if (currentEvent.is(choices)) {
                return true;
            }
        }
        return false;
	}

	/* (non-Javadoc)
	 * @see org.tap4j.parser.Parser#peekEvent()
	 */
	public Event peekEvent() {
		if (currentEvent == null) {
            if (state != null) {
                currentEvent = state.produce();
            }
        }
        return currentEvent;
	}

	/* (non-Javadoc)
	 * @see org.tap4j.parser.Parser#getEvent()
	 */
	public Event getEvent() {
		peekEvent();
        Event value = currentEvent;
        currentEvent = null;
        return value;
    }
	
	/**
     * <pre>
     * stream    ::= STREAM-START implicit_document? explicit_document* STREAM-END
     * implicit_document ::= block_node DOCUMENT-END*
     * explicit_document ::= DIRECTIVE* DOCUMENT-START block_node? DOCUMENT-END*
     * </pre>
     */
    private class ParseStreamStart implements Production {
        public Event produce() {
            // Parse the stream start.
            StreamStartToken token = (StreamStartToken) scanner.getToken();
            Event event = new StreamStartEvent(token.getStartMark(), token.getEndMark());
            // Prepare the next state.
            state = new ParseImplicitDocumentStart();
            return event;
        }
    }
    
    private static class ParseImplicitDocumentStart implements Production {
    	/* (non-Javadoc)
    	 * @see org.tap4j.parser.Production#produce()
    	 */
    	public Event produce() {
    		return null;
    	}
    }

}
