package ar.edu.utn.solicitudes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio que mantiene las solicitudes en memoria y las guarda en un archivo CSV.
 */
public final class RepositorioSolicitudesCsv extends RepositorioSolicitudesEnMemoria {

    private static final int ID = 0;
    private static final int TIPO = 1;
    private static final int PRIORIDAD = 2;
    private static final int ESTADO = 3;
    private static final int DESCRIPCION = 4;
    private static final int FECHA = 5;
    private static final int LEGAJO = 6;
    private static final int NOMBRE = 7;
    private static final int EMAIL = 8;
    private static final int ID_RESPONSABLE = 9;
    private static final int CANTIDAD_CAMPOS = 10;

    private static final int SIN_RESPONSABLE = 0;

    private final Path archivo;
    private final CatalogoResponsables responsables;

    public RepositorioSolicitudesCsv(Path archivo, CatalogoResponsables responsables) {
        this.archivo = archivo;
        this.responsables = responsables;
        cargarArchivo();
    }

    @Override
    public void guardar(Solicitud solicitud) {
        super.guardar(solicitud);
        escribirArchivo();
    }

    // ---------- lectura ----------

    private void cargarArchivo() {
        if (!Files.exists(archivo)) {
            return;
        }
        try (BufferedReader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            String linea;
            int numeroLinea = 0;
            while ((linea = lector.readLine()) != null) {
                numeroLinea++;
                super.guardar(convertirLinea(linea, numeroLinea));
            }
        } catch (IOException ex) {
            throw new PersistenciaException("no se pudo leer " + archivo, ex);
        }
    }

    private Solicitud convertirLinea(String linea, int numeroLinea) {
        List<String> campos = FormatoCsv.separar(linea);
        if (campos.size() != CANTIDAD_CAMPOS) {
            throw new PersistenciaException("linea " + numeroLinea + " de " + archivo + " con formato invalido");
        }
        try {
            Estudiante estudiante = buscarEstudiante(campos.get(LEGAJO))
                    .orElseGet(() -> new Estudiante(campos.get(LEGAJO), campos.get(NOMBRE), campos.get(EMAIL)));
            Responsable responsable = buscarResponsable(Integer.parseInt(campos.get(ID_RESPONSABLE)));
            Prioridad prioridad = Prioridad.desdeCodigo(Integer.parseInt(campos.get(PRIORIDAD)))
                    .orElseThrow(() -> new IllegalArgumentException("prioridad inexistente"));

            return Solicitud.reconstruir(
                    Integer.parseInt(campos.get(ID)),
                    TipoSolicitud.valueOf(campos.get(TIPO)),
                    prioridad,
                    EstadoSolicitud.valueOf(campos.get(ESTADO)),
                    campos.get(DESCRIPCION),
                    LocalDate.parse(campos.get(FECHA)),
                    estudiante,
                    responsable);
        } catch (RuntimeException ex) {
            throw new PersistenciaException("linea " + numeroLinea + " de " + archivo + " con datos invalidos", ex);
        }
    }

    private Responsable buscarResponsable(int id) {
        if (id == SIN_RESPONSABLE) {
            return null;
        }
        return responsables.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("responsable inexistente: " + id));
    }

    // ---------- escritura ----------

    private void escribirArchivo() {
        try (BufferedWriter escritor = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8)) {
            for (Solicitud solicitud : listarTodas()) {
                escritor.write(convertirALineaCsv(solicitud));
                escritor.newLine();
            }
        } catch (IOException ex) {
            throw new PersistenciaException("no se pudo guardar " + archivo, ex);
        }
    }

    private String convertirALineaCsv(Solicitud solicitud) {
        int idResponsable = solicitud.getResponsable() == null
                ? SIN_RESPONSABLE
                : solicitud.getResponsable().getId();
        return FormatoCsv.unir(List.of(
                String.valueOf(solicitud.getId()),
                solicitud.getTipo().name(),
                String.valueOf(solicitud.getPrioridad().getCodigo()),
                solicitud.getEstado().name(),
                solicitud.getDescripcion(),
                solicitud.getFecha().toString(),
                solicitud.getEstudiante().getLegajo(),
                solicitud.getEstudiante().getNombre(),
                solicitud.getEstudiante().getEmail(),
                String.valueOf(idResponsable)));
    }
}
