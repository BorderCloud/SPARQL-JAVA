package com.bordercloud.sparql;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class SparqlResultModel  extends AbstractTableModel {
    protected HashMap<String, Object> resultHashMap;
    protected ArrayList<HashMap<String, Object>> rows;
    protected ArrayList<String> variables;

    public HashMap<String, Object> getResultHashMap() {
        return resultHashMap;
    }

    @Override
    public int getColumnCount() {
        if(variables !=null){
            return variables.size();
        }else {
            return 0;
        }
    }

    @Override
    public String getColumnName(int columnIndex) {
        if(variables != null && columnIndex < getColumnCount()){
            return variables.get(columnIndex);
        }else {
            throw new IndexOutOfBoundsException(
                    "Invalid range : columnIndex "
                            + columnIndex + " (max " + getColumnCount()+")");
        }
    }

    @Override
    public int getRowCount() {
        if(rows !=null){
            return rows.size();
        }else {
            return 0;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int  columnIndex) {
        if(variables != null && columnIndex < getColumnCount() &&
                rows != null && rowIndex < getRowCount()
        ){
            return ((HashMap<String, Object>) rows.get(rowIndex)).get(variables.get(columnIndex));
        }else {
            throw new IndexOutOfBoundsException(
                    "Invalid range : rowIndex is "+ rowIndex + "(max " + getRowCount() + ") columnIndex "
                            + columnIndex + "(max " + getColumnCount()+")");
        }
    }

    public ArrayList<HashMap<String, Object>>  getRows() {
        return rows;
    }
    public ArrayList<String>  getVariables() {
        return variables;
    }

    public static void printResult(SparqlResultModel sr, int size) {
        //printResult(sr.resultHashMap, size);
        for (String variable : sr.variables) {
            System.out.print(String.format("%-" + size + "." + size + "s", variable) + " | ");
        }
        System.out.print("\n");
        for (HashMap<String, Object> value : sr.rows) {
            //System.out.print(value);
            /* for (String key : value.keySet()) {
         System.out.println(value.get(key));
         }*/
            for (String variable : sr.variables) {
                //System.out.println(value.get(variable));
                System.out.print(String.format("%-" + size + "." + size + "s", value.get(variable)) + " | ");
            }
            System.out.print("\n");
        }
    }
}
