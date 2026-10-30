package ar.edu.utn.solicitudes;

import ar.edu.utn.solicitudes.consola.MenuConsola;
import ar.edu.utn.solicitudes.dominio.CatalogoResponsables;
import ar.edu.utn.solicitudes.persistencia.RepositorioSolicitudesCsv;
import ar.edu.utn.solicitudes.servicio.GeneradorReporte;
import ar.edu.utn.solicitudes.servicio.PersistenciaException;
import ar.edu.utn.solicitudes.servicio.SolicitudRepository;
import ar.edu.utn.solicitudes.servicio.SolicitudService;

import java.nio.file.Path;
import java.time.Clock;
import java.util.Scanner;

/**
 * Punto de entrada: arma las dependencias y arranca el menu.
 */
public class Main {

    private static final Path ARCHIVO_DATOS = Path.of("solicitudes.csv");

    public static void main(String[] args) {
        Clock reloj = Clock.systemDefaultZone();
        CatalogoResponsables responsables = CatalogoResponsables.predeterminado();

        SolicitudRepository repositorio;
        try {
            repositorio = new RepositorioSolicitudesCsv(ARCHIVO_DATOS, responsables);
        } catch (PersistenciaException ex) {
            System.out.println("Error: " + ex.getMessage() + ". No se modifico el archivo; revise los datos guardados.");
            return;
        }

        SolicitudService servicio = new SolicitudService(repositorio, responsables);
        GeneradorReporte generadorReporte = new GeneradorReporte(repositorio, reloj);
        MenuConsola menu = new MenuConsola(servicio, generadorReporte, new Scanner(System.in), System.out, reloj);
        menu.iniciar();
    }
}
