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

import org.tap4j.events.BailOutEvent;
import org.tap4j.events.Event;
import org.tap4j.events.Event.ID;
import org.tap4j.events.FooterEvent;
import org.tap4j.events.PlanEvent;
import org.tap4j.events.StreamEndEvent;
import org.tap4j.events.StreamStartEvent;
import org.tap4j.events.TestResultEvent;
import org.tap4j.events.VersionEvent;
import org.tap4j.reader.StreamReader;
import org.tap4j.scanner.Scanner;
import org.tap4j.scanner.ScannerImpl;
import org.tap4j.tokens.AbstractToken;
import org.tap4j.tokens.BailOutToken;
import org.tap4j.tokens.FooterToken;
import org.tap4j.tokens.PlanToken;
import org.tap4j.tokens.StreamEndToken;
import org.tap4j.tokens.StreamStartToken;
import org.tap4j.tokens.TestResultToken;
import org.tap4j.tokens.VersionToken;

public class TAP13Parser implements Parser {

    private final Scanner scanner;
    private Event currentEvent;
    // private final Stack<Production> states;
    // private final Stack<Mark> marks;
    private Production state;
    private boolean planSet = false;

    public TAP13Parser(StreamReader reader) {
        this.scanner = new ScannerImpl(reader);
        currentEvent = null;
        // states = new Stack<Production>();
        // marks = new Stack<Mark>();
        state = new ParseStreamStart();
    }

    /*
     * (non-Javadoc)
     * 
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

    /*
     * (non-Javadoc)
     * 
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

    /*
     * (non-Javadoc)
     * 
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
            Event event = new StreamStartEvent(token.getStartMark(),
                    token.getEndMark());
            // Prepare the next event
            if (scanner.checkToken(AbstractToken.ID.Version)) {
                state = new ParseVersion();
            } else if (scanner.checkToken(AbstractToken.ID.Plan)) {
                state = new ParsePlan(/* beginning */true);
            } else if (scanner.checkToken(AbstractToken.ID.TestResult)) {
                state = new ParseTestResult();
            } else if (scanner.checkToken(AbstractToken.ID.BailOut)) {
                state = new ParseBailOut();
            } else if (!scanner.checkToken(AbstractToken.ID.StreamEnd)) {
                throw new ParserException(null, null,
                        "expected '<version>, <plan> or <test result>', but found "
                                + scanner.peekToken().getTokenId(), scanner
                                .peekToken().getStartMark());
            } else if (scanner.checkToken(AbstractToken.ID.Footer)) {
                state = new ParseFooter();
            } else {
                state = new ParseStreamEnd();
            }
            return event;
        }
    }

    private class ParseStreamEnd implements Production {
        public Event produce() {
            if (scanner.checkToken(AbstractToken.ID.StreamEnd)) {
                StreamEndToken token = (StreamEndToken) scanner.getToken();
                Event event = new StreamEndEvent(token.getStartMark(),
                        token.getEndMark());
                return event;
            } else {
                throw new ParserException(null, null,
                        "expected '<end stream>', but found "
                                + scanner.peekToken().getTokenId(), scanner
                                .peekToken().getStartMark());
            }
        }
    }

    private class ParseVersion implements Production {
        public Event produce() {
            VersionToken token = (VersionToken) scanner.getToken();
            Event event = new VersionEvent(token.getVersion(),
                    token.getComment(), token.getStartMark(),
                    token.getEndMark());
            if (scanner.checkToken(AbstractToken.ID.Plan)) {
                state = new ParsePlan(true);
            } else {
                state = new ParseTestResult();
            }
            return event;
        }
    }

    private class ParsePlan implements Production {
        private final boolean beginning;

        public ParsePlan(boolean beginning) {
            this.beginning = beginning;
        }

        public Event produce() {
            PlanToken token = (PlanToken) scanner.getToken();
            if (planSet) {
                throw new ParserException(null, null,
                        "found '<plan>' defined twice", token.getStartMark());
            }
            planSet = true;
            Event event = new PlanEvent(token.getBegin(), token.getEnd(),
                    token.getStartMark(), token.getEndMark());
            if (beginning && scanner.peekToken() instanceof TestResultToken) {
                state = new ParseTestResult();
            } else if (scanner.checkToken(AbstractToken.ID.BailOut)) {
                state = new ParseBailOut();
            } else if (scanner.peekToken() instanceof FooterToken) {
                state = new ParseFooter();
            } else {
                state = new ParseStreamEnd();
            }
            return event;
        }
    }

    private class ParseTestResult implements Production {
        public Event produce() {
            TestResultToken token = (TestResultToken) scanner.getToken();
            Event event = new TestResultEvent(token.getStatus(),
                    token.getNumber(), token.getDescription(),
                    token.getComment(), token.getSkip(), token.getTodo(),
                    token.getStartMark(), token.getEndMark());
            if (scanner.checkToken(AbstractToken.ID.TestResult)) {
                state = new ParseTestResult();
            } else if (scanner.checkToken(AbstractToken.ID.Plan)) {
                state = new ParsePlan(false);
            } else if (scanner.checkToken(AbstractToken.ID.BailOut)) {
                state = new ParseBailOut();
            } else if (scanner.checkToken(AbstractToken.ID.Footer)) {
                state = new ParseFooter();
            } else {
                state = new ParseStreamEnd();
            }
            return event;
        }
    }

    private class ParseFooter implements Production {
        public Event produce() {
            FooterToken token = (FooterToken) scanner.getToken();
            Event event = new FooterEvent(token.getFooter(),
                    token.getComment(), token.getStartMark(),
                    token.getEndMark());
            state = new ParseStreamEnd();
            return event;
        }
    }

    private class ParseBailOut implements Production {
        public Event produce() {
            BailOutToken token = (BailOutToken) scanner.getToken();
            Event event = new BailOutEvent(token.getDescription(),
                    token.getComment(), token.getStartMark(),
                    token.getEndMark());
            if (scanner.checkToken(AbstractToken.ID.TestResult)) {
                state = new ParseTestResult();
            } else if (scanner.checkToken(AbstractToken.ID.Plan)) {
                state = new ParsePlan(false);
            } else if (scanner.checkToken(AbstractToken.ID.Footer)) {
                state = new ParseFooter();
            } else {
                state = new ParseStreamEnd();
            }
            return event;
        }
    }

    public static void main(String[] args) throws Exception {
        String tap = "# a comment before the version... dan dan dan dannn...\n"
                + "TAP version 13\n" + "1..2\n" + "ok 1 nope\n"
                + "not ok 2 #SKIP yo # d\n" + "ok 3 #TODO enhance it\n"
                + "TAP nothing\n";
        StreamReader reader = new StreamReader(tap);
        Parser parser = new TAP13Parser(reader);
        Event event = null;
        while (!(event instanceof StreamEndEvent)) {
            event = parser.getEvent();
            System.out.println(event);
        }
    }

}
