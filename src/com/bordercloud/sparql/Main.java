/*
 * The MIT License
 *
 * Copyright 2016 Karima Rafes.
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
//Download :  https://github.com/BorderCloud/SPARQL-JAVA/dist
package com.bordercloud.sparql;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Karima Rafes
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            /*
            String queryInsert = "INSERT DATA "+
              " {GRAPH <http://example.com/MyGraph>  "+
              "   {             "+
              "       <http://example.com/test> <http://example.com/prop> \"ontology_bis\""+
              "    }   "+
              "}";
            // with Virtuoso
            String endpoint = "http://example.com/sparql-auth";
            Endpoint sp1 = new Endpoint( endpoint, false);

            sp1.setLogin("login");
            sp1.setPassword("password");
            HashMap<String, HashMap> rs1 =sp1.query(queryInsert);
            printResult(rs1,150);
             */
            String querySelect = "SELECT * "
                    + //"FROM <http://linkedgadget.com/wiki/Data:Jarvis_2016#personal>"+
                    "where {"
                    + "  ?x ?y ?z . "
                    + "} LIMIT 5";
            
            String querySelect2 = "SELECT ?human ?humanLabel \n"
            		+ " WHERE { \n"
            		+ " ?human wdt:P31 wd:Q5 . #find humans \n"
            		+ " ?human rdf:type wdno:P40 . #with at least one P40 (child) statement defined to be \"no value\" \n"
            		+ " SERVICE wikibase:label { bd:serviceParam wikibase:language \"ru\" } \n"
            		+ "} LIMIT 100 ";

            System.out.println("Query : ");
            System.out.println(querySelect);

            String endpoint2 = "http://dbpedia.org/sparql";
            System.out.println("");
            System.out.println("Endpoint : " + endpoint2);
            System.out.println("");
            System.out.println("Result : ");
         
            Endpoint sp2 = new Endpoint(endpoint2, false);
            HashMap<String, HashMap> rs2 = sp2.query(querySelect);
            printResult(rs2, 30);

            String endpoint3 = "https://query.wikidata.org/sparql";
            System.out.println("");
            System.out.println("Endpoint : " + endpoint3);
            System.out.println("");
            System.out.println("Result : ");
            Endpoint sp3 = new Endpoint(endpoint3, false);
            sp3.setMethodHTTPRead("GET");
            HashMap<String, HashMap> rs3 = sp3.query(querySelect);

            printResult(rs3, 30);        
            
            String endpoint4 = "https://query.wikidata.org/sparql";
            System.out.println("");
            System.out.println("Endpoint : " + endpoint4);
            System.out.println("");
            System.out.println("Result : ");
            Endpoint sp4 = new Endpoint(endpoint4, false);
            sp4.setMethodHTTPRead("GET");
            HashMap<String, HashMap> rs4 = sp4.query(querySelect2);

            printResult(rs4, 30);

        } catch (EndpointException eex) {
            System.out.println(eex);
            eex.printStackTrace();
        } 
    }

    public static void printResult(HashMap<String, HashMap> rs, int size) {

        for (String variable : (ArrayList<String>) rs.get("result").get("variables")) {
            System.out.print(String.format("%-" + size + "." + size + "s", variable) + " | ");
        }
        System.out.print("\n");
        for (HashMap<String, Object> value : (ArrayList<HashMap<String, Object>>) rs.get("result").get("rows")) {
            //System.out.print(value);
            /* for (String key : value.keySet()) {
         System.out.println(value.get(key));            
         }*/
            for (String variable : (ArrayList<String>) rs.get("result").get("variables")) {
                //System.out.println(value.get(variable));
                System.out.print(String.format("%-" + size + "." + size + "s", value.get(variable)) + " | ");
            }
            System.out.print("\n");
        }
    }

    public static void exampleWikidata() {
        try {
            String endpoint3 = "https://query.wikidata.org/sparql";
            Endpoint sp3 = new Endpoint(endpoint3, false);
            sp3.setMethodHTTPRead("GET");

            String queryPopulationInFrance
                    = "PREFIX wd: <http://www.wikidata.org/entity/> \n"
                    + "PREFIX wdt: <http://www.wikidata.org/prop/direct/> \n"
                    + "select  ?population \n"
                    + "where { \n"
                    + "        wd:Q142 wdt:P1082 ?population . \n"
                    + "} ";
            HashMap<String, HashMap> rs3_queryPopulationInFrance = sp3.query(queryPopulationInFrance);

            ArrayList<HashMap<String, Object>> rows_queryPopulationInFrance = (ArrayList) rs3_queryPopulationInFrance.get("result").get("rows");
            if (rows_queryPopulationInFrance.size() > 0) {
                System.out.print("Result population in France: " + rows_queryPopulationInFrance.get(0).get("population"));
                //printResult(rs3_queryPopulationInFrance,30);
            }

        } catch (EndpointException eex) {
            System.out.println(eex);
            eex.printStackTrace();
        }
    }
}
