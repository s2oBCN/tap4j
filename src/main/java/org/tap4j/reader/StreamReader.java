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

package org.tap4j.reader;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;

import org.tap4j.error.Mark;
import org.tap4j.error.TAPException;
import org.tap4j.scanner.Constant;

public class StreamReader {

    private String name;
    private String buffer;
    private final Reader stream;
    private boolean eof = true;
    private char[] data;
    private int index = 0;
    private int line = 0;
    private int column = 0;
    private int pointer = 0;

    public StreamReader(String stream) {
        this.name = "'string'";
        this.buffer = ""; // to set length to 0
        this.buffer = stream + "\0";
        this.stream = null;
        this.eof = true;
        this.data = null;
    }

    public StreamReader(Reader reader) {
        this.name = "'reader'";
        this.buffer = "";
        this.stream = reader;
        this.eof = false;
        this.data = new char[1024];
        this.update();
    }

    public Mark getMark() {
        return new Mark(name, index, line, column, buffer, pointer);
    }

    public void forward() {
        forward(1);
    }

    /**
     * Read the next length characters and move the pointer.
     * 
     * @param length
     */
    public void forward(int length) {
        if (this.pointer + length + 1 >= this.buffer.length()) {
            update();
        }
        char ch = 0;
        for (int i = 0; i < length; i++) {
            ch = this.buffer.charAt(this.pointer);
            this.pointer++;
            this.index++;
            if (Constant.LINEBR.has(ch)
                    || (ch == '\r' && buffer.charAt(pointer) != '\n')) {
                this.line++;
                this.column = 0;
            } else if (ch != '\uFEFF') {
                this.column++;
            }
        }
    }

    public char peek() {
        return this.buffer.charAt(this.pointer);
    }

    public char peek(int index) {
        if (this.pointer + index + 1 > this.buffer.length()) {
            update();
        }
        return this.buffer.charAt(this.pointer + index);
    }

    public String prefix(int length) {
        if (this.pointer + length >= this.buffer.length()) {
            update();
        }
        if (this.pointer + length > this.buffer.length()) {
            return this.buffer.substring(this.pointer);
        }
        return this.buffer.substring(this.pointer, this.pointer + length);
    }

    public String prefixForward(int length) {
        final String prefix = prefix(length);
        this.pointer += length;
        this.index += length;
        this.column += length;
        return prefix;
    }

    public String readLine() {
        char ch = '\0';
        int ff = 0;
        StringBuilder buffer = new StringBuilder();
        loop: while (true) {
            ch = peek(ff);
            switch (ch) {
            case '\0':
                break loop;
            case '\r':
                break loop;
            case '\n':
                break loop;
            case '\u0085':
                break loop;
            case '\u2028':
                break loop;
            case '\u2029':
                break loop;
            default:
                buffer.append(peek(ff));
                ff++;
                break;
            }
        }
        return buffer.toString();
    }

    public String readLineForward() {
        final String line = readLine();
        this.pointer += line.length();
        this.index += line.length();
        this.column += line.length();
        return line;
    }

    private void update() {
        if (!this.eof) {
            this.buffer = buffer.substring(this.pointer);
            this.pointer = 0;
            try {
                int converted = this.stream.read(data);
                if (converted > 0) {
                    this.buffer = new StringBuilder(buffer.length() + converted)
                            .append(buffer).append(data, 0, converted)
                            .toString();
                } else {
                    this.eof = true;
                    this.buffer += "\0";
                }
            } catch (IOException ioe) {
                throw new TAPException(ioe);
            }
        }
    }

    public int getColumn() {
        return column;
    }

    public int getLine() {
        return line;
    }

    public int getIndex() {
        return index;
    }

    public Charset getEncoding() {
        return Charset.forName(((UnicodeReader) this.stream).getEncoding());
    }

}
