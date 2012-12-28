package org.tap4j.scanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tap4j.error.Mark;
import org.tap4j.reader.StreamReader;
import org.tap4j.tokens.CommentToken;
import org.tap4j.tokens.PlanToken;
import org.tap4j.tokens.StreamEndToken;
import org.tap4j.tokens.StreamStartToken;
import org.tap4j.tokens.TapVersionToken;
import org.tap4j.tokens.Token;
import org.tap4j.tokens.Token.ID;
import org.tap4j.tokens.UnknownToken;

public class ScannerImpl implements Scanner {
	
	private final StreamReader reader;
	private List<Token> tokens;
	private Stack<Integer> indents;
	
	private boolean done = false;
	//private int indent = -1;
	private int tokensTaken = 0;
	
	public ScannerImpl(StreamReader reader) {
		this.reader = reader;
		this.tokens = new ArrayList<Token>(100);
		this.indents = new Stack<Integer>();
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
			this.tokensTaken++;
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
			fetchComment();
			return;
		}
		if (checkPlan()) {
			fetchPlan();
			return;
		}
		if (checkTestStatus()) {
			fetchTestResult();
			return;
		}
		
		fetchUnkownToken();
		return;
	}
	
	private String scanVersion() {
        String line = reader.readLine();
        if (line.matches("^TAP\\s+version\\s+(\\d*)")) {
        	return line;        	
        }
        return "";
    }
	
	private boolean checkPlan() {
		return reader.readLine().matches("^\\d*\\.\\.\\d*");
	}
	
	private boolean checkTestStatus() {
		// TODO Auto-generated method stub
		return false;
	}
	
	// Fetchers.

	private void fetchComment() {
		Mark startMark = reader.getMark();
		final String comment = reader.readLineForward().trim();
		Mark endMark = reader.getMark();
		Token unknownToken = new CommentToken(comment, startMark, endMark);
		this.tokens.add(unknownToken);
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
			Token unknownToken = new PlanToken(begin, end, startMark, endMark);
			this.tokens.add(unknownToken);
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
		// TODO Auto-generated method stub
		
	}
    
    private void fetchUnkownToken() {
		Mark startMark = reader.getMark();
		String line = reader.readLineForward();
		Mark endMark = reader.getMark();
		Token unknownToken = new UnknownToken(line, startMark, endMark);
		this.tokens.add(unknownToken);
	}
    
    private Token fetchVersion(String version) {
		// Read the token.
        Mark startMark = reader.getMark();
        int versionNumber = Integer.parseInt(version.substring(version.lastIndexOf(' ')+1));
        reader.forward(version.length());
		Mark endMark = reader.getMark();
		Token token = new TapVersionToken(versionNumber, startMark, endMark);
		tokens.add(token);
		return token;
	}
    
    // Scanners.

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
            if (scanLineBreak().length() != 0) {// found a line-break
                // TBD: do something with line break?
            } else {
                found = true;
            }
        }
	}
    
    /**
     * Scan a line break, transforming:
     * 
     * <pre>
     * '\r\n' : '\n'
     * '\r' : '\n'
     * '\n' : '\n'
     * '\x85' : '\n'
     * default : ''
     * </pre>
     */
    private String scanLineBreak() {
        // Transforms:
        // '\r\n' : '\n'
        // '\r' : '\n'
        // '\n' : '\n'
        // '\x85' : '\n'
        // default : ''
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
    
	public static void main(String[] args) {
		//Scanner scanner = new ScannerImpl(new StreamReader("ok 1 everything is fine # yohooo\n"));
		Scanner scanner = new ScannerImpl(new StreamReader("TAP version 13\n1..2\n# eae, beleza??"));
		Token token = null;
		do {
			token = scanner.getToken();
			if(token != null)
				System.out.println(token);
			token = scanner.peekToken();
		} while (token != null && !(token instanceof StreamEndToken));
		System.out.println(token);
	}
	
}
