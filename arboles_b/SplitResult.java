package arboles_b;

// =========================================================
// Auxiliares internos
// =========================================================
public class SplitResult {
    int promotedKey;
    Node left;
    Node right;

    SplitResult(int promotedKey, Node left, Node right) {
        this.promotedKey = promotedKey;
        this.left = left;
        this.right = right;
    }
}
