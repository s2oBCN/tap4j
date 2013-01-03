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
import org.tap4j.tokens.AbstractToken;
import org.tap4j.tokens.AbstractToken.ID;
import org.tap4j.tokens.CommentableToken;
import org.tap4j.tokens.DiagnosticableToken;
import org.tap4j.tokens.PlanToken;
import org.tap4j.tokens.Skip;
import org.tap4j.tokens.StreamEndToken;
import org.tap4j.tokens.StreamStartToken;
import org.tap4j.tokens.TestResultToken;
import org.tap4j.tokens.TestResultToken.Status;
import org.tap4j.tokens.Todo;
import org.tap4j.tokens.VersionToken;

public class ScannerImpl implements Scanner {

    private final StreamReader reader;
    private List<AbstractToken> tokens;
    // private Stack<Integer> indents;
    private AbstractToken latestToken = null;
    private boolean done = false;

    // private int indent = -1;
    // private int tokensTaken = 0;

    public ScannerImpl(StreamReader reader) {
        this.reader = reader;
        this.tokens = new ArrayList<AbstractToken>();
        // this.indents = new Stack<Integer>();
        fetchStreamStart();
    }

    public boolean checkToken(ID... choices) {
        while (needMoreTokens()) {
            fetchMoreTokens();
        }
        if (!this.tokens.isEmpty()) {
            if (choices.length == 0) {
                return true;
            }
            AbstractToken.ID first = this.tokens.get(0).getTokenId();
            for (int i = 0; i < choices.length; i++) {
                if (first == choices[i]) {
                    return true;
                }
            }
        }
        return false;
    }

    public AbstractToken peekToken() {
        while (needMoreTokens()) {
            fetchMoreTokens();
        }
        return this.tokens.get(0);
    }

    public AbstractToken getToken() {
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
            final String comment = reader.readLineForward().trim();
            handleComment(comment);
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
        final String unknown = reader.readLineForward().trim();
        handleUnknown(unknown);
        // return; // already implicit
    }

    private void addToken(AbstractToken token) {
        this.latestToken = token;
        this.tokens.add(token);
    }

    private void handleComment(String comment) {
        if (latestToken != null && latestToken instanceof CommentableToken) {
            ((CommentableToken) latestToken).addComment(comment);
        }
    }

    private void handleUnknown(String line) {
        if (latestToken != null && latestToken instanceof DiagnosticableToken) {
            ((DiagnosticableToken) latestToken).addDiagnostics(line);
        }
    }

    // Fetchers.

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
            AbstractToken planToken = new PlanToken(begin, end, startMark,
                    endMark);
            addToken(planToken);
        } else {
            handleUnknown(plan);
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
        AbstractToken token = new StreamStartToken(mark, mark);
        addToken(token);
    }

    private void fetchStreamEnd() {
        // TBD: ?Set the current intendation to -1.
        // unwindIndent(-1);

        // Read the token.
        Mark mark = reader.getMark();

        // Add STREAM-END.
        AbstractToken token = new StreamEndToken(mark, mark);
        addToken(token);

        // The stream is finished.
        this.done = true;
    }

    private void fetchTestResult() {
        Mark startMark = reader.getMark();

        // match against ^(ok|not ok)\s+\d*.*
        int index = 2;
        String statusText = reader.prefix(index);
        char ch = '\0';
        boolean flag = false;
        if (!"ok".equals(statusText)) {
            index = 6;
            statusText = reader.prefix(index);
            if (!"not ok".equals(statusText)) {
                throw new ScannerException("while scanning a test result",
                        reader.getMark(), "could not find test status",
                        reader.getMark());
            }
        }
        Status status = "ok".equals(statusText) ? Status.OK : Status.NOT_OK;

        ch = reader.peek(index);
        StringBuilder buffer = new StringBuilder();
        final int number;
        if (' ' == ch) {
            while (true) {
                ++index;
                ch = reader.peek(index);
                if (' ' == ch || Constant.NULL_OR_LINEBR.has(ch)) {
                    if (!flag)
                        continue;
                    else
                        break;
                }
                flag = true;
                buffer.append(ch);
            }
            String r = buffer.toString();
            if (r.length() > 0) {
                try {
                    number = Integer.parseInt(r);
                } catch (NumberFormatException nfe) {
                    throw new ScannerException("while scanning a test result",
                            reader.getMark(), "could not find test number",
                            reader.getMark());
                }
            } else {
                throw new ScannerException("while scanning a test result",
                        reader.getMark(), "invalid test result",
                        reader.getMark());
            }
        } else {
            throw new ScannerException("while scanning a test result",
                    reader.getMark(), "could not find test number",
                    reader.getMark());
        }

        ch = reader.peek(index);
        buffer = new StringBuilder();
        String description = "";
        if (' ' == ch) {
            while (true) {
                ch = reader.peek(index);
                if ('#' != ch && !Constant.LINEBR.has(ch))
                    buffer.append(ch);
                else
                    break;
                ++index;
            }
            description = buffer.toString().trim();
        }

        ch = reader.peek(index);
        buffer = new StringBuilder();
        String comment = "";
        Skip skip = null;
        Todo todo = null;
        // Comment or Directive
        if ('#' == ch) {
            while (true) {
                ch = reader.peek(index);
                if (!Constant.NULL_OR_LINEBR.has(ch))
                    buffer.append(ch);
                else
                    break;
                ++index;
            }
            final String r = buffer.toString().trim();
            if (r.matches("\\s?#\\s?SKIP.*")) {
                Pattern pattern = Pattern.compile("\\s?#\\s?SKIP(.*)");
                Matcher matcher = pattern.matcher(r);
                if (matcher.matches() && matcher.groupCount() >= 1) {
                    skip = new Skip(matcher.group(1).trim());
                }
            } else if (r.matches("\\s?#\\s?TODO.*")) {
                Pattern pattern = Pattern.compile("\\s?#\\s?TODO(.*)");
                Matcher matcher = pattern.matcher(r);
                if (matcher.matches() && matcher.groupCount() >= 1) {
                    todo = new Todo(matcher.group(1).trim());
                }
            } else {
                comment = buffer.toString().trim();
            }
        }

        Mark endMark = reader.getMark();
        AbstractToken testResultToken;
        testResultToken = new TestResultToken(status, number, description,
                comment, skip, todo, startMark, endMark);
        reader.forward(index);
        // Add TEST-RESULT.
        addToken(testResultToken);
    }

    private void fetchVersion(String version) {
        // Read the token.
        Mark startMark = reader.getMark();
        int versionNumber = Integer.parseInt(version.substring(version
                .lastIndexOf(' ') + 1));
        reader.forward(version.length());
        Mark endMark = reader.getMark();

        // Add VERSION.
        AbstractToken token = new VersionToken(versionNumber, startMark,
                endMark);
        addToken(token);
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
        // match against ^(ok|not ok)\s+\d*.*
        int index = 2;
        String maybeStatus = reader.prefix(index);
        char ch = '\0';
        boolean flag = false;
        if (!"ok".equals(maybeStatus)) {
            index = 6;
            maybeStatus = reader.prefix(index);
            if (!"not ok".equals(maybeStatus)) {
                return false;
            }
        }
        ch = reader.peek(index);
        StringBuilder buffer = new StringBuilder();
        if (' ' == ch) {
            while (true) {
                ++index;
                ch = reader.peek(index);
                if (' ' == ch || Constant.NULL_OR_LINEBR.has(ch)) {
                    if (!flag)
                        continue;
                    else
                        break;
                }
                flag = true;
                buffer.append(ch);
            }
            String r = buffer.toString();
            if (r.length() > 0) {
                try {
                    Integer.parseInt(r);
                    return true;
                } catch (NumberFormatException nfe) {
                    return false;
                }
            }
        }
        return false;
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
        if (line.matches("^TAP\\s+version\\s+(\\d+)")) {
            return line;
        }
        return "";
    }

    public static void main(String[] args) {
        Scanner scanner = new ScannerImpl(
                new StreamReader(
                        "TAP version 13\n"
                                + "1..1\n"
                                + "ok 1 #TODO"));
        AbstractToken token = null;
        do {
            token = scanner.getToken();
            if (token != null) {
                System.out.println(token);
            }
            token = scanner.peekToken();
        } while (token != null && !(token instanceof StreamEndToken));
        System.out.println(token);
    }
    
    public static void main2(String[] args) {
        Scanner scanner = new ScannerImpl(
                new StreamReader(
                        "TAP version 14\n"
                                + "1..11\n"
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
                                + "ok 9 Hey buddy\n" + "not ok 9\r\n"
                                + "not ok 10  y  \n" + "1..4\n" + "done\n\n"
                        		+ "ok 11 #TODO"));
        AbstractToken token = null;
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
