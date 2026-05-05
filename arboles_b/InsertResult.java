package arboles_b;

// =========================================================
// Resultado de inserción
// =========================================================
public class InsertResult {
    boolean inserted;
    boolean hadOverflow;

    InsertResult(boolean inserted, boolean hadOverflow) {
        this.inserted = inserted;
        this.hadOverflow = hadOverflow;
    }
}
