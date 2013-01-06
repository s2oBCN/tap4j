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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;

import org.junit.Test;
import org.tap4j.model.TestSet;
import org.tap4j.parser.Parser;
import org.tap4j.parser.TAP13Parser;
import org.tap4j.reader.StreamReader;

/**
 * Tests for Consumers.
 * 
 * @see org.tap4j.consumer.Consumer
 */
public class TestConsumer extends BaseConsumerTest {

    // valid tap streams.

    // header_plan.tap
    @Test
    public void testConsumerHeaderPlan() {
        TestSet testSet = getTestSet("header_plan.tap");

        assertNotNull(testSet.getHeader());
        assertNotNull(testSet.getPlan());
        assertTrue(testSet.getNumberOfTestResults() == 0);
        assertNull(testSet.getFooter());
    }

    // header_plan_tr.tap
    @Test
    public void testConsumerHeaderPlanTr() {
        TestSet testSet = getTestSet("header_plan_tr.tap");

        assertNotNull(testSet.getHeader());
        assertNotNull(testSet.getPlan());
        assertTrue(testSet.getNumberOfTestResults() == 2);
        assertTrue(testSet.getTestResults().get(0).getDescription()
                .equals("Test 1"));
        assertNull(testSet.getFooter());
    }

    // header_plan_tr_footer.tap
    @Test
    public void testConsumerHeaderPlanTrFooter() {
        TestSet testSet = getTestSet("header_plan_tr_footer.tap");

        assertNotNull(testSet.getHeader());
        assertNotNull(testSet.getPlan());
        assertTrue(testSet.getNumberOfTestResults() == 2);
        assertTrue(testSet.getTestResults().get(0).getDescription()
                .equals("Test 1"));
        assertNotNull(testSet.getFooter());
        assertNotNull(testSet.getFooter().getComment());
    }

    // header_tr_plan.tap
    @Test
    public void testConsumerHeaderTrPlan() {
        TestSet testSet = getTestSet("header_tr_plan.tap");

        assertTrue(testSet.getTestResults().size() == 2);
        assertNotNull(testSet.getPlan());
        // Assert.assertFalse(
        // ((Tap13YamlParser)consumer).isPlanBeforeTestResult() );
    }

    // plan_comment_tr_footer.tap
    @Test
    public void testConsumerPlanCommentTrFooter() {
        TestSet testSet = getTestSet("plan_comment_tr_footer.tap");

        assertNull(testSet.getHeader());
        assertTrue(testSet.getTestResults().size() == 3);
        assertNotNull(testSet.getPlan());
        // Assert.assertTrue(
        // ((TapConsumerImpl)consumer).isPlanBeforeTestResult() );
        assertTrue(testSet.getTestResults().size() == testSet.getPlan()
                .getLastTestNumber());
        assertNotNull(testSet.getFooter());
    }

    // plan_tr.tap
    @Test
    public void testConsumerPlanTr() {
        TestSet testSet = getTestSet("plan_tr.tap");
        assertNull(testSet.getHeader());
        assertTrue(testSet.getTestResults().size() == 2);
        assertNotNull(testSet.getPlan());
        // assertTrue(
        // ((TapConsumerImpl)consumer).isPlanBeforeTestResult() );
        assertTrue(testSet.getTestResults().size() == testSet.getPlan()
                .getLastTestNumber());
        assertNull(testSet.getFooter());
    }

    /*
     * Tests a TapConsumer with a single Test Result.
     */
    // single_tr.tap
    @Test
    public void testWithSingleTestResult() {
        TestSet testSet = getTestSet("single_tr.tap");

        assertNotNull(testSet);
        assertTrue(testSet.getNumberOfTestResults() == 1);
    }

    @Test
    public void testConsumerTapStream1AndPrintDetails() {
        StringBuilder tapStream = new StringBuilder();

        tapStream.append("TAP version 13 # the header\n");
        tapStream.append("1..1\n");
        tapStream.append("ok 1\n");
        tapStream
                .append("Bail out! Out of memory exception # Contact admin! 9988\n");

        Parser parser = new TAP13Parser(new StreamReader(new StringReader(
                tapStream.toString())));
        Consumer consumer = new Consumer(parser);
        TestSet testSet = consumer.getTestSet();

        assertTrue(testSet.getPlan().getLastTestNumber() == 1);
        assertNotNull(testSet.getHeader());
        assertNotNull(testSet.getHeader().getComment());
        assertEquals("Out of memory exception", testSet.getBailOuts().get(0).getReason());
        assertEquals("Contact admin! 9988", testSet.getBailOuts().get(0).getComment().getText());

    }

}
