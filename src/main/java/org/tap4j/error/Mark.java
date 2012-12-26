package org.tap4j.error;

import org.tap4j.scanner.Constant;

public final class Mark {
	private String name;
	private int index;
	private int line;
	private int column;
	private String buffer;
	private int pointer;
	
	public Mark(String name, int index, int line, int column, String buffer, int pointer) {
		super();
		this.name = name;
		this.index= index;
		this.line =line;
		this.column = column;
		this.buffer = buffer;
		this.pointer = pointer;
	}
	
	public String getName() {
		return name;
	}
	
	public int getLine() {
		return line;
	}
	
	public int getColumn() {
		return column;
	}
	
	public int getIndex() {
		return index;
	}
	
	private boolean isLineBreak(char ch) {
		return Constant.NULL_OR_LINEBR.has(ch);
	}
	
	public String getSnippet(int indent, int maxLength) {
		if (buffer == null) {
			return null;
		}
		float half = maxLength / 2 - 1;
		int start = pointer;
		String head = "";
		while ((start > 0) && (!isLineBreak(buffer.charAt(start - 1)))) {
			start -= 1;
			if (pointer - start > half) {
				head = " ... ";
				start += 5;
				break;
			}
		}
		String tail = "";
		int end = pointer;
		while ((end < buffer.length()) && (!isLineBreak(buffer.charAt(end)))) {
			end += 1;
			if (end - pointer > half) {
				tail = " ... ";
				end -= 5;
				break;
			}
		}
		String snippet = buffer.substring(start, end);
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < indent ; i++) {
			result.append(" ");
		}
		result.append(head);
		result.append(snippet);
		result.append(tail);
		result.append("\n");
		for (int i = 0; i < indent + pointer - start + head.length() ; i++) {
			result.append(" ");
		}
		result.append("^");
		return result.toString();
	}
	
	public String getSnippet() {
		return getSnippet(4, 75);
	}
	
	@Override
	public String toString() {
		String snippet = getSnippet();
		StringBuilder where = new StringBuilder(" in ");
		where.append(name);
		where.append(", line ");
		where.append(line + 1);
		where.append(", column ");
		where.append(column + 1);
		if (snippet != null) {
			where.append(":\n");
			where.append(snippet);
		}
		return where.toString();
	}
}
