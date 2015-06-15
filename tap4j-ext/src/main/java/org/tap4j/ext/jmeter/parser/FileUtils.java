package org.tap4j.ext.jmeter.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.tap4j.ext.jmeter.model.AbstractSample;
import org.tap4j.ext.jmeter.model.ObjectFactory;
import org.tap4j.ext.jmeter.model.TestResults;
import org.tap4j.model.TestSet;
import org.tap4j.parser.ParserException;
import org.tap4j.producer.Producer;
import org.tap4j.producer.TapProducerFactory;
import org.tap4j.representer.DumperOptions;

public class FileUtils {

	private final Charset charset;

	// Line separator.
	public static final CharSequence LINE_SEPARATOR = "\r\n";

	// File's extensions
	private static final String ESC_HTML_EXT = ".escapedHtml.xml";
	private static final CharSequence TAP_EXT = ".tap";

	// SPECIAL HTML CHART THAT CAN CAUSE PARSE PROBLEMS
	private static final String HTML_SPECIAL_CHART = "&#";

	public FileUtils(Charset pCharset) {
		charset = pCharset;
	}

	protected File replaceFileHTMLSpecialChars(File file) {
		File escapedHtmlFile = null;
		try {
			// Get the String of the file
			FileInputStream fis = new FileInputStream(file);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis, charset));
			String line = "", oldtext = "";
			while ((line = bufferedReader.readLine()) != null) {
				oldtext += line + LINE_SEPARATOR;
			}
			bufferedReader.close();
			fis.close();

			// Replace special charts
			String newtext = oldtext.replaceAll(HTML_SPECIAL_CHART, "");

			// Generate new file
			String fileName = file.getAbsolutePath();
			escapedHtmlFile = new File(fileName + ESC_HTML_EXT);
			FileWriter writer = new FileWriter(escapedHtmlFile);
			writer.write(newtext);
			writer.close();

		} catch (FileNotFoundException fileNotFoundException) {
			throw new ParserException("File not found: " + file, fileNotFoundException);
		} catch (UnsupportedEncodingException unsupportedEncodingException) {
			throw new ParserException("File unsupportedEncodingException: " + file, unsupportedEncodingException);
		} catch (IOException iOException) {
			throw new ParserException("File iOException: " + file, iOException);
		}
		return escapedHtmlFile;
	}

	/**
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 * @throws JAXBException
	 * @throws IOException
	 */
	protected List<AbstractSample> getResultList(File file) {
		TestResults results = new TestResults();
		InputStream inputStream = null;
		Reader reader = null;
		try {
			inputStream = new FileInputStream(file);
			reader = new InputStreamReader(inputStream, charset);

			JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			results = (TestResults) unmarshaller.unmarshal(reader);

		} catch (JAXBException jAXBException) {
			throw new ParserException("Exception on parse xml of file:" + file, jAXBException);
		} catch (FileNotFoundException fileNotFoundException) {
			throw new ParserException("XML file not found: " + file, fileNotFoundException);
		}

		try {
			if (reader != null) {
				reader.close();
			}
			if (inputStream != null) {
				inputStream.close();
			}
		} catch (IOException e) {
			throw new ParserException("Error IOException: " + e.getMessage(), e);
		}

		List<AbstractSample> sampleResultList = null;
		if (results == null) {
			sampleResultList = new ArrayList<AbstractSample>();
		} else {
			sampleResultList = results.getHttpSampleOrSample();
		}

		return sampleResultList;
	}

	/**
	 * @param file
	 * @param testSet
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected void generateTapFile(File file, TestSet testSet) {
		String fileName = file.getAbsolutePath();
		String tapFileName = fileName + TAP_EXT;

		DumperOptions options = new DumperOptions();
		options.setPrintDiagnostics(true);
		options.setCharset(charset.name());
		Producer tapProducer = TapProducerFactory.makeTap13YamlProducer();
		Writer out;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tapFileName), charset));
			tapProducer.dump(testSet, out);
			out.close();
		} catch (FileNotFoundException fileNotFoundException) {
			throw new ParserException("TAP file not found: " + tapFileName, fileNotFoundException);
		} catch (IOException e) {
			throw new ParserException("Error IOException: " + e.getMessage(), e);
		}

	}

}
