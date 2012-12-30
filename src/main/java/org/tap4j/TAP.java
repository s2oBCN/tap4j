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

package org.tap4j;

import java.io.InputStream;
import java.io.Reader;

import org.tap4j.consumer.Consumer;
import org.tap4j.model.TestSet;
import org.tap4j.reader.StreamReader;
import org.tap4j.reader.UnicodeReader;

public class TAP {
    private String name;
    protected Consumer consumer;
    
    public TAP() {
        this(null);
    }
    
    public TAP(Consumer consumer) {
        super();
        this.consumer = consumer;
        this.name = "TAP:" + System.identityHashCode(this);
    }

    public TestSet load(String tap) {
        return loadFromReader(new StreamReader(tap));
    }
    
    public TestSet load(InputStream io) {
        return loadFromReader(new StreamReader(new UnicodeReader(io)));
    }
    
    public TestSet load(Reader reader) {
        return loadFromReader(new StreamReader(reader));
    }

    /**
     * @param streamReader
     * @return
     */
    private TestSet loadFromReader(StreamReader streamReader) {
        return null;
    }
    
//    public Iterable<Event> parse(Reader tap) {
//        final Parser parser = 
//    }
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return name;
    }
    
}
