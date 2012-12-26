package org.tap4j.scanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.tap4j.reader.StreamReader;
import org.tap4j.tokens.Token;
import org.tap4j.tokens.Token.ID;

public class ScannerImpl implements Scanner {
	
	private final StreamReader reader;
	private List<Token> tokens;
	private Stack<Integer> indents;
	
	private boolean done = false;
	private int indent = -1;
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
	
	private void fetchMoreTokens() {
		scanToNextToken();
		unwindIndent(reader.getColumn());
		char ch = reader.peek();
	}

}
