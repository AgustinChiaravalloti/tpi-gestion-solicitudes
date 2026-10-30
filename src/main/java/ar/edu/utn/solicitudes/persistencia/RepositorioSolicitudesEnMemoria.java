package ar.edu.utn.solicitudes.persistencia;

import ar.edu.utn.solicitudes.dominio.Estudiante;
import ar.edu.utn.solicitudes.dominio.Solicitud;
import ar.edu.utn.solicitudes.servicio.SolicitudRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Guarda las solicitudes solo en memoria. Util para pruebas y como base del repositorio CSV.
 */
public class RepositorioSolicitudesEnMemoria implements SolicitudRepository {

    private final Map<Integer, Solicitud> solicitudes = new LinkedHashMap<>();

    @Override
    public List<Solicitud> listarTodas() {
        return List.copyOf(solicitudes.values());
    }

    @Override
    public Optional<Solicitud> buscarPorId(int id) {
        return Optional.ofNullable(solicitudes.get(id));
    }

    @Override
    public void guardar(Solicitud solicitud) {
        solicitudes.put(solicitud.getId(), solicitud);
    }

    @Override
    public int siguienteId() {
        int maximo = 0;
        for (int id : solicitudes.keySet()) {
            maximo = Math.max(maximo, id);
        }
        return maximo + 1;
    }

    @Override
    public Optional<Estudiante> buscarEstudiante(String legajo) {
        return solicitudes.values().stream()
                .map(Solicitud::getEstudiante)
                .filter(estudiante -> estudiante.getLegajo().equals(legajo))
                .findFirst();
    }
}
