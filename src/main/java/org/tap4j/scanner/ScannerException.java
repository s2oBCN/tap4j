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

import org.tap4j.error.Mark;
import org.tap4j.error.MarkedTAPException;

/**
 * Exception thrown by the {@link Scanner} implementations in case of malformed
 * input.
 */
public class ScannerException extends MarkedTAPException {

    private static final long serialVersionUID = 4782293188600445954L;

    /**
     * Constructs an instance.
     * 
     * @param context Part of the input document in which vicinity the problem
     *            occurred.
     * @param contextMark Position of the <code>context</code> within the
     *            document.
     * @param problem Part of the input document that caused the problem.
     * @param problemMark Position of the <code>problem</code> within the
     *            document.
     * @param note Message for the user with further information about the
     *            problem.
     */
    public ScannerException(String context, Mark contextMark, String problem,
            Mark problemMark, String note) {
        super(context, contextMark, problem, problemMark, note);
    }

    /**
     * Constructs an instance.
     * 
     * @param context Part of the input document in which vicinity the problem
     *            occurred.
     * @param contextMark Position of the <code>context</code> within the
     *            document.
     * @param problem Part of the input document that caused the problem.
     * @param problemMark Position of the <code>problem</code> within the
     *            document.
     */
    public ScannerException(String context, Mark contextMark, String problem,
            Mark problemMark) {
        this(context, contextMark, problem, problemMark, null);
    }
}
