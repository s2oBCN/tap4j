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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.junit.Test;
import org.tap4j.model.TestSet;
import org.tap4j.parser.TAP13Parser;
import org.tap4j.reader.StreamReader;

public class TestConsumer {

    // header_plan_tr_footer.tap
    @Test
    public void testConsumerHeaderPlanTrFooter() throws FileNotFoundException {
        final String fileName = "header_plan_tr_footer.tap";
        FileReader reader = new FileReader(getClass().getResource(fileName)
                .getFile());
        Consumer consumer = new Consumer(new TAP13Parser(new StreamReader(
                reader)));
        TestSet testSet = consumer.getTestSet();

        assertNotNull(testSet.getHeader());
        assertNotNull(testSet.getPlan());
        assertTrue(testSet.getNumberOfTestResults() == 2);
        assertTrue(testSet.getTestResults().get(0).getDescription()
                .equals("Test 1"));
        assertNotNull(testSet.getFooter());
        assertNotNull(testSet.getFooter().getComment());
    }

}
