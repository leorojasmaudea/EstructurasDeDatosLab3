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
    //////////////////////////////////////////////////////
    // [ | 10 | 20 | 30 |]
    // [01, 04, 06] [11, 12, 13] [21, 22, 23] [31, 32, 33, 40] -> Overflow
    ///////////////////////////////////////////////////////////////////////
    // [ | 10 | 20 | 30 |]
    // [01, 04, 06] [11, 12, 13] [21, 22, 23] [31, 32, 33, 40] -> Overflow (Sube 32)
    ///////////////////////////////////////////////////////////////////////
    //                 [ | 10 | 20 | 30 | 32 | ]
    // [01, 04, 06] [11, 12, 13] [21, 22, 23] [31] [33, 40]
    ////////////////////////////////////////////////////////////////////////////
    //                          [20]
    //           [ 10 ]                    [ 30, 32 ]
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

}
