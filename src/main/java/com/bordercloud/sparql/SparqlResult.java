package com.bordercloud.sparql;

import java.util.*;
import java.util.stream.Collectors;

public class SparqlResult  {
    public SparqlQueryType queryType;
    public MimeType accept;
    public String resultRaw;
    public HashMap<String, Object> resultHashMap;
    public boolean resultAsk;

    public SparqlResultModel getModel(){
        switch (queryType){
            case SELECT:
                if(accept == MimeType.json){
                    return new SparqlResultModelWithJSON(resultHashMap);
                }else if(accept == MimeType.xml){
                    return new SparqlResultModelWithXML(resultHashMap);
                }
            case ASK:
            case CONSTRUCT:
            case DESCRIBE:
            case UPDATE:
            default:
                return null;
        }
    }
    
    public boolean getAskResult() throws Exception {
        if(queryType != SparqlQueryType.ASK) {
            throw new Exception("Use getAskResult only for ASK query.");
        }
        return (boolean) resultHashMap.get("boolean");
    }

    public static void printTree(SparqlResult rs) {
        TreeNode tree = new TreeNode("sparql", getTree((HashMap) rs.resultHashMap));
        System.out.print(tree);
    }

    public static void printTree(HashMap rs) {
        TreeNode tree = new TreeNode("sparql", getTree(rs));
        System.out.print(tree);
    }


    private static List<TreeNode> getTree(ArrayList<HashMap> rs) {
        List<TreeNode> node = new ArrayList<TreeNode>();
        int i = 0;
        for(HashMap map : rs){
            node.add(new TreeNode(String.valueOf(i),  getTree(new TreeMap<Object,Object>(map))));
            i++;
        }
        return  node;
    }

    private static List<TreeNode> getTree(Map<Object, Object> rs) {
        List<TreeNode> node = new ArrayList<TreeNode>();
        rs.forEach((key, value) -> {
            if(value instanceof HashMap) {
                if(((HashMap) value).size()>0){
                    node.add(new TreeNode(key.toString(),  getTree((HashMap<Object, Object>) value)));
                }else{
                    node.add(TreeNode.leaf(key.toString() + " = EMPTY"));
                }
            }else if(value instanceof ArrayList) {
                if(((ArrayList) value).size()>0){
                    if( ((ArrayList) value).get(0) instanceof HashMap){
                        node.add(new TreeNode(key.toString(),  getTree((ArrayList) value)));
                    }else{
                        node.add(TreeNode.leaf(key.toString() + " = " + value.toString()));
                    }
                }else{
                    node.add(TreeNode.leaf(key.toString() + " = EMPTY" ));
                }
            } else {
                node.add(TreeNode.leaf(key.toString() + " = " + value.toString()));
            }
        });
        return node;
    }

////         * TODO write comment and clean
////         *
////         * @param r1
////         * @param rs2
////         * @param bool ordered
////         * @param bool distinct
////         * @return array
////         */
    public static HashMap<Integer, Object> compare(HashMap<String, Object> rs1, HashMap<String, Object> rs2,boolean ordered, boolean distinct)
    {
        HashMap<Integer, Object> differenceEmpty = new HashMap<Integer, Object>();
        HashMap<Integer, Object> difference = new HashMap<Integer, Object>();
        HashMap<String, Object> r1 = (HashMap<String, Object>) rs1.clone();
        HashMap<String, Object> r2 = (HashMap<String, Object>) rs2.clone();
        HashMap<String, Object> result1;
        HashMap<String, Object> result2;
        ArrayList<String> variables1 = null;
        ArrayList<String>  variables2 = null;
        HashMap<Integer,HashMap<String, Object>> rows1;
        HashMap<Integer,HashMap<String, Object>> rows2;
        difference.put(0,r1);
        difference.put(1,r2);

        // Check ASK response
        if (rs1.containsKey("boolean") && rs2.containsKey("boolean")) {
            if ( rs1.get("boolean").equals(rs2.get("boolean")) ) {
                return differenceEmpty; // return nothing ;
            } else {
                return difference; // return diff ;
            }
        }

        r1.replace("result", ((HashMap<String, Object>) r1.get("result")).clone());
        r2.replace("result", ((HashMap<String, Object>) r2.get("result")).clone());
        result1 = ((HashMap<String, Object>) r1.get("result"));
        result2 = ((HashMap<String, Object>) r2.get("result"));

        // A/ Check the variables lists in the header are the same.
        if (rs1.containsKey("result") && rs2.containsKey("result")) {
            if (! ((HashMap<String, ArrayList<String>>)  rs1.get("result")).containsKey("variables")
                    && ! ((HashMap<String, ArrayList<String>>) rs2.get("result")).containsKey("variables")) {
               //do nothing
            } else if (! ((HashMap<String, ArrayList<String>>)  rs1.get("result")).containsKey("variables")
                    || ! ((HashMap<String, ArrayList<String>>) rs2.get("result")).containsKey("variables")) {
                //do nothing
            } else { // compare list

                ArrayList<String> variables1Origin = ((HashMap<String, ArrayList<String>>) rs1.get("result")).get("variables");;
                ArrayList<String>  variables2Origin =((HashMap<String, ArrayList<String>>) rs2.get("result")).get("variables");
                result1.replace("variables", ((HashMap<String, ArrayList<String>>) r1.get("result")).get("variables").clone());
                result2.replace("variables", ((HashMap<String, ArrayList<String>>) r2.get("result")).get("variables").clone());
                variables1 = ((HashMap<String, ArrayList<String>>) r1.get("result")).get("variables");
                variables2 = ((HashMap<String, ArrayList<String>>) r2.get("result")).get("variables");
                int variables1Size = variables1Origin.size();
                int variables2Size = variables2Origin.size();
                int max = Integer.max(variables1Size, variables2Size);

                for(int i = 0; i < max; i++) {
                    //variables1.get(0)
                    if(i < variables1Size && i < variables2Size) {
                        if(variables1Origin.get(i).equals(variables2Origin.get(i))) {
                            variables1.remove(variables1Origin.get(i));
                            variables2.remove(variables2Origin.get(i));
                        }
                    }
                }

                if(variables1.size() == 0){
                    result1.remove("variables");
                }
                if(variables2.size() == 0){
                    result2.remove("variables");
                }
            }
        }

        if (! ((HashMap<String, ArrayList<String>>)  rs1.get("result")).containsKey("rows")
                && ! ((HashMap<String, ArrayList<String>>) rs2.get("result")).containsKey("rows")) {
            return difference;
        } else if (! ((HashMap<String, ArrayList<String>>)  rs1.get("result")).containsKey("rows")
                || ! ((HashMap<String, ArrayList<String>>) rs2.get("result")).containsKey("rows")) {
            return difference;
        }
        // Check if there are blanknodes ref : http://blog.datagraph.org/2010/03/rdf-isomorphism

        ArrayList<HashMap<String, Object>> rows1Origin = ((HashMap<String, ArrayList<HashMap<String, Object>>>) rs1.get("result")).get("rows");
        ArrayList<HashMap<String, Object>> rows2Origin = ((HashMap<String, ArrayList<HashMap<String, Object>>>) rs2.get("result")).get("rows");


        rows1 = new HashMap<Integer,HashMap<String, Object>>();
        rows2 = new HashMap<Integer,HashMap<String, Object>>();
        int rows1Size = rows1Origin.size();
        int rows2Size = rows2Origin.size();
        int rowsmax = Integer.max(rows1Size, rows2Size);

        for(int i = 0; i < rowsmax; i++) {
            //variables1.get(0)
            if(i < rows1Size && i < rows2Size) {
                HashMap<String, Object> cells1Origin = rows1Origin.get(i);
                HashMap<String, Object> cells2Origin = rows2Origin.get(i);


                HashMap<String, Object> cells1 = (HashMap<String, Object>) cells1Origin.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                HashMap<String, Object> cells2 = (HashMap<String, Object>) cells2Origin.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                cells1Origin.forEach(
                        (k, v) -> {
                            if(v.equals(cells2Origin.get(k))){
                                cells1.remove(k);
                                cells2.remove(k);
                            }
                        }
                );
                cells2Origin.forEach(
                        (k, v) -> {
                            if(v.equals(cells1Origin.get(k))){
                                cells1.remove(k);
                                cells2.remove(k);
                            }
                        }
                );

                if(cells1.size() > 0){
                    rows1.put(i,cells1);
                }
                if(cells2.size() > 0){
                    rows2.put(i,cells2);
                }
            }else if (i >= rows1Size ) {
                rows2.put(i,rows2Origin.get(i));
            }else if ( i >= rows2Size) {
                rows1.put(i,rows1Origin.get(i));
            }
        }
        result1.replace("rows", rows1);
        result2.replace("rows", rows2);
        if(rows1.size() == 0){
            result1.remove("rows");
        }
        if(rows2.size() == 0){
            result2.remove("rows");
        }
        if(result1.size() == 0){
            r1.remove("result");
        }
        if(result2.size() == 0){
            r2.remove("result");
        }

        if(r1.size() == 0 && r2.size() == 0) {
           difference = differenceEmpty;
        }
        return difference;
    }
//
////    private static ArrayList<String> getKeys(Map<String, Object> map, Object value) {
////        ArrayList<String> result = new ArrayList<String>();
////        for ( Map.Entry<String, Object> entry : map.entrySet()) {
////            String key = entry.getKey();
////            Object obj = entry.getValue();
////            // do something with key and/or tab
////            if(obj.equals(value)){
////                result.add(key);
////            }
////        }
////        return result;
////    }
}
