package arboles_b;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class BTree {

    private final int order;
    private Node root;

    public BTree(int order) {
        this.order = order;
        this.root = null;
    }

    public boolean isEmpty() {
        return root == null;
    }

    public InsertResult insert(String key) {

        if (root == null) {
            root = new Node(true);
            root.keys.add(key);
            return new InsertResult(true, false);
        }

        InsertState state = new InsertState();

        SplitResult split = insertRecursive(root, key, state);

        if (!state.inserted) {
            return new InsertResult(false, false);
        }

        if (split != null) {
            Node newRoot = new Node(false);
            newRoot.keys.add(split.promotedKey);
            newRoot.children.add(split.left);
            newRoot.children.add(split.right);
            root = newRoot;
        }

        return new InsertResult(true, state.hadOverflow);

    }

    // Insertar -> 40
    // [ | 10 | 20 | 30 |]
    // [01, 04, 06] [11, 12, 13] [21, 22, 23] [31, 32, 33]
    /////////////////////////////////////////////////////
    // [ | 10 | 20 | 30 |]
    // [01, 04, 06] [11, 12, 13] [21, 22, 23] [31, 32, 33, 40] -> Overflow
    //////////////////////////////////////////////////////////////////////
    // [ | 10 | 20 | 30 |]
    // [01, 04, 06] [11, 12, 13] [21, 22, 23] [31, 32, 33, 40] -> Overflow (Sube 32)
    //////////////////////////////////////////////////////////////////////

    // [ | 10 | 20 | 30 | 32 | ]
    // [01, 04, 06] [11, 12, 13] [21, 22, 23] [31] [33, 40]
    ///////////////////////////////////////////////////////////////////////////
    // [20]
    // [ 10 ] [ 30, 32 ]
    // [01, 04, 06] [11, 12, 13] [21, 22, 23] [31] [33, 40]
    private SplitResult insertRecursive(Node node, String key, InsertState state) {

        int pos = findPosition(node, key);

        if (pos < node.keys.size() && node.keys.get(pos).equals(key)) {
            state.inserted = false;
            return null;
        }

        if (node.isLeaf) {
            insertSorted(node.keys, key);
            state.inserted = true;

            if (node.keys.size() >= order) {
                state.hadOverflow = true;
                return splitNode(node);
            }

            return null;
        }

        int childIndex = findPosition(node, key);
        SplitResult childSplit = insertRecursive(node.children.get(childIndex), key, state);

        if (!state.inserted)
            return null;

        if (childSplit != null) {
            node.keys.add(childIndex, childSplit.promotedKey);
            node.children.set(childIndex, childSplit.left);
            node.children.add(childIndex + 1, childSplit.right);

            if (node.keys.size() >= order) {
                state.hadOverflow = true;
                return splitNode(node);
            }

        }

        return null;

    }

    private int findPosition(Node node, String key) {
        int i = 0;
        while (i < node.keys.size() && key.compareTo(node.keys.get(i)) > 0) {
            i++;
        }
        return i;
    }

    // keys = [10,20,30] ----> Insertar key = 25 -------> [10,20,25,30]
    private void insertSorted(List<String> keys, String key) {
        // add -> No remplaza
        // set -> Si remplaza
        int i = 0;
        while (i < keys.size() && key.compareTo(keys.get(i)) > 0) {
            i++;
        }
        keys.add(i, key);
    }

    // [10,20,25,30] -- Lenght: 3 --- Size(): 4 -------- [10] [25,30]
    // middleIndex -> 1
    // total -> 4
    // middleIndex + 1 == i == 2
    // [20]
    // [10][10] [25,30]
    // [...] [...] [...] [...] [...]
    private SplitResult splitNode(Node node) {
        int totalKeys = node.keys.size();
        int mid = (totalKeys - 1) / 2;

        String promotedKey = node.keys.get(mid);

        Node left = new Node(node.isLeaf);
        Node right = new Node(node.isLeaf);

        for (int i = 0; i < mid; i++) {
            left.keys.add(node.keys.get(i));
        }

        for (int i = mid + 1; i < totalKeys; i++) {
            right.keys.add(node.keys.get(i));
        }

        if (!node.isLeaf) {

            for (int i = 0; i <= mid; i++) {
                left.children.add(node.children.get(i));
            }

            for (int i = mid + 1; i < node.children.size(); i++) {
                right.children.add(node.children.get(i));
            }
        }

        return new SplitResult(promotedKey, left, right);
    }

    public void printByLevels() {
        Queue<NodeLevel> queue = new LinkedList<>();
        queue.add(new NodeLevel(root, 0));

        int currentLevel = -1;

        while (!queue.isEmpty()) {
            NodeLevel current = queue.poll();

            if (current.level != currentLevel) {
                currentLevel = current.level;
                System.out.print("Nivel " + currentLevel + ": ");
            }

            System.out.print(current.node + "  ");

            if (!current.node.isLeaf) {
                for (Node child : current.node.children) {
                    queue.add(new NodeLevel(child, current.level + 1));
                }
            }

            if (queue.isEmpty() || queue.peek().level != currentLevel) {
                System.out.println();
            }
        }
    }

    // Logica de eliminacion
    public void delete(String key) {
        if (root == null)
            return;

        deleteRecursive(root, key);

        // Si la raíz quedó vacía tras una fusión (Case 2b), el árbol reduce su altura
        if (root.keys.isEmpty() && !root.isLeaf) {
            root = root.children.get(0);
        }
    }

    private void deleteRecursive(Node node, String key) {
        int pos = findPosition(node, key);

        // ¿La clave está en este nodo?
        if (pos < node.keys.size() && node.keys.get(pos).equals(key)) {
            if (node.isLeaf) {
                // --- CASO 1: Eliminación simple en hoja ---
                // Se elimina la clave directamente[cite: 197].
                node.keys.remove(pos);
                System.out.println("Caso 1: Eliminación simple en hoja (" + key + ")");
            } else {
                // --- CASO 3: Eliminación en nodo interno ---
                // Se reemplaza por el predecesor o sucesor[cite: 209].
                System.out.println("Caso 3: Eliminación en nodo interno (" + key + ")");
                String predecessorKey = getPredecessor(node.children.get(pos));
                node.keys.set(pos, predecessorKey);
                deleteRecursive(node.children.get(pos), predecessorKey);
            }
        } else {
            // La clave no está en este nodo, buscar en los hijos
            if (node.isLeaf) {
                System.out.println("Clave " + key + " no encontrada.");
                return;
            }

            // Antes de descender, verificamos si el hijo necesita refuerzos (Underflow)
            // Para Orden 4, el mínimo de claves es 1[cite: 198].
            int minKeys = (int) Math.ceil(order / 2.0) - 1;

            if (node.children.get(pos).keys.size() <= minKeys) {
                fixUnderflow(node, pos);
                // Re-calcular posición por si la estructura cambió tras fixUnderflow
                pos = findPosition(node, key);
            }

            deleteRecursive(node.children.get(pos), key);
        }
    }

    private void fixUnderflow(Node parent, int childIdx) {
        int minKeys = (int) Math.ceil(order / 2.0) - 1;
        Node child = parent.children.get(childIdx);
        Node leftSibling = (childIdx > 0) ? parent.children.get(childIdx - 1) : null;
        Node rightSibling = (childIdx < parent.children.size() - 1) ? parent.children.get(childIdx + 1) : null;

        // --- CASO 2a: Redistribución (Préstamo) ---
        if (leftSibling != null && leftSibling.keys.size() > minKeys) {
            System.out.println("Caso 2a: Préstamo del hermano izquierdo");
            child.keys.add(0, parent.keys.get(childIdx - 1));
            parent.keys.set(childIdx - 1, leftSibling.keys.remove(leftSibling.keys.size() - 1));
            if (!child.isLeaf) {
                child.children.add(0, leftSibling.children.remove(leftSibling.children.size() - 1));
            }
        } else if (rightSibling != null && rightSibling.keys.size() > minKeys) {
            System.out.println("Caso 2a: Préstamo del hermano derecho");
            child.keys.add(parent.keys.get(childIdx));
            parent.keys.set(childIdx, rightSibling.keys.remove(0));
            if (!child.isLeaf) {
                child.children.add(rightSibling.children.remove(0));
            }
        }
        // --- CASO 2b: Fusión (Merge) ---
        else {
            System.out.println("Caso 2b: Fusión de nodos (Underflow)");
            if (leftSibling != null) {
                // Fusionar con el izquierdo
                leftSibling.keys.add(parent.keys.remove(childIdx - 1));
                leftSibling.keys.addAll(child.keys);
                leftSibling.children.addAll(child.children);
                parent.children.remove(childIdx);
            } else {
                // Fusionar con el derecho
                child.keys.add(parent.keys.remove(childIdx));
                child.keys.addAll(rightSibling.keys);
                child.children.addAll(rightSibling.children);
                parent.children.remove(childIdx + 1);
            }
        }
    }

    private String getPredecessor(Node node) {
        while (!node.isLeaf) {
            node = node.children.get(node.children.size() - 1);
        }
        return node.keys.get(node.keys.size() - 1);
    }

}
