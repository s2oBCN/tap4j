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

package org.tap4j.consumer.issue3525607;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.List;

import org.junit.Test;
import org.tap4j.consumer.BaseConsumerTest;
import org.tap4j.consumer.Consumer;
import org.tap4j.model.Comment;
import org.tap4j.model.TestResult;
import org.tap4j.model.TestSet;
import org.tap4j.parser.Parser;
import org.tap4j.parser.TAP13Parser;
import org.tap4j.reader.StreamReader;

/**
 * Tests for issue 3525607
 * 
 * @author Bruno P. Kinoshita - http://www.kinoshita.eti.br
 * @since 3.0
 */
public class TestIssue3525607 extends BaseConsumerTest {

    @Test
    public void testTestResultWithCommentDiagnostics() {
        final String tap = "1..2\n" + "ok 1 - OK\n" + "# No errors found\n"
                           + "not ok 2\n" + "# Invalid stream character\n"
                           + "# Missing end transmission signal\n"
                           + "# Aborting mission!\n" + "ok";
        Parser parser = new TAP13Parser(new StreamReader(new StringReader(
                tap)));
        Consumer consumer = new Consumer(parser);
        TestSet testSet = consumer.getTestSet();
        
        assertTrue(testSet.getTestResults().size() == 2);

        assertEquals("No errors found", testSet.getTestResult(1).getComments().get(0)
            .getText());

        String expected = "Invalid stream character\n"
                          + "Missing end transmission signal\n"
                          + "Aborting mission!\n";

        StringBuilder actualCommentText = new StringBuilder();
        TestResult testResult = testSet.getTestResult(2);
        List<Comment> comments = testResult.getComments();

        for (Comment comment : comments) {
            actualCommentText.append(comment.getText());
            actualCommentText.append("\n");
        }

        assertEquals(actualCommentText.toString(), expected);
    }

}
