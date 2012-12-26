package org.tap4j.scanner;

import java.util.Arrays;

public class Constant {

	private final static String LINEBR_S = "\n\u0085\u2028\u2029";
	private final static String FULL_LINEBR_S = "\r" + LINEBR_S;
	private final static String NULL_OR_LINEBR_S = "\0" + FULL_LINEBR_S;
    
	public final static Constant LINEBR = new Constant(LINEBR_S);
	public final static Constant NULL_OR_LINEBR = new Constant(NULL_OR_LINEBR_S);
	
	private String content;
    boolean[] contains = new boolean[128];
    boolean noASCII = false;

    private Constant(String content) {
        Arrays.fill(contains, false);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);
            if (ch < 128)
                contains[ch] = true;
            else
                sb.append(ch);
        }
        if (sb.length() > 0) {
            noASCII = true;
            this.content = sb.toString();
        }
    }
    
    public boolean has(char ch) {
        return (ch < 128) ? contains[ch] : noASCII && content.indexOf(ch, 0) != -1;
    }

    public boolean hasNo(char ch) {
        return !has(ch);
    }

    public boolean has(char ch, String additional) {
        return has(ch) || additional.indexOf(ch, 0) != -1;
    }

    public boolean hasNo(char ch, String additional) {
        return !has(ch, additional);
    }
}
