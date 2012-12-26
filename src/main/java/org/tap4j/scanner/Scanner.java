package org.tap4j.scanner;

import org.tap4j.tokens.Token;

public interface Scanner {

	boolean checkToken(Token.ID... choices);
	
	Token peekToken();
	
	Token getToken();
	
}
