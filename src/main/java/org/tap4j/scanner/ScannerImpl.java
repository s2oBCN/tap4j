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

package org.tap4j.scanner;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tap4j.error.Mark;
import org.tap4j.reader.StreamReader;
import org.tap4j.tokens.CommentToken;
import org.tap4j.tokens.PlanToken;
import org.tap4j.tokens.StreamEndToken;
import org.tap4j.tokens.StreamStartToken;
import org.tap4j.tokens.VersionToken;
import org.tap4j.tokens.TestResultToken;
import org.tap4j.tokens.TestResultToken.Status;
import org.tap4j.tokens.Token;
import org.tap4j.tokens.Token.ID;
import org.tap4j.tokens.UnknownToken;

public class ScannerImpl implements Scanner {

    private final StreamReader reader;
    private List<Token> tokens;
    // private Stack<Integer> indents;

    private boolean done = false;

    // private int indent = -1;
    // private int tokensTaken = 0;

    public ScannerImpl(StreamReader reader) {
        this.reader = reader;
        this.tokens = new ArrayList<Token>();
        // this.indents = new Stack<Integer>();
        fetchStreamStart();
    }

    public boolean checkToken( ID... choices ) {
        while (needMoreTokens()) {
            fetchMoreTokens();
        }
        if (!this.tokens.isEmpty()) {
            if (choices.length == 0) {
                return true;
            }
            Token.ID first = this.tokens.get(0).getTokenId();
            for (int i = 0; i < choices.length; i++) {
                if (first == choices[i]) {
                    return true;
                }
            }
        }
        return false;
    }

    public Token peekToken() {
        while (needMoreTokens()) {
            fetchMoreTokens();
        }
        return this.tokens.get(0);
    }

    public Token getToken() {
        if (!this.tokens.isEmpty()) {
            // this.tokensTaken++;
            return this.tokens.remove(0);
        }
        return null;
    }

    private boolean needMoreTokens() {
        if (this.done) {
            return false;
        }
        if (this.tokens.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * Fetch one or more tokens from the StreamReader.
     */
    private void fetchMoreTokens() {
        scanToNextToken();
        // unwindIndent(reader.getColumn());
        // VERSION
        String version = scanVersion();
        if (version.length() > 0) {
            fetchVersion(version);
            return;
        }
        char ch = reader.peek();
        switch (ch) {
        case '\0':
            // Is it the end of stream?
            fetchStreamEnd();
            return;
        case '#':
            // a COMMENT.
            fetchComment();
            return;
        }
        // a PLAN.
        if (scanPlan()) {
            fetchPlan();
            return;
        }
        // a TEST-RESULT.
        if (scanTestResult()) {
            fetchTestResult();
            return;
        }
        // fetch an UNKNOWN token, but attach it to latest processed token
        // if possible. In some documentation and API's, this kind of token is
        // used as COMMENT too.
        fetchUnkownToken();
        // return; // already implicit
    }

    // Fetchers.

    private void fetchComment() {
        final String comment = reader.readLineForward().trim();

        // Add COMMENT.
        Token token = this.fetchComment(comment);
        this.tokens.add(token);
    }

    private Token fetchComment( String comment ) {
        return fetchComment(comment, false);
    }

    private Token fetchComment( String comment, boolean inline ) {
        Mark startMark = reader.getMark();
        Mark endMark = reader.getMark();
        return new CommentToken(comment, inline, startMark, endMark);
    }

    private void fetchPlan() {
        Mark startMark = reader.getMark();
        final String plan = reader.readLineForward().trim();
        Mark endMark = reader.getMark();
        Pattern pattern = Pattern.compile("^(\\d*)\\.\\.(\\d*)$");
        Matcher matcher = pattern.matcher(plan);
        if (matcher.matches() && matcher.groupCount() == 2) {
            int begin = Integer.parseInt(matcher.group(1));
            int end = Integer.parseInt(matcher.group(2));

            // Add PLAN.
            Token unknownToken = new PlanToken(begin, end, startMark, endMark);
            this.tokens.add(unknownToken);
        } else {
            this.fetchUnkownToken(plan);
        }
    }

    /**
     * We always add STREAM-START as the first token and STREAM-END as the last
     * token.
     */
    private void fetchStreamStart() {
        // Read the token.
        Mark mark = reader.getMark();

        // Add STREAM-START.
        Token token = new StreamStartToken(mark, mark);
        this.tokens.add(token);
    }

    private void fetchStreamEnd() {
        // TBD: ?Set the current intendation to -1.
        // unwindIndent(-1);

        // Read the token.
        Mark mark = reader.getMark();

        // Add STREAM-END.
        Token token = new StreamEndToken(mark, mark);
        this.tokens.add(token);

        // The stream is finished.
        this.done = true;
    }

    private void fetchTestResult() {
        String line = reader.readLineForward();
        Pattern pattern = Pattern
                .compile("^(ok|not ok)\\s+(\\d*)\\s?(.*)?(#.*)?");
        Matcher matcher = pattern.matcher(line);
        if (matcher.matches() && matcher.groupCount() >= 2) {
            Token testResultToken;
            int groups = matcher.groupCount();
            String statusText = matcher.group(1);
            Status status = "ok".equals(statusText) ? Status.OK : Status.NOT_OK;
            int number = Integer.parseInt(matcher.group(2));
            Mark startMark = reader.getMark();
            Mark endMark = reader.getMark();
            switch (groups) {
            case 2:
                testResultToken = new TestResultToken(status, number, null,
                        null, startMark, endMark);
                break;
            case 3:
                testResultToken = new TestResultToken(status, number,
                        matcher.group(3), null, startMark, endMark);
                break;
            case 4:
                String comment = matcher.group(4);
                Token commentToken = this.fetchComment(comment, /* inline */
                        true);
                testResultToken = new TestResultToken(status, number,
                        matcher.group(3), (CommentToken) commentToken,
                        startMark, endMark);
                break;
            default:
                testResultToken = new UnknownToken(line, startMark, endMark);
            }

            // Add TEST-RESULT.
            this.tokens.add(testResultToken);
        } else {
            this.fetchUnkownToken(line);
        }
    }

    private void fetchUnkownToken() {
        String line = reader.readLineForward();
        this.fetchUnkownToken(line);
    }

    private void fetchUnkownToken( String token ) {
        Mark startMark = reader.getMark();
        Mark endMark = reader.getMark();

        // Add an UNKNOWN.
        Token unknownToken = new UnknownToken(token, startMark, endMark);
        this.tokens.add(unknownToken);
    }

    private void fetchVersion( String version ) {
        // Read the token.
        Mark startMark = reader.getMark();
        int versionNumber = Integer.parseInt(version.substring(version
                .lastIndexOf(' ') + 1));
        reader.forward(version.length());
        Mark endMark = reader.getMark();

        // Add VERSION.
        Token token = new VersionToken(versionNumber, startMark, endMark);
        tokens.add(token);
    }

    // Scanners.

    private String scanLineBreak() {
        char ch = reader.peek();
        if (ch == '\r' || ch == '\n' || ch == '\u0085') {
            if (ch == '\r' && '\n' == reader.peek(1)) {
                reader.forward(2);
            } else {
                reader.forward();
            }
            return "\n";
        } else if (ch == '\u2028' || ch == '\u2029') {
            reader.forward();
            return String.valueOf(ch);
        }
        return "";
    }

    private boolean scanPlan() {
        return reader.readLine().matches("^\\d*\\.\\.\\d*");
    }

    private boolean scanTestResult() {
        String line = reader.readLine();
        return line.matches("^(ok|not ok)\\s+\\d*.*");
    }

    private void scanToNextToken() {
        // If there is a byte order mark (BOM) at the beginning of the stream,
        // forward past it.
        if (reader.getIndex() == 0 && reader.peek() == '\uFEFF') {
            reader.forward();
        }

        boolean found = false;
        while (!found) {
            int ff = 0;
            // Peek ahead until we find the first non-space character, then
            // move forward directly to that character.
            while (reader.peek(ff) == ' ') {
                ff++;
            }
            if (ff > 0) {
                reader.forward(ff);
            }
            // If we scanned a line break, then (depending on flow level),
            // simple keys may be allowed.
            if (scanLineBreak().length() == 0) {// did not found a line-break
                found = true;
            }
        }
        // this.indent = reader.getIndex();
    }

    private String scanVersion() {
        String line = reader.readLine();
        if (line.matches("^TAP\\s+version\\s+(\\d*)")) {
            return line;
        }
        return "";
    }

    public static void main( String[] args ) {
        Scanner scanner = new ScannerImpl(
                new StreamReader(
                        "TAP version 13\n"
                                + "1..10\n"
                                + "# eae, beleza??\n"
                                + "ok 1 what's up buddy? # a comment is always good, righto?\n\r\n"
                                + "not ok 2 - something's wrong here...\n"
                                + "ok 3    \n"
                                + "ok 4\n"
                                + "# comments are good\n"
                                + "not ok 5\n"
                                + "# something bad happened harry\n"
                                + "# IOException: java.util.....\n"
                                + "ok 6 - Ya-hoo! No regressions!\n"
                                + "not ok 7 #SKIP\r\n"
                                + "ok 8 #TODO not enough memory on this computer\n"
                                + "ok 8 Hey buddy\n" + "not ok 9\r\n"
                                + "not ok 10    \n" + "1..4\n" + "done\n\n"));
        Token token = null;
        do {
            token = scanner.getToken();
            if (token != null) {
                System.out.println(token);
            }
            token = scanner.peekToken();
        } while (token != null && !(token instanceof StreamEndToken));
        System.out.println(token);
    }

}
