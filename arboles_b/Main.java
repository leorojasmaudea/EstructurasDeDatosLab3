package arboles_b;

import java.util.Scanner;

public class Main {

   public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int order = askOrder(sc);
        BTree tree = new BTree(order);

        int option;
        do {
            showMenu();
            option = readInt(sc, "Seleccione una opción: ");

            switch (option) {
                case 1:
                    System.out.println("\n=== VISUALIZACIÓN DEL ÁRBOL B ===");
                    if (tree.isEmpty()) {
                        System.out.println("El árbol está vacío.");
                    } else {

                        System.out.println("\nPor niveles:");
                        tree.printByLevels();
                    }
                    break;

                case 2:
                    String value = readString(sc, "Ingrese la clave a insertar: ");
                    InsertResult result = tree.insert(value);

                    if (!result.inserted) {
                        System.out.println("La clave " + value + " ya existe. No se insertó.");
                    } else if (result.hadOverflow) {
                        System.out.println("Inserción CON OVERFLOW/SPLIT.");
                    } else {
                        System.out.println("Inserción NORMAL.");
                    }
                    break;

                case 3:
                    String keyToDelete = readString(sc, "Ingrese la clave a eliminar: ");
                    tree.delete(keyToDelete);
                    break;

                case 4:
                    String keyToSearch = readString(sc, "Ingrese la clave a buscar: ");
                    Node found = tree.search(keyToSearch);
                    if (found != null) {
                        System.out.println("Clave \"" + keyToSearch + "\" encontrada en el nodo: " + found);
                    } else {
                        System.out.println("Clave \"" + keyToSearch + "\" no encontrada.");
                    }
                    break;

                case 5:
                    System.out.println("Saliendo...");
                    break;

                default:
                    System.out.println("Opción inválida.");
            }

            System.out.println();
        } while (option != 5);

        sc.close();
    }

    private static int askOrder(Scanner sc) {
        int order;
        do {
            order = readInt(sc, "Ingrese el orden del árbol B (entre 4 y 9): ");
            if (order < 4 || order > 9) {
                System.out.println("El orden debe estar entre 4 y 9.");
            }
        } while (order < 4 || order > 9);
        return order;
    }

    private static void showMenu() {
        System.out.println("=================================");
        System.out.println("         MENÚ ÁRBOL B");
        System.out.println("=================================");
        System.out.println("1. Visualizar árbol");
        System.out.println("2. Insertar nuevo registro");
        System.out.println("3. Eliminar registro");
        System.out.println("4. Buscar registro");
        System.out.println("5. Salir");
    }

    private static String readString(Scanner sc, String message) {
        System.out.print(message);
        return sc.nextLine().trim();
    }

    private static int readInt(Scanner sc, String message) {
        while (true) {
            try {
                System.out.print(message);
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida.");
            }
        } 
    }

}