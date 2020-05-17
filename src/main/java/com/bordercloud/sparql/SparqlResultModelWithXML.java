package com.bordercloud.sparql;

import java.util.*;

public class SparqlResultModelWithXML extends SparqlResultModel {

    public SparqlResultModelWithXML(HashMap<String, Object> resultHashMap) {
        super();
        this.resultHashMap = resultHashMap;
        if(resultHashMap != null && resultHashMap.get("result") != null) {
            this.rows = (ArrayList) ((HashMap<String, ArrayList<List>>) resultHashMap.get("result")).get("rows");
            this.variables = (((HashMap<String, ArrayList<String>>) resultHashMap.get("result")).get("variables"));
        }else{
            this.variables = null;
            this.rows = null;
        }
    }
}
