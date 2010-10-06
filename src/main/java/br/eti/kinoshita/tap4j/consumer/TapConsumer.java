/* 
 * The MIT License
 * 
 * Copyright (c) 2010 Bruno P. Kinoshita <http://www.kinoshita.eti.br>
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
package br.eti.kinoshita.tap4j.consumer;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

import br.eti.kinoshita.tap4j.model.BailOut;
import br.eti.kinoshita.tap4j.model.Comment;
import br.eti.kinoshita.tap4j.model.Header;
import br.eti.kinoshita.tap4j.model.Plan;
import br.eti.kinoshita.tap4j.model.TapResult;
import br.eti.kinoshita.tap4j.model.Footer;
import br.eti.kinoshita.tap4j.model.TestResult;
import br.eti.kinoshita.tap4j.model.TestSet;

/**
 * TAP Consumer is the responsible for generating the TAP Stream.
 * 
 * @author Bruno P. Kinoshita - http://www.kinoshita.eti.br
 * @since 1.0
 */
public interface TapConsumer
{
	
	/* -- Reading methods -- */
	
	/**
	 * Parses a Test Result.
	 * 
	 * @param testResult Test Result line
	 */
	public void parseLine( String tapLine ) 
	throws TapParserException;
	
	/**
	 * Parses a Tap Stream.
	 * 
	 * @param tapStream Tap Stream
	 */
	public void parseTapStream( String tapStream ) 
	throws TapParserException;
	
	/**
	 * Parses a Tap File.
	 * 
	 * @param tapFile Tap File
	 */
	public void parseFile( File tapFile ) 
	throws TapParserException;
	
	/* -- Test Execution information methods -- */
	
	/**
	 * @return Header.
	 */
	public Header getHeader();
	
	/**
	 * @return Plan.
	 */
	public Plan getPlan();

	/**
	 * @return List of TAP Lines.
	 */
	public List<TapResult> getTapLines();
	
	/**
	 * @return List of Test Results.
	 */
	public List<TestResult> getTestResults();
	
	/**
	 * @return List of Bail Outs.
	 */
	public List<BailOut> getBailOuts();
	
	/**
	 * @return List of Comments.
	 */
	public List<Comment> getComments();
	
	/**
	 * @return Number of TAP Lines.
	 */
	public Integer getNumberOfTapLines();
	
	/**
	 * @return Number of Test Results.
	 */
	public Integer getNumberOfTestResults();
	
	/**
	 * @return Number of Bail Outs.
	 */
	public Integer getNumberOfBailOuts();
	
	/**
	 * @return Number of Comments.
	 */
	public Integer getNumberOfComments();
	
	/**
	 * @param testNumber Number of test to be retrieved.
	 * @return A single test result.
	 */
	public TestResult getTestResult( Integer testNumber );
	
	/**
	 * @return true if any not ok occurred.
	 */
	public Boolean containsNotOk();
	
	/**
	 * @return true if any ok occurred.
	 */
	public Boolean containsOk();
	
	/**
	 * @return true if the Stream contains any Bail Outs.
	 */
	public Boolean containsBailOut();
	
	/**
	 * @return Footer.
	 */
	public Footer getFooter();
	
	/**
	 * @return Test Set.
	 */
	public TestSet getTestSet();
	
	/* -- Output methods -- */
	
	/**
	 * Prints the summary of tests to a Print Writer.
	 * 
	 * @param pw Print Writer.
	 */
	public void printSummary( PrintWriter pw );
	
	/**
	 * Prints the summary of tests to a Print Stream.
	 * 
	 * @param ps Print Stream.
	 */
	public void printSummary( PrintStream ps );
	
	/**
	 * Prints the details of tests to a Print Writer.
	 * 
	 * @param pw Print Writer.
	 */
	public void printDetails( PrintWriter pw );
	
	/**
	 * Prints the details of tests to a Print Stream.
	 * 
	 * @param ps Print Stream.
	 */
	public void printDetails(PrintStream ps);
	
}