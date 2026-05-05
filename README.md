# EstructurasDeDatosLab3

Implementación de un **Árbol B** en Java con soporte para inserción, eliminación y visualización por niveles.

---

## Estructura del proyecto

```
arboles_b/
├── BTree.java        # Lógica principal del árbol B
├── Node.java         # Clase nodo (claves, hijos, isLeaf)
├── Main.java         # Menú interactivo por consola
├── InsertResult.java # Resultado de una inserción
├── InsertState.java  # Estado interno durante inserción recursiva
├── SplitResult.java  # Resultado de un split de nodo
└── NodeLevel.java    # Auxiliar para recorrido por niveles (BFS)
```

---

## Funcionalidades implementadas

### Inserción
- El usuario elige el **orden del árbol** (mínimo 3) al iniciar.
- Inserción ordenada de claves de tipo `String`.
- Detección de **claves duplicadas** (no se insertan).
- Manejo de **overflow**: cuando un nodo supera el límite de claves, se hace un **split** y la clave media sube al padre (promovedClave).
- Si la raíz hace overflow, se crea una **nueva raíz**.

### Eliminación
- **Caso 1 – Eliminación simple en hoja:** se elimina la clave directamente si está en un nodo hoja.
- **Caso 2a – Redistribución (préstamo):** si el hijo tiene underflow, se toma prestada una clave del hermano izquierdo o derecho a través del padre.
- **Caso 2b – Fusión (merge):** si no hay hermano con claves suficientes, se fusionan dos nodos junto con la clave separadora del padre.
- **Caso 3 – Eliminación en nodo interno:** se reemplaza la clave por su **predecesor en hoja** y luego se elimina recursivamente ese predecesor.
- Reducción automática de la **altura del árbol** si la raíz queda vacía tras una fusión.

### Visualización
- Impresión del árbol **por niveles** (BFS) con `printByLevels()`.
- Cada nodo se muestra como `[clave1 | clave2 | ...]`.

---

## Menú interactivo (`Main.java`)

```
=================================
         MENÚ ÁRBOL B
=================================
1. Visualizar árbol
2. Insertar nuevo registro
3. Eliminar registro
4. Salir
```

- Valida entradas numéricas con manejo de excepciones.
- Informa si una inserción fue **normal** o implicó **split/overflow**.
- Indica qué caso de eliminación se aplicó en cada operación.

---

## Cómo ejecutar

1. Compilar desde la raíz del proyecto:
   ```bash
   javac arboles_b/*.java
   ```
2. Ejecutar:
   ```bash
   java arboles_b.Main
   ```
3. Ingresar el orden del árbol (ej. `4`) y usar el menú.