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

    public Node search(String key) {
        Queue<NodeLevel> queue = new LinkedList<>();
        if (root == null) return null;
        queue.add(new NodeLevel(root, 0));

        while (!queue.isEmpty()) {
            NodeLevel current = queue.poll();
            Node node = current.node;

            int pos = findPosition(node, key);

            if (pos < node.keys.size() && node.keys.get(pos).equals(key)) {
                return node;
            }

            if (!node.isLeaf) {
                queue.add(new NodeLevel(node.children.get(pos), current.level + 1));
            }
        }

        return null;
    }

    // Lógica general de eliminación en B-Tree:
    // 1) Si la clave está en hoja: se elimina directamente.
    // 2) Si está en interno: se reemplaza por su predecesor y luego se elimina ese predecesor.
    // 3) Si no está en el nodo actual: se desciende al hijo correspondiente,
    //    pero antes se garantiza que ese hijo tenga suficientes claves para evitar underflow.
    public void delete(String key) {
        if (root == null)
            return;

        deleteRecursive(root, key);

        // Si la raíz perdió su última clave tras una fusión, el árbol reduce su altura
        // promoviendo al único hijo restante como nueva raíz.
        if (root.keys.isEmpty() && !root.isLeaf) {
            root = root.children.get(0);
        }
    }

    private void deleteRecursive(Node node, String key) {
        // Posición donde debería estar la clave dentro del nodo actual.
        int pos = findPosition(node, key);

        // Si coincide en esta posición, la clave sí está en este nodo.
        if (pos < node.keys.size() && node.keys.get(pos).equals(key)) {
            if (node.isLeaf) {
                // CASO 1: la clave está en una hoja.
                // No hay hijos que reestructurar, por lo que basta removerla.
                node.keys.remove(pos);
                System.out.println("Caso 1: Eliminación simple en hoja (" + key + ")");
            } else {
                // CASO 3: la clave está en un nodo interno.
                // Estrategia aplicada: reemplazar por el predecesor (máxima clave
                // del subárbol izquierdo) para mantener el orden del B-Tree.
                // Luego se elimina recursivamente esa clave predecesora del hijo izquierdo.
                System.out.println("Caso 3: Eliminación en nodo interno (" + key + ")");
                String predecessorKey = getPredecessor(node.children.get(pos));
                node.keys.set(pos, predecessorKey);
                deleteRecursive(node.children.get(pos), predecessorKey);
            }
        } else {
            // La clave no está en este nodo; se debe continuar la búsqueda en un hijo.
            if (node.isLeaf) {
                // Si ya estamos en hoja y no apareció, la clave no existe en el árbol.
                System.out.println("Clave " + key + " no encontrada.");
                return;
            }

            // Mínimo de claves permitido por nodo (excepto raíz): ceil(order/2)-1.
            // Ejemplo: order=4 => minKeys=1.
            int minKeys = (int) Math.ceil(order / 2.0) - 1;

            if (node.children.get(pos).keys.size() <= minKeys) {
                // Antes de bajar, reforzamos el hijo si está al mínimo.
                // Esto evita quedar por debajo del mínimo después de eliminar.
                fixUnderflow(node, pos);

                // Tras préstamo o fusión, el arreglo de claves/hijos del padre puede cambiar,
                // por eso recalculamos la posición de descenso.
                pos = findPosition(node, key);
            }

            deleteRecursive(node.children.get(pos), key);
        }
    }

    private void fixUnderflow(Node parent, int childIdx) {
        // child es el hijo por el que vamos a descender y que puede quedar corto de claves.
        int minKeys = (int) Math.ceil(order / 2.0) - 1; // Mínimo de claves permitido por nodo (excepto raíz).
        Node child = parent.children.get(childIdx); // Nodo que potencialmente tiene underflow.
        Node leftSibling = (childIdx > 0) ? parent.children.get(childIdx - 1) : null; // Hermano izquierdo, si existe.
        Node rightSibling = (childIdx < parent.children.size() - 1) ? parent.children.get(childIdx + 1) : null; // Hermano derecho, si existe.
        // CASO 2a: redistribución (préstamo).
        // Se prioriza tomar del hermano izquierdo si tiene más de minKeys.
        // La clave separadora del padre baja al hijo y una clave del hermano sube al padre.
        if (leftSibling != null && leftSibling.keys.size() > minKeys) {
            System.out.println("Caso 2a: Préstamo del hermano izquierdo");
            child.keys.add(0, parent.keys.get(childIdx - 1));
            parent.keys.set(childIdx - 1, leftSibling.keys.remove(leftSibling.keys.size() - 1));

            // Si no es hoja, también se mueve el subárbol extremo correspondiente.
            if (!child.isLeaf) {
                child.children.add(0, leftSibling.children.remove(leftSibling.children.size() - 1));
            }
        } else if (rightSibling != null && rightSibling.keys.size() > minKeys) {
            // Préstamo simétrico desde el hermano derecho.
            System.out.println("Caso 2a: Préstamo del hermano derecho");
            child.keys.add(parent.keys.get(childIdx));
            parent.keys.set(childIdx, rightSibling.keys.remove(0));

            // Si hay hijos, se mueve el primer hijo del hermano derecho al final de child.
            if (!child.isLeaf) {
                child.children.add(rightSibling.children.remove(0));
            }
        }
        // CASO 2b: fusión.
        // Si ningún hermano puede prestar, se fusiona child con un hermano y una clave del padre.
        // El padre pierde una clave y un puntero hijo.
        else {
            System.out.println("Caso 2b: Fusión de nodos (Underflow)");
            if (leftSibling != null) {
                // Fusión con hermano izquierdo:
                // leftSibling + clave separadora del padre + child.
                leftSibling.keys.add(parent.keys.remove(childIdx - 1));
                leftSibling.keys.addAll(child.keys);
                leftSibling.children.addAll(child.children);
                parent.children.remove(childIdx);
            } else {
                // Fusión con hermano derecho:
                // child + clave separadora del padre + rightSibling.
                child.keys.add(parent.keys.remove(childIdx));
                child.keys.addAll(rightSibling.keys);
                child.children.addAll(rightSibling.children);
                parent.children.remove(childIdx + 1);
            }
        }
    }

    private String getPredecessor(Node node) {
        // El predecesor es la clave más grande del subárbol:
        // bajar siempre por el hijo más a la derecha hasta llegar a hoja.
        while (!node.isLeaf)
            node = node.children.get(node.children.size() - 1);        

        // Última clave de la hoja más a la derecha.
        return node.keys.get(node.keys.size() - 1);
    }
}
