package com.bordercloud.sparql;

import java.util.*;

public class SparqlResultModelWithJSON  extends SparqlResultModel {

    public SparqlResultModelWithJSON(HashMap<String, Object> resultHashMap) {
        super();
        this.resultHashMap = resultHashMap;
        if (resultHashMap != null && resultHashMap.get("results") != null && resultHashMap.get("head") != null) {
            //this.rows = (ArrayList) ((HashMap<String, ArrayList<List>>) resultHashMap.get("results")).get("bindings");
            ArrayList<HashMap<String, Object>> rowsJson = (ArrayList) ((HashMap<String, ArrayList<List>>) resultHashMap.get("results")).get("bindings");
            this.variables = (((HashMap<String, ArrayList<String>>) resultHashMap.get("head")).get("vars"));

            ArrayList<HashMap<String, Object>> rowsFinal = new ArrayList<>();
            HashMap<String, Object> row;
            HashMap<String, Object> field;
            for (HashMap<String, Object> rowJson : rowsJson) {
                row = new HashMap<String, Object>();
                for (String variable : this.variables) {
                    field = (HashMap<String, Object>) rowJson.get(variable);
                    row.put(variable,field.get("value"));
                }
                rowsFinal.add(row);
            }
            this.rows = rowsFinal;
        } else {
            this.variables = null;
            this.rows = null;
        }
    }
}
