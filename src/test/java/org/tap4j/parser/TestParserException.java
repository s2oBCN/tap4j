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

package org.tap4j.parser;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.tap4j.error.Mark;

public class TestParserException {

    private static ParserException exception = null;
    
    private static final String context = "<le_context>";
    private static final Mark contextMark = new Mark("contextMark", 0, 1, 2, "<buffer1>", 3);
    private static final String problem = "<le_problem>";
    private static final Mark problemMark = new Mark("problemMark", 3, 2, 1, "<buffer2>", 0);
    
    @BeforeClass
    public static void setUp() {
        exception = new ParserException(context, contextMark, problem, problemMark);
    }
    
    @Test
    public void testParserException() {
        assertEquals(exception.getContext(), context);
        assertEquals(exception.getContextMark(), contextMark);
        assertEquals(exception.getProblem(), problem);
        assertEquals(exception.getProblemMark(), problemMark);
    }
    
}
