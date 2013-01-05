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

import org.tap4j.events.Event;
import org.tap4j.events.PlanEvent;
import org.tap4j.events.TestResultEvent;
import org.tap4j.events.VersionEvent;
import org.tap4j.model.Header;
import org.tap4j.model.Plan;
import org.tap4j.model.StatusValues;
import org.tap4j.model.TestResult;
import org.tap4j.model.TestSet;
import org.tap4j.parser.Parser;
import org.tap4j.parser.TAP13Parser;
import org.tap4j.reader.StreamReader;
import org.tap4j.tokens.TestResultToken.Status;

public class Consumer {

    private final Parser parser;
    
    public Consumer(Parser parser) {
        this.parser = parser;
    }
    
    public TestSet getTestSet() {
        // Drop STREAM-START
        parser.getEvent();
        
        TestSet testSet = new TestSet();
        
        // VERSION
        if (parser.checkEvent(Event.ID.Version)) {
            VersionEvent event = (VersionEvent) parser.getEvent();
            Header header = new Header(event.getVersion());
            testSet.setHeader(header);
        } 
        
        // PLAN
        if (parser.checkEvent(Event.ID.Plan)) {
            PlanEvent event = (PlanEvent) parser.getEvent();
            // TODO: skip and todo directives... skip all?
            Plan plan = new Plan(event.getBegin(), event.getEnd());
            testSet.setPlan(plan);
        }
        
        // TEST-RESULTS
        while (parser.checkEvent(Event.ID.TestResult)) {
            TestResultEvent event = (TestResultEvent) parser.getEvent();
            TestResult testResult = new TestResult();
            testResult.setStatus(event.getStatus() == Status.OK ? StatusValues.OK : StatusValues.NOT_OK);
            testResult.setDescription(event.getDescription());
            testResult.setTestNumber(event.getNumber());
            testSet.addTestResult(testResult);
        }
        
        return testSet;
    }
    
    public static void main(String[] args) {
        String stream = "TAP version 13\n" +
        		"1..2\n" +
        		"ok 1 no problemo bro # yah!\n" +
        		"not ok 2";
        Parser parser = new TAP13Parser(new StreamReader(stream));
        Consumer consumer = new Consumer(parser);
        TestSet testSet = consumer.getTestSet();
        System.out.println(testSet.getTestResults().size());
    }

}
