![Gradle Package](https://github.com/BorderCloud/SPARQL-JAVA/workflows/Gradle%20Package/badge.svg)

# SPARQL-JAVA
Client SPARQL 1.1 (very simple) for Java

Usage with Gradle :
``` gradle
apply plugin: 'java'
apply plugin: 'application'

// This comes out to package + '.' + mainClassName
mainClassName = 'MainClass1'

group 'org.example'
version '1.0-SNAPSHOT'

repositories {
    mavenCentral()
    
    maven {
        url = uri("https://maven.pkg.github.com/bordercloud/SPARQL-JAVA")
        credentials {
            username = 'YOUR_USERNAME_IN_GITHUB'
            password = 'YOUR_TOKEN_IN_GITHUB_FOR_READING_PACKAGES'
        }
    }
}
dependencies {
    compile "com.bordercloud:SPARQL-JAVA:1.0.2"
    
    testCompile group: 'junit', name: 'junit', version: '4.12'
}
```

Example 1 with Wikidata :
``` java
import com.bordercloud.sparql.*;

import java.net.URI;
import java.net.URISyntaxException;

public class MainClass1 {
    public static void main(String[] args) {
        try {
            URI endpoint = new URI("https://query.wikidata.org/sparql");
            String querySelect  =
                    "PREFIX wd: <http://www.wikidata.org/entity/> \n"
                            + "PREFIX wdt: <http://www.wikidata.org/prop/direct/> \n"
                            + "select  ?population \n"
                            + "where { \n"
                            // wd:Q142{France} wdt:P1082{population} ?population .
                            + "        wd:Q142 wdt:P1082 ?population . \n"
                            + "} ";
            SparqlClient sc = new SparqlClient(false);
            sc.setEndpointRead(endpoint);
            SparqlResult sr = sc.query(querySelect);
            //sc.printLastQueryAndResult();

            SparqlResultModel rows_queryPopulationInFrance = sr.getModel();
            if (rows_queryPopulationInFrance.getRowCount() > 0) {
                System.out.print("Result population in France: " + rows_queryPopulationInFrance.getRows().get(0).get("population"));
            }
        } catch (URISyntaxException | SparqlClientException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }
}
```

Example 2 with Wikidata :
``` java
import com.bordercloud.sparql.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        try {
            URI endpoint = new URI("https://query.wikidata.org/sparql");
            String querySelect  = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>  \n"
                    + "PREFIX psv: <http://www.wikidata.org/prop/statement/value/>  \n"
                    + "PREFIX p: <http://www.wikidata.org/prop/>  \n"
                    + "PREFIX bd: <http://www.bigdata.com/rdf#>  \n"
                    + "PREFIX wikibase: <http://wikiba.se/ontology#>  \n"
                    + "PREFIX wd: <http://www.wikidata.org/entity/>   \n"
                    + "PREFIX wdt: <http://www.wikidata.org/prop/direct/>  \n"
                    + "\n"
                    + "select distinct  \n"
                    + "?lat ?long ?siteLabel ?siteDescription  \n"
                    + "?site \n"
                    + "#(replace(xsd:string(?site),  \n"
                    + "(concat(xsd:string(?image),\'?width=200\') as ?newimage) \n"
                    + "where { \n"
                    + "        ?site wdt:P31/wdt:P279* wd:Q839954 .  \n"
                    + "        ?site wdt:P17 wd:Q142 . \n"
                    + "        ?site wdt:P18 ?image . \n"
                    + "   ?site p:P625 ?coord . \n"
                    + "\n"
                    + "          ?coord   psv:P625 ?coordValue . \n"
                    + "\n"
                    + "          ?coordValue a wikibase:GlobecoordinateValue ; \n"
                    + "                        wikibase:geoLatitude ?lat ; \n"
                    + "                        wikibase:geoLongitude ?long . \n"
                    + "\n"
                    + "        SERVICE wikibase:label { \n"
                    + "             bd:serviceParam wikibase:language \"en,fr\" . \n"
                    + "        } \n"
                    + "      } \n" +
                    "LIMIT 10";
            SparqlClient sc = new SparqlClient(false);
            sc.setEndpointRead(endpoint);
            SparqlResult sr = sc.query(querySelect);
            //sc.printLastQueryAndResult();

            printResult(sr.getModel(),30);
        } catch (URISyntaxException | SparqlClientException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    public static void printResult(SparqlResultModel rs , int size) {

        for (String variable : rs.getVariables()) {
            System.out.print(String.format("%-"+size+"."+size+"s", variable ) + " | ");
        }
        System.out.print("\n");
        for (HashMap<String, Object> row : rs.getRows()) {
            for (String variable : rs.getVariables()) {
                System.out.print(String.format("%-"+size+"."+size+"s", row.get(variable)) + " | ");
            }
            System.out.print("\n");
        }
    }
}
```

Example for writing in a Virtuoso database : 
``` java
endpoint = new URI("http://172.17.0.2:8890/sparql-auth/");
client= new SparqlClient(false);
client.setLogin("dba");
client.setPassword("dba");
client.setEndpointRead(endpoint);
client.setEndpointWrite(endpoint);
//check delete
String q = 
        "PREFIX a: <http://example.com/test/a/>\n" +
        "PREFIX b: <http://example.com/test/b/>\n" +
        "DELETE DATA {\n" +
        "   GRAPH <http://truc.fr/> {\n" +
        "     a:A b:Name \"Test1\" .\n" +
        "     a:A b:Name \"Test2\" .\n" +
        "     a:A b:Name \"Test3\" .\n" +
        "}}";

SparqlResult sr1 = client.query(q);
```

See more examples with DBpedia and Wikidata in the tests.

## Update
+ 2020-05-17 Version 1.0
+ 2019-11-23 Fix bugs and add debug tools