//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.12.02 at 07:47:15 AM CET 
//


package org.tap4j.ext.jmeter.model;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.tap4j.ext.jmeter.model package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.tap4j.ext.jmeter.model
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link TestResults }
     * 
     */
    public TestResults createTestResults() {
        return new TestResults();
    }

    /**
     * Create an instance of {@link HttpSample }
     * 
     */
    public HttpSample createHttpSample() {
        return new HttpSample();
    }

    /**
     * Create an instance of {@link Sample }
     * 
     */
    public Sample createSample() {
        return new Sample();
    }

    /**
     * Create an instance of {@link AbstractSample }
     * 
     */
    public AbstractSample createAbstractSample() {
        return new AbstractSample();
    }

    /**
     * Create an instance of {@link AssertionResult }
     * 
     */
    public AssertionResult createAssertionResult() {
        return new AssertionResult();
    }

    /**
     * Create an instance of {@link TextEl }
     * 
     */
    public TextEl createTextEl() {
        return new TextEl();
    }

}