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

package org.tap4j.consumer;

import java.io.File;

import org.tap4j.error.TAPException;
import org.tap4j.model.TestSet;

/**
 * A TAP consumer. Responsible for consuming a TAP stream and producing a Test
 * Set.
 */
public interface Consumer {

    /**
     * Consume a TAP File.
     * 
     * @param file TAP File.
     * @return TestSet
     * @throws TAPException
     */
    TestSet load(File file) throws TAPException;

    /**
     * Consume a TAP Stream.
     * 
     * @param tapStream TAP Stream
     * @return TestSet
     * @throws TAPException
     */
    TestSet load(String tapStream) throws TAPException;

}
