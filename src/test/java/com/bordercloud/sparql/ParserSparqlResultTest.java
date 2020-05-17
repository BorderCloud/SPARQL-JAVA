package com.bordercloud.sparql;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;


public class ParserSparqlResultTest {

    private String getResourceFileAsString(String s) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
       return IOUtils.toString(
                classLoader.getResource(s),
                "UTF-8"
        );
    }
    
    //ok
    @Test
    public void parserResultAsk() throws IOException, ParserConfigurationException, SAXException {
        String response1 =  getResourceFileAsString("response1.xml");
        String response2 =  getResourceFileAsString("response2.xml");

        SAXParserFactory parserSparql = SAXParserFactory.newInstance();

        SAXParser parser = parserSparql.newSAXParser();
        ParserSparqlResultHandler handler = new ParserSparqlResultHandler();

        parser.parse(new InputSource(new StringReader(response1)), handler);
        Assert.assertNotNull(handler);
        HashMap<String, Object>  result1 = handler.getResult();
        Assert.assertTrue((Boolean) result1.get("boolean"));

        parser.parse(new InputSource(new StringReader(response2)), handler);
        Assert.assertNotNull(handler);
        HashMap<String, Object>  result2 = handler.getResult();
        Assert.assertTrue((Boolean) result2.get("boolean"));
    }

    //ok
    @Test
    public void printResultRows1() throws IOException, ParserConfigurationException, SAXException {
        String response4 =  getResourceFileAsString("response4.xml");
        String response6 =  getResourceFileAsString("response6.xml");

        SAXParserFactory parserSparql = SAXParserFactory.newInstance();

        SAXParser parser = parserSparql.newSAXParser();
        ParserSparqlResultHandler handler = new ParserSparqlResultHandler();

        parser.parse(new InputSource(new StringReader(response4)), handler);
        Assert.assertNotNull(handler);
        HashMap<String, Object>  result4 = handler.getResult();
        Assert.assertNotNull(result4.get("result"));
        SparqlResult.printTree(result4);

        parser.parse(new InputSource(new StringReader(response6)), handler);
        Assert.assertNotNull(handler);
        HashMap<String, Object>  result6 = handler.getResult();
        Assert.assertNotNull(result6.get("result"));
        SparqlResult.printTree(result6);
    }

    //ok
    @Test
    public void printResultBoolean() throws IOException, ParserConfigurationException, SAXException {
        String response1 =  getResourceFileAsString("response1.xml");

        SAXParserFactory parserSparql = SAXParserFactory.newInstance();

        SAXParser parser = parserSparql.newSAXParser();
        ParserSparqlResultHandler handler = new ParserSparqlResultHandler();

        parser.parse(new InputSource(new StringReader(response1)), handler);
        Assert.assertNotNull(handler);
        HashMap<String, Object>  result1 = handler.getResult();

        SparqlResult.printTree(result1);
    }

    //ok
    @Test
    public void testDiffBoolean() throws IOException, ParserConfigurationException, SAXException {
        String response =  getResourceFileAsString("response1.xml");
        String response_diff =  getResourceFileAsString("response3.xml");

        SAXParserFactory parserSparql = SAXParserFactory.newInstance();

        SAXParser parser = parserSparql.newSAXParser();
        ParserSparqlResultHandler handler = new ParserSparqlResultHandler();

        parser.parse(new InputSource(new StringReader(response)), handler);
        Assert.assertNotNull(handler);
        HashMap<String, Object>  result = handler.getResult();

        parser.parse(new InputSource(new StringReader(response_diff)), handler);
        Assert.assertNotNull(handler);
        HashMap<String, Object>  result_diff = handler.getResult();
        HashMap diff = SparqlResult.compare(result, result_diff, false, false);
        SparqlResult.printTree(diff);

        Assert.assertTrue(((HashMap<String, Boolean>) diff.get(0)).get("boolean"));
        Assert.assertFalse(((HashMap<String, Boolean>) diff.get(1)).get("boolean"));
    }

    //ok
    @Test
    public void testDiffResultVariable() throws IOException, ParserConfigurationException, SAXException {
        String response =  getResourceFileAsString("response4.xml");
        String response_diff =  getResourceFileAsString("response4_diff1.xml");

        SAXParserFactory parserSparql = SAXParserFactory.newInstance();

        SAXParser parser = parserSparql.newSAXParser();
        ParserSparqlResultHandler handler = new ParserSparqlResultHandler();

        parser.parse(new InputSource(new StringReader(response)), handler);
        Assert.assertNotNull(handler);
        HashMap<String, Object>  result = handler.getResult();

        parser.parse(new InputSource(new StringReader(response_diff)), handler);
        Assert.assertNotNull(handler);
        HashMap<String, Object>  result_diff = handler.getResult();

        SparqlResult.printTree(result);
        SparqlResult.printTree(result_diff);

        HashMap diff = SparqlResult.compare(result, result_diff, false, false);
        SparqlResult.printTree(diff);
        Assert.assertEquals("y",((HashMap<String, HashMap<String, ArrayList<String>>>) diff.get(0)).get("result").get("variables").get(0));
    }


    //ok
    @Test
    public void testDiffResultRows() throws IOException, ParserConfigurationException, SAXException {
        String response =  getResourceFileAsString("response4.xml");
        String response_diff =  getResourceFileAsString("response4_diff2.xml");

        SAXParserFactory parserSparql = SAXParserFactory.newInstance();

        SAXParser parser = parserSparql.newSAXParser();
        ParserSparqlResultHandler handler = new ParserSparqlResultHandler();

        parser.parse(new InputSource(new StringReader(response)), handler);
        Assert.assertNotNull(handler);
        HashMap<String, Object>  result = handler.getResult();

        parser.parse(new InputSource(new StringReader(response_diff)), handler);
        Assert.assertNotNull(handler);
        HashMap<String, Object>  result_diff = handler.getResult();

        SparqlResult.printTree(result);
        SparqlResult.printTree(result_diff);

        HashMap diff = SparqlResult.compare(result, result_diff, false, false);
        SparqlResult.printTree(diff);

        Assert.assertEquals("http://example.org/x/x",((HashMap<String, HashMap<String, HashMap<Integer, HashMap<String,String>>>>) diff.get(0)).get("result").get("rows").get(1).get("x"));
    }

    //ok
    @Test
    public void testDiffResultCells() throws IOException, ParserConfigurationException, SAXException {
        String response =  getResourceFileAsString("response4.xml");
        String response_diff =  getResourceFileAsString("response4_diff3.xml");

        SAXParserFactory parserSparql = SAXParserFactory.newInstance();

        SAXParser parser = parserSparql.newSAXParser();
        ParserSparqlResultHandler handler = new ParserSparqlResultHandler();

        parser.parse(new InputSource(new StringReader(response)), handler);
        Assert.assertNotNull(handler);
        HashMap<String, Object>  result = handler.getResult();

        parser.parse(new InputSource(new StringReader(response_diff)), handler);
        Assert.assertNotNull(handler);
        HashMap<String, Object>  result_diff = handler.getResult();

        SparqlResult.printTree(result);
        SparqlResult.printTree(result_diff);

        HashMap diff = SparqlResult.compare(result, result_diff, false, false);
        SparqlResult.printTree(diff);

        Assert.assertEquals("y",((HashMap<String, HashMap<String, HashMap<Integer, HashMap<String,String>>>>) diff.get(0)).get("result").get("rows").get(1).get("y"));
    }

    @Test
    public void testResetParserAskDiff() throws IOException, ParserConfigurationException, SAXException {
        String response1 =  getResourceFileAsString("response1.xml");
        String response2 =  getResourceFileAsString("response2.xml");
        String response3 =  getResourceFileAsString("response3.xml");
        String response4 =  getResourceFileAsString("response4.xml");
        String response5 =  getResourceFileAsString("response5.xml");
        String response6 =  getResourceFileAsString("response6.xml");
        String response7 =  getResourceFileAsString("response7.xml");

        SAXParserFactory parserSparql = SAXParserFactory.newInstance();

        SAXParser parser = parserSparql.newSAXParser();
        ParserSparqlResultHandler handler = new ParserSparqlResultHandler();

        parser.parse(new InputSource(new StringReader(response1)), handler);
        Assert.assertNotNull(handler);
        HashMap<String, Object>  result1 = handler.getResult();

        parser.parse(new InputSource(new StringReader(response2)), handler);
        Assert.assertNotNull(handler);
        HashMap<String, Object>  result2 = handler.getResult();

        parser.parse(new InputSource(new StringReader(response3)), handler);
        Assert.assertNotNull(handler);
        HashMap<String, Object>  result3 = handler.getResult();

        parser.parse(new InputSource(new StringReader(response4)), handler);
        Assert.assertNotNull(handler);
        HashMap<String, Object>  result4 = handler.getResult();

        parser.parse(new InputSource(new StringReader(response5)), handler);
        Assert.assertNotNull(handler);
        HashMap<String, Object>  result5 = handler.getResult();

        parser.parse(new InputSource(new StringReader(response6)), handler);
        Assert.assertNotNull(handler);
        HashMap<String, Object>  result6 = handler.getResult();

        parser.parse(new InputSource(new StringReader(response7)), handler);
        Assert.assertNotNull(handler);
        HashMap<String, Object>  result7 = handler.getResult();
    }

}
