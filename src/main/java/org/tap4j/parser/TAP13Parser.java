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

package org.tap4j.parser;

import java.util.Stack;

import org.tap4j.error.Mark;
import org.tap4j.events.Event;
import org.tap4j.events.Event.ID;
import org.tap4j.events.StreamStartEvent;
import org.tap4j.model.TestSet;
import org.tap4j.reader.StreamReader;
import org.tap4j.scanner.Scanner;
import org.tap4j.scanner.ScannerImpl;
import org.tap4j.tokens.StreamStartToken;
import org.tap4j.tokens.Token;
import org.tap4j.tokens.VersionToken;

public class TAP13Parser implements Parser {

    private final Scanner scanner;
    private Event currentEvent;
    private final Stack<Production> states;
    private final Stack<Mark> marks;
    private Production state;
    
    public TAP13Parser(StreamReader reader) {
        this.scanner = new ScannerImpl(reader);
        currentEvent = null;
        states = new Stack<Production>();
        marks = new Stack<Mark>();
        state = new ParseStreamStart();
    }
    
    /* (non-Javadoc)
     * @see org.tap4j.parser.Parser#checkEvent(org.tap4j.events.Event.ID)
     */
    public boolean checkEvent(ID choices) {
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
    
    // Productions.
    
    private class ParseStreamStart implements Production {
        public Event produce() {
            StreamStartToken token = (StreamStartToken) scanner.getToken();
            Event event = new StreamStartEvent(token.getStartMark(), token.getEndMark());
            // Prepare the next event
            state = new ParseVersion();
            return event;
        }
    }
    
    private class ParseVersion implements Production {
        public Event produce() {
            if (scanner.checkToken(Token.ID.TAPVersion)) {
                // VERSION
            } else {
                // PLAN
            }
            return null;
        }
    }
    
    public static void main(String[] args) throws Exception {
        String tap = "TAP version 13\n" +
        		"1..2\n" +
        		"ok 1\n" +
        		"not ok 2";
        StreamReader reader = new StreamReader(tap);
        Parser parser = new TAP13Parser(reader);
        Event event = parser.getEvent();
        System.out.println(event);
        event = parser.getEvent();
        System.out.println(event);
    }

}
