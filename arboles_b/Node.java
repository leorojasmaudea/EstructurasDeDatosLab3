package arboles_b;

import java.util.ArrayList;
import java.util.List;

// =========================================================
// Clase Nodo
// =========================================================
public class Node {
    List<Integer> keys; // 12
    List<Node> children; // 11 14 16
    boolean isLeaf;

    public Node(boolean isLeaf) {
        this.isLeaf = isLeaf;
        this.keys = new ArrayList<>();
        this.children = new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < keys.size(); i++) {
            sb.append(keys.get(i));
            if (i < keys.size() - 1)
                sb.append(" | ");
        }
        sb.append("]");
        return sb.toString();
    }

}
