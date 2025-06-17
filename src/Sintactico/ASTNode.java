
package Sintactico;

import java.util.ArrayList;
import java.util.List;

public class ASTNode {
    private final String label;
    private final List<ASTNode> children = new ArrayList<>();

    public ASTNode(String label) {
        this.label = label;
    }

    public void addChild(ASTNode child) {
        children.add(child);
    }

    public String toDot(String parentName, int[] counter) {
        String nodeName = "n" + counter[0]++;
        StringBuilder sb = new StringBuilder();
        sb.append(nodeName).append(" [label=\"").append(label).append("\"];");

        if (parentName != null) {
            sb.append(parentName).append(" -> ").append(nodeName).append(";");
        }

        for (ASTNode child : children) {
            sb.append(child.toDot(nodeName, counter));
        }

        return sb.toString();
    }

    public String toDotFile() {
        StringBuilder sb = new StringBuilder("digraph AST {");
        sb.append(toDot(null, new int[]{0}));
        sb.append("}");
        return sb.toString();
    }
}
