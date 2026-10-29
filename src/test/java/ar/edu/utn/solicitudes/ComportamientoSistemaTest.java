package ar.edu.utn.solicitudes;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests de caracterizacion (caja negra).
 *
 * Ejecutan el programa a traves del menu de consola, igual que un usuario,
 * y verifican la salida. No dependen de la estructura interna del codigo,
 * por lo que deben pasar SIN CAMBIOS antes y despues del refactoring.
 */
class ComportamientoSistemaTest {

    private static final Path ARCHIVO = Path.of("solicitudes.csv");
    private static final Path RESPALDO = Path.of("solicitudes.csv.bak");

    // Fechas fijas para que el resultado no dependa del dia en que se corren los tests
    private static final String FECHA_PASADA = "2020-01-01";
    private static final String FECHA_FUTURA = "2099-01-01";

    // ---------- preparacion ----------

    @BeforeAll
    static void respaldarDatosReales() throws IOException {
        if (Files.exists(ARCHIVO)) {
            Files.copy(ARCHIVO, RESPALDO, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @AfterAll
    static void restaurarDatosReales() throws IOException {
        Files.deleteIfExists(ARCHIVO);
        if (Files.exists(RESPALDO)) {
            Files.move(RESPALDO, ARCHIVO);
        }
    }

    @BeforeEach
    void empezarSinDatos() throws IOException {
        Files.deleteIfExists(ARCHIVO);
    }

    // ---------- ayudas ----------

    /** Ejecuta el programa con las lineas indicadas como entrada y devuelve todo lo que imprimio. */
    private String ejecutar(String... lineas) {
        String entrada = String.join("\n", lineas) + "\n0\n";
        InputStream inOriginal = System.in;
        PrintStream outOriginal = System.out;
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(salida, true, StandardCharsets.UTF_8));
            Main.main(new String[0]);
        } finally {
            System.setIn(inOriginal);
            System.setOut(outOriginal);
        }
        return salida.toString(StandardCharsets.UTF_8);
    }

    /** Lineas de entrada para la opcion 1 (registrar solicitud). */
    private String[] registrar(String legajo, String nombre, String email, String tipo,
                               String prioridad, String descripcion, String fecha) {
        return new String[] {"1", legajo, nombre, email, tipo, prioridad, descripcion, fecha};
    }

    private String[] concatenar(String[]... partes) {
        return java.util.Arrays.stream(partes).flatMap(java.util.Arrays::stream).toArray(String[]::new);
    }

    // ---------- registro y plazos ----------

    @Test
    @DisplayName("Un certificado con prioridad media tiene plazo de 5 dias")
    void registraCertificadoConPlazoDeCincoDias() {
        String salida = ejecutar(registrar("1001", "Ana Perez", "ana@mail.com", "CERTIFICADO", "2",
                "Certificado de alumno regular", FECHA_FUTURA));

        assertTrue(salida.contains("Solicitud #1 registrada. Plazo: 5 dias"));
    }

    @Test
    @DisplayName("La prioridad alta reduce el plazo a la mitad")
    void prioridadAltaReduceElPlazoALaMitad() {
        String salida = ejecutar(registrar("1001", "Ana Perez", "ana@mail.com", "EQUIVALENCIA", "1",
                "Equivalencia de Algebra I", FECHA_FUTURA));

        assertTrue(salida.contains("Solicitud #1 registrada. Plazo: 15 dias"));
    }

    @Test
    @DisplayName("Un reclamo de nota siempre queda con prioridad alta")
    void reclamoDeNotaSeFuerzaAPrioridadAlta() {
        String salida = ejecutar(concatenar(
                registrar("1001", "Ana Perez", "ana@mail.com", "RECLAMO_NOTA", "3",
                        "Reclamo nota del parcial 1", FECHA_FUTURA),
                new String[] {"4"}));

        assertTrue(salida.contains("Plazo: 3 dias"));
        assertTrue(salida.contains("#1 | RECLAMO_NOTA | ALTA | PENDIENTE"));
    }

    @Test
    @DisplayName("Los ids son consecutivos")
    void losIdsSonConsecutivos() {
        String salida = ejecutar(concatenar(
                registrar("1001", "Ana Perez", "ana@mail.com", "CERTIFICADO", "2",
                        "Certificado de alumno regular", FECHA_FUTURA),
                registrar("1002", "Juan Paz", "juan@mail.com", "CAMBIO_COMISION", "3",
                        "Cambio a comision noche", FECHA_FUTURA)));

        assertTrue(salida.contains("Solicitud #1 registrada. Plazo: 5 dias"));
        assertTrue(salida.contains("Solicitud #2 registrada. Plazo: 10 dias"));
    }

    @Test
    @DisplayName("Un legajo existente reutiliza al estudiante ya registrado")
    void legajoExistenteReutilizaAlEstudiante() {
        String salida = ejecutar(concatenar(
                registrar("1001", "Ana Perez", "ana@mail.com", "CERTIFICADO", "2",
                        "Certificado de alumno regular", FECHA_FUTURA),
                registrar("1001", "Otro Nombre", "otro@mail.com", "CERTIFICADO", "2",
                        "Certificado analitico parcial", FECHA_FUTURA),
                new String[] {"7", "1001"}));

        assertTrue(salida.contains("#2 | CERTIFICADO | MEDIA | PENDIENTE | Ana Perez"));
        assertFalse(salida.contains("Otro Nombre"));
    }

    // ---------- validaciones ----------

    @Test
    @DisplayName("Rechaza un email sin arroba")
    void rechazaEmailInvalido() {
        String salida = ejecutar(concatenar(
                registrar("1001", "Ana Perez", "ana.mail.com", "CERTIFICADO", "2",
                        "Certificado de alumno regular", FECHA_FUTURA),
                new String[] {"4"}));

        assertTrue(salida.contains("Error: email invalido"));
        assertTrue(salida.contains("No hay solicitudes para mostrar"));
    }

    @Test
    @DisplayName("Rechaza un tipo inexistente")
    void rechazaTipoInvalido() {
        String salida = ejecutar(registrar("1001", "Ana Perez", "ana@mail.com", "BAJA_MATERIA", "2",
                "Quiero darme de baja", FECHA_FUTURA));

        assertTrue(salida.contains("Error: tipo invalido"));
    }

    @Test
    @DisplayName("Rechaza una prioridad fuera de rango")
    void rechazaPrioridadFueraDeRango() {
        String salida = ejecutar(registrar("1001", "Ana Perez", "ana@mail.com", "CERTIFICADO", "5",
                "Certificado de alumno regular", FECHA_FUTURA));

        assertTrue(salida.contains("Error: prioridad invalida"));
    }

    @Test
    @DisplayName("Rechaza una descripcion de menos de 10 caracteres")
    void rechazaDescripcionCorta() {
        String salida = ejecutar(registrar("1001", "Ana Perez", "ana@mail.com", "CERTIFICADO", "2",
                "corta", FECHA_FUTURA));

        assertTrue(salida.contains("Error: la descripcion debe tener al menos 10 caracteres"));
    }

    // ---------- responsables y estados ----------

    @Test
    @DisplayName("No permite pasar a EN_PROCESO sin responsable")
    void noPasaAEnProcesoSinResponsable() {
        String salida = ejecutar(concatenar(
                registrar("1001", "Ana Perez", "ana@mail.com", "CERTIFICADO", "2",
                        "Certificado de alumno regular", FECHA_FUTURA),
                new String[] {"3", "1", "EN_PROCESO"}));

        assertTrue(salida.contains("Error: no se puede pasar a EN_PROCESO sin responsable asignado"));
    }

    @Test
    @DisplayName("Flujo completo: asignar, procesar y resolver")
    void flujoCompletoHastaResuelta() {
        String salida = ejecutar(concatenar(
                registrar("1001", "Ana Perez", "ana@mail.com", "CERTIFICADO", "2",
                        "Certificado de alumno regular", FECHA_FUTURA),
                new String[] {"2", "1", "2"},
                new String[] {"3", "1", "EN_PROCESO"},
                new String[] {"3", "1", "RESUELTA"},
                new String[] {"4"}));

        assertTrue(salida.contains("Solicitud #1 asignada a Martin Ruiz (Secretaria Academica)"));
        assertTrue(salida.contains("Solicitud #1 ahora esta EN_PROCESO"));
        assertTrue(salida.contains("Solicitud #1 ahora esta RESUELTA"));
        assertTrue(salida.contains("#1 | CERTIFICADO | MEDIA | RESUELTA | Ana Perez | Martin Ruiz (Secretaria Academica)"));
    }

    @Test
    @DisplayName("Rechaza una transicion de estado no permitida")
    void rechazaTransicionInvalida() {
        String salida = ejecutar(concatenar(
                registrar("1001", "Ana Perez", "ana@mail.com", "CERTIFICADO", "2",
                        "Certificado de alumno regular", FECHA_FUTURA),
                new String[] {"3", "1", "RESUELTA"}));

        assertTrue(salida.contains("Error: no se puede pasar de PENDIENTE a RESUELTA"));
    }

    @Test
    @DisplayName("Una solicitud pendiente se puede rechazar")
    void pendienteSePuedeRechazar() {
        String salida = ejecutar(concatenar(
                registrar("1001", "Ana Perez", "ana@mail.com", "CERTIFICADO", "2",
                        "Certificado de alumno regular", FECHA_FUTURA),
                new String[] {"3", "1", "RECHAZADA"}));

        assertTrue(salida.contains("Solicitud #1 ahora esta RECHAZADA"));
    }

    @Test
    @DisplayName("No permite asignar responsable a una solicitud cerrada")
    void noAsignaResponsableASolicitudCerrada() {
        String salida = ejecutar(concatenar(
                registrar("1001", "Ana Perez", "ana@mail.com", "CERTIFICADO", "2",
                        "Certificado de alumno regular", FECHA_FUTURA),
                new String[] {"3", "1", "RECHAZADA"},
                new String[] {"2", "1", "1"}));

        assertTrue(salida.contains("Error: la solicitud ya esta cerrada"));
    }

    @Test
    @DisplayName("Informa cuando la solicitud o el responsable no existen")
    void informaSolicitudOResponsableInexistente() {
        String salida = ejecutar(concatenar(
                registrar("1001", "Ana Perez", "ana@mail.com", "CERTIFICADO", "2",
                        "Certificado de alumno regular", FECHA_FUTURA),
                new String[] {"2", "99", "1"},
                new String[] {"2", "1", "9"}));

        assertTrue(salida.contains("Error: no existe la solicitud 99"));
        assertTrue(salida.contains("Error: no existe el responsable 9"));
    }

    // ---------- filtros ----------

    @Test
    @DisplayName("Filtra por estado")
    void filtraPorEstado() {
        String[] datos = concatenar(
                registrar("1001", "Ana Perez", "ana@mail.com", "CERTIFICADO", "2",
                        "Certificado de alumno regular", FECHA_FUTURA),
                registrar("1002", "Juan Paz", "juan@mail.com", "EQUIVALENCIA", "3",
                        "Equivalencia de Algebra I", FECHA_FUTURA),
                new String[] {"3", "2", "RECHAZADA"});

        String porEstado = ejecutar(concatenar(datos, new String[] {"5", "RECHAZADA"}));
        assertTrue(porEstado.contains("#2 | EQUIVALENCIA | BAJA | RECHAZADA | Juan Paz"));
        assertFalse(porEstado.contains("#1 | CERTIFICADO"));
    }

    @Test
    @DisplayName("Filtra por tipo y por estudiante")
    void filtraPorTipoYPorEstudiante() {
        String salida = ejecutar(concatenar(
                registrar("1001", "Ana Perez", "ana@mail.com", "CERTIFICADO", "2",
                        "Certificado de alumno regular", FECHA_FUTURA),
                registrar("1002", "Juan Paz", "juan@mail.com", "EQUIVALENCIA", "3",
                        "Equivalencia de Algebra I", FECHA_FUTURA),
                new String[] {"6", "EQUIVALENCIA"},
                new String[] {"7", "1001"},
                new String[] {"7", "9999"}));

        assertTrue(salida.contains("#2 | EQUIVALENCIA | BAJA | PENDIENTE | Juan Paz"));
        assertTrue(salida.contains("#1 | CERTIFICADO | MEDIA | PENDIENTE | Ana Perez"));
        assertTrue(salida.contains("No hay solicitudes para mostrar"));
    }

    // ---------- reporte ----------

    @Test
    @DisplayName("El reporte cuenta por estado y por tipo")
    void reporteCuentaPorEstadoYTipo() {
        String salida = ejecutar(concatenar(
                registrar("1001", "Ana Perez", "ana@mail.com", "CERTIFICADO", "2",
                        "Certificado de alumno regular", FECHA_FUTURA),
                registrar("1002", "Juan Paz", "juan@mail.com", "CERTIFICADO", "3",
                        "Certificado analitico parcial", FECHA_FUTURA),
                registrar("1003", "Luz Rey", "luz@mail.com", "RECLAMO_NOTA", "2",
                        "Reclamo nota del final", FECHA_FUTURA),
                new String[] {"3", "3", "RECHAZADA"},
                new String[] {"8"}));

        assertTrue(salida.contains("Total: 3"));
        assertTrue(salida.contains("PENDIENTE: 2"));
        assertTrue(salida.contains("RECHAZADA: 1"));
        assertTrue(salida.contains("CERTIFICADO: 2"));
        assertTrue(salida.contains("RECLAMO_NOTA: 1"));
        assertTrue(salida.contains("No hay solicitudes vencidas"));
    }

    @Test
    @DisplayName("El reporte lista las solicitudes abiertas vencidas")
    void reporteListaVencidas() {
        String salida = ejecutar(concatenar(
                registrar("1001", "Ana Perez", "ana@mail.com", "CERTIFICADO", "2",
                        "Certificado de alumno regular", FECHA_PASADA),
                new String[] {"8"}));

        assertTrue(salida.contains("#1 CERTIFICADO vencio el 2020-01-06"));
    }

    @Test
    @DisplayName("Una solicitud cerrada no figura como vencida")
    void solicitudCerradaNoFiguraComoVencida() {
        String salida = ejecutar(concatenar(
                registrar("1001", "Ana Perez", "ana@mail.com", "CERTIFICADO", "2",
                        "Certificado de alumno regular", FECHA_PASADA),
                new String[] {"3", "1", "RECHAZADA"},
                new String[] {"8"}));

        assertTrue(salida.contains("No hay solicitudes vencidas"));
    }

    // ---------- persistencia ----------

    @Test
    @DisplayName("Los datos se conservan entre ejecuciones")
    void conservaDatosEntreEjecuciones() {
        ejecutar(concatenar(
                registrar("1001", "Ana Perez", "ana@mail.com", "CERTIFICADO", "2",
                        "Certificado de alumno regular", FECHA_FUTURA),
                new String[] {"2", "1", "3"}));

        String segundaEjecucion = ejecutar(concatenar(
                new String[] {"4"},
                registrar("1002", "Juan Paz", "juan@mail.com", "EQUIVALENCIA", "3",
                        "Equivalencia de Algebra I", FECHA_FUTURA)));

        assertTrue(segundaEjecucion.contains(
                "#1 | CERTIFICADO | MEDIA | PENDIENTE | Ana Perez | Sofia Diaz (Departamento Docente)"));
        assertTrue(segundaEjecucion.contains("Solicitud #2 registrada"));
    }

    // ---------- menu ----------

    @Test
    @DisplayName("Una opcion inexistente del menu se informa como invalida")
    void opcionInvalidaDelMenu() {
        String salida = ejecutar("x");

        assertTrue(salida.contains("Opcion invalida"));
        assertTrue(salida.contains("Hasta luego"));
    }
}