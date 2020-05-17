package com.bordercloud.sparql;

import org.junit.Test;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class SparqlClientTest  {

    @Test
    public void queryReadWikidata1_GET_XML() throws URISyntaxException, SparqlClientException {
        URI endpoint = new URI("https://query.wikidata.org/sparql");
        String querySelect = "SELECT *  where { ?x ?y ?z .} LIMIT 5";
        SparqlClient sc = new SparqlClient(false);
        sc.setEndpointRead(endpoint);
        sc.setMethodHTTPRead(Method.GET);
        SparqlResult sr = sc.query(querySelect);
        sc.printLastQueryAndResult();
        assertEquals(5, sr.getModel().getRows().size());
    }

    @Test
    public void queryReadWikidata_GET_JSON() throws URISyntaxException, SparqlClientException {
        URI endpoint = new URI("https://query.wikidata.org/sparql");
        String querySelect = "SELECT *  where { ?x ?y ?z .} LIMIT 5";
        SparqlClient sc = new SparqlClient(false);
        sc.setEndpointRead(endpoint);
        sc.setMethodHTTPRead(Method.GET);
        SparqlResult sr = sc.query(querySelect,MimeType.ttl);
        sc.printLastQueryAndResult();
    }

    @Test
    public void queryReadWikidata_GET_TTL() throws URISyntaxException, SparqlClientException {
        URI endpoint = new URI("https://query.wikidata.org/sparql");
        String queryConstruct = "CONSTRUCT   { ?x ?y ?z } WHERE {  ?x ?y ?z } LIMIT 5" ;
        SparqlClient sc = new SparqlClient(false);
        sc.setEndpointRead(endpoint);
        sc.setMethodHTTPRead(Method.GET);
        SparqlResult sr = sc.query(queryConstruct,MimeType.ttl);
        sc.printLastQueryAndResult();
    }

    @Test
    public void queryReadWikidata_GETByDefault()  throws URISyntaxException, SparqlClientException {
        String querySelect = "SELECT ?human ?humanLabel \n"
                + " WHERE { \n"
                + " ?human wdt:P31 wd:Q5 . #find humans \n"
                + " ?human rdf:type wdno:P40 . #with at least one P40 (child) statement defined to be \"no value\" \n"
                + " SERVICE wikibase:label { bd:serviceParam wikibase:language \"ru\" } \n"
                + "} LIMIT 100 ";

        URI endpoint = new URI("https://query.wikidata.org/sparql");
        SparqlClient sc = new SparqlClient(false);
        sc.setEndpointRead(endpoint);
        SparqlResult sr = sc.query(querySelect);
        sc.printLastQueryAndResult();
    }

    @Test
    public void queryReadDbpedia_POST_XML() throws URISyntaxException, SparqlClientException {
        URI endpoint = new URI("http://dbpedia.org/sparql");
        String querySelect = "SELECT *  where { ?x ?y ?z .} LIMIT 5";
        SparqlClient sc = new SparqlClient(false);
        sc.setEndpointRead(endpoint);
        SparqlResult sr = sc.query(querySelect);
        sc.printLastQueryAndResult();
        assertEquals(5, sr.getModel().getRows().size());
    }


    @Test
    public void queryReadBNF_POST_XML() throws URISyntaxException, SparqlClientException {
        URI endpoint = new URI("https://data.bnf.fr/sparql");
        String querySelect = "SELECT *  where { ?x ?y ?z .} LIMIT 5";
        SparqlClient sc = new SparqlClient(false);
        sc.setEndpointRead(endpoint);
        SparqlResult sr = sc.query(querySelect);
        sc.printLastQueryAndResult();

        assertEquals(5, sr.getModel().getRows().size());
    }

    @Test
    public void testSetterAndGetter() throws URISyntaxException, SparqlClientException {
        SparqlClient sc = new SparqlClient();
        sc.setMethodHTTPRead(Method.GET);
        assertEquals(sc.getMethodHTTPRead(), Method.GET);

        sc.setMethodHTTPWrite(Method.GET);
        assertEquals(sc.getMethodHTTPWrite(), Method.GET);

        sc.setEndpointRead(new URI("https://query.truc.org/sparql"));
        assertEquals(sc.getEndpointRead(),new URI("https://query.truc.org/sparql"));

        sc.setEndpointWrite(new URI("https://query.truc.org/sparql_auth"));
        assertEquals(sc.getEndpointWrite(),new URI("https://query.truc.org/sparql_auth"));

        sc.setLogin("toto");
        assertEquals(sc.getLogin(),"toto");

        sc.setPassword("pass");
        assertEquals(sc.getPassword(),"pass");

        sc.setNameParameterQueryRead("paramRead");
        assertEquals(sc.getNameParameterQueryRead(),"paramRead");

        sc.setNameParameterQueryWrite("paramWrite");
        assertEquals(sc.getNameParameterQueryWrite(),"paramWrite");

        sc.setProxyHost("http://example.com");
        assertEquals(sc.getProxyHost(),"http://example.com");

        sc.setProxyPort(1234);
        assertEquals(sc.getProxyPort(),1234);
    }

    @Test
    public void testError_JSON() throws URISyntaxException {
        URI endpoint = new URI("https://query.wikidata.org/sparql");
        SparqlClient sc = new SparqlClient(false);
        sc.setEndpointRead(endpoint);
        try {
            String q = "s *  where {?x ?y ?z.} LIMIT 5";
            SparqlResult sr = sc.query(q);
            sc.printLastQueryAndResult();
        } catch (SparqlClientException e) {
            e.printStackTrace();
            //assertEquals(e.getMessage(),"Lexical error at line 1, column 2.  Encountered: \" \" (32), after : \"s\"");

            Pattern p = Pattern.compile("^ERROR QUERY: Lexical error at line 1, column 2\\.  Encountered: \" \" \\(32\\), after : \"s\"") ;
            Matcher m = p.matcher(e.getMessage()) ;
            assertTrue("The error message doesn't match", m.matches());
        }
    }

}