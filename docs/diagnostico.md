# Diagnóstico de calidad — Versión inicial

**Versión analizada:** tag `estado-inicial`
**Alcance:** `Estudiante`, `Responsable`, `Solicitud`, `SistemaSolicitudes` y `Main` (paquete `ar.edu.utn.solicitudes`)

---

## 1. Calidad externa vs. calidad interna (Etapa 2)

### 1.1 Calidad externa: lo que ve el usuario

Desde la mirada del usuario, el sistema **cumple sus funciones**. Todas las operaciones se verificaron manualmente con el guion de prueba (evidencias en `docs/evidencias/inicial/`):

| Operación | Resultado | Evidencia |
|-----------|-----------|-----------|
| Registrar solicitud y calcular plazo | ✔ Funciona | `01-registro-solicitud.png` |
| Asignar responsable | ✔ Funciona | `02-asignar-responsable.png` |
| Cambiar estado con transiciones válidas | ✔ Funciona; rechaza transiciones inválidas | Guion de prueba, pasos 3, 5 y 6 |
| Filtrar por estado, tipo y estudiante | ✔ Funciona | Guion de prueba, paso 8 |
| Reporte por estado, tipo y vencidas | ✔ Funciona | `03-reporte-con-vencida.png` |
| Persistencia entre ejecuciones | ✔ Funciona en el caso normal | `04-recarga-desde-csv.png` |

Sin embargo, al probar casos fuera del camino feliz aparecen **defectos externos**:

| ID | Defecto | Cómo se reproduce | Consecuencia |
|----|---------|-------------------|--------------|
| D-01 | El programa se cierra con una letra en un campo numérico | Registrar → prioridad `a` | `NumberFormatException`; se pierde la operación en curso (`05-error-prioridad-letra.png`) |
| D-02 | El programa se cierra con una fecha en otro formato | Registrar → fecha `28/10/2026` | `DateTimeParseException` no controlada |
| D-03 | **Pérdida silenciosa y permanente de datos** | Registrar una solicitud cuya descripción contenga `;` y luego otras solicitudes. Cerrar y volver a abrir | Ver detalle abajo |

**Detalle de D-03 (reproducido):**

1. Se registran 3 solicitudes; la #2 tiene la descripción `Equivalencia de Algebra; urgente`.
2. El CSV guarda la línea de la #2 con un `;` de más, por lo que queda con 11 campos en lugar de 10.
3. Al reiniciar, `cargar()` intenta leer la fecha en la posición equivocada, falla, y el `catch` vacío **detiene la carga sin avisar**. Se cargan solo las solicitudes anteriores a la #2.
4. La próxima operación ejecuta `guardar()`, que **reescribe el archivo** solo con lo cargado: las solicitudes #2 y #3 se pierden definitivamente.
5. Además, como `ultimoId` quedó en 1, la siguiente solicitud nueva **reutiliza el id #2**.

> Este defecto externo es consecuencia directa de problemas internos: formato de persistencia frágil, excepciones tragadas (CS-08) y lógica de negocio acoplada al archivo (CS-09). Es el ejemplo más claro de cómo la calidad interna termina afectando lo que ve el usuario.

### 1.2 Calidad interna: lo que no se ve

| Indicador | Valor en la versión inicial |
|-----------|-----------------------------|
| Líneas de `SistemaSolicitudes` | 334 (69 % del código total, 483 líneas) |
| Métodos públicos en `SistemaSolicitudes` | 11 |
| Responsabilidades distintas en `SistemaSolicitudes` | 7: almacenamiento en memoria, validación, reglas de negocio, búsqueda y filtrado, presentación, reportes y persistencia |
| Literales de tipo y estado escritos a mano (`"PENDIENTE"`, `"CERTIFICADO"`, etc.) | 55 apariciones en 2 clases |
| Llamadas a `System.out.print` dentro de la lógica de negocio | 32 |
| Bloques `catch` vacíos | 2 |
| Interfaces o abstracciones | 0 |
| Tests automatizados | 0 |
| Atributos públicos en entidades | 14 de 14 |

### 1.3 Relación con mantenibilidad, legibilidad y evolución futura

**Escenario A — Agregar un tipo de solicitud nuevo (por ejemplo, `BAJA_MATERIA`).**
Hay que modificar **5 lugares en 2 clases**: la validación (`SistemaSolicitudes:38`), el `switch` de plazos (`:83–96`), los contadores del reporte (`:247–263`, que requiere una variable, un `else if` y un `println`) y dos mensajes del menú (`Main:40` y `Main:79`). Si se olvida uno, **el compilador no avisa**: por ejemplo, el reporte simplemente no contaría el tipo nuevo.

**Escenario B — Reemplazar el CSV por una base de datos.**
Hay que reescribir `guardar()` y `cargar()`, que están dentro de la misma clase que las reglas de negocio, y revisar el constructor y los tres métodos que llaman a `guardar()`. No existe un punto único de cambio.

**Escenario C — Verificar automáticamente el reporte.**
No es posible sin capturar la salida de consola: el método no devuelve datos, depende de `LocalDate.now()` (el resultado cambia según el día) y el constructor del sistema lee el archivo real del disco.

**Legibilidad.** Para entender `registrarSolicitud` hay que leer 55 líneas que mezclan cinco tareas, separadas solo por comentarios. Nombres como `lst`, `x`, `s`, `e`, `desc` o `resp` obligan a leer el contexto para saber qué representan.

**Conclusión.** El sistema funciona en el caso normal, pero el **costo de cambio es alto** y el **riesgo de regresión es elevado**: cualquier modificación toca varias responsabilidades a la vez y no hay tests que detecten errores. La deuda técnica ya se manifiesta como defectos externos (D-01 a D-03).

---

## 2. Deuda técnica y code smells (Etapa 3)

### 2.1 Criterio de prioridad

| Prioridad | Criterio |
|-----------|----------|
| **Alta** | Puede causar pérdida de datos o errores, o bloquea otros refactorings |
| **Media** | Dificulta cambios frecuentes o la testeabilidad |
| **Baja** | Afecta la legibilidad de forma localizada |

### 2.2 Resumen

| ID | Code smell | Ubicación principal | Prioridad | Principio / refactoring |
|----|-----------|---------------------|-----------|-------------------------|
| CS-01 | God Class | `SistemaSolicitudes` (completa) | Alta | SRP · Extract Class |
| CS-02 | Long Method | `registrarSolicitud` (24–79) | Alta | Extract Method |
| CS-03 | Long Parameter List | `registrarSolicitud` (24) | Media | Introduce Parameter Object |
| CS-04 | Condicionales por tipo | `calcularDiasLimite` (81–101), `cambiarEstado` (120–130) | Alta | OCP · Replace Conditional with Polymorphism |
| CS-05 | Primitive Obsession y números mágicos | `Solicitud` (tipo, estado, prioridad) y 55 literales | Alta | Replace Type Code with Enum |
| CS-06 | Código duplicado | Búsquedas (5 bloques) y filtros (3 métodos) | Alta | Extract Method · Parametrizar con `Predicate` |
| CS-07 | Feature Envy | `reporte` (268–269), `asignarResponsable` (150), `mostrar` (202–223) | Media | Move Method |
| CS-08 | Excepciones tragadas | `guardar` (293), `cargar` (331) | Alta | Manejo explícito de errores |
| CS-09 | Acoplamiento a la persistencia | `guardar` / `cargar` (281–333), constructor (21) | Alta | DIP · Extract Interface (repositorio) |
| CS-10 | Presentación mezclada con negocio | 32 `println` en `SistemaSolicitudes`; `Main.main` (8–96) | Media | SRP · Separar capa de consola |
| CS-11 | Falta de encapsulamiento | Entidades con atributos públicos; `Main` usa `sis.lst` y `sis.responsables` | Media | Encapsulate Field |
| CS-12 | Nombres poco expresivos | `lst`, `x`, `s`, `e`, `f`, `desc`, `est`, `resp`, `pend`, `cc`, `rn` | Baja | Rename |

### 2.3 Fichas

---

#### CS-01 — God Class

- **Ubicación:** `SistemaSolicitudes.java` (334 líneas, 11 métodos).
- **Evidencia:** una sola clase guarda las listas, valida datos, aplica reglas de negocio, filtra, imprime en pantalla, arma el reporte y lee y escribe el archivo.
- **Impacto:** cualquier cambio, aunque sea de presentación, obliga a modificar la clase central. Concentra el 69 % del código.
- **Riesgo de mantenimiento:** alto. Dos cambios independientes (por ejemplo, el formato del reporte y una regla de negocio) se cruzan en el mismo archivo, y un error en una parte puede romper otra.
- **Prioridad:** Alta.
- **Propuesta:** dividir en clases con una responsabilidad cada una: servicio de solicitudes, repositorio, generador de reporte y menú de consola (**SRP**, Extract Class).

---

#### CS-02 — Long Method

- **Ubicación:** `SistemaSolicitudes.registrarSolicitud`, líneas 24–79.
- **Evidencia:** 55 líneas divididas en bloques con comentarios (`// validaciones`, `// buscar o crear estudiante`, `// crear solicitud`, `// los reclamos de nota...`).
- **Impacto:** cinco tareas en un método; los comentarios hacen de separadores porque el código no se explica solo.
- **Riesgo de mantenimiento:** alto. Modificar una validación obliga a leer y entender todo el método.
- **Prioridad:** Alta.
- **Propuesta:** **Extract Method** por bloque: `validarDatos`, `buscarOCrearEstudiante`, `crearSolicitud`, `aplicarReglasDePrioridad`. Cada comentario se convierte en el nombre de un método.

---

#### CS-03 — Long Parameter List

- **Ubicación:** `SistemaSolicitudes.registrarSolicitud`, línea 24.
- **Evidencia:**
  ```java
  registrarSolicitud(String legajo, String nombre, String email, String tipo,
                     int prioridad, String desc, String fecha)
  ```
- **Impacto:** 7 parámetros, 6 de ellos `String`. Si se invierte el orden de dos (por ejemplo, `nombre` y `email`), el compilador no lo detecta.
- **Riesgo de mantenimiento:** medio. Agregar un dato nuevo cambia la firma y todas las llamadas.
- **Prioridad:** Media.
- **Propuesta:** **Introduce Parameter Object**: clase `DatosSolicitud` (o `NuevaSolicitudRequest`) que agrupe los datos de entrada.

---

#### CS-04 — Condicionales por tipo y por estado

- **Ubicación:** `calcularDiasLimite` (81–101) y cadena de `if / else if` en `cambiarEstado` (120–130). También los contadores de `reporte` (229–263).
- **Evidencia:**
  ```java
  switch (s.tipo) {
      case "CERTIFICADO": dias = 5; break;
      case "EQUIVALENCIA": dias = 30; break;
      ...
  }
  ```
- **Impacto:** el comportamiento que depende del tipo o del estado está repartido en condicionales. Agregar un tipo o un estado obliga a modificar código existente en varios lugares (escenario A).
- **Riesgo de mantenimiento:** alto. Viola **OCP**: el sistema no está abierto a extensión sin modificación.
- **Prioridad:** Alta.
- **Propuesta:** **Replace Conditional with Polymorphism**: un enum `TipoSolicitud` que conozca sus días de plazo y un enum `EstadoSolicitud` con un método `puedePasarA(EstadoSolicitud)`. Cada valor respeta el mismo contrato, lo que permite analizar también **LSP**.

---

#### CS-05 — Primitive Obsession y números mágicos

- **Ubicación:** atributos `tipo`, `estado` (`String`) y `prioridad` (`int`) de `Solicitud`; 55 literales repetidos en `SistemaSolicitudes` y `Main`; valores `5`, `30`, `10`, `7`, `1`, `/ 2` en `calcularDiasLimite`; índices `x[0]` a `x[9]` en `cargar`.
- **Evidencia:** `s.estado = "PENDIENTE";` · `if (s.prioridad == 1)` · `new Estudiante(x[6], x[7], x[8])`.
- **Impacto:** un error de tipeo (`"PENDENTE"`) compila sin problemas y produce un comportamiento incorrecto. El significado de `prioridad == 1` hay que adivinarlo.
- **Riesgo de mantenimiento:** alto. Cambiar el nombre de un estado implica buscar y reemplazar texto en varias clases.
- **Prioridad:** Alta (es base para resolver CS-04).
- **Propuesta:** **Replace Type Code with Enum**: `TipoSolicitud`, `EstadoSolicitud` y `Prioridad`. Constantes con nombre para los índices del CSV.

---

#### CS-06 — Código duplicado

- **Ubicación:**
    - "Buscar solicitud por id": líneas 104–109 y 139–144 (idénticas).
    - "Buscar responsable por id": líneas 155–160 (misma estructura).
    - "Buscar o crear estudiante": líneas 52–61 y 304–313.
    - Filtros: `filtrarPorEstado` (172–180), `filtrarPorTipo` (182–190) y `filtrarPorEstudiante` (192–200), que solo difieren en la condición del `if`.
- **Evidencia:**
  ```java
  Solicitud s = null;
  for (int i = 0; i < lst.size(); i++) {
      if (lst.get(i).id == id) {
          s = lst.get(i);
      }
  }
  ```
- **Impacto:** la misma lógica copiada en 8 lugares. Una corrección en una copia no se aplica a las demás.
- **Riesgo de mantenimiento:** alto. Ejemplo concreto: si cambia la regla de cómo se identifica a un estudiante, hay que acordarse de cambiarla en `registrarSolicitud` **y** en `cargar`.
- **Prioridad:** Alta.
- **Propuesta:** **Extract Method** (`buscarSolicitud`, `buscarResponsable`, `buscarOCrearEstudiante`) y un único `filtrar(Predicate<Solicitud>)`.

---

#### CS-07 — Feature Envy

- **Ubicación:** `reporte` (268–269), `asignarResponsable` (150), `mostrar` (202–223).
- **Evidencia:**
  ```java
  if (!s.estado.equals("RESUELTA") && !s.estado.equals("RECHAZADA")) {
      LocalDate limite = s.fecha.plusDays(calcularDiasLimite(s));
      if (LocalDate.now().isAfter(limite)) { ... }
  }
  ```
- **Impacto:** `SistemaSolicitudes` usa casi exclusivamente datos de `Solicitud` para decidir si está cerrada o vencida. La pregunta "¿está cerrada?" aparece 2 veces con el mismo código. Además, `LocalDate.now()` dentro del método impide verificarlo con una fecha fija.
- **Riesgo de mantenimiento:** medio. La regla de "vencida" no está donde un desarrollador la buscaría.
- **Prioridad:** Media.
- **Propuesta:** **Move Method**: `solicitud.estaCerrada()`, `solicitud.fechaLimite()` y `solicitud.estaVencida(LocalDate hoy)`, recibiendo la fecha como parámetro.

---

#### CS-08 — Excepciones tragadas

- **Ubicación:** `guardar` (línea 293) y `cargar` (línea 331).
- **Evidencia:**
  ```java
  } catch (Exception ex) {
  }
  ```
- **Impacto:** cualquier error de lectura o escritura se ignora sin avisar. Es la causa directa del defecto **D-03** (pérdida de datos). Además, si ocurre una excepción antes de `close()`, el archivo queda abierto.
- **Riesgo de mantenimiento:** alto. Los errores no dejan rastro, lo que hace muy difícil diagnosticar fallas.
- **Prioridad:** Alta.
- **Propuesta:** capturar excepciones específicas, informar el error (excepción propia `PersistenciaException`) y usar `try-with-resources`. Validar o escapar el separador del CSV.

---

#### CS-09 — Acoplamiento a la persistencia concreta

- **Ubicación:** `guardar` y `cargar` (281–333); llamada a `cargar()` en el constructor (línea 21); llamadas a `guardar()` en las líneas 75, 133 y 167.
- **Evidencia:** la lógica de negocio crea directamente `new FileWriter("solicitudes.csv")` y `new FileReader("solicitudes.csv")`, con la ruta escrita dos veces.
- **Impacto:** el negocio depende de un detalle de bajo nivel (el archivo). No se puede cambiar el almacenamiento sin modificar las reglas de negocio, y no se puede probar el sistema sin tocar el archivo real del disco.
- **Riesgo de mantenimiento:** alto. Viola **DIP**: un módulo de alto nivel depende de uno de bajo nivel.
- **Prioridad:** Alta.
- **Propuesta:** **Extract Interface** `SolicitudRepository`, con una implementación CSV y otra en memoria para pruebas. El servicio recibe el repositorio por constructor. Permite además aplicar **ISP**, separando las operaciones de consulta (que usa el reporte) de las de escritura.

---

#### CS-10 — Presentación mezclada con lógica de negocio

- **Ubicación:** 32 llamadas a `System.out.print` en `SistemaSolicitudes`; métodos `mostrar` y `reporte` que imprimen en lugar de devolver datos; `Main.main` (líneas 8–96) con un `switch` de 9 casos.
- **Evidencia:** `registrarSolicitud` informa los errores con `println` y devuelve `null`; quien llama no sabe por qué falló.
- **Impacto:** no se puede reutilizar la lógica con otra interfaz (web, tests) sin arrastrar la consola. Los errores no se pueden manejar de forma distinta según el contexto.
- **Riesgo de mantenimiento:** medio. Cambiar un mensaje obliga a tocar la clase de negocio.
- **Prioridad:** Media.
- **Propuesta:** el servicio devuelve resultados o lanza excepciones; una clase `MenuConsola` se encarga de leer, imprimir y validar la entrada (corrige también D-01 y D-02).

---

#### CS-11 — Falta de encapsulamiento

- **Ubicación:** los 14 atributos de `Estudiante`, `Responsable` y `Solicitud` son públicos; las listas de `SistemaSolicitudes` también (líneas 12–14), y `Main` las usa directamente (`sis.lst` en línea 72, `sis.responsables` en línea 55).
- **Evidencia:** `s.estado = "PENDIENTE";` se asigna desde afuera de la clase; el constructor de `Solicitud` permite crear una solicitud con `estado == null`.
- **Impacto:** cualquier clase puede dejar un objeto en un estado inválido (por ejemplo, pasar de RESUELTA a PENDIENTE sin pasar por las validaciones de `cambiarEstado`).
- **Riesgo de mantenimiento:** medio. Las reglas de negocio no están protegidas por el objeto que las tiene que cumplir.
- **Prioridad:** Media.
- **Propuesta:** **Encapsulate Field**: atributos privados, getters, y métodos de comportamiento (`asignarResponsable`, `cambiarEstado`) dentro de `Solicitud`. El constructor crea siempre la solicitud en PENDIENTE.

---

#### CS-12 — Nombres poco expresivos

- **Ubicación:** `lst` (línea 12), `desc`, `est`, `resp` (atributos de `Solicitud`), `s`, `e`, `f`, `x`, `aux`, `r`, `p`, `pend`, `proc`, `cert`, `eq`, `cc`, `rn` en distintos métodos.
- **Evidencia:** `new Estudiante(x[6], x[7], x[8])`; `int cert = 0, eq = 0, cc = 0, rn = 0;`.
- **Impacto:** el lector tiene que reconstruir el significado a partir del contexto.
- **Riesgo de mantenimiento:** bajo, pero constante.
- **Prioridad:** Baja (se corrige de forma transversal durante los demás refactorings).
- **Propuesta:** **Rename**: `solicitudes`, `descripcion`, `estudiante`, `responsable`, `campos`, etc.

---

## 3. Plan de corrección

Antes de modificar el código se escriben **tests de caracterización** sobre la versión inicial, que luego deben pasar sin cambios en la versión refactorizada.

Orden propuesto, de lo que habilita a lo que depende:

| Orden | Smells | Motivo |
|-------|--------|--------|
| 1 | CS-05 | Los enums son la base para eliminar condicionales y literales |
| 2 | CS-11 | Encapsular las entidades antes de mover comportamiento hacia ellas |
| 3 | CS-07 | Mover la lógica de cerrada/vencida a `Solicitud` |
| 4 | CS-04 | Reemplazar `switch` y cadenas de `if` por comportamiento de los enums |
| 5 | CS-02, CS-06 | Extraer métodos y unificar duplicados |
| 6 | CS-03 | Introducir el objeto de parámetros |
| 7 | CS-09, CS-08 | Extraer el repositorio y corregir el manejo de errores (corrige D-03) |
| 8 | CS-10, CS-01 | Separar consola y servicio; la God Class desaparece (corrige D-01 y D-02) |
| Transversal | CS-12 | Renombrar en cada paso |