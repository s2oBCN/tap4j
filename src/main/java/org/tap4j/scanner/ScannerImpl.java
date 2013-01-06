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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tap4j.consumer.Consumer;
import org.tap4j.error.Mark;
import org.tap4j.model.TestSet;
import org.tap4j.parser.Parser;
import org.tap4j.parser.TAP13Parser;
import org.tap4j.reader.StreamReader;
import org.tap4j.tokens.AbstractToken;
import org.tap4j.tokens.AbstractToken.ID;
import org.tap4j.tokens.BailOutToken;
import org.tap4j.tokens.CommentableToken;
import org.tap4j.tokens.DiagnosticableToken;
import org.tap4j.tokens.FooterToken;
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
        if (scanVersion()) {
            fetchVersion();
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

        // a FOOTER.
        if (scanFooter()) {
            fetchFooter();
            return;
        }
        
        // a BAIL-OUT
        if (scanBailOut()) {
            fetchBailOut();
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

    private void eatSpaces() {
        eatSpaces(/* index */ 0);
    }
    
    private void eatSpaces(int index) {
        char ch = reader.peek(index);
        while (ch == ' ') {
            ++index;
            ch = reader.peek(index);
        }
        if (index > 0) {
            reader.forward(index);
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

    private void fetchVersion() {
        Mark startMark = reader.getMark();
        // match against ^TAP\s+version\s+(\d+)\s+#(.*)
        String tapEntry = reader.prefixForward("TAP".length());
        char ch = '\0';
        if (!"TAP".equals(tapEntry)) {
            throw new ScannerException(null, null, "could not find TAP text",
                    reader.getMark());
        }

        this.scanToNextToken();
        String versionEntry = reader.prefixForward("version".length());
        if (!"version".equals(versionEntry)) {
            throw new ScannerException(null, null,
                    "could not find version text", reader.getMark());
        }

        eatSpaces();
        ch = reader.peek();
        StringBuilder buffer = new StringBuilder();
        int versionNumber = -1;
        int index = 0;
        if (!Constant.NULL_OR_LINEBR.has(reader.peek())) {
            while (true) {
                ch = reader.peek(index);
                if (' ' != ch && !Constant.NULL_OR_LINEBR.has(ch))
                    buffer.append(ch);
                else
                    break;
                ++index;
            }
            try {
                versionNumber = Integer.parseInt(buffer.toString().trim());
            } catch (NumberFormatException nfe) {
                throw new ScannerException(null, null,
                        "expected <number> but got <string>", reader.getMark());
            }
        } else {
            throw new ScannerException(null, null,
                    "could not find version <number>", reader.getMark());
        }
        reader.forward(index);

        eatSpaces();
        index = 0;
        ch = reader.peek(index);
        buffer = new StringBuilder();
        String comment = "";
        // Comment or Directive
        if ('#' == ch) {
            ++index;
            while (true) {
                ch = reader.peek(index);
                if (!Constant.NULL_OR_LINEBR.has(ch))
                    buffer.append(ch);
                else
                    break;
                ++index;
            }
            comment = buffer.toString().trim();
        }
        
        eatSpaces();
        if (!Constant.NULL_OR_LINEBR.has(reader.peek(index))) {
            throw new ScannerException(null, null, "extra characters found",
                    reader.getMark());
        }

        Mark endMark = reader.getMark();
        AbstractToken versionToken = new VersionToken(versionNumber, comment,
                startMark, endMark);
        reader.forward(index);
        // Add VERSION.
        addToken(versionToken);
    }

    private void fetchFooter() {
        Mark startMark = reader.getMark();

        // match against ^TAP\s+(?!#)(.*)\s+#(.*)
        int index = 3;
        String footerEntry = reader.prefix(index);
        char ch = '\0';
        if (!"TAP".equals(footerEntry)) {
            throw new ScannerException("while scanning footer",
                    reader.getMark(), "could not find footer text",
                    reader.getMark());
        }

        ch = reader.peek(index);
        StringBuilder buffer = new StringBuilder();
        String description = "";
        if (' ' == ch) {
            while (true) {
                ch = reader.peek(index);
                if ('#' != ch && !Constant.NULL_OR_LINEBR.has(ch))
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
        // Comment or Directive
        if ('#' == ch) {
            ++index;
            while (true) {
                ch = reader.peek(index);
                if (!Constant.NULL_OR_LINEBR.has(ch))
                    buffer.append(ch);
                else
                    break;
                ++index;
            }
            comment = buffer.toString().trim();
        }

        Mark endMark = reader.getMark();
        AbstractToken footerToken;
        footerToken = new FooterToken(description, comment, startMark, endMark);
        reader.forward(index);
        // Add FOOTER.
        addToken(footerToken);
    }
    
    private void fetchBailOut() {
        Mark startMark = reader.getMark();

        // match against ^Bail out!\s+(?!#)(.*)\s+#(.*)
        String bailEntry = reader.prefixForward("Bail".length());
        if (!"Bail".equals(bailEntry)) {
            throw new ScannerException("while scanning footer",
                    reader.getMark(), "could not find footer text",
                    reader.getMark());
        }
        
        eatSpaces();
        String outEntry = reader.prefixForward("out!".length());
        if (!"out!".equals(outEntry)) {
            throw new ScannerException("while scanning footer",
                    reader.getMark(), "could not find footer text",
                    reader.getMark());
        }
        
        eatSpaces();
        int index = 0;
        char ch = reader.peek(index);
        StringBuilder buffer = new StringBuilder();
        String description = "";
        while (true) {
            ch = reader.peek(index);
            if ('#' != ch && !Constant.NULL_OR_LINEBR.has(ch))
                buffer.append(ch);
            else
                break;
            ++index;
        }
        description = buffer.toString().trim();

        ch = reader.peek(index);
        buffer = new StringBuilder();
        String comment = "";
        // Comment or Directive
        if ('#' == ch) {
            ++index;
            while (true) {
                ch = reader.peek(index);
                if (!Constant.NULL_OR_LINEBR.has(ch))
                    buffer.append(ch);
                else
                    break;
                ++index;
            }
            comment = buffer.toString().trim();
        }

        Mark endMark = reader.getMark();
        AbstractToken footerToken;
        footerToken = new BailOutToken(description, comment, startMark, endMark);
        reader.forward(index);
        // Add BAIL-OUT.
        addToken(footerToken);
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

    private boolean scanVersion() {
        String line = reader.readLine();
        return line.matches("^TAP\\s+version\\s+(\\d+)\\s?(#.*)?");
    }

    private boolean scanFooter() {
        String line = reader.readLine();
        return line.matches("^TAP\\s+(.*)");
    }
    
    private boolean scanBailOut() {
        String line = reader.readLine();
        return line.matches("^Bail out!.*");
    }

    public static void main(String[] args) {
        StringBuilder tapStream = new StringBuilder();

        tapStream.append("TAP version 13 # the header\n");
        tapStream.append("1..1\n");
        tapStream.append("ok 1\n");
        tapStream
                .append("Bail out! Out of memory exception # Contact admin! 9988\n");
        Parser parser = new TAP13Parser(new StreamReader(new StringReader(
                tapStream.toString())));
        Consumer consumer = new Consumer(parser);
        TestSet testSet = consumer.getTestSet();
        System.out.println(testSet);
    }
    
    public static void main2(String[] args) {
        Scanner scanner = new ScannerImpl(new StreamReader("TAP version 13\n"
                + "1..1\n" + "ok 1 #TODO"));
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

    public static void main3(String[] args) {
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
                                + "ok 11 #TODO\n" + "TAP finished #yo"));
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
