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

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.tap4j.model.TestSet;
import org.tap4j.parser.TAP13Parser;
import org.tap4j.reader.StreamReader;

/**
 * Base class for consumer tests.
 */
public abstract class BaseConsumerTest {

    /**
     * Get a file reader from a file name. The file is loaded using the current
     * class to get the file as a resource.
     * 
     * @param fileName File name.
     * @return FileReader.
     * @throws RuntimeException when it could not get the reader.
     */
    protected FileReader getFileReader(String fileName) {
        FileReader reader;
        try {
            reader = new FileReader(getClass().getResource(fileName).getFile());
            return reader;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Failed to get reader for [" + fileName
                + "]");
    }

    protected TestSet getTestSet(String fileName) {
        FileReader reader = getFileReader(fileName);
        Consumer consumer = new Consumer(new TAP13Parser(new StreamReader(
                reader)));
        return consumer.getTestSet();
    }

}
