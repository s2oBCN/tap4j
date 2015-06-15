package org.tap4j.ext.jmeter.parser;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.tap4j.ext.jmeter.model.AbstractSample;
import org.tap4j.ext.jmeter.model.AssertionResult;
import org.tap4j.ext.jmeter.model.HttpSample;
import org.tap4j.model.Header;
import org.tap4j.model.Plan;
import org.tap4j.model.TestResult;
import org.tap4j.model.TestSet;
import org.tap4j.util.StatusValues;

/**
 * Parses Jmeter xml
 * 
 * @author s2o
 */
public class JmeterResultParser {

	private static final Integer TAP_VERSION = 13;
	private static final Integer INITIAL_TEST_STEP = 1;

	private static final String FAILURE_MESSAGE = "- FailureMessage:";
	private static final String FAIL_ASSERT = "failAssert ";
	private static final String ERROR = "error ";
	private static final String DUMP = "dump";
	private static final String SEVERITY = "severity";
	private static final String MESSAGE = "message";
	public static final String VALUE_SPLIT = " - ";

	private final Charset charset;

	public JmeterResultParser() {
		charset = Charset.defaultCharset();
	}

	public JmeterResultParser(Charset pCharset) {
		charset = pCharset;
	}

	/**
	 * Parses jMeter result file into TestSet and optionally generates a Tap file with the same name of the parsed file
	 * 
	 * @param file
	 * @param generateTapFile
	 * @return
	 */
	public TestSet parseFile(File file, boolean generateTapFile) {

		TestSet testSet = new TestSet();
		final Header header = new Header(TAP_VERSION);
		testSet.setHeader(header);
		FileUtils fileUtils = new FileUtils(charset);
		File fileScaped = fileUtils.replaceFileHTMLSpecialChars(file);
		List<AbstractSample> sampleResultList = fileUtils.getResultList(fileScaped);
		Plan plan = new Plan(INITIAL_TEST_STEP, sampleResultList.size());
		testSet.setPlan(plan);

		for (AbstractSample httpSample : sampleResultList) {
			List<AssertionResult> assetionResultList = httpSample.getAssertionResult();
			boolean resultError = false;
			String failitureMessage = "";
			String severity = "";
			// Searching an assertion failed
			for (AssertionResult assertionResult : assetionResultList) {
				resultError = (assertionResult.isFailure() || assertionResult.isError());
				if (resultError) {
					failitureMessage += FAILURE_MESSAGE + assertionResult.getFailureMessage();

					// Log the type of fail
					if (assertionResult.isFailure()) {
						severity = FAIL_ASSERT;
					}
					if (assertionResult.isError()) {
						severity += ERROR;
					}
				}
			}

			TestResult testResult = new TestResult();
			testResult.setDescription(httpSample.getLb());
			StatusValues status = StatusValues.OK;
			if (resultError) {
				final Map<String, Object> yamlish = testResult.getDiagnostic();
				createYAMLishMessage(yamlish, httpSample, failitureMessage);
				createYAMLishSeverity(yamlish, severity);
				createYAMLishDump(yamlish, httpSample);
				status = StatusValues.NOT_OK;
			}
			testResult.setStatus(status);
			testSet.addTestResult(testResult);
		}

		if (generateTapFile) {
			fileUtils.generateTapFile(file, testSet);
		}

		return testSet;
	}

	/**
	 * @param yamlish
	 * @param testNgTestResult
	 */
	private void createYAMLishMessage(Map<String, Object> yamlish, AbstractSample httpSample, String failitureMessage) {
		yamlish.put(MESSAGE, httpSample.getRc() + VALUE_SPLIT + httpSample.getRm() + failitureMessage);
	}

	/**
	 * @param yamlish
	 * @param testNgTestResult
	 */
	private void createYAMLishSeverity(Map<String, Object> yamlish, String severity) {
		yamlish.put(SEVERITY, severity);
	}

	/**
	 * @param yamlish
	 * @param testNgTestResult
	 */
	private void createYAMLishDump(Map<String, Object> yamlish, AbstractSample httpSample) {
		String dump = "";
		if (httpSample instanceof HttpSample && httpSample != null) {
			dump = dump + requestHeader((HttpSample) httpSample);
			dump = dump + queryString((HttpSample) httpSample);
			dump = dump + response((HttpSample) httpSample);
			dump = dump.replaceAll("(?<=<data>)(.*?)(?=</data>)", "...");
			dump = dump.replaceAll("(?<=<binaryContent>)(.*?)(?=</binaryContent>)", "...");
		}
		yamlish.put(DUMP, dump);
	}

	private String requestHeader(HttpSample httpSample) {
		String dump = "requestHeader: ";
		if (httpSample.getRequestHeader() != null) {
			dump = dump + httpSample.getRequestHeader().getValue() + FileUtils.LINE_SEPARATOR;
		}
		return dump;
	}

	private String queryString(HttpSample httpSample) {
		String dump = "queryString: ";
		if (httpSample.getQueryString() != null) {
			dump = dump + httpSample.getQueryString().getValue() + FileUtils.LINE_SEPARATOR;
		}
		return dump;
	}

	private String response(HttpSample httpSample) {
		String dump = "response: ";
		if (httpSample.getResponseData() != null) {
			dump = dump + httpSample.getResponseData().getValue() + FileUtils.LINE_SEPARATOR;
		}
		return dump;
	}
}
