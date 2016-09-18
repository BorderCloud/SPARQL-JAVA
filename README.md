# SPARQL-JAVA
Lib Java for SPARQL 1.1 (Very simple...)

Usage Read :
``` java
import com.bordercloud.sparql.Endpoint;

          try {
            String endpoint3 = "https://query.wikidata.org/sparql";
            Endpoint sp3 = new Endpoint(endpoint3, false);
            sp3.setMethodHTTPRead("GET");

            String queryPopulationInFrance
                    = "PREFIX wd: <http://www.wikidata.org/entity/> \n"
                    + "PREFIX wdt: <http://www.wikidata.org/prop/direct/> \n"
                    + "select  ?population \n"
                    + "where { \n"
                    // wd:Q142{France} wdt:P1082{population} ?population .
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
```

Usage Write with Virtuoso :
``` java
import com.bordercloud.sparql.Endpoint;

          try {
                      String queryInsert = "INSERT DATA "+
              " {GRAPH <http://example.com/MyGraph>  "+
              "   { "+
              "       <http://example.com/test> <http://example.com/prop> \"text...\""+
              "    }   "+
              "}";
            // with Virtuoso
            String endpoint = "http://example.com/sparql-auth";
            Endpoint sp1 = new Endpoint( endpoint, false);

            sp1.setLogin("login");
            sp1.setPassword("password");

            HashMap<String, HashMap> rs1 =sp1.query(queryInsert);
            printResult(rs1,150);

        } catch (EndpointException eex) {
            System.out.println(eex);
            eex.printStackTrace();
        }
```

Test with DBpedia and Wikidata :
```
java -jar "SPARQL-JAVA.jar" 
```
Source of Main Class 
``` java
package com.bordercloud.sparql;

import java.util.ArrayList;
import java.util.HashMap;

public class Main {

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
            String querySelect = "SELECT * "+
              //"FROM <http://linkedgadget.com/wiki/Data:Jarvis_2016#personal>"+
              "where {"+
              "  ?x ?y ?z . "+
              "} LIMIT 5";
                                    
            System.out.println("Query : ");
            System.out.println(querySelect); 
            
            String endpoint2 = "http://dbpedia.org/sparql";
            System.out.println("");            
            System.out.println("Endpoint : "+endpoint2);       
            System.out.println("");
            System.out.println("Result : ");
            Endpoint sp2 = new Endpoint(endpoint2, false);
            HashMap<String, HashMap> rs2 = sp2.query(querySelect);
            printResult(rs2,30);          
            
            String endpoint3 = "https://query.wikidata.org/sparql";
            System.out.println("");            
            System.out.println("Endpoint : "+endpoint3);       
            System.out.println(""); 
            System.out.println("Result : ");
            Endpoint sp3 = new Endpoint( endpoint3, false);
            sp3.setMethodHTTPRead("GET");
            HashMap<String, HashMap> rs3 =  sp3.query(querySelect);

            printResult(rs3,30);
            
        }catch(EndpointException eex) {
            System.out.println(eex);
            eex.printStackTrace();
        }
    }

    public static void printResult(HashMap<String, HashMap> rs , int size) {

      for (String variable : (ArrayList<String>) rs.get("result").get("variables")) {
        System.out.print(String.format("%-"+size+"."+size+"s", variable ) + " | ");
      }    
      System.out.print("\n");
      for (HashMap<String, Object> value : (ArrayList<HashMap<String, Object>>) rs.get("result").get("rows")) {
        //System.out.print(value);
        /* for (String key : value.keySet()) {
         System.out.println(value.get(key));            
         }*/
        for (String variable : (ArrayList<String>) rs.get("result").get("variables")) {            
          //System.out.println(value.get(variable));
          System.out.print(String.format("%-"+size+"."+size+"s", value.get(variable)) + " | ");
        }
        System.out.print("\n");
      }
    }
}
```