package ar.edu.utn.solicitudes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Scanner;

import ar.edu.utn.solicitudes.consola.MenuConsola;
import ar.edu.utn.solicitudes.dominio.CatalogoResponsables;
import ar.edu.utn.solicitudes.persistencia.RepositorioSolicitudesEnMemoria;
import ar.edu.utn.solicitudes.servicio.GeneradorReporte;
import ar.edu.utn.solicitudes.servicio.SolicitudService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de la consola para los defectos D-01 y D-02: el programa ya no se cierra
 * ante una entrada invalida, sino que vuelve a pedir el dato.
 */
class MenuConsolaTest {

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final LocalDate HOY = LocalDate.of(2026, 10, 29);

    private RepositorioSolicitudesEnMemoria repositorio;

    private String ejecutar(String... lineas) {
        repositorio = new RepositorioSolicitudesEnMemoria();
        CatalogoResponsables responsables = CatalogoResponsables.predeterminado();
        Clock reloj = Clock.fixed(HOY.atStartOfDay(ZONA).toInstant(), ZONA);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();

        MenuConsola menu = new MenuConsola(
                new SolicitudService(repositorio, responsables),
                new GeneradorReporte(repositorio, reloj),
                new Scanner(String.join("\n", lineas) + "\n0\n"),
                new PrintStream(salida, true, StandardCharsets.UTF_8),
                reloj);
        menu.iniciar();
        return salida.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("D-01: una letra en la prioridad vuelve a pedir el dato")
    void letraEnPrioridadVuelveAPedir() {
        String salida = ejecutar("1", "1001", "Ana Perez", "ana@mail.com", "CERTIFICADO",
                "a", "2", "Certificado de alumno regular", "2026-10-01");

        assertTrue(salida.contains("Debe ingresar un numero entero. Intente de nuevo."));
        assertTrue(salida.contains("Solicitud #1 registrada. Plazo: 5 dias"));
    }

    @Test
    @DisplayName("D-01: una letra en un id vuelve a pedir el dato")
    void letraEnIdVuelveAPedir() {
        String salida = ejecutar("1", "1001", "Ana Perez", "ana@mail.com", "CERTIFICADO",
                "2", "Certificado de alumno regular", "2026-10-01",
                "2", "uno", "1", "1");

        assertTrue(salida.contains("Debe ingresar un numero entero. Intente de nuevo."));
        assertTrue(salida.contains("Solicitud #1 asignada a Laura Gomez (Bedelia)"));
    }

    @Test
    @DisplayName("D-02: una fecha con otro formato vuelve a pedir el dato")
    void fechaInvalidaVuelveAPedir() {
        String salida = ejecutar("1", "1001", "Ana Perez", "ana@mail.com", "CERTIFICADO",
                "2", "Certificado de alumno regular", "01/10/2026", "2026-10-01");

        assertTrue(salida.contains("Fecha invalida, use el formato AAAA-MM-DD. Intente de nuevo."));
        assertEquals(LocalDate.of(2026, 10, 1), repositorio.buscarPorId(1).get().getFecha());
    }

    @Test
    @DisplayName("Una fecha vacia toma el dia del reloj")
    void fechaVaciaTomaElDiaActual() {
        ejecutar("1", "1001", "Ana Perez", "ana@mail.com", "CERTIFICADO",
                "2", "Certificado de alumno regular", "");

        assertEquals(HOY, repositorio.buscarPorId(1).get().getFecha());
    }
}
