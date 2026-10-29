package ar.edu.utn.solicitudes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Pruebas del repositorio CSV sobre una carpeta temporal (no toca los datos reales).
 */
class RepositorioSolicitudesCsvTest {

    @TempDir
    Path carpeta;

    private final CatalogoResponsables responsables = CatalogoResponsables.predeterminado();
    private final Estudiante ana = new Estudiante("1001", "Ana Perez", "ana@mail.com");

    private Solicitud solicitud(int id, String descripcion) {
        return new Solicitud(id, TipoSolicitud.EQUIVALENCIA, Prioridad.MEDIA, descripcion,
                LocalDate.of(2026, 10, 21), ana);
    }

    @Test
    @DisplayName("Si el archivo no existe, arranca vacio")
    void archivoInexistente() {
        RepositorioSolicitudesCsv repositorio = new RepositorioSolicitudesCsv(carpeta.resolve("no-existe.csv"), responsables);

        assertEquals(0, repositorio.listarTodas().size());
        assertEquals(1, repositorio.siguienteId());
    }

    @Test
    @DisplayName("D-03: una descripcion con punto y coma no hace perder datos")
    void descripcionConPuntoYComaNoPierdeDatos() {
        Path archivo = carpeta.resolve("solicitudes.csv");
        RepositorioSolicitudesCsv original = new RepositorioSolicitudesCsv(archivo, responsables);
        original.guardar(solicitud(1, "Certificado de alumno regular"));
        original.guardar(solicitud(2, "Equivalencia de Algebra; urgente"));
        original.guardar(solicitud(3, "Cambio a comision noche"));

        RepositorioSolicitudesCsv recargado = new RepositorioSolicitudesCsv(archivo, responsables);

        assertEquals(3, recargado.listarTodas().size());
        assertEquals("Equivalencia de Algebra; urgente", recargado.buscarPorId(2).get().getDescripcion());
        assertEquals(4, recargado.siguienteId());
    }

    @Test
    @DisplayName("Conserva estado y responsable al recargar")
    void conservaEstadoYResponsable() {
        Path archivo = carpeta.resolve("solicitudes.csv");
        RepositorioSolicitudesCsv original = new RepositorioSolicitudesCsv(archivo, responsables);
        Solicitud solicitud = solicitud(1, "Equivalencia de Algebra I");
        solicitud.asignarResponsable(responsables.buscarPorId(3).get());
        solicitud.cambiarEstado(EstadoSolicitud.EN_PROCESO);
        original.guardar(solicitud);

        Solicitud recargada = new RepositorioSolicitudesCsv(archivo, responsables).buscarPorId(1).get();

        assertEquals(EstadoSolicitud.EN_PROCESO, recargada.getEstado());
        assertEquals("Sofia Diaz", recargada.getResponsable().getNombre());
        assertEquals("Ana Perez", recargada.getEstudiante().getNombre());
    }

    @Test
    @DisplayName("Una linea corrupta se informa y el archivo no se modifica")
    void lineaCorruptaSeInformaSinModificarElArchivo() throws IOException {
        Path archivo = carpeta.resolve("solicitudes.csv");
        List<String> contenido = List.of(
                "1;CERTIFICADO;2;PENDIENTE;Certificado de alumno regular;2026-10-20;1001;Ana Perez;ana@mail.com;0",
                "linea rota");
        Files.write(archivo, contenido, StandardCharsets.UTF_8);

        PersistenciaException error = assertThrows(PersistenciaException.class,
                () -> new RepositorioSolicitudesCsv(archivo, responsables));

        assertTrue(error.getMessage().contains("linea 2"));
        assertEquals(contenido, Files.readAllLines(archivo, StandardCharsets.UTF_8));
    }
}
