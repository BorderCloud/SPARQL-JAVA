package com.bordercloud.sparql;

import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VirtuosoTest {
    private SparqlClient client;
    private URI endpoint;
    private String login = "dba";
    private String password = "dba";

    @Before
    public void setUp() throws SparqlClientException, URISyntaxException {
        endpoint = new URI("http://172.17.0.2:8890/sparql-auth/");
        client= new SparqlClient(false);
        client.setLogin("dba");
        client.setPassword("dba");
        client.setEndpointRead(endpoint);
        client.setEndpointWrite(endpoint);
        //check delete
        String q = 
                "        PREFIX a: <http://example.com/test/a/>\n" +
                "    PREFIX b: <http://example.com/test/b/>\n" +
                "    DELETE DATA {\n" +
                "        GRAPH <http://truc.fr/> {\n" +
                "        a:A b:Name \"Test1\" .\n" +
                "                a:A b:Name \"Test2\" .\n" +
                "                a:A b:Name \"Test3\" .\n" +
                "    }}";

        SparqlResult sr1 = client.query(q);
    }

    @Test
    public void queryWriteVirtuosoWithAuth_POST_XML() throws URISyntaxException, SparqlClientException {
        URI endpoint = new URI("http://database-test:8890/sparql-auth");
        String queryInsert = "INSERT DATA " +
                " {GRAPH <http://example.com/MyGraph>  " +
                "   {             " +
                "       <http://example.com/test> <http://example.com/prop> \"ontology_bis\"" +
                "    }   " +
                "}";
        SparqlClient sc = new SparqlClient(false);
        sc.setLogin("dba");
        sc.setPassword("dba");
        sc.setEndpointRead(endpoint);
        sc.setEndpointWrite(endpoint);
        SparqlResult sr1 = sc.query(queryInsert);
        sc.printLastQueryAndResult();

        String querySelect = "SELECT *  where " +
                " {GRAPH <http://example.com/MyGraph>  " +
                "   {             " +
                "      ?x ?y ?z . " +
                "    }   " +
                "}";
        SparqlResult sr2 = sc.query(querySelect);
        sc.printLastQueryAndResult();
        assertEquals(1, sr2.getModel().getRowCount());
    }
    
    @Test
    public void testError_endpointwrite() throws URISyntaxException {
        try {
            URI endpoint = new URI("http://database-test:8890/sparql-auth");
            String queryInsert = "INSERT DATA "+
                    " {GRAPH <http://example.com/MyGraph>  "+
                    "   {             "+
                    "       <http://example.com/test> <http://example.com/prop> \"ontology_bis\""+
                    "    }   "+
                    "}";
            SparqlClient sc = new SparqlClient(false);
            sc.setLogin("dba");
            sc.setPassword("dba");
            sc.setEndpointRead(endpoint);
            SparqlResult sr1 = sc.query(queryInsert);
            sc.printLastQueryAndResult();
        } catch (SparqlClientException e) {
            e.printStackTrace();
            //assertEquals(e.getMessage(),"Lexical error at line 1, column 2.  Encountered: \" \" (32), after : \"s\"");

            Pattern p = Pattern.compile("The endpoint for writing is not defined.") ;
            Matcher m = p.matcher(e.getMessage()) ;
            assertTrue("The endpoint for writing is not defined.", m.matches());
        }
    }
    
    @Test
    public void testVirtuosoRead() throws SparqlClientException {
        String q = "select *  where {?x ?y ?z.} LIMIT 5";
        SparqlResult sr1 = client.query(q);
        assertEquals(5,sr1.getModel().getRowCount());
    }

    @Test
    public void testVirtuosoAsk() throws Exception {
        //read if empty
        String q = "PREFIX a: <http://example.com/test/a/> \n" +
                "  select *  where {a:A ?y ?z.} LIMIT 5";
        SparqlResult res = client.query(q);
        assertEquals(0,res.getModel().getRows().size());

        //check ask false
        q = "        PREFIX a: <http://example.com/test/a/>\n" +
                "        PREFIX b: <http://example.com/test/b/>\n" +
                "    ask where { GRAPH <http://truc.fr/> {a:A b:Name \"Test3\" .}} ";
        res = client.query(q);
        assertTrue(!res.getAskResult());
        
        res = client.query(q,MimeType.json);
        assertTrue(!res.getAskResult());

        //check write
        q = "        PREFIX a: <http://example.com/test/a/>\n" +
                "        PREFIX b: <http://example.com/test/b/>\n" +
                "        INSERT DATA {\n" +
                "            GRAPH <http://truc.fr/> {\n" +
                "            a:A b:Name \"Test1\" .\n" +
                "                    a:A b:Name \"Test2\" .\n" +
                "                    a:A b:Name \"Test3\" .\n" +
                "        }}";

        res = client.query(q);
        client.printLastQueryAndResult();

        // check if write is OK
        q = "PREFIX a: <http://example.com/test/a/>\n" +
                "        select *  where {a:A ?y ?z.} LIMIT 5";
        res = client.query(q);
        assertEquals(3, res.getModel().getRowCount());

        //check ask is true
        q = "PREFIX a: <http://example.com/test/a/>\n" +
                "        PREFIX b: <http://example.com/test/b/>\n" +
                "    ask where { GRAPH <http://truc.fr/> {a:A b:Name \"Test3\" .}} ";
        res = client.query(q);
        assertTrue(res.getAskResult());

        //check delete
        q =  "PREFIX a: <http://example.com/test/a/>\n" +
                "        PREFIX b: <http://example.com/test/b/>\n" +
                "        DELETE DATA {\n" +
                "            GRAPH <http://truc.fr/> {\n" +
                "            a:A b:Name \"Test1\" .\n" +
                "                    a:A b:Name \"Test2\" .\n" +
                "                    a:A b:Name \"Test3\" .\n" +
                "        }}";

        res = client.query(q);
        client.printLastQueryAndResult();

        // check if write is OK
        q = "PREFIX a: <http://example.com/test/a/>\n" +
                "        select *  where {a:A ?y ?z.} LIMIT 5";
        res = client.query(q);
        assertEquals(0, res.getModel().getRowCount());
    }

    @Test
    public void  testErrorQuery() {
        //read if empty
        try {
            String q = "se *  where {?x ?y ?z.} LIMIT 5";
            SparqlResult res = client.query(q);
            client.printLastQueryAndResult();
        } catch (SparqlClientException e) {
            e.printStackTrace();
            Pattern p = Pattern.compile(".*Virtuoso 37000 Error SP030: SPARQL compiler, line 1: syntax error at 'se' before '\\*'.*",Pattern.CASE_INSENSITIVE+Pattern.DOTALL) ;
            Matcher m = p.matcher(e.getMessage()) ;
            
            assertTrue("The error message doesn't match", m.matches());
        }
    }

    @Test
    public void testErrorUpdate() {
        try {
            String q = "        INS {\n" +
                    "            GRAPH <http://truc.fr/> {\n" +
                    "            a:A b:Name \"Test1\" .\n" +
                    "                    a:A b:Name \"Test2\" .\n" +
                    "                    a:A b:Name \"Test3\" .\n" +
                    "        }}";
            SparqlResult res = client.query(q);
            client.printLastQueryAndResult();
        } catch (SparqlClientException e) {
            e.printStackTrace();
            Pattern p = Pattern.compile(".*Virtuoso 37000 Error SP030: SPARQL compiler, line 1: syntax error at 'INS' before '\\{'.*",Pattern.CASE_INSENSITIVE+Pattern.DOTALL) ;
            Matcher m = p.matcher(e.getMessage()) ;

            assertTrue("The error message doesn't match", m.matches());
        }
    }


    @Test
    public void testErrorPasswordUpdate() {
        try {
            String q = "        PREFIX a: <http://example.com/test/a/>\n" +
                    "        PREFIX b: <http://example.com/test/b/>\n" +
                    "        INSERT DATA {\n" +
                    "            GRAPH <http://truc.fr/> {\n" +
                    "            a:A b:Name \"Test1\" .\n" +
                    "                    a:A b:Name \"Test2\" .\n" +
                    "                    a:A b:Name \"Test3\" .\n" +
                    "        }}";
            client.setPassword("pipo");
            SparqlResult res = client.query(q);

            client.printLastQueryAndResult();
        } catch (SparqlClientException e) {
            //assertEquals(401, e.getCause().);
        }
    }

    @Test
    public void modelXMLAndJson() throws URISyntaxException, SparqlClientException {
        URI endpoint = new URI("http://database-test:8890/sparql-auth");
        String queryInsert = "INSERT DATA " +
                " {GRAPH <http://example.com/MyGraph2>  " +
                "   {             " +
                "       <http://example.com/test> <http://example.com/prop> \"ontology_bis\"" +
                "    }   " +
                "}";
        SparqlClient sc = new SparqlClient(false);
        sc.setLogin("dba");
        sc.setPassword("dba");
        sc.setEndpointRead(endpoint);
        sc.setEndpointWrite(endpoint);
        String qDelete = " CLEAR GRAPH <http://example.com/MyGraph2>";
        sc.query(qDelete);
        SparqlResult sr1 = sc.query(queryInsert);
        sc.printLastQueryAndResult();

        String querySelect = "SELECT *  where " +
                " {GRAPH <http://example.com/MyGraph2>  " +
                "   {             " +
                "      ?x ?y ?z . " +
                "    }   " +
                "}";

        SparqlResult sr2 = sc.query(querySelect,MimeType.json);
        sc.printLastQueryAndResult();
        assertEquals(1, sr2.getModel().getRowCount());
        
        SparqlResult sr = sc.query(querySelect,MimeType.xml);
        sc.printLastQueryAndResult();
        assertEquals(1, sr2.getModel().getRowCount());
    }
}

