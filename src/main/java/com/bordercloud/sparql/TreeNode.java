package com.bordercloud.sparql;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TreeNode {

    final String name;
    final List<TreeNode> children;

    public static TreeNode leaf(String name){
        return new TreeNode(name, new ArrayList<TreeNode>());
    }

    public TreeNode(String name, List<TreeNode> children) {
        this.name = name;
        this.children = children;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder(50);
        print(buffer, "", "");
        return buffer.toString();
    }

    private void print(StringBuilder buffer, String prefix, String childrenPrefix) {
        buffer.append(prefix);
        buffer.append(name);
        buffer.append('\n');
        for (Iterator<TreeNode> it = children.iterator(); it.hasNext();) {
            TreeNode next = it.next();
            if (it.hasNext()) {
                next.print(buffer, childrenPrefix + "├── ", childrenPrefix + "│   ");
            } else {
                next.print(buffer, childrenPrefix + "└── ", childrenPrefix + "    ");
            }
        }
    }
}